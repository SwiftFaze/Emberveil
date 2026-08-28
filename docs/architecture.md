# Architecture

Veil is a 2D ASCII-tile desktop RPG built with Java 17 Swing (no game
engine). Rendering draws Unicode/ASCII glyphs with `Graphics2D.drawString`
onto a `JPanel`.

**Entry point / window assembly** (`Main.java`): builds a `JFrame` with
`NorthPanel`/`SouthPanel`/`EastPanel`/`GamePanel` in a `BorderLayout`. There
is no game loop/ticker — the world only repaints in response to key events
(see `GamePanel.keyListen`).

**`GamePanel`** is the core of the simulation: it owns the `Player`, the
active `WorldScene`, and a `Camera`, wires keyboard input directly to player
movement, and drives all rendering from `paintComponent`. The world is a
single flat layer — `paintComponent` centers the camera on the player and
makes one `scene.renderWorld(...)` call; there is no floor/depth dimension,
brightness falloff, or fog overlay.

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
`Stats`, and a `PlayerClass` (strategy pattern — `Warrior`/`Mage` implement
`applyBaseStats`); derived combat stats (`getAttackPower`, `getDefense`)
live on `Stats`. This data isn't wired into gameplay yet beyond display in
`PlayerInfoPanel`.

**Rendering contracts**: `Positionable` (x/y) → `DrawableAsciiEntity` (adds
glyph/color/`render`) is what `GamePanel` iterates over in `entitiesToDraw`
to draw non-scene entities (currently just `Player`); `WorldScene` itself
also implements `DrawableAsciiEntity` but is rendered specially (via
`renderWorld`), not through the generic entity loop.

**UI shell** (`ui/`): `EastPanel` composes `PlayerInfoPanel` +
`InventoryPanel` + `MenuPanel` and implements `GameListener`, which
`GamePanel` notifies after every keypress so the side panel can refresh
(`EastPanel.updatePlayer`). Pressing **I** toggles `InventoryPanel`
visibility via a direct `GamePanel` → `EastPanel` reference (not the
listener interface).

**`GameConst`** centralizes all tunable constants (window/tile dimensions,
map size, player start position) — check here first before hardcoding a
magic number elsewhere.
