# Architecture

Veil is a 2D ASCII-tile desktop RPG built with Java 17 Swing (no game
engine). Rendering draws Unicode/ASCII glyphs with `Graphics2D.drawString`
onto a `JPanel`.

**Entry point / window assembly** (`Main.java`): builds a `JFrame` with
`NorthPanel`/`SouthPanel`/`EastPanel`/`GamePanel` in a `BorderLayout`. There
is no game loop/ticker — the world only repaints in response to key events
(see `GamePanel.bindKeys`).

**`GamePanel`** is the core of the simulation: it owns the `Player`, the
active `WorldScene`, and a `Camera`, wires keyboard input directly to player
movement, and drives all rendering from `paintComponent`. The world is a
single flat layer — `paintComponent` centers the camera on the player and
makes one `scene.renderWorld(...)` call; there is no floor/depth dimension,
brightness falloff, or fog overlay. `Camera` (`Camera.java`) is a plain
offset holder — `centerOn(x, y)` sets its top-left offset to the target
position minus half the viewport, with no smoothing between calls and no
clamping to the map's bounds, so the viewport can extend past the map edge
when the player is near one.

**World representation** (`world/WorldScene.java`): an abstract base holding
a `Tile[width][height]` grid. The concrete scene (`TileTestScene2`)
subclasses it and populates tiles in its constructor; `GamePanel` hardcodes
`TileTestScene2` as the active scene. `Tile` (`world/Tile.java`) is a plain
glyph/color/walkability data class — no longer an enum — with instances
loaded from JSON (`mods/core/tiles/*.json`) via `ModLoader` into a
`ModRegistry`, keyed by namespaced ID (`core:grass`, etc.). `CoreTiles`
(`world/CoreTiles.java`) exposes those IDs as String constants for
production call sites (`registry.getTile(CoreTiles.GRASS)`) so they keep
some compile-time safety without `Tile` itself being an enum. Walkability
and rendering are entirely data-driven off these tile definitions, there's
no separate collision or sprite system. This is phase 2 of the
data-driven-mod-content initiative; see `specs/intent/data-driven-tile.md`.

**Buildings** are authored as JSON blueprints under `mods/core/buildings/`
(a flat 2D array of namespaced tile IDs — `{"id", "name", "type", "width",
"height", "tiles": [...]}`) and loaded by `ModLoader.load(modsRoot)` into a
`ModRegistry`, from which a `Building` (a `Tile[][]` blueprint plus a world
X/Y offset) is looked up by its namespaced `"id"` (e.g. `core:small_house_01`)
and stamped into a scene via `WorldScene.placeBuilding`. `ModLoader` reads
content from an external `mods/` directory (resolved relative to the JVM's
working directory) rather than the classpath — `core` is itself just a mod
living at `mods/core/`, loaded through the same path any third-party mod
would use. `ModLoader` makes two full passes over mods in dependency order:
first to load all tiles from `tiles/*.json` into a registry, then to load
all buildings from `buildings/*.json` with tile references resolved against
the tile registry. This is phase 2 of a larger data-driven-mod-content
initiative; see `specs/intent/mod-loader.md` and `specs/intent/data-driven-tile.md`.

**Player movement** (`entities/player/Player.java`): each directional move
checks whether the target tile is walkable and, if so, moves onto it —
there is no floor to step up onto or fall through, so a blocked move simply
does nothing.

