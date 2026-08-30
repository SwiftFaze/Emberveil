# Intent: Add startup welcome menu and settings screen (visual only)

- **Slug(s):** startup-welcome-screen, settings-screen, settings-keybinds-page,
  ui-widget-slider (match `/specs/features/startup-welcome-screen.feature`,
  `/specs/features/settings-screen.feature`,
  `/specs/features/settings-keybinds-page.feature`,
  `/specs/features/ui-widget-slider.feature`) — four distinct concepts
  filed under one issue: the title screen, the settings screen shell, the
  dedicated keybind-rebinding sub-page (enough distinct behavior —
  capturing a raw keypress via a popup — to warrant its own file), and a
  new generic slider widget the settings screen needs but which wasn't
  scoped by any existing widget-framework issue (see Constraints).
- **Author:** rwoolley
- **Date:** 2026-08-30
- **Source:** [GitHub issue #54](https://github.com/SwiftFaze/Veil/issues/54)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [x] Approved by human
- [x] Implemented
- [x] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [x] Acceptance tests passing
- [ ] Mutation testing passed
- [x] Documentation updated (`docs/`, and the wiki if player-facing)

## Problem

`Main.loadGame()` (`src/main/java/com/swiftfaze/veil/Main.java:22-50`) launches
straight into the default world/view — no title screen, no way to reach
game-level settings, no visual home for options like fullscreen, volume,
or keybinds (none of which exist as real systems yet either).

## Scope

- In scope:
  - A **welcome/title screen** shown on launch, before the game view:
    "VEIL" title text (Delta Corps Priest 1 font, title screen only) and
    a menu — Continue, New, Load, Settings, Exit. Continue/Load are
    present but non-functional (no save system exists). New behaves like
    today's `Main.loadGame()` startup, just one screen deeper. No
    decorative ASCII art/logo/border for v1.
  - A **settings screen shell**, reached from Settings: a navigable,
    back-able list of items — brightness, fullscreen, font, volume,
    keybinds, open game folder, open mod folder, about/version info,
    reset to defaults. Default item interaction is a slider (Left/Right
    adjusts a value) — brightness and volume use this directly.
    Fullscreen/window mode is a radio-style Left/Right toggle between
    "Fullscreen"/"Windowed". Font is a Left/Right cycle through a fixed
    list of choices. "Open Game Folder"/"Open Mod Folder" use
    `Desktop.open` (or equivalent) on the install directory / a `mods/`
    folder (created next to the install if missing) — independent of the
    mod-loader system itself. None of brightness/fullscreen/font/volume/
    keybind-rebinding has any real backing mechanism yet (no
    rendering/audio/config-persistence system exists) — this pass is
    visual/input shape only.
  - A dedicated **keybinds page**, opened from the settings screen's
    Keybinds item: lists every rebindable action
    (`Keybindings.ACTION_MOVE_UP/DOWN/LEFT/RIGHT`,
    `ACTION_TOGGLE_INVENTORY` — see
    `src/main/java/com/swiftfaze/veil/input/Keybindings.java`) with its
    current keybind. Enter on an action opens a "press any key" popup;
    the next keypress replaces that action's displayed keybind (no real
    rebinding takes effect — visual only, see Out of scope). Footer:
    Apply, Cancel, Go back.
  - A new **slider widget**: Left/Right adjusts a bounded value within a
    caller-supplied range, rendered as a terminal-style slider, built on
    the same `Widget`/`WidgetTheme` contract as every other widget in
    `src/main/java/com/swiftfaze/veil/ui/widget/`. Not scoped by issue
    #35 (which only covers table/radio-group/pattern-field) — this issue
    is the actual real consumer requiring it, so it's scoped here
    instead of speculatively elsewhere. See Constraints for its
    relationship to #35's widget library.
  - All screens/widgets built on the existing widget framework from #36
    (`ui/widget/Widget.java`, `FocusManager`, `WidgetTheme`, `ListWidget`,
    `ButtonWidget`, `PopupWidget`) — **not** the `MenuPanel`/
    `SelectableMenu` shell the original issue text references, which no
    longer exists (deleted entirely by #36 — see Constraints).
  - Wiring the title screen into `Main.loadGame()` as the new entry
    point, with New navigating to what `loadGame()` builds today.
- Out of scope:
  - Any real mechanism behind brightness, fullscreen, font, volume, or
    keybind rebinding — visual/input shape only, no
    rendering/audio/config-persistence changes.
  - Continue/Load actually loading a save (no save system exists).
  - The mod-loader system itself (tracked separately,
    `specs/intent/mod-loader.md`).
  - Reset to Defaults actually resetting anything (no setting persists
    real state yet).
  - Applying Delta Corps Priest 1 anywhere beyond the title screen.
  - Decorative ASCII art/logo/border on the title screen.
  - Sourcing/licensing the actual Delta Corps Priest 1 font file — see
    Constraints; this doc can specify the loading mechanism but not
    produce the binary asset itself.
  - Implementing the table or pattern-field widgets from #35 — this
    issue only needs the radio group widget (Fullscreen/Windowed toggle,
    font cycle) and the new slider widget.

## Actors

- The player, on game launch and while navigating settings.

## Desired behavior

- On launch, the title screen shows "VEIL" (Delta Corps Priest 1) and the
  menu, instead of dropping straight into the world.
- New behaves like today's startup. Settings opens the settings screen.
- The settings screen lists all nine items; Up/Down moves between items
  (matching every other widget's vertical list convention); Left/Right
  adjusts the currently-selected item per its interaction type (slider,
  radio toggle, or font cycle); Escape/a Back item returns to the title
  screen's menu.
- Keybinds opens its own page: Up/Down moves between listed actions,
  Enter on one opens a "press any key" popup, the next keypress updates
  that action's displayed keybind, Apply/Cancel/Go back at the footer
  (Apply and Cancel both just return to settings in this visual-only
  pass — see Open questions).
- Open Game Folder / Open Mod Folder invoke `Desktop.open` on the
  install directory / `mods/` (created if missing) immediately on
  confirm — no confirmation dialog.

## Constraints / non-functional notes

- Must fit the existing complexity budgets (40-line functions, cyclomatic
  complexity 8, 4 params max, SLAP).
- Stays Swing-based, no new UI framework/dependency.
- **The issue text is stale on one point**: it describes building on
  `TerminalPanel`/`SelectableMenu`/`MenuPanel` ("which already has a
  working navigable-menu pattern used elsewhere"), but `MenuPanel` and
  `SelectableMenu` were deleted entirely by #36
  (`specs/intent/ui-component-framework.md`'s Clarifications — confirmed
  by grep, neither class exists in `src/main/java` anymore). This intent
  corrects that: the title screen's menu and the settings screen are
  built on the current `ListWidget`/`ButtonWidget`/`PopupWidget`
  framework instead.
- **Depends on #35's radio group widget** (`specs/intent/ui-widget-library.md`,
  not yet approved/implemented) for the Fullscreen/Windowed toggle and
  font cycle — both need the horizontal Left/Right orientation that
  intent doc's Clarifications corrected specifically because of this
  issue. This issue cannot be fully implemented until that widget lands,
  same dependency shape as #35 had on #36.
- **The slider widget is new scope, not previously tracked anywhere** —
  #35 only scoped table/radio-group/pattern-field. It's included here
  because this issue is its first real consumer (same "size against a
  real consumer" principle #35 itself was deferred under). It should
  follow the same `Widget`/`WidgetTheme` conventions as #35's widgets so
  it's a natural fit alongside them if/when a widget-library doc is
  revisited.
- **No font asset exists in the repo** — `src/main/resources/` currently
  only has `logback.xml`; there's no `.ttf`/font-loading code anywhere
  (`TerminalPanel`'s `TERMINAL_FONT` is a plain `new Font(Font.MONOSPACED,
  Font.PLAIN, 16)`). This intent can specify the loading mechanism
  (`Font.createFont(Font.TRUETYPE_FONT, ...)` from a bundled resource,
  registered via `GraphicsEnvironment`), but the actual Delta Corps
  Priest 1 font file itself needs to be sourced/supplied by a human — see
  Open questions.

## Open questions

- Where does the actual Delta Corps Priest 1 `.ttf` file come from? A
  real licensed/distributed font is a binary asset that has to be
  supplied by a human — it cannot be fabricated. Genuinely still open
  (see Clarifications for how this doesn't block spec/implementation
  work in the meantime).

## Clarifications

- Q: Do Apply/Cancel on the keybinds page need to behave differently
  from each other in this visual-only pass?
  A: Confirmed at Step 3 approval (2026-08-30). No — since nothing persists yet (per the issue's
  own out-of-scope note: "no config-persistence changes in this pass"),
  both Apply and Cancel just return to the settings screen identically.
  Differentiating them meaningfully (Cancel discards in-page edits,
  Apply commits them) only makes sense once there's real state to
  discard/commit — that's follow-up work once keybind rebinding actually
  persists.
  Affects: Desired behavior, Scope (settings-keybinds-page).

- Q: What's the concrete list of font choices for the font cycler, since
  the issue doesn't name any?
  A: Confirmed at Step 3 approval (2026-08-30). Java's built-in logical font families —
  "Monospaced", "Serif", "SansSerif" — as v1 placeholders. These are
  guaranteed available on any JVM with no new font asset needed, which
  also keeps the font cycler's own scenarios independent of the Delta
  Corps Priest 1 asset-availability question below (it cycles through
  these three regardless of whether that title-screen font has been
  supplied yet).
  Affects: Desired behavior, Scope (settings-screen).

- Q: The Delta Corps Priest 1 `.ttf` file can't be fabricated (see Open
  questions) — does that block writing/implementing this feature at all?
  A: Confirmed at Step 3 approval (2026-08-30). No — the font-loading mechanism is built generically
  (`Font.createFont(Font.TRUETYPE_FONT, ...)` from a bundled resource
  path, e.g. `src/main/resources/fonts/DeltaCorpsPriest1.ttf`), and falls
  back to the existing plain `Font(Font.MONOSPACED, Font.PLAIN, ...)`
  (same as every other panel today) if that resource is absent, logging
  a warning rather than failing. This means the title screen's structure,
  navigation, and every other scenario can be built and tested now,
  independent of when the actual font file is supplied — swapping it in
  later is a drop-in resource addition, no code change needed. The "is
  the actual Delta Corps Priest 1 glyph rendering correct" question
  itself stays a manual, visual concern for whoever eventually supplies
  the file (not something a Gherkin scenario can usefully assert on
  either way).
  Affects: Desired behavior, Scope (startup-welcome-screen), Constraints.

- Q (raised during Step 4.5 manual playtest, 2026-08-30): the first playtest
  pass found the settings/keybinds screens visually and functionally
  incomplete beyond the centering/interactivity gaps already fixed that
  round — no discoverable way back from settings other than Escape, rows
  of inconsistent width, the keybinds footer's Apply/Cancel/Go back order
  not matching a natural left-to-right convention, no way to reset
  keybinds specifically, and no visual indicator on which action is armed
  for rebinding once its "press any key" popup opens. Do these get folded
  into this intent doc and its specs, or treated as new out-of-scope
  follow-up?
  A: Folded in directly — this is corrective feedback on the same feature
  from the same playtest step the intent doc's own Status checklist
  already gates on, not new scope. Settings screen gained an explicit
  "Go Back" row (tenth item) and uniform row widths (all rows share the
  widest row's width). Keybinds footer reordered to Go back, Reset to
  Defaults, Cancel, Apply (left to right) and gained a real Reset to
  Defaults action — genuinely functional, unlike the settings screen's
  own placeholder Reset to Defaults, since keybind display state is
  local to that page and safe to actually reset. The currently-highlighted
  action row gets a green border (matching `RadioGroupWidget`'s existing
  confirmed-border convention) while its press-any-key popup is open, so
  it's visually distinguishable from a merely-highlighted row.
  Affects: Desired behavior, Scope (settings-screen, settings-keybinds-page).
