# Intent: UI component library: table, radio group, pattern-validated form fields

- **Slug(s):** ui-widget-table, ui-widget-radio-group, ui-widget-pattern-field
  (match `/specs/features/ui-widget-table.feature`,
  `/specs/features/ui-widget-radio-group.feature`,
  `/specs/features/ui-widget-pattern-field.feature`) — three distinct
  widgets, each independently usable and testable, so each gets its own
  `.feature` file per `/specs/features/README.md`'s "one file per distinct
  concept" convention, even though they're filed together here as one
  intent doc (they were filed as one batch in the source issue, and all
  three build on the same framework contract).
- **Author:** rwoolley
- **Date:** 2026-08-30
- **Source:** [GitHub issue #35](https://github.com/SwiftFaze/Veil/issues/35)

## Status

- [x] Intent drafted
- [ ] Spec drafted (`.feature` file)
- [ ] Approved by human
- [ ] Implemented
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [ ] Acceptance tests passing
- [ ] Mutation testing passed
- [ ] Documentation updated (`docs/`, and the wiki if player-facing)

## Problem

The terminal-style UI component framework built in #36
(`specs/intent/ui-component-framework.md`) shipped a base widget contract
(`Widget`), a focus manager (`FocusManager`), shared styling
(`WidgetTheme`), and three concrete widgets — `ListWidget`, `ButtonWidget`,
`PopupWidget` — proven by rebuilding the game's inventory popup. That first
pass deliberately deferred three widgets that had no current in-game
consumer: a table, a radio group, and a pattern-validated text/form field.
They're useful building blocks for future screens (a settings screen, a
character-creation screen) but building them without a real consumer risks
designing them speculatively against the wrong shape.

This intent doc tracks building those three remaining widgets on the same
shared framework, so the component library is complete and ready for the
next screen that needs them.

## Scope