**Player RPG data** (`entities/player/`): `PlayerInfo` composes `Level`,
`Stats`, and a `PlayerClass`. `PlayerClass` is a plain data holder (name +
base stat values + optional per-level growth curves) loaded from JSON via
the mod system (`mods/core/classes/warrior.json`, `mage.json`, etc.), using
the same `ModLoader` and `ModRegistry` as tiles and buildings — see "World
representation" above. `CoreClasses` exposes class IDs as String constants
(`core:warrior`, `core:mage`) for production call sites. Class growth is
defined via `calc` expressions (plain arithmetic: `+ - * /`, parentheses,
the `level` variable, and numeric literals — no embedded scripting), validated
at load time against a stat registry (`mods/core/stats.json`), and exposed
via `PlayerClass.applyStatsAtLevel(stats, level)` to compute stat values at
any level (currently level 0 only in gameplay, since no level-up trigger
exists yet). `PlayerInfo` defaults new players to `core:warrior`. Only the
eight base-stat *values* and growth curves are data-driven — `Stats`' fields
and its derived `getAttackPower`/`getDefense` formulas stay plain Java. The
eight registered stats are `strength`, `dexterity`, `constitution`,
`intelligence`, `wisdom`, `luck`, `maxHp`, `maxMana`. This data isn't wired
into gameplay yet beyond display in `PlayerInfoPanel`.

**Items** (`entities/items/Item.java`): a plain data holder (name, glyph,
type, slot, base damage min/max, and an `effects` list of `{type, stat,
calc}` entries) loaded from JSON under `mods/core/items/*.json` via the
same `ModLoader`/`ModRegistry` mechanism as tiles/buildings/classes. Only
`stat_bonus` is a supported effect type so far. `effects[].stat` is
validated at load time against the same stat registry
(`mods/core/stats.json`) classes use, and `effects[].calc` is parsed with
`CalcExpressionParser` for syntactic validity — but, unlike `PlayerClass`,
nothing evaluates an item's `calc` to a number yet, since no equip/
inventory-management system exists to consume it. `EastPanel` loads the
`ModRegistry` itself (same self-contained pattern as `PlayerInfo`/
`TileTestScene2`) and pushes `ModRegistry.getAllItems()`'s result into
`InventoryPanel` via `showItems(List<Item>)`, replacing its previous
hardcoded stub labels. This is phase 4 of the data-driven-mod-content
initiative; see `specs/intent/data-driven-item.md`.

**Quests** (`entities/quests/Quest.java`): a plain data holder (name, an
`objective` — `{type, target, count}`, fixed to `"kill"` this slice — and
a `rewards` list of `{type, id?, count?, calc?}` entries, `item` or `xp`)
loaded from JSON under `mods/core/quests/*.json` via the same
`ModLoader`/`ModRegistry` mechanism as tiles/buildings/classes/items.
`ModLoader` loads quests after items within the same mod-load pass, since
`rewards[].type: "item"` entries validate their `id` against the item
registry already populated earlier in that pass; an unresolved item ID
fails loading immediately, matching the tile/class/item unregistered-
reference pattern. `rewards[].type: "xp"` entries parse their `calc` with
`CalcExpressionParser` for syntactic validity only, same as items —
nothing evaluates it to a number yet, and no combat/monster system exists
to detect a `kill` objective being satisfied either. Minimal per-player
quest state (`entities/player/QuestLog.java`, an unvalidated
`Map<String, QuestLog.State>` of not-started/offered/active/complete,
defaulting unseen quest IDs to not-started) is composed onto `PlayerInfo`
alongside `stats`/`playerClass`, with no order validation on transitions
and no persistence across restarts — no save/load system exists in the
project yet. This is phase 5 of the data-driven-mod-content initiative;
see `specs/intent/data-driven-quest.md`.

**Rendering contracts**: `Positionable` (x/y) → `DrawableAsciiEntity` (adds
glyph/color/`render`) is what `GamePanel` iterates over in `entitiesToDraw`
to draw non-scene entities (currently just `Player`); `WorldScene` itself
also implements `DrawableAsciiEntity` but is rendered specially (via
`renderWorld`), not through the generic entity loop.

