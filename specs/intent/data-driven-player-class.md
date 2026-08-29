# Intent: Data-driven PlayerClass via JSON stat-growth curves

- **Slug(s):** data-driven-player-class (matches `/specs/features/data-driven-player-class.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-29
- **Source:** [GitHub issue #50](https://github.com/SwiftFaze/Veil/issues/50), phase 3 of the mod-structure design in [#26](https://github.com/SwiftFaze/Veil/issues/26)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [x] Approved by human
- [x] Implemented
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [x] Acceptance tests passing
- [ ] Mutation testing passed
- [x] Documentation updated (`docs/`, and the wiki if player-facing)

## Problem

`PlayerClass` (`Warrior`/`Mage`) is already data-driven for *base* stats, but
only via classpath resources (`src/main/resources/classes/*.json`, loaded by
`PlayerClassLoader`) — a refactor that predates and doesn't follow the
mod-structure design in #26. It has no namespaced IDs, isn't loaded through
the external `mods/` folder, and has no concept of per-level stat growth at
all. To be moddable per #26's phasing, class definitions need to live in
`mods/core/classes/*.json`, loaded through the same `ModLoader`/`ModRegistry`
already used for tiles and buildings, and support per-level growth curves
expressed as data (`calc` strings) rather than requiring Java code for new
classes or new growth behavior.

## Scope

- In scope:
  - Migrate `PlayerClass` definitions from `src/main/resources/classes/*.json`
    (classpath, unnamespaced) to `mods/core/classes/*.json` (external,
    namespaced `core:warrior` / `core:mage`), loaded through `ModLoader`
    following the same pattern already used for `mods/core/tiles/*.json` and
    `mods/core/buildings/*.json` (including the existing ID-collision/
    `overrides` check).
  - Introduce a first-ever stat/vocabulary registry, `mods/core/stats.json`
    (or equivalent `core:stats.json`-style resource), listing the 8 stat
    names a class definition may target: `strength`, `dexterity`,
    `constitution`, `intelligence`, `wisdom`, `luck`, `maxHp`, `maxMana`.
    These are exactly `Stats`' settable fields today — no derived stats
    (`attackPower`, `defense`) are included, since nothing in this ticket's
    scope would ever reference them.
  - Introduce a first-ever constrained `calc` expression parser (per #26:
    fixed arithmetic grammar over a small variable set such as `level`, e.g.
    `"level*1.5+2"` — not arbitrary code / no embedded scripting).
  - Each class JSON specifies base stat values plus per-level growth via
    `calc` strings for the 8 registry stats.
  - Loader validates every `calc` string's referenced stat name against the
    registry at load time, failing fast with a clear error on an unknown
    stat name.
  - Expose growth as a Java API on `PlayerClass` (e.g. computing/returning
    the stat values for an arbitrary level) that is directly exercised by
    unit/acceptance tests at specific levels.
  - `core:warrior` / `core:mage` base stats must exactly match today's
    values (see `mods/core/tiles/*.json`-style JSON, and today's
    `src/main/resources/classes/warrior.json` / `mage.json`) — no balance
    change.
  - `specs/features/default-player-class.feature` (default class on new
    player creation) must keep passing unmodified against the
    `ModRegistry`-backed `PlayerClass`, same as it did across the earlier
    classpath-based migration.
  - Wiki **Classes** page (per `docs/wiki.md`) updated to reflect the new
    data-driven source of truth and note per-level growth, since class base
    stats are explicitly player-facing per that doc's existing convention.
  - Remove the now-superseded classpath path: `PlayerClassLoader`,
    `src/main/resources/classes/warrior.json` / `mage.json`, and
    `specs/features/data-driven-player-classes.feature` (superseded by this
    ticket's own `.feature` file, drafted in Step 2).
- Out of scope:
  - Items, quests, maps — separate follow-on issues (see #26).
  - Any runtime level-up trigger or mechanic. No leveling-system trigger
    exists anywhere in the codebase today (`Level.setCurrentLevel` is never
    called outside its own setter, and no separate "leveling system" issue
    exists), and building one is materially bigger than "data-drive
    PlayerClass." `Level.java` / `PlayerInfo.java`'s runtime flow is
    untouched by this ticket — growth is a capability (data + parser +
    validation + a directly-callable API), not a wired-up game mechanic.
  - Extending the stat registry to derived stats (`attackPower`, `defense`)
    or any stat items would need — deferred until items are data-driven.
  - Rebalancing any class's numbers.

## Actors

- Character creation / class selection code (`PlayerInfo`, currently backed
  by `PlayerClassLoader`, moves to `ModRegistry`).
- Mod authors defining new playable classes via `mods/<mod-id>/classes/*.json`.

## Desired behavior

- A class JSON under `mods/core/classes/` (e.g. `warrior.json`) declares a
  namespaced `id` (`core:warrior`), a display name, base values for the 8
  registry stats, and a per-level growth `calc` string for each of those
  stats.
- `ModLoader` reads `mods/<mod-id>/classes/*.json` the same way it already
  reads `tiles/` and `buildings/`: per-mod, in dependency load order, with
  the existing collision/`overrides` check for a class ID already claimed by
  an earlier-loaded mod.
- At load time, every class's `calc` strings are checked against the stat
  registry; an unknown stat name fails loading immediately with a message
  naming the offending class, stat, and file.
- `PlayerClass` (or its replacement) exposes a way to compute the full stat
  set for an arbitrary level, applying base values plus each stat's growth
  `calc` evaluated at that level — callable directly by tests without any
  in-game level-up event.
- Creating a new player still defaults to `core:warrior` with today's exact
  base stats (HP 120, mana 20, etc.) — `default-player-class.feature`
  continues to pass unmodified.
- Loading an unknown/missing class file still fails clearly (matches
  today's `PlayerClassException` behavior, adapted to the `ModLoadException`
  pattern `ModLoader` already uses for tiles/buildings).
- Wiki **Classes** page describes per-level growth in player-facing terms
  (not raw `calc` strings), matching `docs/wiki.md`'s existing convention.

## Constraints / non-functional notes

- No embedded scripting — fixed vocabulary + `calc` strings only, per #26.
  The `calc` grammar itself (supported operators, whether parentheses/
  functions are needed beyond arithmetic) is left to spec drafting, not
  fixed here.
- Namespaced IDs (`<mod-id>:<name>`) for every class, matching the tile/
  building precedent.
- Follows the repo's existing intent → spec → approval → implementation
  pipeline.

## Open questions

- Exact set of supported growth-curve "types" beyond linear `calc` — carried
  over verbatim from issue #50, which explicitly defers this to spec
  drafting (Step 2). Resolved — see Clarifications below.

## Clarifications

- Q: Does a stat's total value at level N equal `base + calc(N)`, or does
  `calc(N)` replace the stat's value outright once N > 0 (with `base` only
  mattering at creation)?
  A: Additive — total = `base + calc(N)`. `calc` expresses the cumulative
  bonus earned from leveling, matching #26's item example (`calc` as a
  total bonus amount); `base` stays exactly today's Warrior/Mage numbers
  regardless of growth curves.
  Affects: growth-curve scenario in data-driven-player-class.feature.

- Q: calc grammar scope — plain arithmetic only, or a separate
  fixed-vocabulary `"type"` field (`"linear"`/`"quadratic"`/etc.) alongside
  or instead of `calc`? (Issue #50's own explicitly-deferred question.)
  A: Plain arithmetic only (`+ - * /`, parentheses, the `level` variable,
  numeric literals). No separate `"type"` field — in #26, `"type"` selects
  a *behavior* (item's `stat_bonus`, quest's `kill` objective); class
  growth has no behavior to select, it's pure numbers, so curve shape
  (linear, quadratic, etc.) emerges from the formula itself.
  Affects: general — calc parser grammar.

- Q: Rounding rule for a calc result applied to an int `Stats` field?
  A: Round-half-up (`Math.round`).
  Affects: general — growth application.

- Q: Must every one of the 8 registry stats have both a base value and a
  growth calc in a class JSON, or can either be omitted?
  A: Both optional per stat, defaulting to 0 when omitted.
  Affects: growth-curve scenario in data-driven-player-class.feature.

- Q: What does the `level` parameter mean numerically — 0-indexed or
  1-indexed?
  A: 0-indexed, matching the existing `Level.getCurrentLevel()` convention
  already used elsewhere in the codebase (`PlayerInfoPanel.java` displays
  `"LV " + currentLevel` with no offset, and `Level`'s constructor starts
  `currentLevel` at 0). Found by reading the code, not asked — this ticket
  doesn't change that convention, just aligns with it.
  Affects: growth-curve scenario in data-driven-player-class.feature.