- In scope:
  - A **table widget**: rows/columns of terminal-style cells, keyboard
    navigable, built on the same `Widget` base contract
    (`src/main/java/com/swiftfaze/veil/ui/widget/Widget.java`) and
    `WidgetTheme` selected/normal styling conventions
    (`src/main/java/com/swiftfaze/veil/ui/widget/WidgetTheme.java`) as
    `ListWidget`. Needs both vertical (row) and horizontal (column)
    navigation — `Keybindings.java`
    (`src/main/java/com/swiftfaze/veil/input/Keybindings.java`) currently
    only defines `MENU_UP`/`MENU_DOWN`/`MENU_CONFIRM`/`MENU_CANCEL`, so
    this widget needs new `MENU_LEFT`/`MENU_RIGHT` (or equivalent)
    keystroke constants added there — no horizontal menu navigation exists
    anywhere in the codebase yet.
  - A **radio group widget**: single-select from a set of options,
    vertical (Up/Down) layout only — no new keybindings needed, reusing
    `MENU_UP`/`MENU_DOWN`/`MENU_CONFIRM` — using the same selected/normal
    `WidgetTheme` indication as every other widget (no separate
    hover/focused state — see the parent framework's Clarifications on
    this, and this doc's own Clarifications for why horizontal layout is
    deferred).
  - A **text/form-field widget** with regex pattern validation: accepts
    keyboard text entry, validates against a caller-supplied pattern,
    surfaces valid/invalid state visually via a new
    `WidgetTheme.INVALID_HIGHLIGHT` constant (see Clarifications).
  - A minimal **dev-only demo harness** exercising all three widgets
    together, following the `ClassSandboxPanel`/`ClassSandbox` precedent
    (`src/main/java/com/swiftfaze/veil/sandbox/`), run via
    `mvn compile exec:java -Dexec.mainClass=...`, not part of the packaged
    build — this is what Step 4.5's manual playtest is performed against,
    since no real in-game screen consumes these widgets yet (see
    Clarifications).
  - All three extend `Widget` directly (matching `ListWidget`/
    `ButtonWidget`/`PopupWidget`'s pattern) and bind their own key bindings
    via `Keybindings` constants, following the existing
    `WHEN_FOCUSED`/`WHEN_ANCESTOR_OF_FOCUSED_COMPONENT` conventions
    established by `ListWidget`/`PopupWidget`
    (`src/main/java/com/swiftfaze/veil/ui/widget/ListWidget.java`,
    `.../PopupWidget.java`).
  - Reusing `TerminalScrollBarUI`
    (`src/main/java/com/swiftfaze/veil/ui/widget/TerminalScrollBarUI.java`)
    for the table widget if/when its content needs to scroll, rather than
    building a second scrollbar style.
  - Unit tests for each widget (à la
    `src/test/java/com/swiftfaze/veil/ui/widget/ListWidgetTest.java`),
    since — per Constraints below — none of these widgets has a real
    screen to be proven against yet.
- Out of scope:
  - Wiring any of these widgets into an actual in-game screen (settings,
    character creation, or otherwise) — no such screen exists yet (see
    Constraints). That's future work once a real consumer exists.
  - Any mouse/pointer handling — this game is keyboard-only by design,
    same as the parent framework.
  - Redesigning `FocusManager` or the existing list/button/popup widgets —
    this feature only adds new widgets on top of the existing contract, it
    doesn't change it.
  - Deciding the validation patterns a real text field will eventually use
    (e.g. for a character name) — that depends on the first real consumer,
    which doesn't exist yet (see Open questions).

## Actors

- Future feature work (a settings screen, character-creation screen, or
  any other screen needing tabular data, single-select options, or
  validated text entry) is the actor that will consume these widgets —
  none does yet.
- The player is not a direct actor for this feature, since nothing in the
  live game surfaces these widgets.

## Desired behavior

- **Table widget:** a developer can construct one with column definitions
  and row data; the currently-selected cell is visually indicated per
  `WidgetTheme.SELECTED_HIGHLIGHT`; Up/Down moves the selection between
  rows, Left/Right moves it between columns (new keybindings); Enter
  confirms the selected row (see Clarifications). Both dimensions wrap
  around at the ends by default (Up from the first row goes to the last,
  Left from the first column goes to the last, and vice versa), matching
  `ListWidget`'s own `wrapAround` default of `true` — a caller can
  override this the same way `ListWidget.setWrapAround(false)` already
  does, if a future consumer needs stop-at-the-ends behavior like
  `InventoryPanel` does today.
- **Radio group widget:** a developer supplies a set of options; Up/Down
  moves the highlighted option (vertical layout only — see
  Clarifications); Enter selects it; only one option is selected at a
  time, matching standard radio-button semantics; wraps around at the
  ends by default, same as the table/list widgets.
- **Text/pattern field widget:** a developer supplies a regex pattern; the
  widget accepts keyboard character entry, shows the current input text,
  and visually distinguishes valid vs. invalid state as the pattern
  matches or fails to match the current input, using a new
  `WidgetTheme.INVALID_HIGHLIGHT` color when invalid (see
  Clarifications).
- All three widgets integrate with the shared framework the same way
  `ListWidget`/`ButtonWidget` do today: no custom per-widget focus
  management, reusing `WidgetTheme` styling, and (where relevant)
  `PopupWidget`'s `onUp()`/`onDown()`-style delegation pattern for use
  inside a modal popup.
- Since no real screen consumes them yet, "desired behavior" is verified
  through widget-level unit/acceptance tests exercising each widget in
  isolation (construction, navigation, selection/validation state), plus
  the dev-only demo harness (see Clarifications) for Step 4.5's manual
  playtest.

## Constraints / non-functional notes

- Must fit the existing complexity budgets (40-line functions, cyclomatic
  complexity 8, 4 params max, SLAP) — same as the parent framework intent.
- Stays Swing-based (`JPanel`/Key Bindings), no new UI framework/dependency
  — same as the parent framework.
- **No real consumer exists yet.** `specs/intent/ui-component-framework.md`
  explicitly deferred these three widgets for this reason ("none of the
  current screens need them, so they're deferred until there's a real
  consumer"), and that's still true today — no settings, character
  creation, or other screen needing a table/radio-group/pattern-field
  exists in the codebase or in any other open intent doc. This means:
  - There's no real screen to rebuild/playtest against (unlike #36's
    inventory-popup rebuild) — Step 4.5's manual playtest for this feature
    is necessarily limited to whatever minimal demo/harness proves the
    widgets work (see Open questions), not a real in-game screen.
  - Design choices below that would normally be driven by a concrete
    consumer's real requirements (exact confirm granularity for the table,
    exact validation-error visual treatment for the text field, an actual
    regex pattern) are instead being designed generically, and may need
    revisiting once a real consumer shows up.