**Keyboard input** (`input/Keybindings.java`, `GamePanel.bindKeys`): all
keyboard input goes through Swing Key Bindings (`InputMap`/`ActionMap`,
`WHEN_IN_FOCUSED_WINDOW`) — the same mechanism `Main.java` already used for
F5/reset. `Keybindings` centralizes the `KeyStroke` and action-name
constants; `GamePanel` registers one `Action` per named binding (movement,
inventory toggle) instead of a raw `KeyListener` switch. Each `Action`
notifies `GameListener`s and repaints itself, so there's no catch-all
"notify after every keypress" path — an unbound key simply never invokes
an `Action`.

**UI shell** (`ui/`): `NorthPanel`, `SouthPanel`, and `PlayerInfoPanel`
extend `TerminalPanel`, a shared `JPanel` base centralizing the black
background, monospaced label styling, and a `makeLabel` helper — each
panel still sets its own border/layout specifics that genuinely differ
(e.g. `PlayerInfoPanel`'s bottom-line border, `NorthPanel`'s centered
title). `EastPanel` composes just `PlayerInfoPanel` (in `BorderLayout.NORTH`)
and implements `GameListener`: `updatePlayer` refreshes `PlayerInfoPanel`,
and the interface's `toggleInventory` default method is overridden to
open/dismiss the inventory popup — this replaced the old direct `GamePanel`
→ `EastPanel` field reference that pressing **I** used to go through, and
there is no menu widget in between: **I** calls `toggleInventory()`
directly.

