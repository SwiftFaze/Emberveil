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
  - A **radio group widget**: single-select from a set of options, using
    the same selected/normal `WidgetTheme` indication as every other
    widget (no separate hover/focused state — see the parent framework's
    Clarifications on this).
  - A **text/form-field widget** with regex pattern validation: accepts
    keyboard text entry, validates against a caller-supplied pattern,
    surfaces valid/invalid state visually.
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
  confirms the selected cell/row (exact confirm granularity — cell vs.
  row — is an open question, see below).
- **Radio group widget:** a developer supplies a set of options; Up/Down
  (or Left/Right, depending on orientation — see Open questions) moves the
  highlighted option; Enter selects it; only one option is selected at a
  time, matching standard radio-button semantics.
- **Text/pattern field widget:** a developer supplies a regex pattern; the
  widget accepts keyboard character entry, shows the current input text,
  and visually distinguishes valid vs. invalid state as the pattern
  matches or fails to match the current input (exact visual treatment for
  "invalid" — since `WidgetTheme` only currently defines a "selected"
  highlight, not an error state — is an open question, see below).
- All three widgets integrate with the shared framework the same way
  `ListWidget`/`ButtonWidget` do today: no custom per-widget focus
  management, reusing `WidgetTheme` styling, and (where relevant)
  `PopupWidget`'s `onUp()`/`onDown()`-style delegation pattern for use
  inside a modal popup.
- Since no real screen consumes them yet, "desired behavior" is verified
  through widget-level unit/acceptance tests exercising each widget in
  isolation (construction, navigation, selection/validation state) rather
  than an end-to-end screen playtest.

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

- What's the first real consumer of the text/pattern-field widget? This
  was flagged as open in issue #35 itself and is still unresolved —
  worth identifying (even just conceptually, e.g. "a future
  character-creation name field") before finalizing validation-pattern
  API shape, so it isn't designed in a vacuum.
- Since there's no real screen to prove these widgets against, how should
  Step 4.5's manual playtest be satisfied? Options include a throwaway
  demo screen/harness (similar in spirit to the class/stats sandbox,
  `com.swiftfaze.veil.sandbox.ClassSandboxPanel`) that exercises all three
  widgets together, or treating comprehensive unit/acceptance test
  coverage as sufficient given the explicit "no consumer yet" framing.
- Table widget: does Enter confirm the whole selected row, or just the
  selected cell? Depends on what a future consumer (e.g. a settings table)
  would actually need.
- Radio group widget: vertical (Up/Down) or horizontal (Left/Right)
  option layout, or does the widget support both depending on caller
  configuration?
- Text/pattern field: `WidgetTheme` only defines one highlight color
  (`SELECTED_HIGHLIGHT`) today — does an invalid-input state need a new
  color constant (e.g. an error/red highlight), or does it reuse existing
  styling some other way (e.g. a border change)?
