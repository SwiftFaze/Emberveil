# Intent: Camera behavior

- **Slug(s):** camera-behavior (matches `/specs/features/camera-behavior.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-29

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [x] Approved by human
- [x] Implemented
- [x] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [x] Acceptance tests passing
- [x] Mutation testing passed
- [x] Documentation updated (`docs/`, and the wiki if player-facing)

## Problem

`Camera` (`src/main/java/com/swiftfaze/veil/Camera.java`) centers the
viewport on the player every frame (`GamePanel.paintComponent` calls
`camera.centerOn(player.getX(), player.getY())`), and that world-space
offset is what every renderer (`WorldScene`, `Player`, and any future
`DrawableAsciiEntity`) uses to translate world coordinates into screen
coordinates. This behavior has no acceptance-test coverage today — it's
implicit in `GamePanel`'s wiring, not a Cucumber-verified contract. Issue
#31 (split out of #25, which bundled several unrelated mechanics into one
issue) asks for `.feature` coverage of this mechanic as it exists today.

## Scope

- In scope:
  - `Camera.centerOn(targetX, targetY)` setting the camera's world-space
    top-left offset (`getX()`/`getY()`) so the target position sits at the
    center of the viewport.
  - The viewport-size-driven math: offset = target position minus half the
    viewport's tile dimensions (`GAME_WINDOW_WIDTH` × `GAME_WINDOW_HEIGHT`,
    50×50 tiles per `GameConst`).
  - Current absence of edge-of-map clamping — the camera can produce an
    offset that puts part of the viewport outside the map bounds when the
    player is near an edge, and that's today's actual behavior, not a bug
    to fix here.
- Out of scope:
  - Adding edge-of-map clamping, zoom, panning, or any floor/depth-aware
    camera behavior — `docs/architecture.md` already notes the world is a
    flat single layer with no depth dimension, and issue #31 explicitly
    excludes new camera features.
  - Pixel-level rendering output (Swing painting) — same non-goal pattern
    already used in `world-single-floor-rendering.feature`; only the
    `Camera` domain object's math is in scope, not `Graphics2D` calls.
  - Changing how `WorldScene`/`Player`/other entities consume
    `cameraX`/`cameraY` to compute screen coordinates — that consumption
    is existing, unrelated behavior.

## Actors

- The player, indirectly: their movement (`GamePanel.paintComponent`,
  driven by `bindKeys`-registered movement actions) is what triggers a
  camera re-center each repaint.
- Any `DrawableAsciiEntity` implementer (`WorldScene`, `Player`) that reads
  `camera.getX()`/`getY()` to translate world coordinates into screen
  coordinates depends on this contract staying stable.

## Desired behavior

This is a retroactive spec of existing, unchanged behavior:

- Given a camera with a viewport of W×H tiles, when `centerOn(targetX,
  targetY)` is called, the camera's offset becomes
  `(targetX - W/2, targetY - H/2)` (integer division).
- The camera has no independent state beyond the last `centerOn` call —
  calling it again with a new target fully replaces the previous offset;
  there's no smoothing/interpolation between positions.
- Near a map edge, the computed offset can go negative or otherwise place
  the viewport partially outside the map's tile grid — the camera does
  not clamp to `[0, mapWidth - viewportWidth]` (or the height equivalent)
  today. The spec should capture this as current behavior, not assert
  clamping that doesn't exist.
- `getX()`/`getY()` return exactly the last-computed offset with no
  additional transformation.

## Constraints / non-functional notes

None beyond the global budget in `.claude/workflow.md`.

## Open questions

None — `Camera`'s implementation is small and fully read; behavior is
fully specified above from the existing code, not inferred.
