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
    - The whole details pane is restructured into two tables (see the
      Step 4.5 Clarifications entry below for why): a static, non-
      selectable "Field | Value" table for every item property (ID,
      Name, Glyph, Type, Slot, Base Damage Min/Max), and — below an
      "Effects:" label — a real, row-navigable three-column (Type, Stat,
      Calc) table for `item.getEffects()`, row-highlighted, row-confirm
      a no-op for now. Both tables render a header row — the fields
      table's rows are no longer separate `JLabel`s, they're rows of the
      static table instead.
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

- Q: (Surfaced during Step 4.5 manual playtest) Playtesting the effects
  table showed it working, but the rest of the details pane (name/type/
  slot/damage) was still plain, ungridded `JLabel` text — inconsistent
  with the new table styling right next to it. Should the whole item
  details section become a table too, and should the effects table gain
  visible column headers (neither was built — headers were left as an
  optional/cosmetic nice-to-have when the table widget was first
  implemented)?
  A: Yes to both. The details pane is restructured into two tables: a
  "Field | Value" table listing every item property (ID, Name, Glyph,
  Type, Slot, and Base Damage Min/Max when the item has damage — same
  conditional as the original `detailLines()`), followed by an "Effects:"
  label and the effects table, now three columns (Type, Stat, Calc,
  matching `Item.Effect`'s actual record fields directly instead of the
  earlier combined "+stat (calc)" two-column format). Both tables get a
  header row. This required two `TableWidget` additions: an optional
  `columnHeaders` constructor parameter (rendered as a non-selectable
  header row, excluded from row-navigation indexing) and a
  `setSelectable(boolean)` flag (the new fields table uses
  `setSelectable(false)` since it's static reference data, not meant to
  be keyboard-navigated — only the effects table keeps real row
  navigation/highlighting, unchanged from before). Both additions are
  backward-compatible defaults (no headers, selectable) so the existing
  isolated `ui-widget-table.feature` scenarios are unaffected.
  Affects: Scope, Desired behavior (details-pane structure), and
  `ui-widget-table.feature` (new scenario for the fields table).

- Q: (Surfaced during a second round of Step 4.5 manual playtest, after
  the field-value table restructure above) Three more issues: (1) the
  effects table's first row showed the selected-row highlight color by
  default, even before the details pane had navigation focus at all;
  (2) pressing Right only worked when the selected item had at least
  one effect — items without effects couldn't reach the details pane
  at all; (3) the tables rendered as plain left-aligned text, not
  full-width with visible cell/table borders. How should these be
  fixed?
  A: (1)+(2) reveal the same underlying gap: "effects table has
  navigation focus" was being conflated with "effects table's row 0 is
  highlighted," and Right's guard required an existing effects row.
  Fixed by reworking the details pane into one continuous, always-
  reachable navigable region instead of a single effects-table-only
  target: `InventoryPanel` now tracks a 3-state `Focus` (`ITEM_LIST`,
  `FIELDS`, `EFFECTS`) instead of a boolean. Right from the item list
  always enters `FIELDS` (which always has rows — every item has at
  least ID/Name/Glyph/Type/Slot) and highlights its first row. Down at
  the fields table's last row falls through into the effects table's
  first row (only if it has any); Up at the effects table's first row
  falls back to the fields table's last row. Left, from either table,
  exits straight back to `ITEM_LIST`. Only the table matching the
  current `Focus` is ever highlighted — both tables call
  `setSelectable(false)` whenever they're not the active one (including
  on every item-selection change, via `updateDetails`), which is also
  what fixes issue (1): the stray default highlight was `TableWidget`
  showing row 0 highlighted purely because `selectable` was left `true`
  by default, independent of whether the popup's own focus-tracking
  agreed the table was actually "active." `TableWidget` gained
  `moveToStart()`/`moveToEnd()`/`isAtFirstRow()`/`isAtLastRow()` to
  support these boundary transitions, and both tables are now
  constructed with `setWrapAround(false)` (matching the item list's own
  convention) so a boundary is actually reachable to trigger a
  transition, rather than wrapping past it.
  (3) `TableWidget`'s rendering was reworked from one space-joined
  `JLabel` per row to one `JPanel` per row (a `GridLayout` of per-cell
  `JLabel`s), each cell bordered on its bottom/right edge
  (`WidgetTheme.TABLE_BORDER`, a new constant) with the table's own
  top/left border completing the grid rectangle; the header row (and,
  for now, only the header row) uses a new `WidgetTheme.TABLE_HEADER_BACKGROUND`
  constant to read as visually distinct. Each row panel's maximum width
  is set to `Integer.MAX_VALUE` so `BoxLayout` stretches it to fill the
  table's full width, and the table itself gets the same treatment so
  it fills `detailsPanel`'s full width in turn.
  This also obsoletes the earlier "Pressing Right does nothing when the
  selected item has no effects" scenario (issue (2) above was that
  exact behavior, now deliberately reversed) — removed from
  `ui-widget-table.feature` and replaced with scenarios for the new
  fields-table-first, fall-through navigation model.
  Affects: Scope, Desired behavior (navigation model substantially
  reworked), `ui-widget-table.feature` (scenarios rewritten/added).

