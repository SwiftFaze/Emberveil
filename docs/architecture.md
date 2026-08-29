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
`TileTestScene2` as the active scene. `Tile` (`world/Tile.java`) is an enum
of glyph/color/walkability triples — walkability and rendering are entirely
data-driven off this enum, there's no separate collision or sprite system.

**Buildings** are authored as JSON blueprints under
`src/main/resources/buildings/` (a flat 2D array of `Tile` enum names —
`{"name", "type", "width", "height", "tiles": [...]}`) and loaded by
`BuildingLoader.load(fileName)` into a `Building` (a `Tile[][]` blueprint
plus a world X/Y offset), then stamped into a scene via
`WorldScene.placeBuilding`. Buildings are read via
`getResourceAsStream("/buildings/<file>")`, i.e. off the classpath — this
works identically whether run from the IDE, `mvn test`, or the packaged
Windows installer (see `docs/release.md`).

**Player movement** (`entities/player/Player.java`): each directional move
checks whether the target tile is walkable and, if so, moves onto it —
there is no floor to step up onto or fall through, so a blocked move simply
does nothing.

**Player RPG data** (`entities/player/`): `PlayerInfo` composes `Level`,
`Stats`, and a `PlayerClass`. `PlayerClass` is a plain data holder (name +
base stat values) populated from JSON under `src/main/resources/classes/`
(e.g. `warrior.json`, `mage.json`) by `PlayerClassLoader.load(fileName)`,
mirroring `BuildingLoader`'s pattern — `getResourceAsStream("/classes/<file>")`,
Gson parse, throws `PlayerClassException` on failure. `loadAll()` returns
every known class (today via an explicit filename list — classpath resource
directories can't be listed portably across the IDE, `mvn test`, and the
shaded jar without an extra scanning dependency; add new class files to
that list). `PlayerInfo` defaults new players to `warrior.json`. Only the
six base-stat *values* are data-driven — `Stats`' fields and its derived
`getAttackPower`/`getDefense` formulas stay plain Java. The JSON schema
abbreviates the six attributes (`str`, `dex`, `con`, `int`, `wis`, `luck`);
since `int` is a Java reserved word, `PlayerClass`'s `intelligence` field
maps to it via Gson's `@SerializedName("int")`. This data isn't wired into
gameplay yet beyond display in `PlayerInfoPanel`.

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

**UI shell** (`ui/`): `NorthPanel`, `SouthPanel`, `MenuPanel`,
`InventoryPanel`, and `PlayerInfoPanel` all extend `TerminalPanel`, a
shared `JPanel` base centralizing the black background, monospaced label
styling, and a `makeLabel` helper — each panel still sets its own
border/layout specifics that genuinely differ (e.g. `InventoryPanel`'s and
`PlayerInfoPanel`'s bottom-line border, `NorthPanel`'s centered title).
`EastPanel` composes `PlayerInfoPanel` + `InventoryPanel` + `MenuPanel` and
implements `GameListener`: `updatePlayer` refreshes `PlayerInfoPanel`, and
the interface's `toggleInventory` default method is overridden to show/hide
`InventoryPanel` — this replaced the old direct `GamePanel` → `EastPanel`
field reference that pressing **I** used to go through. `MenuPanel` wires
its item list to a `SelectableMenu` (current index plus wrap-around
`moveUp`/`moveDown`) via its own focus-scoped (`WHEN_FOCUSED`) Up/Down/Enter
bindings; only the "Inventory" entry does anything on confirm today
(toggling the same way pressing **I** does) — Help/Journal/Map/Character/
Stats stay decorative for now.

**Class/stats sandbox** (`sandbox/`): a dev-only stat inspector, not
referenced from `Main.java` and not the packaged/jpackage build's entry
point (`pom.xml`'s `main.class` stays `com.swiftfaze.veil.Main`). Run it
explicitly: `mvn compile exec:java -Dexec.mainClass=com.swiftfaze.veil.sandbox.ClassSandbox`.
`ClassSandboxModel` wraps `PlayerClassLoader.loadAll()` and exposes class
names plus computed `Stats` per class (via `PlayerClass.applyBaseStats`, no
duplicated formulas); `ClassSandboxPanel` (a `TerminalPanel`) reuses the
`SelectableMenu`/Key Bindings pattern from `MenuPanel` — Up/Down moves the
selection and immediately refreshes the displayed attack power/defense/HP/
mana, no separate confirm step. Editing a class's JSON and re-launching the
sandbox picks up the change with no recompile, since `PlayerClassLoader`
reads the resource fresh on every `ClassSandboxModel` construction — there
is no static caching of loaded classes anywhere in this path.

**`GameConst`** centralizes tunable gameplay constants (window/tile
dimensions, map size, player start position) — check here first before
hardcoding a magic number elsewhere. Keyboard bindings live separately in
`input/Keybindings.java`, since key mapping is a distinct, separately-
growing concern.
