# Intent: Controls hint bar

- **Slug(s):** controls-hint-bar (matches `/specs/features/controls-hint-bar.feature`)
- **Author:** rwoolley
- **Date:** 2026-09-02
- **Source:** [GitHub issue #134](https://github.com/SwiftFaze/Veil/issues/134)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file) — high-risk path only, see `.claude/workflow.md`
- [ ] Approved by human — high-risk path only (standard path, no blocking gate)
- [x] Implemented
- [x] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [x] Acceptance tests passing
- [x] Mutation testing passed — `BUILD SUCCESS`, 93% line coverage on mutated classes, 72% mutation kill rate
- [x] Documentation updated (`docs/`, and the wiki if player-facing) — not player-facing/wiki-relevant (see `docs/wiki.md`'s scope), covered in `docs/ui-widgets.md` and `docs/screens.md`

## Problem

Players have no on-screen indication of what keys do what, on any screen.
Codex, Inventory, Settings, Keybinds, Title, and the in-game view all rely
on players already knowing or guessing the bindings, or discovering them
by trial and error. There is no discoverability mechanism for controls at
all.

## Scope

- In scope:
  - A single shared controls-hint-bar widget, docked at the bottom of the
    window/frame (one persistent instance, not one embedded per screen),
    that renders a list of `[key] - action` hint strings via a simple push
    API (e.g. `setHints(List<...>)`).
  - Every screen pushes its current hint list into it: Title, Settings,
    Keybinds, Inventory, Codex, and the in-game/movement view.
  - Hints update at row/sub-focus granularity, not just once per screen
    switch:
    - A Settings slider row (Brightness, Volume) shows
      `[left]-Decrease [right]-Increase`; an action row (Open Mod Folder,
      About) shows no left/right hints.
    - Codex/Inventory's Left/Right pane transitions (item list <-> details,
      Fields table <-> Effects table) update the shown hints the same way,
      hung off the existing `ListWidget.onSelectionChange`-style transition
      points and `PopupWidget`'s `onLeft`/`onRight` hooks.
- Out of scope (tracked in #133): Contextual world-action hints (e.g.
  `[E] - Open door` when standing near a door) — depends on an interaction
  system, door/tile state, and facing/adjacency detection that don't exist
  yet.

## Actors

Every player, on every screen.

## Desired behavior

A persistent bar at the bottom of the window always reflects the
currently-valid key bindings for wherever keyboard focus currently is,
updating live as focus moves between rows, panes, or screens. It is never
stale and never blank on a screen that has real bindings available.

## Constraints / non-functional notes

Soft dependency, not a hard blocker: wiring this bar into the in-game
view, Inventory, and Codex depends on the in-progress composition-root
reimplementation (`EastPanel`/`NorthPanel`/`SouthPanel`/`PlayerInfoPanel`/
`TerminalPanel` are being removed on `feat/shared-list-detail-ui-contract`;
`InventoryPanel`/`CodexPanel`/`DetailsPaneWidget` currently stay but are
unwired from the live game until that composition root is rebuilt). The
bar itself and its wiring into Title/Settings/Keybinds don't depend on
that work and can proceed independently.

None beyond the usual (see CLAUDE.md function-length/complexity/coverage
budgets).

## Open questions

None remaining — scope settled via a grilling session on 2026-09-02.

## Clarifications

- Q: Movement has two bindings per direction (letters Z/S/Q/D and arrow
  keys — see `Keybindings.java`). Every other screen already uses
  arrow-key vocabulary (`[up]`/`[down]`/`[left]`/`[right]`) for menu
  navigation, and `MOVE_UP_ARROW` is literally the same `KeyStroke` as
  `MENU_UP`. Which form should the in-game/movement view's hint bar show?
  A: Letters (Z/S/Q/D), not arrows — movement isn't arrow-exclusive, and
  showing the letter scheme avoids conflating it with the arrow-key
  vocabulary already used for menu navigation elsewhere.
  Affects: the in-game/movement view scenarios in
  `controls-hint-bar.feature` (movement hints read `[z]-Move`,
  `[s]-Move`, `[q]-Move`, `[d]-Move`, not `[up]`/`[down]`/`[left]`/`[right]`).

- Q: Settings screen radio-cycle rows (Fullscreen, Font, Theme) genuinely
  respond to Left/Right (`SettingsScreenPanel.moveLeft/moveRight` calls
  `radio.moveLeft()`/`moveRight()`), unlike the issue's only-explicit
  contrast (slider Decrease/Increase vs. action-row nothing). Do they get
  Left/Right hints too, and with what wording?
  A: Yes — show them, worded `[left]-Previous` / `[right]-Next` (distinct
  from slider's Decrease/Increase, since cycling Windowed/Fullscreen or
  Monospaced/Serif/SansSerif isn't "increasing"). Omitting the hint here
  would leave the bar stale on a row with live bindings.
  Affects: settings-screen row-focus scenarios in
  `controls-hint-bar.feature`.

- Q: On the Keybinds page, once focus moves into the footer row (Go
  back/Reset to Defaults/Cancel/Apply), Up exits back to the table but
  Down is a no-op, and Left/Right become live for the first time
  (`SettingsKeybindsPanel.moveFooterLeft/Right` only act when
  `footerFocused`). Should the hint bar track this sub-focus state
  precisely, or stay static (`[up]-Navigate`/`[down]-Navigate`
  throughout)?
  A: Track it precisely — same "verb changes with real effect" case as
  the slider Decrease/Increase example; the intent doc requires
  sub-focus-granular updates, not just per-screen ones.
  Affects: Keybinds page footer-focus scenario in
  `controls-hint-bar.feature`.

- Q: `SettingsKeybindsPanel.KeybindsKeyListener` treats every keypress
  while `popupOpen` — including Escape — as the new binding to assign
  (`pressKey(...)` runs unconditionally, no separate cancel path). A hint
  bar honest about live bindings can't say `[escape]-Cancel` during
  capture, since Escape doesn't cancel, it rebinds to Escape. What should
  the hint bar show during capture, and is fixing Escape's actual
  behavior in scope here?
  A: Show `[any key]-Set binding` only during capture, accurately
  reflecting current behavior. Fixing Escape to actually cancel capture
  is a separate, pre-existing behavior question — out of scope for this
  hint-bar feature, worth its own follow-up issue rather than a silent
  side effect here.
  Affects: Keybinds page armed-capture scenario in
  `controls-hint-bar.feature`.

- Q: After playtesting the first implementation, plain vertical list
  movement's `[up]-Navigate`/`[down]-Navigate` pair showed up on nearly
  every screen (Title, all three Settings row types, Keybinds table nav,
  Inventory, Codex) and added no information — arrow-key list navigation
  is assumed player knowledge, unlike Decrease/Increase, Previous/Next,
  or "Back to list"/"Back to table", which name a specific, non-obvious
  effect. Should the bar keep showing Up/Down-Navigate everywhere, or
  suppress it where it's just plain movement?
  A: Suppress it everywhere it means plain vertical list movement (Title,
  Settings action/slider/radio rows, Keybinds table nav, Inventory,
  Codex). Keep every hint that names a distinct, non-obvious effect:
  `[left]-Decrease`/`[right]-Increase`, `[left]-Previous`/`[right]-Next`,
  `[left]-Back to list`, `[up]-Back to table` (Keybinds footer — not
  plain Navigate), `[enter]-Select`/`Rebind`, `[escape]-Back`/`Close`,
  and the in-game `[z]/[s]/[q]/[d]-Move` letter scheme (not arrow-based,
  so not self-explanatory the way arrow-key Up/Down is).
  Affects: every scenario in `controls-hint-bar.feature` whose expected
  hint list previously included `"[up]-Navigate", "[down]-Navigate"` —
  those two entries are dropped from the expected list, nothing else
  about the scenario changes.

- Q: A follow-up visual-design pass (reverse-video terminal keycap style,
  each key rendered as a `NORMAL_TEXT`-on-`BACKGROUND` block showing its
  literal label — `Up`, `Esc`, `Enter` — instead of a plain
  `"[key]-Action"` string) requires `ControlsHintBarWidget` to accept
  structured key/action pairs instead of one pre-formatted string per
  hint. Does this change what `.feature` scenarios assert?
  A: No new scenarios and no step-vocabulary change — `Then the hint bar
  shows exactly "[key]-Action", ...` keeps working unchanged; the step
  definitions parse that bracket-formatted string back into a structured
  pair for comparison. This is purely a widget-contract/rendering change
  (Step 4 implementation detail), not a behavior the spec needs to
  describe differently. See `docs/ui-widgets.md` once updated (Step 7)
  for the actual `Hint` record shape.
