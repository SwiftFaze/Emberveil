# Screens

How `Main.java` assembles and navigates between the game's screens, and
how the individual screen panels compose the reusable widgets from
`docs/ui-widgets.md` into player-facing UI. For the shared list/detail
data contract Inventory and Codex use to display domain content, see
`docs/components.md`.

**UI shell** (`ui/`): removed (`EastPanel`, `NorthPanel`, `SouthPanel`,
`PlayerInfoPanel`, `TerminalPanel`) as early scaffolding pending a proper
reimplementation — `GamePanel` currently has no sidebar and no title bar
(see Clarifications in `specs/intent/shared-list-detail-ui-contract.md`).
`Main.buildGameCard`/`wirePopups` now do just enough plumbing to keep
`InventoryPanel`/`CodexPanel` reachable via the I/X toggles: constructing
them, loading mod content into them, and registering `ui/
PopupToggleListener` (implements `GameListener`) to handle open/dismiss/
mutual-exclusion/focus-restore — the same behavior `EastPanel` used to
provide, just as a small standalone class instead of a full sidebar panel
(it has to live in `ui/`, not nested in `Main`, for
`ModuleDependencyTest`'s engine/UI ArchUnit gate — `Main`'s own
UI-dependency exemption doesn't extend to classes nested inside it). There
is no player-info display feeding `updatePlayer` right now. The descriptions below of `InventoryPanel`/`CodexPanel`
themselves are unaffected either way.

**Screen flow** (`Main.java` and screen panels): `Main.loadGame()` first calls
`loadAndApplyDefaultTheme()` (loads the mod registry and applies `core:default`'s
theme — see `docs/ui-widgets.md`'s "Widget theming") before building any screen, then uses
`CardLayout` to manage four screens: title, game, settings, and keybinds,
navigated via a shared `cards` map + `navigateTo()` helper (needed because
settings and keybinds reference each other - a genuine two-way cycle plain
lambda capture can't express, since Java lambdas can't forward-reference a
local variable declared later in the same method). `TitleScreenPanel` and
`SettingsScreenPanel` are each real `Widget`-style Swing focus targets in
their own right (focusable, with their own `InputMap`/`ActionMap` bound at
`WHEN_FOCUSED`) rather than relying on an inner child widget to hold real
focus - `navigateTo()` calls `requestFocusInWindow()` on whichever screen a
navigation lands on, matching the same bind-and-delegate idiom
`InventoryPanel` already established for composite screens with mixed
navigation.

`TitleScreenPanel` shows the "VEIL" title (with Delta Corps Priest 1 font, or
monospaced fallback if the font resource is absent) and a centered menu
(Continue, New, Load, Settings, Exit) — New navigates to the game view and
starts the game loop. `SettingsScreenPanel` is a centered, bordered, navigable,
back-able list of eleven settings items, every row sharing one width (matching
the widest row, same convention `RadioGroupWidget`'s vertical mode already
uses): Brightness and Volume (both sliders, rendered as an actual bar via
`SliderWidget.getDisplayText()`), Fullscreen (radio toggle: Windowed/
Fullscreen), Font (radio cycle: Monospaced/Serif/SansSerif), Theme (radio
cycle: Default/Midnight/Sunrise — a fixed placeholder list built the same way
as Font's, purely visual and not wired to the real mod-driven theme registry
described in `docs/ui-widgets.md`'s "Widget theming"; see `specs/intent/widget-theming.md`'s
Clarifications), Keybinds (opens
the dedicated keybinds page), placeholder action items (Open Game Folder,
Open Mod Folder - both call `Desktop.open`, creating `mods/` next to the
install if missing; About, Reset to Defaults), and an explicit Go Back item
(added after Step 4.5 playtest found Escape-only back navigation wasn't
discoverable). Left/Right calls `moveLeft()`/`moveRight()` on sliders or
radio groups directly (bypassing their own now-unused internal key bindings,
same as `InventoryPanel`'s sub-widgets), which updates the highlighted
option; Up/Down navigates the menu; Enter triggers actions; Escape or Go Back
returns to the title screen. Its "Reset to Defaults" row opens a
`ResetConfirmationPopup` (a `CompactPopupWidget` asking "Reset
all settings to their defaults?" via a horizontal `RadioGroupWidget<String>`
defaulting to "No", the same safe-default convention `DropConfirmationPopup`
uses; choosing either option just dismisses the popup, since no setting
persists real state yet). `SettingsScreenPanel` doesn't host that popup
inside its own layout, mirroring `InventoryPanel`/`GameWindow`'s approach:
`ui/SettingsWindow.buildContentArea(SettingsScreenPanel)` builds a
`JLayeredPane` with the settings screen at `DEFAULT_LAYER` and the reset
popup at `POPUP_LAYER` above it, stretched to match via `FillLayout`, and
`Main.java` wires that layered pane into the settings card instead of adding
`SettingsScreenPanel` directly.

`SettingsKeybindsPanel` lists every rebindable action (Move up/down/left/
right, Toggle inventory) and its current key in a real `TableWidget<ActionRow>`
(Action/Key columns, bordered grid, header row - the same widget
`InventoryPanel` uses for its field/value table), allows navigation between
actions and a footer (Go back, Reset to Defaults, Cancel, Apply - left to
right), and opens a "press any key" popup on Enter to capture an arbitrary
key as a new binding (a `KeyListener`, not `InputMap`/`ActionMap`, since it
must catch any keystroke while armed rather than a fixed set). Rebinding a
key calls `TableWidget.updateRow()` to refresh just that row's Key cell
without disturbing the selected row - `setRows()` would have reset selection
to the first row on every keypress. The armed action row gets a green accent
border via `TableWidget.setSelectedRowAccentColor()` (the same
`WidgetTheme.VALID_HIGHLIGHT` convention `RadioGroupWidget`'s confirmed-option
border already uses), and every other row dims via
`TableWidget.setOtherRowsDimmed()`, so the armed row reads as the only
currently-active thing, like a modal dimming its backdrop. Reset to Defaults
restores every action's default binding (via `updateRow()` per row, same
selection-preserving reasoning) and stays on this page (state genuinely local
to this page); Go back/Cancel/Apply all return to the settings screen
identically (nothing else persists yet). The popup itself is still internal
boolean state (`popupOpen`), not yet a real rendered overlay component.
Actual key rebinding is visual only - no persistent state, `Keybindings.java`'s
real constants are untouched. F5 still resets the entire game (back to the
title screen).

`InventoryPanel` extends `PopupWidget`: its body is a 50/50 split
(`GridLayout`) between an item `ListWidget<Item>` on the left (scrollable
via a `JScrollPane` styled with `TerminalScrollBarUI`, non-wrapping) and a
details pane on the right (name/type/slot/damage range/effects table,
refreshed live off the list's `onSelectionChange` hook), divided by a 2px
light-gray line matching the rest of the UI's border style. The effects are
rendered as a `TableWidget<Item.Effect>` with two columns (stat and value),
row-highlighted when selected. Navigation can be switched between the item
list and the effects table via Left/Right keys; Up/Down then navigate within
the current pane. Pressing D (Drop) from any pane opens a nested
`DropConfirmationPopup` (a `CompactPopupWidget` containing a
horizontal `RadioGroupWidget<String>` asking "Drop item?", defaulting to "No"
highlighted), which closes on any selection or Escape without actually
removing items. It's
populated externally (`showItems(List<Item>)`, called from
`Main.wirePopups`) rather than loading mod content itself.
`ui/GameWindow.buildContentArea(GamePanel)` builds a `JLayeredPane`
wrapping just `GamePanel` at `DEFAULT_LAYER`; `Main.buildGameCard` then
adds the inventory popup and its nested drop-confirmation popup directly
(`POPUP_LAYER`/`DRAG_LAYER`) rather than `GameWindow` doing it — that
layering logic used to live inside `GameWindow` itself, driven by an
`EastPanel` parameter, before that composition root was removed (see the
UI shell note above). `SelectableMenu` (the old hand-rolled index-wrap
counter `MenuPanel` used to drive) is deleted entirely, superseded by
`ListWidget`.

**Controls hint bar** (`ui/widget/ControlsHintBarWidget`, see
`docs/ui-widgets.md`): `Main.loadGame()` builds one shared instance and
threads it through every screen's constructor, docked at the game frame's
`BorderLayout.SOUTH`. Each screen computes its own current hint list and
calls `hintBar.setHints(...)` whenever a row/sub-focus change would change
what's valid to press — `TitleScreenPanel`'s hints are static (set once);
`SettingsScreenPanel.computeHints()` adds Decrease/Increase for a slider row
or Previous/Next for a radio row, on top of a fixed Select/Back tail;
`SettingsKeybindsPanel.computeHints()` switches between its table, footer,
and press-any-key-capture hint sets based on `footerFocused`/`popupOpen`;
`InventoryPanel`/`CodexPanel` swap a Back-to-list/View-details hint based on
`detailsPane.hasFocus()`. Plain vertical list movement's Up/Down is
deliberately omitted from every one of these — assumed player knowledge,
unlike a row-specific effect (Decrease/Increase, Previous/Next, "Back to
list") or the in-game view's non-arrow Z/S/Q/D movement scheme, which still
gets shown since it isn't self-explanatory the same way. `Main.navigateTo()`
re-pushes the newly-focused screen's hints on every `CardLayout` switch via
the `HintAware` interface, since a screen's own key-bound methods only cover
in-screen focus changes, not screen switches. Only Title/Settings/Keybinds
are wired into the live `Main` composition root today — Inventory/Codex/the
in-game view's hint pushes are exercised directly against the panel classes
in `controls-hint-bar.feature`, pending the composition-root rebuild
mentioned in the UI shell note above (see that feature file's Non-goals for
why).

`CodexPanel` extends `PopupWidget` and mirrors `InventoryPanel`'s list+detail
split structure: a tab switcher across Items, Tiles, and Classes (three
`JLabel`s styled as tabs with selection highlighting) above a 50/50 split body
with a category-specific entry list on the left (scrollable `ListWidget`,
non-wrapping) and a field/value detail table on the right (`TableWidget`
showing ID, Name, Glyph/Symbol/Color, etc. per category). Up/Down/Left/Right
navigate within the current pane and can switch focus between list and detail
via Left/Right. Tab and Shift+Tab cycle forward/backward through tabs. Data is
populated externally from mod content (`showItems`/`showTiles`/`showClasses`)
same as `InventoryPanel`. Opening Codex via the X key while Inventory is
open still closes Inventory first, and vice versa, so only one popup is
ever visible at a time — that mutual exclusion moved from
`EastPanel.toggleCodex()`/`toggleInventory()` into `Main`'s
`PopupToggleListener` (see the UI shell note above) when `EastPanel` was
removed, same behavior either way. Buildings and Quests tabs are deferred
(see `specs/intent/codex-ui.md`). `InventoryPanel` and `CodexPanel`'s
duplicated details-pane/focus-navigation code is being extracted per
`docs/components.md`; see that doc's worked example.
