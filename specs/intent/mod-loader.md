# Intent: Mod Loader (external mods/ directory)

- **Slug(s):** mod-loader (matches `/specs/features/mod-loader.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-29

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [x] Approved by human
- [x] Implemented
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [x] Acceptance tests passing
- [x] Mutation testing passed
- [x] Documentation updated (`docs/`, and the wiki if player-facing)

## Problem

The only precedent for data-driven content today is buildings: JSON
blueprints under `src/main/resources/buildings/`, loaded via
`BuildingLoader` (Gson) off the **classpath**. Classpath resources mean
adding or changing content requires a rebuild, so nothing is actually
moddable by a player or third party yet. Everything else (`Tile`,
`PlayerClass`, items, quests, maps) is either hardcoded Java or doesn't
exist.

## Scope

- In scope:
  - Generalize `BuildingLoader` into a `ModLoader`/`ContentRegistry` that
    reads content from an external `mods/` directory next to the packaged
    jar/installer, not `getResourceAsStream`.
  - `mods/` layout: `mods/<mod-id>/mod.json` + subfolders per content
    type. Only `buildings/*.json` needs to actually exist as content in
    this phase — the directory layout should be generic enough that later
    phases (tiles, classes, items, quests, maps) can add their own
    subfolders without changing the loader's shape.
  - Re-host the existing building JSON as `core` mod content under
    `mods/core/buildings/`, using the exact same loader path as
    third-party mods — no special-cased hardcoded classpath path for
    "official" content.
  - Namespaced IDs: every content ID becomes `<mod-id>:<name>` (e.g.
    `core:some_building`), preventing collisions between mods.
  - `mod.json` per mod: id, display name, and a declared load order /
    dependency list.
  - Loader reads `core` first, then other mods in declared load order.
  - Override rule: a mod may only override another mod's ID if it
    explicitly flags an override in its own content definition — silent
    clobbering is a load-time error, not a silent replace.
  - jpackage installer build (`docs/release.md`) bundles a `mods/` folder
    (containing `core`) alongside the executable, not just the fat jar.
- Out of scope:
  - Data-driving `Tile`, `PlayerClass`, items, quests, or maps themselves
    — each is a separate follow-on issue/intent (#49, #50, #51, #52, #53).
  - Any embedded scripting engine (GraalVM JS/Lua) — not part of this or
    any near-term phase; structured JSON + `calc` strings only, and this
    phase (buildings) has no `calc` fields to begin with.
  - A stat/vocabulary registry (`core:stats.json`) — introduced in the
    Tile/PlayerClass phases, not needed for buildings.
  - In-game UI for browsing/enabling/disabling mods — out of scope for
    this phase; `mods/` contents are loaded unconditionally at startup.

## Actors

- Game code at startup (invokes the `ModLoader` to build the building
  registry used to stamp buildings into a scene as a `Tile[][]`).
- Third-party mod authors (drop a mod folder into `mods/` alongside
  `core`, containing their own `buildings/*.json`).
- The release build pipeline (jpackage installers must ship a `mods/`
  folder containing `core`).

## Desired behavior

- On startup, the game scans the `mods/` directory next to the running
  jar/installer for subdirectories, each expected to contain a
  `mod.json`.
- `core`'s own building content lives at `mods/core/buildings/*.json` and
  loads through the identical code path as any third-party mod — there is
  no special-cased "built-in" loading branch.
- Each mod's `mod.json` declares an `id` and (optionally) a list of mod
  IDs it depends on / should load after. The loader loads `core` first,
  then remaining mods in an order consistent with declared dependencies.
- Every building loaded gets a namespaced ID: `<mod-id>:<building-name>`,
  derived from the mod's `mod.json` id plus the building's own declared
  name/filename.
- If a mod's content declares an ID that collides with an already-loaded
  ID from another mod, the loader fails fast with a clear error message
  naming both the colliding ID and the two mods involved — unless the
  later-loaded content explicitly flags itself as an intentional override
  (exact flag mechanism, e.g. an `"overrides"` field, TBD in spec
  drafting), in which case it replaces the earlier definition and the
  loader logs that an override occurred.
- If `mods/` doesn't exist at all (e.g. a dev checkout with no mods
  folder set up), the loader treats this as "no mods to load beyond
  whatever ships in `mods/core/`" — it should not crash, but `core`
  itself must still be present and load correctly since it ships with the
  game.
- Existing game behavior (buildings stamped into `TileTestScene2`) is
  unchanged from a player's perspective — this phase is a re-plumbing of
  where building data comes from, not a change to what buildings exist or
  how they render.
- `mvn package`'s jpackage installer output includes a `mods/` directory
  (containing `core`) alongside the runnable executable, so a fresh
  install has working buildings out of the box.

## Constraints / non-functional notes

- No embedded scripting — this phase's content (buildings) has no numeric
  tuning fields (`calc` strings) at all, so the constrained-expression-
  parser question doesn't arise yet; it's relevant starting with the Tile
  and PlayerClass phases.
- Must not regress `mvn compile exec:java` (dev run) or the packaged
  jar/installer — both need to find `mods/` relative to wherever they're
  actually running from, which likely differ (working directory during
  `exec:java` vs. install directory for the packaged app). Resolving the
  correct base path for each run mode is part of this phase's
  implementation, not a follow-on.
- Follows the repo's existing intent → spec → approval → implementation
  pipeline (see root `CLAUDE.md`).

## Open questions

None outstanding — see Clarifications below.

## Clarifications

- Q: What mod.json fields matter for this phase, beyond `id`?
  A: `id` + an optional `dependsOn` list (mod IDs this mod should load
     after). No `displayName`/`version`/`description` yet — deferred
     until something actually consumes them.
  Affects: general (mod.json schema)

- Q: How does a mod flag that it intentionally overrides another mod's
     content ID?
  A: A per-item `overrides` field on the specific content JSON file that
     intends to replace another mod's ID (e.g. an `"overrides":
     "core:small_house_01"` field alongside its own id) — not a blanket
     per-mod flag. Only that specific file is marked as an intentional
     override.
  Affects: "A mod declaring a colliding ID with an explicit override
  replaces the earlier definition"

- Q: Is "mods/ directory doesn't exist at all" a real case to handle, or
     does core always ship as a present mods/core/ folder?
  A: core always ships inside mods/ — checked into the repo for dev runs,
     packaged into the installer/jar's working directory for release. A
     missing mods/ directory is a broken install, not a supported case;
     no special fallback loading path for core is needed.
  Affects: general (removes the "missing mods/ directory" open question;
  no such scenario is added)

- Q: What should a failed load (missing resource, malformed JSON,
     unflagged ID collision) raise?
  A: A new `ModLoadException`, replacing `BuildingException`'s role for
     building loads specifically going forward — ModLoader is a real
     generalization, not just BuildingLoader renamed.
  Affects: "A mod declaring a colliding ID without an override flag fails
  to load"; also means `specs/features/building-loader-failure-path.feature`
  needs reconciling since it currently asserts `BuildingException`.

- Q: Where should the ModLoader look for the mods/ directory during
     `mvn compile exec:java` (dev run) versus the packaged jar/installer?
  A: Relative to the JVM's current working directory in both cases —
     project root when running `mvn compile exec:java` from the repo
     root, install directory when launching the packaged app (typically
     the cwd there too). One code path, no run-mode detection.
  Affects: general (removes the "mods/ resolution path" open question);
  the Background step ("a mods directory containing...") is implemented
  against a working-directory-relative mods/ path.

- Q: (Self-resolved while prepping implementation, from already-approved
     source material) Is a content item's namespaced ID auto-derived from
     `<mod-id>:<filename>`, or does each content JSON declare its own
     explicit "id" field?
  A: Explicit and mandatory, per content file — matching issue #26's own
     JSON examples (`"id": "core:iron_sword"` is written directly in the
     file). Building JSON gains a new required `"id"` field (e.g.
     `"id": "core:small_house_01"`); the existing `"name"` field is
     unaffected. Nothing prevents a mod from declaring an `"id"` inside
     another mod's namespace — that's exactly the case the `"overrides"`
     field (see above) exists to gate: an unflagged `"id"` collision at
     load time is a ModLoadException, a flagged one (matching
     `"overrides"` value to its own `"id"`) is an intentional, logged
     replacement. Without this, the auto-derived alternative would make
     the unflagged-collision scenario impossible to trigger across two
     different mods, since the mod-id prefix would already disambiguate
     them.
  Affects: "A mod declaring a colliding ID without an override field
  fails to load"; "A mod declaring a colliding ID with an explicit
  override replaces the earlier definition"; building JSON schema now
  requires an "id" field.

## Source

Design doc: [GitHub issue #26](https://github.com/SwiftFaze/Veil/issues/26) (overall mod-structure proposal).
This intent covers phase 1 specifically: [GitHub issue #48](https://github.com/SwiftFaze/Veil/issues/48).
