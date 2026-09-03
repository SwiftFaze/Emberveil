# Intent: Settings persistence

- **Slug(s):** settings-persistence (matches `/specs/features/settings-persistence.feature`)
- **Author:** rwoolley
- **Date:** 2026-09-03
- **Source:** [GitHub issue #135](https://github.com/SwiftFaze/Veil/issues/135)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file) — high-risk path only, see `.claude/workflow.md`
- [ ] Approved by human — high-risk path only (standard path, no blocking gate)
- [x] Implemented
- [x] Manually playtested (human) — confirmed working in the running game
- [x] Acceptance tests passing — `mvn verify` green (362 tests), confirmed deterministic across repeated clean runs
- [x] Mutation testing passed — `BUILD SUCCESS`, 93% line coverage on mutated classes, 72% mutation kill rate
- [x] Documentation updated (`docs/`, and the wiki if player-facing) — not
  wiki-relevant (UI/QoL persistence, not player-facing game data per
  `docs/wiki.md`'s scope); `docs/screens.md`, `docs/ui-widgets.md`, and
  `docs/architecture.md` updated, and `settings-keybinds-page.feature`'s
  now-stale header/scenario (superseded by this feature) corrected

## Problem

`SettingsScreenPanel` (Brightness, Fullscreen, Font, Theme, Volume) and
`SettingsKeybindsPanel` (per-action key rebinding) both hold their
selected values purely in memory. Every app restart resets every row to
its hardcoded default (`SettingsScreenPanel.initializeRows`,
`SettingsKeybindsPanel.setDefaultBindings`) — confirmed as a known gap in
`settings-screen.feature`'s own non-goals list ("no rendering/audio/
config-persistence system exists yet; this screen is visual/input shape
only"). No config-persistence mechanism exists anywhere in this codebase
yet; this is net-new infrastructure, not an extension of something
existing.

## Scope

- In scope:
  - A `settings.json` config file, written next to the install — the same
    base directory `Main.openFolder`/`ModLoader.load` already treat as
    "the install" (`Path.of("").toAbsolutePath()`, sibling to the `mods/`
    directory) — kept separate from any future game-save/progress file
    (settings are per-installation, save data will be per-playthrough
    with a different lifecycle; see the issue's own scope note).
  - Capturing the current selected value of every `SettingsScreenPanel`
    row that holds a value (Brightness, Fullscreen/Windowed, Font, Theme,
    Volume) and every `SettingsKeybindsPanel` action-to-key mapping.
  - Loading `settings.json` once at startup (before `SettingsScreenPanel`/
    `SettingsKeybindsPanel` are constructed) and using its values as the
    initial widget state in place of the current hardcoded defaults.
    Missing file or an entry the file doesn't have -> fall back to today's
    hardcoded default for that value, not an error.
  - Writing `settings.json` on change: immediately when a
    `SettingsScreenPanel` row's value changes (matches how that screen
    already applies changes live in-widget, no separate commit step), and
    on `SettingsKeybindsPanel`'s Apply footer action only (see below —
    Apply becomes the sole write path for that page).
  - Wiring the main Settings screen's `Reset to Defaults` (confirmation
    popup -> Yes) to actually reset every row to its hardcoded default in
    memory and write that immediately — today it's a non-functional
    placeholder (`confirmation-popup-variant.feature`'s own non-goal:
    "Reset to Defaults's 'Yes' choice actually resetting anything"). This
    feature closes that gap rather than persisting around it, since
    leaving it broken next to a feature literally about persisting
    settings would read as an obvious bug.
  - Reworking `SettingsKeybindsPanel`'s footer (Go back, Reset to
    Defaults, Cancel, Apply — currently documented as behaving
    identically, `settings-keybinds-page.feature`'s own non-goal) into
    real, distinct semantics now that Apply must actually commit:
    - A "committed" bindings snapshot, synced from `settings.json` at
      startup and re-synced every time Apply runs. The visible table is a
      "pending" copy that rebind actions mutate directly (unchanged from
      today).
    - **Apply**: writes the pending table to `settings.json` immediately;
      the committed snapshot updates to match.
    - **Cancel**: if pending differs from committed, shows a new Yes/No
      discard-confirmation popup first (Yes -> pending reverts to
      committed, stays on the page; No -> dismiss popup, pending
      untouched, stays on the page). If nothing differs, reverts (a
      no-op) and stays on the page directly, no popup.
    - **Go back**: same discard-confirmation gate as Cancel, but Yes (or
      "nothing pending") navigates back to the Settings screen afterward
      instead of staying.
    - **Escape**, pressed from the action table (not the footer) —
      currently an unconditional, unguarded exit
      (`SettingsKeybindsPanel.back()`, wired straight to `VK_ESCAPE` in
      `KeybindsKeyListener`, bypassing the footer entirely) — gets the
      same discard-confirmation-then-navigate treatment as Go back, so it
      isn't a silent bypass around the safeguard Cancel/Go back just
      gained.
    - **Reset to Defaults**: gains its own new Yes/No confirmation popup
      (today it resets immediately with none —
      `settings-keybinds-page.feature`'s existing scenario). Confirming
      Yes sets the *pending* table to hardcoded defaults only — it does
      **not** write to `settings.json` by itself, keeping Apply as the
      only thing on this page that writes. A reset the player never
      Applies is just another pending edit, subject to the same
      discard-confirmation as any other unsaved change if they then
      Cancel/Go back/Escape.
  - Corrupt or unparsable `settings.json` (not just a missing file)
    falling back to defaults for every value, the same as a missing file
    — a bad file must never crash startup.
- Out of scope:
  - Any game-save/progress-tracking system (player position, inventory,
    quest state) — different lifecycle (per-playthrough, potentially
    multiple slots), left for its own future issue per the source issue.
  - Making Brightness, Fullscreen, Font, Theme, or Volume actually affect
    rendering/audio/window state. None of them do today (confirmed in
    `settings-screen.feature`'s non-goals) except whatever the separate
    Fullscreen/Windowed live-toggle work (referenced by the source issue)
    already wires up on its own branch — this feature persists whatever
    value the relevant widget/mechanism currently reads on startup and
    writes on change; it doesn't add new live effects itself.
  - Making Keybinds rebinding actually retarget real input dispatch.
    `SettingsKeybindsPanel.keyBindings` is today a display-only
    `Map<String,String>` of label strings ("Up", "I", ...), unconnected to
    the actual `Keybindings` `KeyStroke` constants every other panel binds
    directly (`SettingsScreenPanel.bindKeys`, movement handling, etc.).
    This feature persists that display map faithfully across restarts; it
    does not wire it into real key dispatch, which is a separate,
    larger, pre-existing gap this issue doesn't take on.
  - Any settings not currently on these two screens (e.g. no About/version
    state to persist, per `settings-screen.feature`'s non-goals).
  - Fixing `SettingsKeybindsPanel.KeybindsKeyListener`'s pre-existing
    capture-popup bug (Escape, while the press-any-key popup is open,
    rebinds the action to "Escape" instead of canceling capture) — already
    flagged and deliberately deferred in `controls-hint-bar.md`'s
    Clarifications as its own follow-up issue, not reopened here.

## Actors

Every player who changes a setting or rebinds a key, and expects it to
still be set the next time they launch the game.

## Desired behavior

On startup, before the Settings/Keybinds screens are first shown,
`settings.json` (if present and parsable) is read and its values become
the initial state of every `SettingsScreenPanel` row and every
`SettingsKeybindsPanel` action mapping — a player who set Volume to 8 and
rebound "Toggle inventory" to `O` last session sees exactly that on next
launch, not the hardcoded defaults. Any value absent from the file
(partial/older file) or the file being missing entirely falls back to
today's hardcoded default for just that value, not a crash or a reset of
everything else.

Every change on the main Settings screen (Left/Right on a slider or radio
row) writes the full current settings state back to `settings.json`
immediately, the same way the screen already applies the change to its
own widget state. Confirming that screen's Reset to Defaults now actually
resets every row and writes the result immediately too — no longer a
placeholder.

On the Keybinds screen, edits accumulate in a pending table as they do
today. Only Apply writes to `settings.json` (and only then does the
"committed" snapshot used for reverting move forward). Cancel, Go back,
and Escape (from the action table) all discard pending edits back to that
snapshot — each asks for confirmation first if there's actually something
to lose, skipping the popup when the table already matches the snapshot.
Cancel stays on the page after discarding; Go back and Escape navigate to
the Settings screen afterward. Reset to Defaults asks its own
confirmation, then sets the pending table to hardcoded defaults — exactly
like any other unsaved edit, still requiring Apply to actually persist.

A missing, first-run, or corrupt `settings.json` is silent and
non-fatal: the game starts with today's hardcoded defaults exactly as it
does now, and a file is created/overwritten the first time something
changes.

## Constraints / non-functional notes

No new dependency needed — Gson is already a project dependency
(`ModLoader.java` already parses JSON with it), so `settings.json`
read/write reuses it rather than introducing a new JSON library.

`settings.json` lives next to the install (working-directory-relative,
matching `mods/`'s existing convention), not under a per-OS user-config
directory — this keeps the convention consistent with how `mods/` is
already resolved, and matches the issue's framing of settings as
"per-installation."

None beyond the usual (see CLAUDE.md function-length/complexity/coverage
budgets).

## Open questions

None blocking Step 4 — this is the standard path, no approval gate.
Two implementation-shape questions are left for whoever picks up Step 4
to settle against the actual widget code, not scope-affecting enough to
block drafting the `.feature` file:

- Exact `settings.json` key names/shape (e.g. whether Keybinds are nested
  under a `"keybinds"` object keyed by action name, matching
  `SettingsKeybindsPanel`'s existing `Map<String,String>` shape).
- Whether "on change" for main-screen sliders/radios means "write on
  every single Left/Right keypress" or "write once when focus leaves that
  row" — both satisfy "written on change" from the issue; the former is
  simpler and matches the screen having no separate commit step today, so
  is the default absent a reason found during implementation to prefer
  the latter (e.g. write-amplification on a slider held down).
- Exact wording of the two new Yes/No confirmation popups (Keybinds'
  discard-confirmation and its Reset to Defaults confirmation), and
  whether they reuse `ResetConfirmationPopup`'s widget/host mechanism
  as-is or need their own — same "minor copy/wiring decision, not
  reviewed yet" status `confirmation-popup-variant.md` left open for its
  own popup's title/question text.

## Clarifications

- Q: Does this feature also need to wire the main Settings screen's
  Reset to Defaults (confirmation popup -> Yes) to actually reset rows
  in memory and persist that, given it currently does nothing but close
  the popup (`confirmation-popup-variant.feature`'s own non-goal)? Or is
  that wiring gap left out of scope, as a pre-existing placeholder?
  A: Wire it — leaving it broken next to a feature literally about
  persisting settings would read as an obvious bug, not a deferred scope
  boundary.
  Affects: a new settings-persistence.feature scenario for the main
  Settings screen's Reset to Defaults actually resetting rows and
  writing them.

- Q: `SettingsKeybindsPanel.keyBindings` is mutated immediately by every
  rebind action, with no snapshot of "bindings when the page was
  opened." Should Cancel (a) just skip writing the already-mutated
  in-memory map to disk, or (b) actually revert the in-memory map to its
  pre-edit state, requiring new snapshot/restore logic?
  A: Superseded by the next entry below — the user's answer to how Go
  back and Cancel should differ requires real revert, i.e. (b): "cancel
  just reverts current changes and stays on page." A snapshot/restore
  mechanism (a "committed" state synced on Apply/startup-load, with the
  visible table treated as "pending") is in scope.
  Affects: Keybinds Cancel scenarios in settings-persistence.feature.

- Q: Now that Apply must diverge from Cancel (commit+write vs. discard),
  what should Go back do — behave like Apply, like Cancel, or something
  else, given CardLayout keeps the page's Swing component (and its
  in-memory state) alive between visits?
  A: "Go back cancels and goes to previous page, cancel just reverts
  current changes and stays on page" — i.e. Go back and Cancel both
  discard pending edits (revert to the committed snapshot); they differ
  only in navigation (Go back leaves to Settings, Cancel stays on the
  Keybinds page).
  Affects: Keybinds Go back and Cancel scenarios in
  settings-persistence.feature.

- Q: (Volunteered alongside the Go back/Cancel answer above.) Should
  Cancel/Go back show a confirmation before discarding, and should
  Keybinds' Reset to Defaults also get a confirmation?
  A: Yes to both — "just when you cancel or go back with changes not
  applied a confirmation popup appears, restore defaults also has a
  confirmation." The discard-confirmation only appears when there are
  actually unapplied pending edits to lose (nothing pending -> Cancel/Go
  back act directly, no popup).
  Affects: new Keybinds scenarios in settings-persistence.feature for
  the discard-confirmation (Cancel and Go back, with and without pending
  edits) and for Reset to Defaults's new confirmation.

- Q: Given Apply is now the only thing on the Keybinds page that writes
  to `settings.json`, should confirming Reset to Defaults (a) write
  immediately, itself acting like an implicit Apply, or (b) just reset
  the pending table (still requires a separate Apply to persist)?
  A: (b) — keeps exactly one write path (Apply) for the whole page,
  consistent with how every other pending edit already works; an
  un-Applied reset is just another unsaved edit the player can still
  Cancel/Go back away from (with the same discard-confirmation).
  Affects: the Keybinds Reset to Defaults scenario in
  settings-persistence.feature.

- Q: Escape, pressed from the Keybinds action table (not the footer),
  currently bypasses the footer entirely and calls an unconditional,
  unguarded `back()` — should it get the same discard-confirmation
  treatment as Go back?
  A: Yes — otherwise Escape would be a silent, unprotected bypass around
  the exact safeguard Cancel/Go back just gained.
  Affects: a new Keybinds Escape-with-pending-edits scenario in
  settings-persistence.feature.

- Q (autonomous, not put to the user — flagged here per this repo's
  convention of recording every clarification, not just user-answered
  ones): Does the main Settings screen's Reset to Defaults also reset
  Keybinds, given `ResetConfirmationPopup`'s question text already says
  "Reset all settings to their defaults"?
  A: No — it resets only the five main-screen value rows (Brightness,
  Fullscreen, Font, Theme, Volume). Keybinds keeps its own separate
  Reset to Defaults on its own page. Resetting Keybinds from the main
  screen would either have to bypass the Keybinds page's pending/Apply
  gate entirely (an immediate write, inconsistent with everything else
  on that page) or silently mutate pending Keybinds state out from under
  a player who might be mid-edit on that page — both worse than just
  scoping this row's reset to what it already visibly controls. Matches
  `settings-screen.feature`'s existing framing of "Keybinds" as a
  navigation row to another page, distinct from the five value rows.
  Affects: settings-persistence.feature's main-screen Reset to Defaults
  scenario, which only asserts Volume/Fullscreen resetting, not Keybinds.
