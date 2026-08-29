# Intent: Data-driven Tile (JSON definitions + registry)

- **Slug(s):** data-driven-tile (matches `/specs/features/data-driven-tile.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-29

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

`Tile` (`src/main/java/com/swiftfaze/veil/world/Tile.java`) is currently a
hardcoded Java enum of glyph/color/walkability triples (`GRASS`, `DIRT`,
`WATER`, `STONE`, `DOOR`, `WOOD`, ... 19 constants total). To be moddable
the same way buildings already are (see `specs/intent/mod-loader.md`),
tile definitions need to move to data instead of requiring a Java code
change and recompile to add or tune a tile type.

## Scope

- In scope:
  - Replace the `Tile` enum with JSON definitions
    (`mods/core/tiles/*.json`) loaded through `ModLoader`
    (`src/main/java/com/swiftfaze/veil/mods/ModLoader.java`) into an
    internal tile registry, keyed by namespaced ID (`core:grass`,
    `core:water`, etc.) — the same `ModLoader`/`mods/` mechanism phase 1
    (#48, merged) built for buildings.
  - Each tile JSON definition specifies glyph, color, and walkability,
    mirroring today's enum fields (`symbol` (char), `color` (RGB), and
    `walkable` (boolean)).
  - Definitions validated at load time: fail fast with a clear
    `ModLoadException` on invalid/malformed tile data (missing field, bad
    color format, duplicate ID without an `"overrides"` field — reusing
    the collision rule `ModLoader` already enforces for buildings) rather
    than silently no-opping.
  - `TileTestScene2` and the existing building blueprints
    (`mods/core/buildings/small_house_01.json`, whose `"tiles"` arrays
    currently reference `Tile` enum names like `"STONE"`, `"WOOD"`,
    `"DOOR"`) continue to work, now referencing `core:`-namespaced tile
    IDs (`"core:stone"`, `"core:wood"`, `"core:door"`) instead of Java
    enum constant names.
  - Modding documentation is initialized in this phase, per #26's
    "Decisions" section: once this schema ships, add the first
    schema-reference page to the GitHub wiki (see `docs/wiki.md`), not
    `docs/`.
- Out of scope:
  - `PlayerClass`, items, quests, maps — separate follow-on issues (#50,
    #51, #52, #53).
  - Any embedded scripting engine — fixed JSON vocabulary only, per #26.
  - A general stat/vocabulary registry (`core:stats.json`) — not
    introduced by this phase; tiles have no numeric `calc` fields.

## Actors

- Rendering code (`GamePanel`'s render loop) that currently reads
  `Tile.getSymbol()` / `Tile.getColor()` directly off the enum.
- Movement/collision code (`Player`'s directional-move methods,
  `WorldScene`) that currently checks `Tile.isWalkable()`.
- `WorldScene.fillAll(...)` / `fillRegion(...)` and `TileTestScene2`,
  which currently construct scenes by referencing `Tile` enum constants
  directly (e.g. `fillAll(Tile.GRASS)`).
- Building JSON content (`mods/core/buildings/*.json`), whose `"tiles"`
  arrays currently hold raw `Tile` enum names, parsed via
  `Tile.valueOf(...)` in `ModLoader.loadBuilding(...)`.
- Existing tests that reference `Tile` enum constants directly:
  `WorldSceneTest`, `PlayerTest`,
  `WorldScenePopulationAndBuildingPlacementSteps`,
  `WorldSingleFloorRenderingSteps`, `ModLoaderIT`, `ModLoaderSteps` (12
  files total reference `Tile.` today — see grep across `src/`).
- Mod authors defining new tile types.

## Desired behavior

- On startup, `ModLoader` (or a sibling loader following the same
  pattern) reads `mods/*/tiles/*.json` alongside the existing
  `buildings/*.json` handling, building a tile registry keyed by
  namespaced ID.
- `core`'s own tile set (today's 19 enum constants) ships as
  `mods/core/tiles/*.json`, one file per tile (or one file listing all of
  them — resolve shape during spec drafting), loaded through the exact
  same path as any third-party mod's tiles would be.
- Building JSON's `"tiles"` arrays reference tile IDs (`"core:stone"`)
  instead of raw enum names (`"STONE"`); `ModLoader`'s building-parsing
  code resolves those IDs against the tile registry instead of calling
  `Tile.valueOf(...)`.
- Rendering and walkability checks go through the registry-resolved tile
  object's `getSymbol()`/`getColor()`/`isWalkable()`-equivalent accessors
  — call sites shouldn't need to know whether a tile came from `core` or
  a third-party mod.
- A malformed or duplicate (unflagged) tile definition fails fast with a
  clear error at load time, same philosophy as the building-collision
  rule from phase 1.
- Once this ships, the GitHub wiki gets a new page documenting the tile
  JSON schema for mod authors (first entry in what `docs/wiki.md`
  anticipates as ongoing modding reference documentation).

## Constraints / non-functional notes

- No embedded scripting — fixed JSON vocabulary only, per #26.
- This is expected to be the most invasive phase so far: unlike buildings
  (additive — nothing previously depended on `Building`/`BuildingLoader`
  outside a handful of files), `Tile` is referenced by both production
  code (rendering, movement, world population) and a wide swath of
  existing tests as compile-time enum constants (`Tile.GRASS`,
  `Tile.WATER`, ...). See Clarifications below for the resolved
  migration approach (a `CoreTiles` String-ID constants class plus
  registry lookups).
- Follows the repo's existing intent → spec → approval → implementation
  pipeline (see root `CLAUDE.md`).

## Open questions

None outstanding — see Clarifications below.

## Clarifications

- Q: Does walkability need more than a boolean in this phase (e.g.
     movement cost)?
  A: Boolean only, for now — matches today's enum exactly. Nothing in the
     current codebase uses a movement-cost concept yet; a cost field can
     be added later as a non-breaking schema addition when something
     actually needs it.
  Affects: general (tile JSON schema stays `{"id", "symbol", "color",
  "walkable"}`)

- Q: Tile is referenced as a compile-time Java enum in ~12 files
     (`WorldScene.fillAll(Tile.GRASS)`, `TileTestScene2`, several tests).
     How should those call sites reference tiles once `Tile` is no
     longer an enum?
  A: A small `CoreTiles` class exposes `core:`-namespaced IDs as String
     constants (e.g. `CoreTiles.GRASS = "core:grass"`); call sites do
     `registry.getTile(CoreTiles.GRASS)`. `WorldScene`'s internal tile
     storage moves from `Tile[][]` (enum-typed) to holding
     registry-resolved tile objects (or their IDs) rather than an enum
     type. Existing production call sites get a mechanical
     find-and-replace from `Tile.GRASS` to `registry.getTile(CoreTiles.GRASS)`
     (or an equivalent resolved reference threaded through). Crucially,
     the **Gherkin text** of `specs/features/world-single-floor-rendering.feature`
     and `specs/features/world-scene-population-and-building-placement.feature`
     never references `Tile` enum names directly (their steps say "is
     walkable" / "tile (3,4) is WALL", resolved to enum constants only
     inside the Java step-definition code) — so those two `.feature`
     files stay unmodified; only their step-definition `.java` files
     change internally to use `CoreTiles`/registry lookups instead of
     direct enum references.
  Affects: general (implementation approach); confirms
  `specs/features/world-single-floor-rendering.feature` and
  `specs/features/world-scene-population-and-building-placement.feature`
  need no Gherkin changes, only step-definition changes.

## Source

Design doc: [GitHub issue #26](https://github.com/SwiftFaze/Veil/issues/26) (overall mod-structure proposal).
This intent covers phase 2 specifically: [GitHub issue #49](https://github.com/SwiftFaze/Veil/issues/49).
