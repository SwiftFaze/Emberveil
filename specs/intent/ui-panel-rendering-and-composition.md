# Intent: UI panel rendering and composition

- **Slug(s):** ui-panel-rendering-and-composition (matches
  `/specs/features/ui-panel-rendering-and-composition.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-29
- **Source:** [GitHub issue #33](https://github.com/SwiftFaze/Veil/issues/33)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [x] Approved by human
- [x] Implemented (no production code changed — spec coverage for
  existing behavior only, per Scope)
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [x] Acceptance tests passing (42 Cucumber scenarios in this feature;
  91 tests total, `mvn test` green)
- [x] Mutation testing — N/A: `pom.xml`'s pitest `<targetClasses>` excludes
  TerminalPanel/PlayerInfoPanel/NorthPanel/SouthPanel as layout-only
  panels, and EastPanel (which is a target) had zero code changes, so
  there's nothing new for PIT to mutate.
- [x] Documentation — no update needed: `docs/architecture.md`'s "UI
  shell" section (lines 78-93) already accurately documents the
  composition this feature specs; no wiki update needed (no player-facing
  behavior or game-data change).

## Problem

The UI shell's panel composition and display behavior — `PlayerInfoPanel`'s
stat display, `NorthPanel`/`SouthPanel`/`EastPanel`'s layout/composition,
and `TerminalPanel`'s shared styling contract — has no `.feature` coverage.
It was originally bundled into #25 along with player move, world gen, tile
physics, and camera, and has been split out so each concern gets its own
focused spec (see `specs/features/README.md`).

## Scope

- In scope:
  - `TerminalPanel` (`src/main/java/com/swiftfaze/veil/ui/TerminalPanel.java`)
    as the shared styling contract: black background, white monospaced
    terminal-style labels, top-to-bottom (`BoxLayout.Y_AXIS`) layout, not
    focusable.
  - `PlayerInfoPanel` (`.../ui/PlayerInfoPanel.java`): `updatePlayer(Player)`
    populating the name/class, level/XP, and position labels from a given
    `Player`, in the panel's initial (pre-update) state and after an update.
  - `NorthPanel` (`.../ui/NorthPanel.java`): a fixed-size, bordered panel
    displaying the centered "Veil" title.
  - `SouthPanel` (`.../ui/SouthPanel.java`): a fixed-size, bordered panel
    (currently just a layout placeholder — no content beyond vertical glue).
  - `EastPanel` (`.../ui/EastPanel.java`): composition — that it lays out
    `PlayerInfoPanel` (NORTH), `InventoryPanel` (CENTER), `MenuPanel`
    (SOUTH) in a `BorderLayout`, and that `updatePlayer(Player)` delegates
    to its `PlayerInfoPanel` — plus `EastPanel`'s own panel properties
    (preferred size, black background, light-gray border, not focusable),
    `MenuPanel`'s `CancelAction` (`onCancel` — closes the inventory if
    open and always invokes the `restoreGameFocus` callback), and
    `MenuPanel`'s `ConfirmSelectionAction` when "Inventory" is selected
    (invokes `onInventoryConfirmed`, wired to `EastPanel::toggleInventory`)
    — see Clarifications for why the latter two are in scope even though
    an earlier round removed two similar `toggleInventory()` scenarios.
  - `PlayerInfoPanel`'s own border (2px light-gray bottom line + 10px
    padding on all sides).
  - `NorthPanel`'s title label color override (`#eeb392`, distinct from
    `TerminalPanel`'s default white label color).
- Out of scope:
  - Keyboard navigation/dispatch and the inventory-toggle *listener* path
    (the key binding that invokes `toggleInventory`) — already covered by
    `specs/features/keyboard-input-and-menu-navigation.feature`.
  - Calling `toggleInventory()` directly and asserting its visibility-flip
    and focus request/restore — already covered in detail by the existing
    `EastPanelTest` unit test. (`MenuPanel`'s cancel/confirm *Actions* are
    in scope per above; only the direct-call path stays out — see
    Clarifications.)
  - `MenuPanel` and `InventoryPanel`'s own internal rendering/behavior
    (e.g. `SelectableMenu` highlight color, item list) beyond what's
    needed to exercise `EastPanel`'s cancel/confirm composition above.
  - Any new panel features, layout changes, or visual redesign — this is
    spec coverage for existing behavior only.

## Actors

- The player, indirectly: panel content is what they see reflecting their
  character's state (name, class, level, XP, position) and menu state
  (inventory open/closed).
- `GamePanel`/`EastPanel`'s owner, which calls `updatePlayer(Player)` and
  `toggleInventory()` to drive these panels — the spec exercises these
  panels the way that caller does, not via keyboard events.

## Desired behavior

- **`TerminalPanel` styling contract:** any panel extending `TerminalPanel`
  renders with a black background, is not keyboard-focusable, and any label
  created via `makeLabel(...)` is white, monospaced, left-aligned by
  default (or at the given alignment when specified).
- **`PlayerInfoPanel` stat display:** given a `Player`, calling
  `updatePlayer(player)` updates the panel's labels to show:
  - `"<first> <last> | <class name>"`
  - `"LV <level> | <xp>/<maxXp> XP"` — note `Level.getXp()` returns a
    `double`, so a whole-number XP value renders with a trailing `.0`
    (e.g. `40.0`); `maxXp` is derived (`Level.getMaxXp()` =
    `(currentLevel + 20) * 5`), not independently settable, so example
    data must use the real formula's output, not an arbitrary round number.
  - `"Pos: (<x>, <y>)"`

  Before any `updatePlayer` call, the labels show their initial placeholder
  text (`"Name:"`, `"LV:"`, `"Pos: "`).
- **`NorthPanel` composition:** constructs with a fixed preferred size of
  `(GAME_WINDOW_WIDTH, 4 * TILE_HEIGHT)`, a light-gray bordered panel, and
  a single centered title label reading `"Veil"` colored `#eeb392`
  (overriding `TerminalPanel`'s default white label color).
- **`SouthPanel` composition:** constructs with a fixed preferred size of
  `(GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT * 4)`, and a light-gray bordered
  panel (no additional content today beyond layout glue).
- **`PlayerInfoPanel`'s border:** a `CompoundBorder` of a 2px light-gray
  matte line on the bottom edge only, outside a 10px empty-border padding
  on all sides.
- **`EastPanel` composition:** constructs with `PlayerInfoPanel` in the
  `NORTH` slot, `InventoryPanel` in `CENTER`, `MenuPanel` in `SOUTH` of a
  `BorderLayout`. `InventoryPanel` never calls `setVisible` itself, so it
  starts **visible** by default (Swing's `JComponent` default).
  `updatePlayer(player)` on `EastPanel` delegates to the child
  `PlayerInfoPanel`'s `updatePlayer`. `EastPanel` itself (it does not
  extend `TerminalPanel`) has a fixed preferred size of
  `(500, GAME_WINDOW_HEIGHT * TILE_HEIGHT)`, a black background, a
  light-gray line border, and is not focusable.
- **`MenuPanel`'s cancel/confirm wiring, as exercised through `EastPanel`:**
  - `CancelAction` (bound to `Keybindings.ACTION_MENU_CANCEL` on
    `MenuPanel`'s `ActionMap`) always invokes `EastPanel`'s
    `restoreGameFocus` callback, and additionally hides `InventoryPanel`
    first if it was visible.
  - `ConfirmSelectionAction` (bound to `Keybindings.ACTION_MENU_CONFIRM`)
    invokes `onInventoryConfirmed` (wired to `EastPanel::toggleInventory`)
    only when `MenuPanel`'s currently-selected item is "Inventory"
    (`INVENTORY_INDEX = 0`, the default selection on construction, so no
    menu navigation is needed to exercise this path).

## Constraints / non-functional notes

None beyond the global budget in the workflow CLAUDE.md.

## Open questions

None — `InventoryPanel`'s initial visibility was confirmed by reading the
source (see Desired behavior above).

## Clarifications

- Q: The draft `.feature` file included two `EastPanel` scenarios testing
  `toggleInventory()`'s visibility flip and keyboard-focus request/restore
  as a "composition side effect." Should these stay in scope?
  A: No — remove them. This game has no mouse usage, so that toggle/focus
  behavior is keyboard-dispatch machinery, not a display/composition
  contract this feature should cover. It's already covered by the
  existing `EastPanelTest` unit test.
  Affects: removes "Toggling inventory on a freshly created EastPanel
  closes it and restores game focus" and "Toggling inventory again
  reopens it and moves focus to the menu panel" from
  `ui-panel-rendering-and-composition.feature`.

- Q: A follow-up review comparing the approved `.feature` file against the
  actual source (`EastPanel`, `PlayerInfoPanel`, `NorthPanel`) found five
  untested gaps: (1) `EastPanel`'s own panel properties (size/background/
  border/focusable), (2) `PlayerInfoPanel`'s border, (3) `NorthPanel`'s
  title color override, (4) `MenuPanel`'s `CancelAction` (unit-tested in
  `EastPanelTest` but in no `.feature` file), (5) the confirm→toggle
  wiring end-to-end (`ConfirmSelectionAction` → `onInventoryConfirmed` →
  `EastPanel::toggleInventory`, untested anywhere). Should all five be
  added to this feature?
  A: Yes, add all five — including (4) and (5), even though they're the
  same category of behavior (`MenuPanel` Action → `EastPanel` focus/
  visibility side effects) as the two `toggleInventory()` scenarios
  removed in the entry above. **Correction to that earlier entry's
  reasoning:** "this game has no mouse usage" was never actually the
  operative distinction — the game not using a mouse was true then and
  still true now, so it doesn't explain including (4)/(5) while excluding
  a direct `toggleInventory()` call. The actual, narrower distinction
  going forward: a *direct* `toggleInventory()` call stays out of scope
  (redundant with `EastPanelTest`'s existing detailed coverage of that
  exact call), while exercising the same effects *through* `MenuPanel`'s
  real `CancelAction`/`ConfirmSelectionAction` trigger paths is in scope,
  because it's proving `EastPanel`'s actual composition/wiring (which
  Action calls which callback), not just `toggleInventory()`'s internals.
  Affects: adds six new scenarios/scenario-extensions to
  `ui-panel-rendering-and-composition.feature` — EastPanel's own panel
  properties; PlayerInfoPanel's border; NorthPanel's title color (appended
  to the existing "NorthPanel composes a centered title" scenario); two
  MenuPanel-cancel scenarios (inventory open, and already-hidden); one
  MenuPanel-confirm scenario.
