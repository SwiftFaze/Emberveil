# Intent: Data-driven items via JSON + minimal InventoryPanel wiring

- **Slug(s):** data-driven-item (matches `/specs/features/data-driven-item.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-29
- **Source:** [GitHub issue #51](https://github.com/SwiftFaze/Veil/issues/51), phase 4 of the mod-structure design in [#26](https://github.com/SwiftFaze/Veil/issues/26)

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

Items don't exist yet — `InventoryPanel` (`src/main/java/com/swiftfaze/veil/ui/InventoryPanel.java`)
is a UI stub with five hardcoded `"N. Item"` labels and nothing behind
them. `Tile` and `PlayerClass` are already data-driven and moddable
through `ModLoader`/`ModRegistry` (`mods/core/tiles/*.json`,
`mods/core/classes/*.json`); items are the next content type in #26's
phasing and need the same treatment from the start, rather than being
hardcoded in Java first and retrofitted later.

## Scope

- In scope:
  - Item JSON definitions under `mods/core/items/*.json`, loaded through
    the same `ModLoader`/`ModRegistry` pattern already used for tiles,
    buildings, and classes (per-mod, dependency load order, existing
    `overrides`-flag collision check via `registerWithCollisionCheck`).
  - Item schema fields: namespaced `id` (`core:iron_sword`), `name`,
    `glyph`, `type`, `slot`, `baseDamage` (`{ "min": ..., "max": ... }`),
    and `effects` — a JSON array of `{ "type": ..., "stat": ...,
    "calc": ... }` entries (real array, not numbered fields — see #26's
    "Prior art" section on why D2's flat numbered-column tables are the
    thing *not* to copy).
  - Every `effects[].stat` value validated against the existing stat
    registry (`mods/core/stats.json`, already loaded by
    `ModLoader.loadStatRegistry` for classes) at load time, failing fast
    with a clear `ModLoadException` naming the offending item, stat, and
    file — same pattern as `loadClass`'s unregistered-stat check.
  - `effects[].calc` strings parsed for validity with the existing
    `CalcExpressionParser` at load time (catches a malformed expression
    early) — no new parser needed, items reuse the one built for class
    growth curves. Not evaluated to a number this slice (see
    Clarifications: no equip system exists yet to consume a computed
    value).
  - `effects[].type` vocabulary for this slice: `stat_bonus` only (see
    Clarifications).
  - `EastPanel` loads the `ModRegistry` itself (mirroring the
    self-contained `ModLoader.load()` pattern `PlayerInfo`/
    `TileTestScene2` already use) and pushes the resolved item list down
    into `InventoryPanel` via a new `InventoryPanel.showItems(List<Item>)`
    method (mirroring `PlayerInfoPanel.updatePlayer`'s externally-pushed
    pattern between sibling panels) — see Clarifications.
  - `ModRegistry` gains a `getAllItems()` accessor mirroring
    `getAllPlayerClasses()`.
  - `InventoryPanel` renders at least one real `core:` item end-to-end,
    replacing its hardcoded stub labels with data pulled from
    `ModRegistry` — proves the schema/loader/UI path works, not a full
    inventory management system (see Out of scope).
  - At least one example item ships as `core` content (e.g.
    `mods/core/items/iron_sword.json`, matching #26's own example) to
    exercise the schema end-to-end and give `InventoryPanel` something
    real to render.
  - Wiki update (per `docs/wiki.md`) introducing items as player-facing
    content, since this is the first time items exist in any player-
    visible form.
- Out of scope:
  - Quests, maps — separate follow-on issues (see #26's phasing).
  - Any inventory *management* system: picking items up, dropping them,
    equipping/unequipping, stacking, or using/consuming them. Nothing in
    the codebase today gives `Player` an inventory or equipped-items
    concept (`Player.java` has no such field), and #26's phasing lists no
    separate "inventory management" phase — building one is materially
    bigger than "items schema + wire into the InventoryPanel stub," which
    is this ticket's literal scope per the issue title. `InventoryPanel`
    rendering real item data is the full extent of the UI wiring; how a
    player acquires or manages items is future work.
  - Effect *types* beyond `stat_bonus` — resolved during spec drafting
    (see Clarifications); more types are additive later work, not this
    slice.
  - A callable API to compute/evaluate an item's effect value (e.g. at a
    given player level) — deferred to whatever ticket adds equip/
    inventory management, when something will actually consume it. This
    slice only validates `effects[].stat` and parses `effects[].calc` for
    syntactic validity.
  - Extending the stat registry itself (`mods/core/stats.json`) — items
    reference existing registry stats only, per #50/#26's explicit
    deferral of registry growth until items existed.
  - Rebalancing or removing any existing tile/class/building content.

## Actors

- `InventoryPanel` UI (`src/main/java/com/swiftfaze/veil/ui/InventoryPanel.java`),
  instantiated by `EastPanel`.
- Mod authors defining new items via `mods/<mod-id>/items/*.json`.
- `ModLoader`/`ModRegistry` (`src/main/java/com/swiftfaze/veil/mods/`),
  extended with an item-loading path mirroring `loadClasses`/`loadClass`.

## Desired behavior

- An item JSON under `mods/core/items/` (e.g. `iron_sword.json`) declares
  a namespaced `id`, `name`, `glyph`, `type`, `slot`, `baseDamage`
  min/max, and an `effects` array of `{ "type": "stat_bonus", "stat":
  ..., "calc": ... }` entries — `stat_bonus` is the only supported
  `effects[].type` this slice.
- `ModLoader` reads `mods/<mod-id>/items/*.json` the same way it reads
  `tiles/`, `buildings/`, and `classes/`: per-mod, in dependency load
  order, with the existing collision/`overrides` check for an item ID
  already claimed by an earlier-loaded mod.
- At load time, every item's `effects[].stat` values are checked against
  the stat registry (`mods/core/stats.json`); an unknown stat name fails
  loading immediately with a message naming the offending item, stat, and
  file — matching `loadClass`'s existing behavior exactly. `effects[].calc`
  is parsed with `CalcExpressionParser` for syntactic validity only — not
  evaluated to a number this slice.
- `EastPanel` loads the `ModRegistry` in its own constructor and calls a
  new `InventoryPanel.showItems(List<Item>)` method with
  `ModRegistry.getAllItems()`'s result. `InventoryPanel` no longer shows
  hardcoded `"1. Item"`..`"5. Item"` labels; it renders one line per real
  item it's given (name, and enough of its data to prove it came from
  `ModRegistry` rather than being hardcoded).
- Loading an unknown/missing/malformed item file fails clearly via
  `ModLoadException`, matching the existing tile/building/class error
  pattern.
- Wiki gets an **Items** page (or equivalent, per `docs/wiki.md`'s
  existing convention) introducing the item schema in player-facing
  terms.

## Constraints / non-functional notes

- No embedded scripting — fixed `effects[].type` vocabulary + `calc`
  strings only, per #26.
- Namespaced IDs (`<mod-id>:<name>`) for every item, matching the tile/
  building/class precedent.
- Follows the repo's existing intent → spec → approval → implementation
  pipeline.

## Open questions

None outstanding — see Clarifications below.

## Clarifications

- Q: Full set of `effects[].type` values needed for a first playable
  slice — `stat_bonus` only, or more (e.g. a flat `damage_bonus`, a
  `resist` type)?
  A: `stat_bonus` only. It's the only type #26's own worked example
  (`iron_sword.json`) uses, and it's enough to exercise the full pipeline
  (schema → stat-registry validation → `InventoryPanel` render). The
  `effects` array is already additive, so more types are later,
  non-breaking work.
  Affects: item-loading scenarios in data-driven-item.feature.

- Q: Does this slice need a callable calc-evaluation API (mirroring
  `PlayerClass`'s per-level growth API), or is load-time validation of
  `effects[].stat`/`effects[].calc` enough?
  A: Load-time validation only. Issue #51's desired behavior says stats
  are "validated... at load time," not evaluated, and no equip/inventory-
  management system exists yet to consume a computed bonus (out of scope
  per this doc's Scope). `effects[].stat` is checked against the stat
  registry and `effects[].calc` is parsed for syntactic validity, but
  neither is evaluated to a number this slice. A callable
  compute-at-level API is deferred to whatever ticket adds equip/
  inventory management.
  Affects: general — item schema/loader behavior; rules out a
  growth-curve-style scenario like data-driven-player-class.feature's.

- Q: How does `InventoryPanel` obtain its item data — self-contained
  `ModLoader.load()` (like `PlayerInfo`/`TileTestScene2`), or an external
  push (like `PlayerInfoPanel.updatePlayer`)?
  A: Hybrid, closest to the `PlayerInfoPanel` precedent since
  `InventoryPanel` is a sibling Swing panel composed by `EastPanel` just
  like `PlayerInfoPanel` is: `EastPanel` loads the `ModRegistry` itself in
  its own constructor (no change needed to `Main.java`'s wiring), then
  pushes the resolved item list into `InventoryPanel` via a new
  `showItems(List<Item>)` method. `ModRegistry` gains a `getAllItems()`
  accessor mirroring `getAllPlayerClasses()`.
  Affects: InventoryPanel-renders-a-real-item scenario in
  data-driven-item.feature.
