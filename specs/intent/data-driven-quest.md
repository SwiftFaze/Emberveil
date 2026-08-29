# Intent: Data-driven quests via JSON + minimal quest-state tracking

- **Slug(s):** data-driven-quest (matches `/specs/features/data-driven-quest.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-29
- **Source:** [GitHub issue #52](https://github.com/SwiftFaze/Veil/issues/52), phase 5 of the mod-structure design in [#26](https://github.com/SwiftFaze/Veil/issues/26)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [x] Approved by human
- [x] Implemented
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [x] Acceptance tests passing
- [x] Mutation testing passed
- [x] Documentation updated (`docs/`, and the wiki if player-facing)

## Implementation notes

- All 145 tests pass (`mvn test`), including every scenario in
  `specs/features/data-driven-quest.feature`.
- Mutation testing (`mvn org.pitest:pitest-maven:mutationCoverage`): new/
  changed classes are all above the 85% line-coverage floor — `Quest`
  90%, `QuestLog` 100%, `PlayerInfo` 100%, `ModRegistry` 95%, `ModLoader`
  95%. The one surviving mutant in `Quest` is on `getId()`'s return
  statement — nothing calls it directly since `ModRegistry` looks quests
  up by the external map key, not via the returned object's own getter;
  the same shape already exists for `Item.getId()`, not a gap introduced
  by this feature.
- **Manual playtest (Step 4.5) intentionally left unchecked**: this slice
  has no player-visible surface — no UI wiring, no way to trigger a quest
  in-game (no quest-giving NPCs, no combat system to satisfy a `kill`
  objective), per this doc's own Out of scope. Running
  `mvn compile exec:java` shows nothing different from before this
  change; there is nothing for a human to feel out via movement, menus,
  or rendering, unlike prior phases (tiles/items) which did touch
  rendering/UI. Flagging for the human to confirm rather than silently
  checking the box or silently skipping it.
- No wiki update: nothing player-visible changed (no quest log UI, no
  in-game way to see/accept a quest yet), unlike the items ticket which
  had `InventoryPanel` wiring to justify a wiki page.

## Problem

Quests don't exist yet in any form. `Tile`, `PlayerClass`, and `Item` are
already data-driven and moddable through `ModLoader`/`ModRegistry`
(`mods/core/tiles/*.json`, `mods/core/classes/*.json`,
`mods/core/items/*.json`); quests are the next content type in #26's
phasing and, per that design doc, need the same schema-first treatment
plus minimal per-player state tracking (offered/active/complete) rather
than being hardcoded in Java first and retrofitted later.

## Scope

- In scope:
  - Quest JSON definitions under `mods/core/quests/*.json`, loaded
    through the same `ModLoader`/`ModRegistry` pattern already used for
    tiles, buildings, classes, and items (per-mod, dependency load order,
    existing `overrides`-flag collision check via
    `registerWithCollisionCheck`).
  - Quest schema fields: namespaced `id` (`core:goblin_slayer`), `name`,
    `objective` (an object with a fixed-vocabulary `type`, e.g. `"kill"`,
    plus type-specific fields such as `target` and `count`), and
    `rewards` — a real JSON array of `{ "type": ..., "id"?, "count"?,
    "calc"? }` entries (array, not numbered fields — see #26's "Prior
    art" section on why D2's flat numbered-column tables are the thing
    *not* to copy).
  - `rewards[].type: "item"` entries carry an `id` that must resolve
    against `ModRegistry`'s item registry (`getItem(id)`, populated by
    the items schema, phase 4/#51) — an unresolved reference fails fast
    at load time, matching the existing unregistered-stat/unparseable-calc
    failure pattern from `loadItem`/`loadClass`.
  - `rewards[].type: "xp"` entries carry a `calc` string, parsed for
    syntactic validity with the existing `CalcExpressionParser` at load
    time (same reuse `loadItem`/`loadClass` already make) — no new parser
    needed.
  - `objective.type` vocabulary for this slice: `kill` only (see
    Clarifications) — the only type #26's own worked example
    (`goblin_slayer.json`) uses.
  - Minimal per-player quest state: for each quest ID, one of
    not-started / offered / active / complete, tracked somewhere
    reachable from `Player` (see Clarifications for exactly where) and
    surviving at least the current play session.
  - `ModRegistry` gains a `getQuest(id)` and `getAllQuests()` accessor
    mirroring `getItem`/`getAllItems()`.
  - At least one example quest ships as `core` content
    (`mods/core/quests/goblin_slayer.json`, matching #26's own example)
    to exercise the schema end-to-end.
- Out of scope:
  - Maps — separate follow-on issue (see #26's phasing, phase 6).
  - Quest-giving NPCs / dialogue UX — no NPC concept exists in the
    codebase yet (only `Player`, buildings, and tiles); this issue covers
    the schema and state model only, per the issue's own Actors section.
  - Any mechanism that actually *transitions* quest state in response to
    gameplay (e.g. detecting a `kill` objective's target being defeated) —
    no combat/monster/enemy system exists anywhere in the codebase today
    (confirmed: no `Monster`/`Enemy`/`Combat` classes), so there is
    nothing yet to hook a `kill` objective into. This slice provides the
    schema, load-time validation, and a state container/API a future
    combat system can call into — not the combat system itself. Mirrors
    the items ticket's precedent of validating `effects[].calc` without
    an equip system yet to consume it.
  - `objective.type` values beyond `kill` — resolved during spec drafting
    (see Clarifications); more types are additive later work, not this
    slice.
  - `rewards[].type` values beyond `item` and `xp` — matches the issue's
    explicit scope ("item and/or `calc`-based xp rewards").
  - Persisting quest state across game restarts (save/load) — the issue
    flags this as an open question to resolve during spec drafting (Step
    2), not Step 1; no save/load system exists anywhere in the codebase
    today to persist into regardless. See Open questions.
  - Any UI surface for quests (a quest log panel, dialogue prompts) — not
    mentioned in the issue's desired behavior, and no quest UI precedent
    exists (unlike items, which had `InventoryPanel` already stubbed out).
  - Extending the stat registry or the item schema themselves — quests
    only reference existing item IDs.
  - Rebalancing or removing any existing tile/class/building/item
    content.

## Actors

- Quest-giving NPCs / dialogue (not yet designed — this issue covers the
  schema and state model, not NPC UX).
- Player character (accepts/completes quests, receives rewards) —
  `src/main/java/com/swiftfaze/veil/entities/player/Player.java` has no
  quest-state field today; this slice adds one.
- Mod authors defining new quests via `mods/<mod-id>/quests/*.json`.
- `ModLoader`/`ModRegistry`
  (`src/main/java/com/swiftfaze/veil/mods/`), extended with a
  quest-loading path mirroring `loadItems`/`loadItem`.

## Desired behavior

- A quest JSON under `mods/core/quests/` (e.g. `goblin_slayer.json`)
  declares a namespaced `id`, `name`, an `objective` object (`{ "type":
  "kill", "target": "core:goblin", "count": 5 }` for this slice), and a
  `rewards` array mixing `{ "type": "item", "id": ..., "count": ... }`
  and `{ "type": "xp", "calc": ... }` entries.
- `ModLoader` reads `mods/<mod-id>/quests/` the same way it reads
  `tiles/`, `buildings/`, `classes/`, and `items/`: per-mod, in
  dependency load order, with the existing collision/`overrides` check
  for a quest ID already claimed by an earlier-loaded mod.
- At load time, every `rewards[].type: "item"` entry's `id` is checked
  against the item registry (`ModRegistry.getItem`, populated earlier in
  the same `ModLoader.load()` pass since items load before quests); an
  unresolved item ID fails loading immediately with a message naming the
  offending quest, item ID, and file. Every `rewards[].type: "xp"`
  entry's `calc` is parsed with `CalcExpressionParser` for syntactic
  validity only — not evaluated to a number this slice.
- Loading an unknown/missing/malformed quest file, or an `objective.type`
  outside the supported vocabulary, fails clearly via `ModLoadException`,
  matching the existing tile/building/class/item error pattern.
- Minimal per-player quest state exists: for a given quest ID, the state
  is one of not-started, offered, active, or complete, and it changes
  when explicitly set (no automatic gameplay-driven transitions this
  slice — see Out of scope). It survives at least the current play
  session (see Open questions for the save/load boundary).

## Constraints / non-functional notes

- No embedded scripting — fixed `objective.type`/`rewards[].type`
  vocabulary + `calc` strings only, per #26.
- Namespaced IDs (`<mod-id>:<name>`) for every quest, matching the
  tile/building/class/item precedent.
- Quest loading must run after item loading within `ModLoader.load()`,
  since `rewards[].id` resolution depends on the item registry already
  being populated (mirrors items loading after the stat registry for the
  same reason).
- Follows the repo's existing intent → spec → approval → implementation
  pipeline.

## Open questions

- Whether quest state needs to persist across game restarts (save/load)
  in this phase, or only within a session — the issue defers this to
  spec drafting (Step 2). No save/load system exists anywhere in the
  codebase today (`Player`/`PlayerInfo` are constructed fresh each run),
  so "persist across restarts" would mean building the first save/load
  mechanism in the project — worth confirming that's actually in scope
  before `spec-feature` drafts scenarios around it.
- Exactly where per-player quest state should live: a new field on
  `Player`/`PlayerInfo` (mirroring how `PlayerInfo` already holds
  class/stats), or a separate `QuestLog`-style object owned by `Player` —
  resolve during spec drafting.

## Clarifications

- Q: Does quest state need to persist across game restarts (save/load),
  or is within-session tracking enough for this phase?
  A: Session-only. No save/load system exists anywhere in the codebase
  today (`PlayerInfo` is constructed fresh every run via
  `ModLoader.load(Paths.get("mods"))` in its constructor — see
  `PlayerInfo.java:23`) — building the project's first save/load
  mechanism is materially bigger than "minimal... state tracking" per the
  issue's own title, and the issue's desired-behavior wording ("survives
  a play session **at minimum**") reads as session-scoped being the
  explicit floor for this phase, not a placeholder for something bigger.
  Self-resolved (no human review requested for this pass — see
  session note); persistence is future follow-on work once a save/load
  system exists.
  Affects: quest-state scenarios in data-driven-quest.feature — no
  restart/reload scenario included.

- Q: Where does per-player quest state live — a field on `Player`
  directly, or a separate object?
  A: A `QuestLog` object owned by `PlayerInfo` (new
  `PlayerInfo.getQuestLog()`), not a field directly on `Player` or
  `PlayerInfo` itself. `PlayerInfo` already composes the player's
  session-scoped data this way (`stats`, `playerClass`, `level` — see
  `PlayerInfo.java`), and `Player` only holds position/rendering state
  today (`Player.java`), delegating everything else to
  `PlayerInfo`/`playerInfo`. `QuestLog` mirrors that composition rather
  than growing `Player` or `PlayerInfo` with quest-specific fields
  inline.
  Self-resolved.
  Affects: quest-state scenarios in data-driven-quest.feature (phrased
  in domain terms — "a player's quest state" — not tied to this class
  name, but recorded here for the implementation step).

- Q: Are quest-state transitions validated (e.g. must go
  not-started → offered → active → complete in order), or is this a
  free-form setter?
  A: Free-form setter, no ordering validation. The issue calls this
  "minimal" state tracking, and no gameplay system exists yet (no
  combat/dialogue) to drive transitions through any particular order —
  adding transition-order validation now would be enforcing rules for a
  caller that doesn't exist yet. `QuestLog.setState(questId, state)`
  accepts any `QuestState` value at any time.
  Self-resolved.
  Affects: quest-state scenarios in data-driven-quest.feature.

- Q: Is a quest ID validated against the loaded quest registry
  (`ModRegistry`) when setting player quest state, or can `QuestLog`
  track arbitrary IDs unvalidated?
  A: Unvalidated — `QuestLog` has no dependency on `ModRegistry`. Nothing
  else in `PlayerInfo` cross-validates against the registry post-load
  either (e.g. `setPlayerClass` doesn't check the class came from
  `ModRegistry`), and wiring `QuestLog` to the registry would be new
  coupling this "minimal" slice doesn't need.
  Self-resolved.
  Affects: quest-state scenarios in data-driven-quest.feature.

- Q: (Self-caught, not user-asked) #26's worked example uses
  `"calc": "count*25"` for the xp reward — does `CalcExpressionParser`
  actually support a `count` variable?
  A: No. `CalcExpressionParser.evaluate(String, int level)`
  (`src/main/java/com/swiftfaze/veil/mods/CalcExpressionParser.java:8`)
  only recognizes the identifier `level` (`Parser.parseFactor`, line 112);
  any other identifier — including `count` — throws
  `IllegalArgumentException("Unexpected token: ...")`. #26's design doc
  predates this parser (built for `PlayerClass` growth curves, which only
  ever needed `level`). Since this ticket reuses the existing parser
  as-is (no new parser, matching the items ticket's precedent) rather
  than extending its grammar, `data-driven-quest.feature`'s xp-reward
  examples use `level`-based calc strings (e.g. `"level*25"`), not
  `count`-based ones. Extending `CalcExpressionParser` with a `count`
  variable bound to the objective's count is plausible future work but
  out of scope here — it would need a design decision (is `count` only
  valid on xp rewards tied to a `kill`-style objective? what does it mean
  for a reward on a hypothetical future objective type without a count?)
  that this slice doesn't need to make.
  Affects: all xp-reward examples in data-driven-quest.feature.