- **Repo-specific Step 4.5 (manual playtest) still applies** per root
  `CLAUDE.md`, even without a real screen — see Open questions for how
  this feature will satisfy it.

## Open questions

- What's the first real consumer of the text/pattern-field widget? Still
  genuinely unresolved (see Clarifications — this doesn't block the
  widget's generic contract, but the eventual real validation patterns
  used in-game depend on it).

## Clarifications

- Q: Table widget: does Enter confirm the whole selected row, or just the
  selected cell?
  A: (Decided autonomously, no human available — flag for confirmation
  at Step 3 approval) The whole selected row, mirroring `ListWidget`'s
  existing row-level confirm semantics (`getSelectedItem()`/
  `setOnConfirm(Consumer<T>)`) rather than introducing a separate
  cell-level confirm concept. Column position still matters for
  navigation/highlighting, just not for what "confirm" returns. Simplest
  generalization, and most plausible future consumers (e.g. a settings
  table) want a row-level action.
  Affects: Desired behavior, Scope.

- Q: Radio group widget: vertical (Up/Down) or horizontal (Left/Right)
  option layout?
  A: (Decided autonomously, no human available — flag for confirmation
  at Step 3 approval) Vertical only (Up/Down), matching every other
  existing widget (`ListWidget`, `PopupWidget`'s onUp/onDown hooks) and
  the keybindings that already exist. This also means the radio group
  does NOT need new `MENU_LEFT`/`MENU_RIGHT` keybindings — those remain
  scoped to the table widget only, which genuinely needs 2D navigation.
  Reduces scope/risk for a widget with no real consumer yet; horizontal
  layout can be added later if a real consumer needs it.
  Affects: Desired behavior, Scope (narrows the Keybindings addition to
  the table widget only).

- Q: Text/pattern field: how is invalid-input state visually
  distinguished, given `WidgetTheme` only defines one highlight color
  today?
  A: (Decided autonomously, no human available — flag for confirmation
  at Step 3 approval) Add a new `WidgetTheme.INVALID_HIGHLIGHT` constant
  (a distinct color, e.g. a red tone) alongside the existing
  `SELECTED_HIGHLIGHT`/`NORMAL_TEXT`/`BACKGROUND` constants, applied to
  the field's text/border when the current input fails the pattern.
  Keeps the "shared styling convention" principle intact (a framework-
  level constant, not a widget-local color) so any future widget needing
  an error state can reuse it.
  Affects: Desired behavior, Constraints.

- Q: With no real screen to prove these widgets against, how is Step
  4.5's manual playtest (CLAUDE.md's repo-specific gate, "no feature is
  done without this playtest") satisfied?
  A: (Decided autonomously, no human available — flag for confirmation
  at Step 3 approval) Build a minimal dev-only demo harness exercising
  all three widgets together, following the existing precedent of
  `com.swiftfaze.veil.sandbox.ClassSandboxPanel` /
  `com.swiftfaze.veil.sandbox.ClassSandbox` (run via
  `mvn compile exec:java -Dexec.mainClass=...`, not part of the packaged
  build — see `docs/architecture.md`). This gives the human something
  real to visually playtest against without inventing a fake in-game
  screen, consistent with how the sandbox already serves this purpose for
  dev-only, no-current-consumer code.
  Affects: Scope (new in-scope bullet: a dev sandbox harness), Desired
  behavior.