- Q: (Bug found from a playtest screenshot, same round) Both tables
  rendered as completely empty black boxes — no header text, no data
  rows, just the outer table border. Root cause?
  A: `TableWidget.buildRowPanel()` called `rowPanel.getPreferredSize()`
  to compute `setMaximumSize`'s height *before* any cell labels had
  been added to the panel — an empty `JPanel`'s preferred height is
  ~0, so `BoxLayout` clamped every row (including the header) to zero
  visible height, even though the labels themselves existed with
  correct text/borders. Fixed by moving the `getPreferredSize()` call
  to after the cells are added. Not something the headless Cucumber
  suite could catch (it doesn't exercise real Swing layout), so this
  needed a real playtest screenshot to surface.
  Affects: none (bug fix, no behavior/scope change).

- Q: (Third round of Step 4.5 playtest, after the above fix) Two more
  findings: (1) the details pane has no scrollbar, so a field/effects
  table taller than the fixed `BODY_HEIGHT` (280px) gets clipped
  instead of being reachable; (2) the selected-row highlight only
  recolors the text, not the whole row — same for the item list.
  A: (1) `detailsPanel` is now wrapped in the same `buildScrollPane()`
  helper (`JScrollPane` + `TerminalScrollBarUI`) the item list already
  uses, rather than being added to the body directly.
  (2) Both `ListWidget` and `TableWidget`'s highlight logic now set the
  row's/cell's *background* to `WidgetTheme.SELECTED_HIGHLIGHT` (with
  foreground switching to a new `WidgetTheme.SELECTED_TEXT`, black, for
  contrast) instead of only recoloring the foreground text — matching
  every other widget's still-single-highlight-state convention, just
  rendered as a filled row instead of colored text. For `ListWidget`,
  this also needed each item label stretched to the widget's full width
  (`setMaximumSize` after the label's text is set, same technique
  `TableWidget`'s row panels already use) — otherwise a background fill
  would only show behind the text itself, not "the whole row" as asked.
  `RadioGroupWidget` was deliberately left alone — not asked, and it
  has no current real-screen consumer to playtest against yet (see
  Open questions).
  Affects: Desired behavior (list/table selected-row rendering), no
  scope change.

- Q: (Fourth round of Step 4.5 playtest) Three more findings: (1) the
  scrollbar should be gray, not the orange highlight color; (2)
  scrolling the details pane to the bottom and back to the top still
  leaves the fields table's header cut off; (3) the drop-confirmation
  popup's Yes/No radio group still only highlights by text color —
  "this needs to be a global thing, highlight is the orange background
  that it currently has." This corrects the immediately preceding
  entry's claim that `RadioGroupWidget` "has no current real-screen
  consumer" — it does (`DropConfirmationPopup`'s Yes/No choice, wired
  up earlier in this same feature); that claim was simply wrong, not a
  behavior change since then.
  A: (1) `TerminalScrollBarUI` had two separate places defining the
  thumb color — the inherited `configureScrollBarColors()` (which set
  the `thumbColor` field) and a `paintThumb()` override that hardcoded
  `WidgetTheme.SELECTED_HIGHLIGHT` directly, bypassing that field
  entirely. Both now use a new `WidgetTheme.SCROLLBAR_THUMB` (gray)
  constant.
  (2) `InventoryPanel` now keeps a reference to the details
  `JScrollPane` and explicitly resets its viewport to `(0, 0)` every
  time `updateDetails()` rebuilds the pane's content — `JScrollPane`
  doesn't do this automatically when its view's content changes, so
  switching items while scrolled down previously left the new item's
  header starting mid-scroll. Also added a few pixels of top padding to
  `detailsPanel`'s border, so the header row isn't flush against the
  viewport's very top edge even if a scroll position lands a pixel or
  two short. (This is a defensive fix for a bug in a live GUI's real
  Swing layout/scroll behavior, which the headless Cucumber suite
  structurally cannot exercise or verify — it doesn't render real
  Swing frames. Confirming it's fully fixed depends on the next
  playtest, not `mvn test`.)
  (3) Rather than duplicating the "background fill, not just text
  color" highlight logic a third time, added one shared
  `WidgetTheme.applySelection(JLabel, boolean)` helper and refactored
  `ListWidget`, `TableWidget`, and now `RadioGroupWidget` to all call
  it — directly addressing "this needs to be a global thing" by making
  it structurally impossible for a widget to reinvent its own selected-
  row look instead of reusing the one shared definition.
  Affects: Desired behavior (scrollbar color, details-pane scroll
  reset, radio group highlight), and a small internal refactor
  (`WidgetTheme.applySelection`) with no scenario-visible behavior
  change for `ListWidget`/`TableWidget` (same colors as before, just
  centralized).

- Q: (Fifth round of Step 4.5 playtest) The scrollbar color and radio
  group fixes were confirmed working, but the fields table's header
  was still hidden after scrolling — two screenshots at the same
  selection ("Steel Dagger", "ID" row highlighted) showed the header
  visible only after scrolling up slightly *further* than wherever the
  automatic positioning had stopped. What was still wrong, given the
  previous entry's viewport-reset fix?
  A: A different bug than previously diagnosed — the viewport-reset-to-
  (0,0) fix was real and correct, but only runs on `updateDetails()`
  (item *selection* changes). Entering the fields table via Right calls
  `TableWidget.moveToStart()`, which calls `refreshHighlight()`, which
  calls `scrollRectToVisible()` on row 0's own bounds — and
  `scrollRectToVisible` scrolls the *minimum* distance needed to reveal
  exactly the rectangle it's given. Since the header is a separate
  component sitting above row 0 that this call knows nothing about,
  "just enough to reveal row 0" can leave the header scrolled out of
  view immediately above it — confirmed by the screenshots (more room
  existed above; the automatic scroll simply didn't use it). Fixed by
  having `TableWidget` union the header panel's bounds into the scroll
  target whenever row 0 is the selected row, so revealing row 0 always
  drags the header along with it. This is a `TableWidget`-level fix
  (not `InventoryPanel`-specific), so it applies to both the fields and
  effects tables, and to any future consumer with a header.
  Affects: none (bug fix, no behavior/scope change beyond "row 0 +
  header now scroll into view together").

- Q: The pattern-field widget still has no real consumer (see Open
  questions), but its visual quality was checked against a throwaway,
  unshipped dev harness (`sandbox/PatternFieldSandbox.java` — a
  standalone `main()` with a few labeled fields, explicitly for a
  one-off look, not committed as part of this feature and deleted
  after). That look prompted real widget-level feedback: it needs a
  border, full width, the border colored by validity (red/green, not
  just the text), and a visible focused state when tabbed to. Since
  the widget itself is real, approved scope even without a shipped
  consumer yet, should this feedback be applied to `PatternFieldWidget`
  itself?
  A: Yes — this is feedback on the real widget, the throwaway harness
  was just how it got looked at. Added a `FocusListener` (new state:
  `PatternFieldWidget` didn't track focus at all before) and a
  compound border: a colored line border (new
  `WidgetTheme.VALID_HIGHLIGHT`, green, alongside the existing
  `INVALID_HIGHLIGHT`, red — chosen by `patternIsValid()`) at 1px
  normally, 2px while focused, plus fixed internal padding. Full width
  via the same `setMaximumSize`-after-content technique every other
  widget in this feature already uses. No change to the widget's
  actual validation/input behavior — `PatternFieldWidgetTest` and
  `ui-widget-pattern-field.feature`'s scenarios (which don't assert on
  color/border/focus, matching this framework's precedent of not
  testing exact colors headlessly) are unaffected.
  Affects: Desired behavior (pattern-field widget's visual
  presentation), no scope change — still no real consumer.

- Q: A follow-up look at the same throwaway harness showed the border
  looking too thick and the typed text not visible at all inside it.
  Root cause?
  A: The widget's total height was fixed once at construction, computed
  tightly from the label's own preferred height plus a small, fixed
  buffer — barely enough for the *thinnest* (unfocused) border+padding
  combination, with no slack budgeted for the border growing 1px->2px
  on focus. Since that total height never grew to compensate, the
  thicker focused border ate further into an already-tight budget,
  squeezing the label's own content area toward zero — "thick border"
  and "invisible text" were the same bug, not two. Fixed by using a
  generous fixed height (40px) that isn't derived from the label's
  metrics at all, comfortably fitting label + border + padding in
  either focus state.
  Affects: none (bug fix, no behavior/scope change).

- Q: Requested a Material Design "outlined text field" look instead:
  only a bottom border when unfocused, full outline when focused, the
  field's own label floating on/breaking the top border edge (shown a
  reference screenshot of this pattern), and the border should be
  white while the field is empty rather than immediately red (red/
  green only once there's actual input to judge).
  A: Implemented using `javax.swing.border.TitledBorder` wrapping a
  swappable inner border (`BorderFactory.createLineBorder` — full
  outline — when focused, `createMatteBorder(0,0,width,0,...)` —
  bottom only — when not) rather than hand-rolling custom
  gap-cutting/`Border`-painting logic: `TitledBorder` already
  implements exactly "a label that breaks the wrapped border's line,
  positioned at its top edge" — it reserves the same label space
  regardless of the wrapped border's actual drawn edges, so the label
  only visibly "breaks" a line when one is actually drawn there
  (focused), and just floats above the field otherwise (unfocused,
  bottom-only border has no top line to interrupt) — the two states
  fall out of the same mechanism for free, no separate label-
  positioning logic needed.
  Added a new empty/neutral state: `WidgetTheme.NORMAL_TEXT` (white)
  while `input` is empty, `VALID_HIGHLIGHT`/`INVALID_HIGHLIGHT`
  (green/red) only once there's at least one character — this doesn't
  change `patternIsValid()`'s own return value (an empty string still
  fails most patterns, per the already-approved "empty field defaults
  to invalid" scenario in `ui-widget-pattern-field.feature`), only the
  border/label *color* shown for that state.
  The field's caption is now the widget's own concern (a new
  `PatternFieldWidget(String pattern, String fieldLabel)` overload,
  the label text passed straight to `TitledBorder`) rather than a
  separate external `JLabel` the caller builds and positions above
  it — `sandbox/PatternFieldSandbox.java` (still throwaway, still
  unshipped) updated to match, no longer building its own caption.
  The original single-arg constructor is kept, delegating with a null
  label (falls back to the plain, unlabeled border from the last few
  entries) — existing callers (`PatternFieldWidgetTest`,
  `UiWidgetPatternFieldSteps`) are unaffected.
  Affects: Desired behavior (pattern-field widget's border/label
  presentation), a new constructor overload (additive, old one kept),
  no scope change — still no real consumer.

- Q: Outlined-field look confirmed good, just wanted the border a bit
  thinner.
  A: Focused width dropped from 2px to 1px (matching the unfocused
  width — Swing's line/matte borders only take integer pixel widths,
  so 1px is as thin as either state can get). Focus is still conveyed
  by the outline shape (full box vs. bottom-only) and color, not by
  extra thickness.
  Affects: none (minor visual tweak).

- Q: Pressing Enter turned the border red — expected, or a bug? If a
  bug, Enter should probably behave like Tab (move to the next field)
  instead.
  A: A real bug, not intended: `Character.isWhitespace('\n')` returns
  `true`, so Enter's keyTyped character (`\n`/`\r`) satisfied the same
  "appendable" check a space does and got silently typed into the
  input as a literal newline — a character no single-line pattern ever
  matches, turning the field invalid (red) the instant Enter was
  pressed. Fixed by excluding `\n`/`\r` from the appendable-character
  check (used by both `keyTyped` and `typeCharacters`), and — matching
  the suggestion — bound `VK_ENTER` to `transferFocus()`, so Enter now
  moves to the next field exactly like Tab, rather than doing nothing
  (previously) or typing an invisible character (the bug).
  Affects: Desired behavior (Enter now moves focus instead of being
  silently swallowed as input) — this wasn't covered by any existing
  scenario (Enter's real-keyboard behavior isn't exercised in headless
  tests, matching this framework's precedent), so nothing to update
  there.

- Q: Requested real cursor support — a visible caret, Left/Right to
  move it, Ctrl+A to select all. The widget had no cursor concept at
  all (a `StringBuilder` that only supported appending at the end and
  removing from the end). Hand-roll caret/selection tracking and
  rendering on top of that, or a different approach?
  A: Switched the internal representation from a `StringBuilder` +
  plain `JLabel` (no cursor/selection rendering capability at all) to
  a real `javax.swing.JTextField`, styled to match (monospaced font,
  black background, the shared `WidgetTheme` selection colors reused
  for the text-selection highlight too) rather than hand-rolling
  cursor/selection tracking and painting. `JTextField` already
  implements caret rendering, Left/Right/Home/End movement, Ctrl+A
  select-all, click-to-position, and selection-replace-on-type
  correctly and for free — reinventing that would just be re-deriving
  well-tested Swing behavior with more room for bugs, not a capability
  gap this framework needed to fill itself. The only custom piece left
  is a `DocumentFilter` restricting which characters the pattern field
  will actually accept (reusing the same `isAppendable()` check the
  removed `KeyListener` used to apply inline), which now also
  transparently covers `typeCharacters()`'s programmatic insertion,
  Backspace/Delete, and real interactive typing uniformly, instead of
  three separate call sites each needing their own filtering.
  `getInput()`/`patternIsValid()`/`typeCharacters()`/
  `deleteLastCharacter()` — the four methods the existing tests and
  `ui-widget-pattern-field.feature`'s step definitions call — keep
  their exact existing signatures and observable behavior;
  `deleteLastCharacter()` is now genuinely "delete at the cursor" (or
  delete the active selection) rather than "always remove the very
  last character," but the two happen to coincide for every existing
  scenario, since nothing in them moves the cursor away from the end
  before deleting.
  One caret quirk surfaced by the existing unit tests: `DefaultCaret`
  doesn't reliably auto-advance past a programmatic
  `replaceSelection()` call on a component that's never been realized/
  shown (true of every headless unit/Cucumber test here) — fixed by
  explicitly setting the caret to the document's end after
  `typeCharacters()`'s insert. Real interactive typing (via the
  field's own native key handling once it has real focus in an actual
  window) doesn't hit this, since it isn't going through
  `replaceSelection()` from outside the component.
  Affects: Desired behavior (real cursor/selection support), internal
  implementation (JTextField instead of StringBuilder+JLabel — no
  public API removed, only added: none of the four existing methods
  changed signature).

- Q: Wanted the cursor to look more like a console/terminal — a solid
  block/rectangle instead of Swing's default thin vertical line.
  A: A custom `Caret` (`DefaultCaret` subclass overriding just
  `paint()`/`damage()`) rather than anything TableWidget/ListWidget-
  style — Swing's `Caret` interface is specifically designed for this
  kind of shape customization, and `DefaultCaret` already handles
  blink timing correctly, so only the paint shape needed changing:
  fills a `charWidth('M')`-wide rectangle at the caret's row instead
  of drawing the default 1px line.
  Affects: Desired behavior (cursor appearance only, no
  functional/API change).
