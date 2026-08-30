# Intent: Terminal-style UI component framework

- **Slug(s):** ui-component-framework (matches
  `/specs/features/ui-component-framework.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-29
- **Source:** [GitHub issue #36](https://github.com/SwiftFaze/Veil/issues/36)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [x] Approved by human
- [ ] Implemented
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [ ] Acceptance tests passing
- [ ] Mutation testing passed
- [ ] Documentation updated (`docs/`, and the wiki if player-facing)

## Problem

The current `ui/` package has no real component system:
`TerminalPanel` (`.../ui/TerminalPanel.java`) only centralizes background
color, font, and a label helper; `SelectableMenu` (`.../ui/SelectableMenu.java`)
is a bare index-wrap counter with no concept of items, styling, or nesting;
and each screen (`MenuPanel`, `InventoryPanel`, `PlayerInfoPanel`) hand-rolls
its own labels, borders, and Swing `InputMap`/`ActionMap` wiring from
scratch. There's no shared notion of a popup, button, table, or hovered vs.
selected state, and no reusable keyboard-navigation/focus manager — every
panel reimplements Up/Down/Enter/Escape handling independently
(`MenuPanel.bindKeys`, duplicated again in `ClassSandboxPanel`). This makes
every new screen a copy-paste-and-tweak job, with no consistent way to show
what's currently focused vs. hovered vs. selected.

The game is entirely keyboard-driven, terminal-style (monospaced glyphs on
black, ASCII/Unicode borders) — no mouse support is needed or wanted for
this system.

## Scope

- In scope:
  - A shared component framework: a base widget contract/interface, a
    keyboard focus/navigation manager (up/down/left-right/tab-style
    movement between and within widgets, plus modal focus capture — see
    Clarifications), and shared styling conventions for selected/disabled
    state (consistent color/highlight rules, replacing each panel's own ad
    hoc `SELECTED_COLOR` handling like `MenuPanel`'s). There is only one
    visual highlight state, "selected" — no separate hovered/focused
    states; those are mouse-era concepts that don't apply to a
    keyboard-only, single-focus-owner UI (see Clarifications).
  - Three concrete widgets built on that framework:
    - A list widget, replacing `SelectableMenu` + `MenuPanel`'s hand-rolled
      label list. Must accept a pluggable data source (even a simple
      `List<String>`/supplier abstraction) rather than hardcoded static
      item strings — see Constraints, coupling with #26.
    - A button widget, whose concrete use in the rebuilt screen is a Close
      button inside the new popup (see Clarifications).
    - A popup/modal widget (a dismissible overlay), replacing ad hoc
      show/hide toggling like `EastPanel.toggleInventory`. Opening it
      captures keyboard focus (onto its Close button), blocking the menu
      behind it until the popup is dismissed (Escape, or Enter on Close),
      which then restores focus to the menu — same restore pattern as
      today's `EastPanel.toggleInventory`/`restoreGameFocus`. See
      Clarifications.
  - Deleting the current hand-rolled menu/inventory implementation
    (`SelectableMenu`, the bespoke wiring in `MenuPanel`, `InventoryPanel`'s
    static label list) and rebuilding the existing in-game menu + inventory
    screen on top of the new components, so the framework is proven against
    a real screen rather than a standalone demo. This rebuilt screen is a
    disposable skeleton proof, not final UX — layout doesn't matter yet and
    it's expected to be fully deleted and rebuilt once milestone "6. Intro
    Quest & UI" (#7) builds the real inventory UI (list, hover details,
    drop/equip, search). See Clarifications.
  - Migrating `ClassSandboxPanel` (`src/main/java/com/swiftfaze/veil/sandbox/ClassSandboxPanel.java`)
    off `SelectableMenu` onto the new list widget — a minimal, mechanical
    swap of its selection model only; its stats-display logic is untouched.
    `SelectableMenu` is deleted entirely, so this is required for the
    codebase to compile, not optional cleanup. A deeper sandbox redesign is
    a separate future milestone ("3. Dev sandbox framework") and out of
    scope here. See Clarifications.
  - Migrating the acceptance-test scenarios in four existing `.feature`
    files that assert on the classes being deleted/rebuilt here, so
    `mvn verify` stays green: `keyboard-input-and-menu-navigation.feature`
    (its `SelectableMenu` scenarios), `ui-panel-rendering-and-composition.feature`
    (its `MenuPanel` cancel/confirm-through-`EastPanel` scenarios),
    `class-sandbox-panel-selection.feature` (its `ClassSandboxPanel`
    selection scenarios), and `data-driven-item.feature` (its "EastPanel
    wires real core item data into its inventory panel" scenario, which
    also asserts on `InventoryPanel`'s current component structure) —
    plus updating their rows in `specs/features/README.md`. See
    Clarifications.
  - Clear, consistent visual indication of the selected state across all
    new widgets.
- Out of scope:
  - Table widget, radio group widget, and text/form-input fields with
    pattern (regex) validation — tracked separately in #35; none of the
    current screens need them, so they're deferred until there's a real
    consumer.
  - Any mouse/pointer handling — this game is keyboard-only by design.
  - Changing what item data is loaded or how — #26 phase 4 already shipped
    this (`data-driven-item`, issue #51); this feature only needs the
    rebuilt list widget to keep rendering the same real `core:item` data
    `EastPanel` already loads, not to change the loading mechanism.
  - Real keyboard navigation/selection within the rebuilt inventory popup's
    item list — it stays a static, non-interactive display (list widget
    used for consistent rendering only). Per-item inventory interaction is
    milestone "6. Intro Quest & UI" (#7)'s job. See Clarifications.
  - A deeper redesign of `ClassSandboxPanel` beyond the mechanical
    `SelectableMenu` → list-widget swap — tracked in milestone "3. Dev
    sandbox framework".

## Actors

- The player, navigating all in-game menus/popups/lists via keyboard only
  (no mouse).
- Future feature work (inventory, character screen, dialogs, the class
  sandbox) becomes the actor building new screens on top of this framework
  instead of hand-rolling Swing wiring per screen.

## Desired behavior

- A developer building a new screen composes it from shared widgets (list,
  button, popup) rather than writing bespoke `InputMap`/`ActionMap` and
  highlight-color logic per panel.
- Keyboard navigation (movement between/within widgets, confirm, cancel) is
  handled once by a shared focus/navigation manager, not reimplemented per
  screen.
- At any point, it's visually unambiguous which item is currently selected
  within the widget that owns keyboard focus, using one consistent
  highlight style across the whole UI (no separate hovered/focused visual
  states — see Clarifications).
- The existing menu (Inventory/Help/Journal/Map/Character/Stats) and
  inventory display are rebuilt on the new components with equivalent
  behavior to today (Up/Down/Enter/Escape navigation, only Inventory wired
  to an action, the rest decorative) — confirming the framework actually
  supports a real screen end-to-end. The rebuilt inventory list itself
  stays static/non-interactive, matching today.
- The popup (opened via Inventory) is modal: opening it moves keyboard
  focus onto its Close button, blocking the menu behind it from receiving
  Up/Down until the popup is dismissed via Escape or Enter-on-Close, at
  which point focus returns to the menu — see Clarifications.
- `ClassSandboxPanel`'s Up/Down selection is migrated onto the new list
  widget so `SelectableMenu` can be deleted outright, with no other
  behavior change to the sandbox.

## Constraints / non-functional notes

- Keyboard-only; no mouse/pointer handling needed anywhere in this system.
- Must fit the existing complexity budgets (40-line functions, cyclomatic
  complexity 8, 4 params max, SLAP) — a generic component framework is
  exactly the kind of code that tends to balloon past these, so expect
  this to decompose into several small classes (widget base, focus
  manager, style/theme constants, each concrete widget) rather than one
  large one.
- Stays Swing-based (`JPanel`/Key Bindings), matching the rest of the
  codebase (see `docs/architecture.md`) — no new UI framework/dependency.
- **Coupling with #26 (data-driven mod structure):** #26 phase 4
  (`data-driven-item`, issue #51) has already shipped — `EastPanel`
  already loads real `core:item` data via `ModLoader` and passes it to
  `InventoryPanel.showItems(List<Item>)`, and
  `data-driven-item.feature`'s "EastPanel wires real core item data into
  its inventory panel" scenario already covers this. (The issue #36 text
  describes this as still-future work; it isn't — see Clarifications.)
  This changes the constraint from "keep the door open for future item
  data" to "don't regress already-shipped real item data": the rebuilt
  list widget must keep rendering real loaded items, not just arbitrary
  strings, and must still accept a pluggable data source (even a simple
  `List<String>`/supplier abstraction) rather than hardcoded static item
  strings, since that's what lets it keep consuming real `Item` data
  without further rework.

## Open questions

None — all forks identified while drafting the `.feature` file were
resolved in a grilling session; see Clarifications below.

## Clarifications

- Q: Scope says delete `SelectableMenu`, but `ClassSandboxPanel`
  (`src/main/java/com/swiftfaze/veil/sandbox/ClassSandboxPanel.java:19,26`)
  directly instantiates it and isn't mentioned as being migrated —
  deleting `SelectableMenu` as scoped won't compile otherwise. Widen scope
  to migrate `ClassSandboxPanel` onto the new list widget too, keep
  `SelectableMenu` alive just for it, or something else?
  A: Delete `SelectableMenu` outright; migrate `ClassSandboxPanel`'s
  selection onto the new list widget as a minimal, mechanical swap (its
  stats-display logic untouched). The deeper sandbox rework is a separate
  future milestone ("3. Dev sandbox framework"), not blocking this issue
  from making the minimal change needed to keep it compiling.
  Affects: Scope (new in-scope bullet), general.

- Q: `keyboard-input-and-menu-navigation.feature` (4 scenarios on
  `SelectableMenu`) and `ui-panel-rendering-and-composition.feature`
  (scenarios on `MenuPanel`'s Cancel/Confirm through `EastPanel`) will
  break structurally once this feature deletes/rebuilds those classes.
  Should this feature own migrating those scenarios, or leave it as a
  follow-up?
  A: This feature owns migrating the affected scenarios out of both files
  into `ui-component-framework.feature`, updating
  `specs/features/README.md`'s rows for both — required so `mvn verify`
  stays green, per the workflow's "never mark a feature done without
  acceptance tests passing."
  Affects: Scope (new in-scope bullet), general. (Also surfaced a third
  affected file while investigating this — see next entry.)

- Q: (Self-identified while implementing the above) A third existing file,
  `class-sandbox-panel-selection.feature`, also asserts directly on
  `ClassSandboxPanel`'s current `SelectableMenu`-backed internals
  (`getClassLabel(i).getForeground()`, `getActionMap().get(...)`), and
  will break once `ClassSandboxPanel` migrates per the first entry above.
  `class-stats-sandbox.feature` was checked and confirmed unaffected (it
  only exercises `ClassSandboxModel`, not the panel).
  A: Same policy applies — this feature migrates
  `class-sandbox-panel-selection.feature`'s scenarios too and updates its
  README row.
  Affects: Scope (folded into the existing-specs-migration bullet).

- Q: The rebuilt screen needs list, button, and popup widgets proven
  end-to-end, but nothing in the current menu/inventory screen is
  naturally a button — where does Button get a real use?
  A: A Close button inside the new popup.
  Affects: Scope, Desired behavior.

- Q: The issue's language ("hovered vs. selected vs. focused") implies
  three distinct visual states, but there's no mouse and today's UI has
  exactly one (the single highlighted row). What do these terms actually
  mean here?
  A: There is no hovered or focused state — those are mouse-era concepts
  that don't apply. Only one visual state exists: "selected" (today's one
  highlight, formally named).
  Affects: Scope, Desired behavior (all hover/focused language struck).

- Q: Does the rebuilt inventory list get real keyboard navigation, or stay
  a static display like today?
  A: Stays static/non-interactive for now — matches "equivalent behavior
  to today," and confirmed by milestone "6. Intro Quest & UI" (#7)'s
  description, which explicitly owns "a real inventory UI (list, hover
  details, drop/equip, search)" as future work; this screen is a
  disposable skeleton proof, not final UX, and is expected to be fully
  deleted and rebuilt once that milestone lands.
  Affects: Scope (new out-of-scope bullet), Desired behavior.

- Q: Given the popup now has a real focusable Close button, does opening
  it pull keyboard focus into the popup (modal), or does focus stay on the
  menu behind it the whole time like today (non-modal, making the Close
  button unreachable by keyboard)?
  A: Modal — opening the popup moves focus onto its Close button, like
  opening a chat with an NPC; you need to be able to navigate the popup's
  content. Up/Down is blocked from reaching the menu behind it until the
  popup is dismissed (Escape, or Enter-on-Close), at which point focus
  returns to the menu — same restore pattern as today's
  `EastPanel.toggleInventory`/`restoreGameFocus`.
  Affects: Scope, Desired behavior.

- Q: (Self-identified while checking for other existing specs affected by
  the `InventoryPanel` rebuild) Issue #36's Constraints section frames
  #26 phase 4 (wiring real item data into `InventoryPanel`) as future
  work. Checking `specs/intent/data-driven-item.md`'s Status shows every
  box checked (issue #51, already merged) — `EastPanel` already loads
  real `core:item` data via `ModLoader` and passes it to
  `InventoryPanel.showItems(List<Item>)`, and
  `data-driven-item.feature`'s "EastPanel wires real core item data into
  its inventory panel" scenario already covers it. That scenario asserts
  directly on `InventoryPanel`'s current component structure
  (`getComponents()` → `JLabel`), so it's a fourth existing spec broken by
  this feature's `InventoryPanel` rebuild, in addition to the three found
  earlier. Should this be treated the same as those three?
  A: Yes — same policy: migrate its scenario too, update its
  `specs/features/README.md` row, and correct the Constraints section's
  stale "#26 phase 4 plans to" framing to reflect that it already shipped.
  Affects: Scope (existing-specs-migration bullet, now four files),
  Constraints (corrected).

- Q: (Surfaced during Step 4.5 manual playtest) The first implementation
  laid the inventory popup out inline inside `EastPanel`'s own
  `BorderLayout.CENTER`, sized to the sidebar. Playtesting it in the real
  game showed this didn't read as a popup at all — it just looked like the
  sidebar's old always-visible inventory list, and (compounding a separate
  real bug where `FocusManager.captureModally()` didn't actually gate
  `MenuPanel`'s key bindings) the menu behind it kept responding to
  Up/Down. Given the "disposable skeleton, no layout polish" framing this
  spec approved under, should the popup's on-screen position be reworked
  now, or left as-is and deferred with the rest of the layout?
  A: Rework it now — the popup should cover the game view, not just sit in
  the sidebar. Implemented via a `JLayeredPane` (`ui/GameWindow.java`)
  layering the popup above a `mainArea` panel (`GamePanel` + `EastPanel`),
  stretched to match via a small `FillLayout`. This is judged a real
  behavior gap (a "popup" that doesn't visually behave like one), not the
  visual polish/layout-tuning the disposable-skeleton framing was meant to
  defer.
  Affects: Desired behavior, Scope (the disposable-skeleton/no-layout-polish
  non-goal now carries one explicit exception).

- Q: (Surfaced during the same Step 4.5 playtest, right after the popup
  overlay fix above) With the popup now correctly covering the game view,
  playtesting also showed the sidebar's MenuPanel (I/H/J/M/P/O) sitting
  underneath/behind it — only "I - Inventory" ever did anything, the rest
  were always decorative placeholders. Given the inventory toggle already
  works directly via the keyboard "I" shortcut (`GamePanel` ->
  `EastPanel.toggleInventory()`), independent of the menu, should
  MenuPanel be kept (trimmed to just the working Inventory entry) or
  deleted outright?
  A: Delete it entirely — the "I" key already opens/closes the inventory
  on its own; the menu was a second, redundant way to do the same one
  thing, plus five entries that don't do anything. `MenuPanel.java` is
  deleted, `EastPanel` no longer composes it, and the popup's dismiss
  path now restores focus straight to the game (`restoreGameFocus`)
  instead of handing off to a menu that no longer exists.
  Affects: Scope, Desired behavior (menu<->popup transition and modal
  focus capture no longer apply — nothing exists to block).
