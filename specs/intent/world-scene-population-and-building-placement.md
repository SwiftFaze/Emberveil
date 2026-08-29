# Intent: World Scene Population And Building Placement

- **Slug(s):** world-scene-population-and-building-placement (matches
  `/specs/features/world-scene-population-and-building-placement.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-29
- **Source:** [GitHub issue #32](https://github.com/SwiftFaze/Veil/issues/32)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [x] Approved by human
- [x] Implemented
- [x] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5";
      waived by human — no production/gameplay code changed, test-only diff
- [x] Acceptance tests passing
- [x] Mutation testing passed
- [x] Documentation updated (`docs/`, and the wiki if player-facing) — no
      update needed; see note below

## Problem

There is no acceptance-spec coverage for how a `WorldScene` gets its tiles
populated: filling a scene with a base tile type, and stamping a loaded
`Building` blueprint into the scene at a world offset via
`WorldScene.placeBuilding`. `TileTestScene2` exercises this pattern
(`fillAll` a base tile, load a building blueprint, set its world X/Y
offset, then `placeBuilding`) but nothing currently specs or tests it as
behavior.

This was originally part of #25, which bundled player move, world gen,
tile physics, camera, and UI into a single issue. #32 splits out just the
scene/building composition slice so it can be specced and implemented
independently.

## Scope

- In scope:
  - Populating a scene's tiles via a base fill (`fillAll`), matching the
    pattern `TileTestScene2` uses.
  - `WorldScene.placeBuilding(Building)` stamping a `Building` blueprint's
    `Tile[][]` into the scene's tiles at the building's world X/Y offset
    (`getWorldX()`/`getWorldY()`), such that blueprint cell `[y][x]` lands
    at scene tile `(worldX + x, worldY + y)`.
  - Placement overwriting whatever tiles were already at that location
    (e.g. a building footprint replacing the base fill underneath it).
- Out of scope:
  - `BuildingLoader` JSON parsing and blueprint width/height correctness —
    already covered by `specs/features/world-single-floor-rendering.feature`
    ("loading a building blueprint from JSON").
  - Basic tile walkability — already covered by the same existing feature
    file.
  - Player movement, world generation, tile physics, camera behavior, and
    UI — split into other issues from #25, not this one.
  - Out-of-bounds building placement. `placeBuilding` currently does no
    bounds-checking against the scene's width/height (unlike `fillRegion`,
    which does), so a footprint extending past scene bounds throws
    `ArrayIndexOutOfBoundsException`. The issue doesn't mention this case
    and it isn't part of the desired behavior below — it's a known gap in
    the current implementation, not something this spec asserts on. A
    future issue can decide whether to add bounds-checking.

## Actors

- Scene authors defining a `WorldScene` subclass (e.g. `TileTestScene2`)
  that composes a base fill with one or more placed buildings.
- The rendering/game-loop pipeline that reads the resulting populated
  `tiles[][]` from the scene once composition is complete.

## Desired behavior

- A newly constructed scene that calls `fillAll(Tile.GRASS)` (or any given
  tile type) has every tile in the scene set to that type.
- Given a `Building` with a loaded blueprint and a world offset set via
  `setWorldX`/`setWorldY`, calling `placeBuilding(building)` on the scene
  copies every blueprint tile into the scene at `(worldX + x, worldY + y)`
  for each blueprint-local `(x, y)`, so the placed footprint matches the
  blueprint's layout exactly at that offset.
- Placing a building overwrites pre-existing scene tiles under its
  footprint (e.g. grass from `fillAll` is replaced by the building's wall
  and floor tiles where the two overlap).
- This is the same shape as `TileTestScene2`'s construction
  (`fillAll(Tile.GRASS)`, load `small_house_01.json`, set world offset
  `(130, 130)`, `placeBuilding(house2)`), which motivated this issue — but
  the spec should NOT depend on `TileTestScene2` itself. It's the author's
  scratch/experimentation scene, not a fixture meant to be pinned down by
  an acceptance spec. The acceptance scenario(s) should build a fresh,
  minimal scene and `Building` fixture of their own (e.g. directly in the
  `.feature` file's `Background`/`Given` steps or a small test-only
  builder), so `TileTestScene2` stays free to keep changing without
  breaking the spec.

## Constraints / non-functional notes

None beyond the global constraints in the workflow `CLAUDE.md`.

## Open questions

None remaining. Both prior questions are resolved above (see Scope's
out-of-bounds bullet, and Desired behavior's fixture bullet).

## Clarifications

- Q: The generated spec includes no failure/error scenario, since the one
  obvious candidate — placing a building whose footprint runs past scene
  bounds — is explicitly out of scope per this intent doc. An
  empty-blueprint scenario was used as the edge case instead. Is that
  acceptable, or should out-of-bounds placement be brought into scope
  after all?
  A: Acceptable.
  Affects: general (confirms no scenario added to
  world-scene-population-and-building-placement.feature)

## Implementation notes

- No production code changed. `WorldScene.fillAll`/`placeBuilding` and
  `Building` already behaved exactly as specced — this issue only added
  test coverage (unit + acceptance) for existing behavior, confirming the
  intent doc's Problem statement.
- Unit tests: `src/test/java/com/swiftfaze/veil/world/WorldSceneTest.java`
  (4 new tests: fillAll, placeBuilding offset mapping with an asymmetric
  blueprint, placeBuilding overwrite, placeBuilding with an empty
  blueprint).
- Acceptance test wiring: new
  `src/test/java/com/swiftfaze/veil/steps/WorldScenePopulationAndBuildingPlacementSteps.java`.
  Two of its step definitions intentionally use Cucumber's regex-style
  matching (`^...$`) restricted to the specific tile names this feature
  asserts on (GRASS/WALL/WOOD/STONE/DOOR), instead of a generic `{word}`
  parameter — a generic `{word}` step would have been ambiguous with
  `WorldSingleFloorRenderingSteps`'s existing `tile (x, y) is walkable`
  step (both would match a step ending in a single word).
- `mvn test`: 78/78 passing, 0 skipped (29 Cucumber scenarios, up from 25).
- Mutation testing: 100% of mutants in `fillAll` and `placeBuilding` were
  killed (verified in `target/pit-reports/com.swiftfaze.veil.world/`);
  `Building` package is 100% mutation coverage. Surviving mutants
  elsewhere in `WorldScene` (`fillRegion`, `createBorder`, `isWalkable`,
  `getTile`, `render`, `renderWorld`) predate this issue and are out of
  its scope.
- Documentation: no `docs/` or wiki update needed — no public API,
  domain concept, base stats, or combat formula changed; this issue only
  added test coverage for pre-existing behavior.
- Manually playtested (human): left unchecked — nothing gameplay-visible
  changed (no production code touched), so there's nothing new to
  playtest in the usual sense, but per CLAUDE.md's Step 4.5 this is a
  human decision gate, not something the agent should self-certify.