**Widget theming** (`mods/<modid>/themes/*.json`): a directory of files, one
theme per file, matching the `tiles/`/`items/`/`quests/` directory-of-many-files
convention (not the `stats.json` singleton) — each file defining all 11 colors
`WidgetTheme` (see below) exposes as static fields: `SELECTED_HIGHLIGHT`,
`SELECTED_TEXT`, `NORMAL_TEXT`, `DIMMED_TEXT`, `BACKGROUND`, `INVALID_HIGHLIGHT`,
`VALID_HIGHLIGHT`, `TABLE_HEADER_BACKGROUND`, `BORDER`, `SCROLLBAR_THUMB`,
`ACCENT` (`WidgetColorTheme.REQUIRED_KEYS`), each an `{r, g, b}` object using
the same color shape tiles already use (`ModLoader.readColor`). `BORDER` was
named `TABLE_BORDER` until the UI color-cleanup sweep below broadened its use
well beyond tables (every panel border in `ui/`) — the old name was misleading
once `CompactPopupWidget`/`RadioGroupWidget` and then general panel chrome all
started reusing it, so it was renamed to the general-purpose `BORDER`. `ACCENT`
(`#eeb392`) was added in that same sweep to capture a hardcoded leftover: it's
the widget library's original `SELECTED_HIGHLIGHT` color from before an earlier
commit switched selection highlighting to neutral gray, and `NorthPanel`'s
title and `ClassSandboxPanel`'s selected-row color still used that literal
directly. Loaded by `ModLoader.loadThemes`/`loadTheme` — shaped like
`loadTiles`/`loadTile`'s directory scan, still routed through
`registerWithCollisionCheck` for id/`overrides` parity with every other content
type — into a `WidgetColorTheme` (id + `Map<String, Color>`,
`WidgetColorTheme.color(key)`) held in `ModRegistry` (`getTheme`/`getAllThemes`),
keyed by namespaced ID. The core mod's default theme lives at
`mods/core/themes/default.json` (id `core:default`); a file's name doesn't need
to match its id, same as tiles/items/quests. A theme missing a required color
key, or with a malformed `{r,g,b}` value, throws `ModLoadException` the same as
any other content type. `Main.loadGame()` loads the mod registry once at
startup and calls `WidgetTheme.applyTheme(...)` with whichever theme owns ID
`core:default`, before any screen/widget is constructed (they read
`WidgetTheme`'s statics at construction time). No settings/config persistence
system exists yet to pick a non-default theme — the Settings screen's "Theme"
row (see "Screen flow" below) is a purely visual placeholder, not wired to this
registry. Theming coverage isn't limited to the original widget-library files
either: every hardcoded `Color` literal across `ui/`, `ui/widget/`, and the dev
`sandbox/ClassSandboxPanel` was swept to reference a `WidgetTheme` field instead
(gameplay/world rendering — `Player`, `WorldScene`, `GamePanel` — stays
hardcoded, since those colors are game content, not UI chrome). This is the
widget-theming initiative; see `specs/intent/widget-theming.md`.

A small reusable widget framework lives in `ui/widget/`: `Widget` (base
`JPanel` — themed background via `WidgetTheme.BACKGROUND`, focusable),
`FocusManager` (a modal-open flag a popup's content can consult), `WidgetTheme`
(11 mutable `static Color` fields — `SELECTED_HIGHLIGHT`/`SELECTED_TEXT`/
`NORMAL_TEXT`/`DIMMED_TEXT`/`BACKGROUND`/`INVALID_HIGHLIGHT`/`VALID_HIGHLIGHT`/
`TABLE_HEADER_BACKGROUND`/`BORDER`/`SCROLLBAR_THUMB`/`ACCENT` — hardcoded as
field initializers so any widget built without `ModLoader` ever running still
gets sane defaults, but overwritten from a loaded `WidgetColorTheme` via
`applyTheme` at startup; see "Widget theming" above), `ListWidget<T>` (a
keyboard-navigable,
optionally non-wrapping list over a pluggable data source, with
`onConfirm`/`onSelectionChange` callbacks and auto-scroll-into-view of the
selected row), `ButtonWidget` (an Enter-confirmable label), `TableWidget<T>`
(a keyboard-navigable row/column table with row-level confirm; `updateRow()`
replaces one row's data and re-renders just its cells without resetting
selection, unlike `setRows()`; `setSelectedRowAccentColor()` and
`setOtherRowsDimmed()` let a consumer flag the selected row as additionally
"armed" for some other in-progress action, with every other row dimmed to
match — the accent outline paints inside each cell's existing padding rather
than adding new border thickness, so a cell's insets never change between
accented and un-accented (an earlier version reserved extra space instead,
which stopped the whole table resizing on selection but shifted the grid
lines inward and opened a visible gap between rows); used by
`SettingsKeybindsPanel` below), `RadioGroupWidget<T>`
(a single-select radio group, vertical by default or horizontal on demand),
`PatternFieldWidget` (a text-input field validating its content against a
caller-supplied regex pattern), `PopupWidget` (a dismissible overlay,
keyboard-only like the rest of this game — no Close button, since it never
responded to anything but a click; `open()`/`dismiss()` manage visibility and
focus, Escape dismisses it, and `onUp()`/`onDown()`/`onLeft()`/`onRight()`
hooks — bound at `WHEN_ANCESTOR_OF_FOCUSED_COMPONENT`, so they fire whether
the popup itself or a descendant has real Swing focus — let a subclass wire
keyboard navigation to
its own content; `isFullScreen()` returns true by default, but subclasses can
return false to be centered at their preferred size instead of stretched);
`SliderWidget` (a bounded numeric slider with left/right adjustment within a
[min, max] range by fixed steps, with hard bounds—no wrap-around), `FillLayout`
(a `LayoutManager` stretching every child to the parent's full bounds by
default, for `JLayeredPane` overlays; now respects `PopupWidget.isFullScreen()`
to center non-full-screen popups at their preferred size instead), and
`TerminalScrollBarUI` (a flat black-track/solid-thumb `BasicScrollBarUI`
replacing the platform look-and-feel's default scrollbar chrome).

**Screen flow** (`Main.java` and screen panels): `Main.loadGame()` first calls
`loadAndApplyDefaultTheme()` (loads the mod registry and applies `core:default`'s
theme — see "Widget theming" above) before building any screen, then uses
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
described in "Widget theming" above; see `specs/intent/widget-theming.md`'s
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
`ResetConfirmationPopup` (a `CompactPopupWidget` — see above — asking "Reset
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
`DropConfirmationPopup` (a `CompactPopupWidget` — see above — containing a
horizontal `RadioGroupWidget<String>` asking "Drop item?", defaulting to "No"
highlighted), which closes on any selection or Escape without actually
removing items. It's
populated externally (`showItems(List<Item>)`, called from `EastPanel`'s
constructor) rather than loading mod content itself, mirroring
`PlayerInfoPanel`'s `updatePlayer`-style external push. Rather than living
inside `EastPanel`'s own layout, the popup is promoted to window level:
`ui/GameWindow.buildContentArea(GamePanel, EastPanel)` builds a `JLayeredPane`
with a `mainArea` panel (`GamePanel` + `EastPanel`) at `DEFAULT_LAYER`, the
inventory popup at `POPUP_LAYER` above it, and the drop-confirmation popup at
`DRAG_LAYER` (even higher) — all positioned by the same `FillLayout`, which
stretches the full-screen inventory popup to cover the whole game view and
sidebar, but centers the compact drop-confirmation popup at its preferred
size on top of that. `Main.java` wires that layered
pane into the frame's `BorderLayout.CENTER` instead of adding `GamePanel`/
`EastPanel` directly. `SelectableMenu` (the old hand-rolled index-wrap
counter `MenuPanel` used to drive) is deleted entirely, superseded by
`ListWidget`.

`CodexPanel` extends `PopupWidget` and mirrors `InventoryPanel`'s list+detail
split structure: a tab switcher across Items, Tiles, and Classes (three
`JLabel`s styled as tabs with selection highlighting) above a 50/50 split body
with a category-specific entry list on the left (scrollable `ListWidget`,
non-wrapping) and a field/value detail table on the right (`TableWidget`
showing ID, Name, Glyph/Symbol/Color, etc. per category). Up/Down/Left/Right
navigate within the current pane and can switch focus between list and detail
via Left/Right. Tab and Shift+Tab cycle forward/backward through tabs. Data is
populated externally from mod content (`showItems`/`showTiles`/`showClasses`)
same as `InventoryPanel`. Opening Codex via the X key while Inventory is open
closes Inventory first, and vice versa, so only one popup is ever visible at
a time — mutual exclusion is handled in `EastPanel.toggleCodex()`/
`toggleInventory()`. The codex is placed at `POPUP_LAYER` in `GameWindow`'s
layered pane, same as the inventory. Buildings and Quests tabs are deferred
(see `specs/intent/codex-ui.md`).

**Class/stats sandbox** (`sandbox/`): a dev-only stat inspector, not
referenced from `Main.java` and not the packaged/jpackage build's entry
point (`pom.xml`'s `main.class` stays `com.swiftfaze.veil.Main`). Run it
explicitly: `mvn compile exec:java -Dexec.mainClass=com.swiftfaze.veil.sandbox.ClassSandbox`.
`ClassSandboxModel` wraps `PlayerClassLoader.loadAll()` and exposes class
names plus computed `Stats` per class (via `PlayerClass.applyBaseStats`, no
duplicated formulas); `ClassSandboxPanel` (a `TerminalPanel`) reuses
`ui/widget/ListWidget` (wrap-around left on, the framework default) via
its own Key Bindings wiring — Up/Down moves the selection and immediately
refreshes the displayed attack power/defense/HP/mana, no separate confirm
step. Editing a class's JSON and re-launching the
sandbox picks up the change with no recompile, since `PlayerClassLoader`
reads the resource fresh on every `ClassSandboxModel` construction — there
is no static caching of loaded classes anywhere in this path.

**`GameConst`** centralizes tunable gameplay constants (window/tile
dimensions, map size, player start position) — check here first before
hardcoding a magic number elsewhere. Keyboard bindings live separately in
`input/Keybindings.java`, since key mapping is a distinct, separately-
growing concern.
