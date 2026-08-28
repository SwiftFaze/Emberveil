# Architecture

Emberveil is a 2D ASCII-tile desktop RPG built with Java 17 Swing (no game
engine). Rendering draws Unicode/ASCII glyphs with `Graphics2D.drawString`
onto a `JPanel`; there is no sprite/texture pipeline for tiles (a `grass.png`
resource and a `DrawableImageEntity` interface exist but are currently
unused by any scene).

**Entry point / window assembly** (`Main.java`): builds a `JFrame` with
`NorthPanel`/`SouthPanel`/`EastPanel`/`GamePanel` in a `BorderLayout`. There
is no game loop/ticker — the world only repaints in response to key events
(see `GamePanel.keyListen`).

**`GamePanel`** is the core of the simulation: it owns the `Player`, the
active `WorldScene`, and a `Camera`, wires keyboard input directly to player
movement, and drives all rendering from `paintComponent`. Z-level (floor)
transitions are animated by smoothing `preciseZLevel` toward the player's
actual `z` each frame (`roundZLevel`, controlled by
`GameConst.Z_TRANSITION_SPEED`), and floors are drawn from `lowestVisibleZ`
up through the top of the map, each dimmed by depth-based `brightness` and
overlaid with a fog/cloud layer for floors above the player
(`WorldScene.renderClouds`, gated by
`GameConst.LEVEL_ABOVE_FOG_Z_LEVEL_START`).

**World representation** (`world/WorldScene.java`): an abstract base holding
a `Tile[depth][width][height]` grid (note the axis order: index is
`[z][x][y]`, not `[x][y][z]`). Concrete scenes (`TileTestScene`,
`TileTestScene2`, `MountainScene`) subclass it and populate tiles in their
constructor; `GamePanel` currently hardcodes `TileTestScene2` as the active
scene. `Tile` (`world/Tile.java`) is an enum of glyph/color/walkability
triples — walkability and rendering are entirely data-driven off this enum,
there's no separate collision or sprite system.

**Buildings** are authored as JSON blueprints under
`src/main/resources/buildings/` (one 2D array of `Tile` enum names per
floor) and loaded by `BuildingLoader.load(fileName)` into a `Building` (a
`Tile[][][]` blueprint plus a world offset), then stamped into a scene via
`WorldScene.placeBuilding`. Buildings are read via
`getResourceAsStream("/buildings/<file>")`, i.e. off the classpath — this
works identically whether run from the IDE, `mvn test`, or the packaged
Windows installer (see `docs/release.md`).

**Player movement** (`entities/player/Player.java`): each directional move
checks the target tile's walkability on the current floor, then one floor up
(auto step-up), then searches downward for the first walkable floor
(fall-through) — see `Player.move`/`findFloorBelow`. `forceAscend`/
`forceDescend` (Page Up/Down) bypass walkability entirely for debugging.

**Player RPG data** (`entities/player/`): `PlayerInfo` composes `Level`,
`Stats`, and a `PlayerClass` (strategy pattern — `Warrior`/`Mage` implement
`applyBaseStats`); derived combat stats (`getAttackPower`, `getDefense`)
live on `Stats`. This data isn't wired into gameplay yet beyond display in
`PlayerInfoPanel`.

**Rendering contracts**: `Positionable` (x/y/z) → `DrawableAsciiEntity`
(adds glyph/color/`render`) is what `GamePanel` iterates over in
`entitiesToDraw` to draw non-scene entities (currently just `Player`);
`WorldScene` itself also implements `DrawableAsciiEntity` but is rendered
specially (per-Z-level with fog), not through the generic entity loop.

**UI shell** (`ui/`): `EastPanel` composes `PlayerInfoPanel` +
`InventoryPanel` + `MenuPanel` and implements `GameListener`, which
`GamePanel` notifies after every keypress so the side panel can refresh
(`EastPanel.updatePlayer`). Pressing **I** toggles `InventoryPanel`
visibility via a direct `GamePanel` → `EastPanel` reference (not the
listener interface).

**`GameConst`** centralizes all tunable constants (window/tile dimensions,
map size, Z-transition speed, brightness falloff, fog thresholds) — check
here first before hardcoding a magic number elsewhere.

**`NoiseGenerator`** (`tools/`) is a Perlin noise implementation not
currently called from any scene — likely scaffolding for future procedural
terrain generation.
