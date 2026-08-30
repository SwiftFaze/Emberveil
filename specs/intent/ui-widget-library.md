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
- [x] Spec drafted (`.feature` file)
- [x] Approved by human
- [x] Implemented
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [x] Acceptance tests passing
- [ ] Mutation testing passed
- [x] Documentation updated (`docs/`, and the wiki if player-facing)

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
    anywhere in the codebase yet. (These are shared with the radio group
    widget below, not table-only — see Clarifications.)
  - A **radio group widget**: single-select from a set of options,
    vertical (Up/Down) layout by default, matching every other widget's
    convention — with an optional horizontal (Left/Right) variant the
    caller can select instead, sharing the new `MENU_LEFT`/`MENU_RIGHT`
    keybindings with the table widget when used (see Clarifications for
    the full back-and-forth on this — it went vertical-only, then
    horizontal-only, and settled here) — using the same selected/normal
    `WidgetTheme` indication as every other widget (no separate
    hover/focused state — see the parent framework's Clarifications on
    this). Real consumer: the "Drop item?" confirmation popup below (a
    2-option horizontal instance).
  - A **text/form-field widget** with regex pattern validation: accepts
    keyboard text entry, validates against a caller-supplied pattern,
    surfaces valid/invalid state visually via a new
    `WidgetTheme.INVALID_HIGHLIGHT` constant (see Clarifications). Still
    has no identified real consumer (see Open questions) — built
    generically.
  - **Real-world proof via `InventoryPanel`, replacing the earlier
    dev-only-harness plan** (see Clarifications for why): rather than a
    speculative demo, the table and radio group widgets are proven by
    extending the existing, already-shipped `InventoryPanel`
    (`src/main/java/com/swiftfaze/veil/ui/InventoryPanel.java`):
    - The details pane's effects list (currently plain `"+" +
      effect.stat() + " (" + effect.calc() + ")"` text lines built from
      `item.getEffects()` in `detailLines()`) becomes a table widget: two
      columns (Stat, Value), one row per effect, row-highlighted,
      row-confirm is a no-op for now. The rest of the details pane
      (name/type/slot/damage) is unchanged — those are single per-item
      values, not a row-per-record dataset.
    - `PopupWidget` (`src/main/java/com/swiftfaze/veil/ui/widget/PopupWidget.java`)
      gains `onLeft()`/`onRight()` hooks, mirroring its existing
      `onUp()`/`onDown()` pattern exactly (same
      `WHEN_ANCESTOR_OF_FOCUSED_COMPONENT` binding, using the new
      `MENU_LEFT`/`MENU_RIGHT` keybindings) — a generic framework
      addition any future two-pane popup can reuse, not
      `InventoryPanel`-specific.
    - `InventoryPanel` tracks which pane currently owns Up/Down
      delegation (item list, or the new effects table). Right moves
      delegation to the effects table; Left moves it back to the item
      list. Pressing Right when the selected item has no effects is a
      no-op (nothing to navigate to).
    - A new keybinding (`Keybindings.java` gains e.g.
      `ACTION_DROP_ITEM`/`VK_D`) opens a second, nested full-screen
      `PopupWidget` containing a Yes/No radio group asking "Drop item?"
      — works regardless of which pane currently has navigation focus,
      since it targets the selected item, not the active pane. Defaults
      to "No" highlighted (safer default for a destructive-sounding
      action). No real drop logic — visual only, matching every other
      placeholder action already established this session (#54, #99).
      Deliberately nests a second full-screen popup rather than waiting
      on #99's smaller/centered variant, which isn't approved/built yet.
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
    including the table and radio group widgets even though they now
    also have real-screen coverage via `InventoryPanel` — isolated
    widget tests still matter for edge cases (empty data, wrap-around at
    the ends) a single real consumer might not exercise. The
    pattern-field widget is unit-tested only, since it still has no real
    consumer (see Open questions).
- Out of scope:
  - Wiring the **pattern-field widget** into an actual in-game screen —
    still no real consumer exists (see Open questions). The table and
    radio group widgets *are* wired into a real screen now
    (`InventoryPanel`, see above) — that's no longer out of scope for
    them.
  - Any mouse/pointer handling — this game is keyboard-only by design,
    same as the parent framework.
  - Redesigning `FocusManager` or the existing list/button/popup widgets —
    this feature only adds new widgets on top of the existing contract, it
    doesn't change it.
  - Deciding the validation patterns a real text field will eventually use
    (e.g. for a character name) — that depends on the first real consumer,
    which doesn't exist yet (see Open questions).

## Actors

- The player, navigating the rebuilt inventory popup's new effects table
  and "Drop item?" confirmation — the table and radio group widgets now
  have a real, in-game surface.
- Future feature work (a settings screen, character-creation screen, or
  any other screen needing validated text entry) is still the actor for
  the pattern-field widget, which has no real consumer yet.

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
- **Radio group widget:** a developer supplies a set of options; by
  default Up/Down moves the highlighted option, or Left/Right if
  constructed as horizontal (see Clarifications for the full history);
  Enter selects it; only one option is selected at a time, matching
  standard radio-button semantics; wraps around at the ends by default,
  same as the table/list widgets. The "Drop item?" popup below uses the
  horizontal variant.
- **Text/pattern field widget:** a developer supplies a regex pattern; the
  widget accepts keyboard character entry, shows the current input text,
  and visually distinguishes valid vs. invalid state as the pattern
  matches or fails to match the current input, using a new
  `WidgetTheme.INVALID_HIGHLIGHT` color when invalid (see
  Clarifications).
- All three widgets integrate with the shared framework the same way
  `ListWidget`/`ButtonWidget` do today: no custom per-widget focus
  management, reusing `WidgetTheme` styling, and (where relevant)
  `PopupWidget`'s `onUp()`/`onDown()`/`onLeft()`/`onRight()`-style
  delegation pattern for use inside a modal popup.
- **In the rebuilt inventory popup:** opening it and selecting an item
  with effects shows those effects as a table in the details pane, row 1
  highlighted. Pressing Right moves navigation focus to that table
  (Up/Down now moves between effect rows instead of item-list rows);
  pressing Left moves focus back to the item list. Pressing Right when
  the selected item has no effects does nothing. Pressing "D" (from
  either pane, for the currently selected item) opens a nested
  confirmation popup — a horizontal Yes/No radio group defaulting to "No"
  highlighted — asking "Drop item?"; confirming either choice, or
  Escape, closes it and returns to the inventory popup with no item
  actually removed.
- The pattern-field widget, having no real consumer, is verified through
  widget-level unit/acceptance tests exercising it in isolation
  (construction, typing, valid/invalid state) — same as the table and
  radio group widgets' own isolated edge-case coverage, alongside their
  new `InventoryPanel` integration coverage above. Step 4.5's manual
  playtest is performed against the real, running inventory popup — no
  separate dev-only harness is needed (see Clarifications).

## Constraints / non-functional notes

- Must fit the existing complexity budgets (40-line functions, cyclomatic
  complexity 8, 4 params max, SLAP) — same as the parent framework intent.
- Stays Swing-based (`JPanel`/Key Bindings), no new UI framework/dependency
  — same as the parent framework.
- **The table and radio group widgets now have a real consumer:** the
  rebuilt `InventoryPanel` (effects table + "Drop item?" confirmation —
  see Scope/Desired behavior above). `specs/intent/ui-component-framework.md`
  originally deferred all three widgets for lack of a consumer, but that's
  now only true for the **pattern-field widget** — it's still designed
  generically (no real validation pattern to design against yet, see Open
  questions).
  - This means Step 4.5's manual playtest is a real in-game playtest —
    running the game, opening the inventory, navigating the new effects
    table, and triggering the drop-confirmation popup — not a
    speculative dev-only harness (see Clarifications for how this
    replaced the earlier harness plan).
  - The pattern-field widget's design choices (exact validation-error
    visual treatment, an actual regex pattern) are still generic absent
    a real consumer, and may need revisiting once one shows up.
- **This feature modifies already-shipped code from #36**
  (`InventoryPanel`, `PopupWidget`), not just adding new widgets
  alongside it — `PopupWidget` gains `onLeft()`/`onRight()` hooks, and
  `InventoryPanel`'s existing `detailLines()`/`updateDetails()` are
  restructured to render the effects portion as a table. Both changes
  are additive (existing `onUp()`/`onDown()` behavior and the
  name/type/slot/damage header are unchanged), but this is worth calling
  out since it's broader than "three new standalone widget classes."

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

- Q: (Supersedes the "vertical only" radio-group answer above) While
  investigating issue #54 (same milestone, "2. Terminal UI component
  framework") for a separate task, its settings screen turned out to be
  a real consumer that had already been filed: a Fullscreen/Windowed
  "radio-style choice" and a font cycler, both explicitly navigated via
  Left/Right within a settings row (the same left/right-arrow gesture
  the rest of that screen's sliders use), not Up/Down. Does this change
  the radio group widget's default orientation?
  A: Yes — reverse the earlier "vertical only" decision. The radio group
  widget's default orientation is horizontal (Left/Right), matching this
  real consumer. It therefore does need the new `MENU_LEFT`/`MENU_RIGHT`
  keybindings after all — shared with the table widget's requirement for
  the same constants, not scoped to table alone as the earlier entry
  said. Vertical (Up/Down) support is not ruled out, but horizontal is
  now the proven, needed default rather than a deferred nice-to-have.
  This was found by reading the tracker, not invented — per this repo's
  workflow, this kind of correction belongs in the intent doc before
  regenerating the `.feature` file, which is why it's recorded here
  rather than silently fixed in the spec. The pattern-field widget still
  has no identified real consumer even after checking #54 (its settings
  items are all sliders/radio/cycler/keybind-capture, no free-text
  entry) — that open question stands.
  Affects: Desired behavior, Scope (radio group's Keybindings addition
  restored; the earlier "table widget only" framing corrected).

- Q: (Human review session, all five items below asked together) Table
  widget — confirm row-level confirm (from the first Clarifications
  entry above)?
  A: Confirmed as recommended. Added detail: the table is primarily a
  visual/display component; row-level confirm and row highlighting are
  both part of that, not a competing design.
  Affects: Desired behavior (no substantive change from the original
  entry, just confirmed).

- Q: Radio group — confirm horizontal-by-default (from the "supersedes"
  entry above)?
  A: Reversed again — vertical (Up/Down) by default, matching every
  other widget's convention, with an optional horizontal (Left/Right)
  variant the caller selects instead. This is the third and final answer
  on this topic: vertical-only -> horizontal-only (after #54) ->
  vertical-by-default-with-a-horizontal-option (here). The horizontal
  variant still needs the new `MENU_LEFT`/`MENU_RIGHT` keybindings when
  used — that part of the earlier correction stands.
  Affects: Desired behavior, Scope (widget now supports both
  orientations instead of picking one).

- Q: Text/pattern field — confirm the new `WidgetTheme.INVALID_HIGHLIGHT`
  constant (from the third Clarifications entry above)?
  A: Confirmed as recommended, no changes.
  Affects: none (confirmation only).

- Q: Step 4.5 playtest — confirm the dev-only demo harness (from the
  fourth Clarifications entry above)?
  A: Rejected in favor of real integration: rather than a standalone
  harness, the table and radio group widgets are proven by extending the
  already-shipped `InventoryPanel` — its effects list becomes a table
  (see the next entry for the details), and a new "D" keybinding opens a
  "Drop item?" Yes/No confirmation (a real, if visual-only, use of the
  radio group widget). Step 4.5 is now a real playtest of the running
  inventory popup, not a synthetic harness. See the Scope/Desired
  behavior sections above for the full mechanics.
  Affects: Scope (removes the dev-only harness bullet, adds the
  `InventoryPanel` integration bullet), Desired behavior, Constraints.

- Q: Table widget's `InventoryPanel` integration — does it replace the
  whole details pane, or just the effects list? And should navigation be
  able to move from the item list into the table and back?
  A: Just the effects list (`item.getEffects()`, currently rendered as
  plain `"+stat (calc)"` lines in `detailLines()`) becomes the table;
  name/type/slot/damage stay as they are, since those are single
  per-item values, not tabular data. Additionally: pressing Right from
  the item list moves navigation focus into the effects table (Up/Down
  then navigates its rows instead of the item list's), and pressing Left
  moves focus back to the item list. This requires `PopupWidget` to gain
  `onLeft()`/`onRight()` hooks mirroring its existing `onUp()`/
  `onDown()` pattern (same `WHEN_ANCESTOR_OF_FOCUSED_COMPONENT` binding,
  new `MENU_LEFT`/`MENU_RIGHT` keybindings) — decided as the natural,
  generically-reusable way to implement this rather than
  `InventoryPanel`-only plumbing, since it's the obvious extension of an
  already-established pattern (not re-litigated as a separate question).
  Right when the selected item has no effects is a no-op.
  Affects: Scope, Desired behavior (both substantially expanded — see
  above).

- Q: The "Drop item?" confirmation popup — nest a second full-screen
  `PopupWidget`, or wait for #99's smaller variant (from the sixth
  Clarifications entry above, in the original round)?
  A: Confirmed as recommended — nest a second full-screen `PopupWidget`
  now; #99's smaller/centered sizing is a future visual improvement, not
  a blocker.
  Affects: none (confirmation only).

- Q: Pattern-field widget's first real consumer (from the Open questions
  section)?
  A: Confirmed as recommended — stays genuinely open, no concrete
  consumer identified even after this review. The widget is built
  generically (any caller-supplied regex); the real validation pattern
  is decided once an actual consumer exists.
  Affects: none (Open questions entry stands as-is).
