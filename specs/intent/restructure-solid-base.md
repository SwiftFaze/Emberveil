# Intent: Restructure to a solid base

- **Slug(s):**
  - `world-single-floor-rendering`
  - `keyboard-input-and-menu-navigation`
  - `data-driven-player-classes`
  - `class-stats-sandbox`

  One intent, four `.feature` files — these are four distinct concepts
  (per `specs/features/README.md`), sequenced and landed together in one
  branch/PR (see Sequencing below).
- **Author:** SwiftFaze
- **Date:** 2026-08-28
- **Source:** [GitHub issue #15](https://github.com/SwiftFaze/Veil/issues/15) — transcribed here as the spec-first source of truth per the workflow (intent docs live in `specs/intent/`, not only in the issue tracker). No content changed; file paths below use the current `com.swiftfaze.veil` package (the issue predates the Emberveil → Veil rename).

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` files, x4)
- [x] Approved by human
- [ ] Implemented
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [ ] Acceptance tests passing
- [ ] Mutation testing passed
- [ ] Documentation updated (`docs/`, and the wiki if player-facing)

## Problem

The codebase has been solo-developed and has accumulated real architectural
debt in three areas the user called out directly, plus a fourth gap
discovered during exploration:

1. **Z-axis/floor system + fog-of-war** feels "weird" and doesn't work in
   some cases. It's deeply woven into the render loop and world-array
   shape, but the *only active scene* (`TileTestScene2`) already uses a
   single Z-layer — the multi-floor machinery is largely unused. Safe to
   remove.
2. **Player classes/stats are hardcoded** (one Java subclass per class,
   magic numbers, no registry) — many more classes and items are planned,
   and stats need to be easy to add/modify.
3. **Keyboard input and terminal-style menus are ad hoc** — two different
   Swing input APIs coexist, no keymap config, five duplicated UI-styling
   helpers, and a backdoor wiring path that bypasses the app's own listener
   interface. Navigation needs to be solid since the game is keyboard-only.
4. **No sandbox for testing classes/stats** — today the only way to check a
   class's numbers is to actually play the game. Once classes become
   data-driven (item 2), there should be a lightweight way to inspect them
   without playing.

## Scope

**In scope:** prune dead/broken code, remove the Z-axis/fog system
entirely, make class/stat base values JSON-driven (mirroring the existing
`BuildingLoader` pattern), solidify input/menu architecture, and add a
stat-inspector sandbox — leaving a smaller, more scalable base to build
classes/items/menus on top of going forward. No new gameplay features are
added in this pass.

**Out of scope:**
- Any fully-dynamic attribute map — `Stats`' six fixed fields and the
  `getAttackPower`/`getDefense` formulas stay plain Java for this pass;
  only each class's *base-stat values* move to JSON.
- A playable test arena — the sandbox is a stat inspector only (pick a
  class, see computed stats), since combat isn't wired to gameplay yet.
- The Item/equipment system (greenfield, no existing code) and
  `Level.getMaxXp()`'s hardcoded curve — both are candidates for their own
  future spec-first features reusing the `PlayerClassLoader`/JSON pattern
  established here.
- Making Help/Journal/Map/Character/Stats menu entries functional — only
  `Inventory` needs to keep working end-to-end through the new
  `SelectableMenu`; the rest stay decorative.

**Scope decisions confirmed with the user:**
- Dead code is deleted aggressively (see Area 1), not salvaged or left in
  place.
- Branch target: `develop` (revised — see Clarifications). The issue
  originally called for branching off `master` directly, since this
  restructure *is* the architecture stabilization the develop-first
  policy was waiting on. That's no longer viable: `master-source-check`
  (added in PR #21, after the issue was filed) hard-fails any PR into
  `master` whose head branch isn't `develop` or `hotfix/*`. This work
  reaches `master` the normal way, via the next `develop`→`master`
  promotion.

## Actors

- **The developer (SwiftFaze)**, building classes/items/menus on top of
  this base going forward — the direct beneficiary of the simplified
  architecture.
- **Players**, indirectly: no player-visible behavior changes in this
  pass (numeric diff in Area 2 must show zero drift), but the removed
  Z/fog system changes internal rendering, and `docs/wiki.md`'s
  player-facing Classes/Stats pages need confirmation that nothing
  factual changed.

## Desired behavior

### Area 1 — Remove Z-axis/floor system and fog-of-war

**Delete outright:**
- `world/MountainScene.java` (broken: `depth=0` ctor would throw
  `ArrayIndexOutOfBoundsException`; unreferenced elsewhere)
- `world/TileTestScene.java` and `tools/NoiseGenerator.java` (unused —
  commented out in `GamePanel`, nothing else calls the noise generator)
- `DrawableImageEntity.java` and `grass.png` (already dead per
  `docs/architecture.md`; no scene uses image rendering)
- `WorldScene.renderClouds(...)`
- `Tile.CLOUD`, `Tile.STAIR_UP`, `Tile.STAIR_DOWN` enum members
- `GamePanel`: `preciseZLevel`, `roundZLevel()`, `getBrightnessFromDepth()`,
  the `VK_PAGE_UP`/`VK_PAGE_DOWN` switch cases, the multi-layer loop in
  `paintComponent()` (lines ~104–142 today)
- `Player.java`: `z` field, `getZ()` override, `findFloorBelow`,
  `forceAscend()`, `forceDescend()`
- `Positionable.getZ()` default method
- `Building.worldZ` field + accessor
- `GameConst`: `Z_TRANSITION_SPEED`, `BRIGHTNESS_LEVEL_DECAY_RATE`,
  `SHADOW_START_DEPTH`, `MAX_BRIGHTNESS`, `MIN_BRIGHTNESS`,
  `FOG_ALPHA_COEFFICIENT`, `LEVEL_ABOVE_FOG_Z_LEVEL_START`,
  `DEFAULT_MAP_DEPTH`, `DEFAULT_PLAYER_START_Z`

**Modify:**
- `WorldScene`: backing array `Tile[depth][width][height]` →
  `Tile[width][height]`. Collapse every Z-aware/Z-less method pair
  (`fillAll`, `fillRegion`, `createBorder`, `isWalkable`, `getTile`) into a
  single 2D signature. `placeBuilding` writes one 2D blueprint, no z-loop.
  `renderWorld` drops its `z`/`brightness` params — becomes a flat draw.
- `GamePanel.paintComponent()`: single `scene.renderWorld(...)` call, no
  loop, no brightness/fog. `player.setPosition(x, y)` (2-arg).
- `Player.move()`: collapse to the single same-tile walkability check —
  delete the step-up and fall-through tiers entirely.
- `Building`/`BuildingLoader`: `blueprint` becomes `Tile[][]`. Drop the
  `layers`/`floors` wrapper in the JSON schema (it was already
  aspirational — `small_house_01.json` declares `"floors": 2` but only ever
  had one layer, and `BuildingLoaderIT` already asserts a single layer).
  `small_house_01.json` becomes `{"name", "type", "width", "height",
  "tiles": [...]}`. Update `BuildingLoaderIT` to match.
- `TileTestScene2`: drop its now-redundant Z parameter/usage — it already
  only ever wrote to one Z layer.
- `PlayerTest.java`: currently a stub with a fully-commented-out body. Since
  `Player.move()` is being rewritten and simplified, un-stub this test and
  cover basic movement/blocked-movement cases against the new 2D
  `WorldScene`.

**Docs:** `docs/architecture.md` needs a full rewrite of every
Z/fog/floor/brightness-related section (not a patch) — do this as part of
this PR since it's the area with the most content to correct.

### Area 3 — Input centralization and terminal-style UI

**New classes:**
- `com.swiftfaze.veil.input.Keybindings` (or similar) — centralized key
  constants, replacing inline `KeyEvent.VK_*` literals in the switch
  statement. A separate class rather than folding into `GameConst`, since
  key mapping is a distinct concern that will grow as menus gain real
  keybinds.
- `TerminalPanel` (shared `JPanel` base) — centralizes the black-bg /
  `Font.MONOSPACED` / border setup and `makeLabel(String)` helper currently
  duplicated across `NorthPanel`, `SouthPanel`, `MenuPanel`,
  `InventoryPanel`, `PlayerInfoPanel`. Style constants (colors, font,
  border insets) live here once, not five times.
- A minimal selection/focus model (e.g. `SelectableMenu`) — current index,
  `moveUp()`/`moveDown()`, `selected()`, Enter/Esc contract. Deliberately
  small: `MenuPanel` is the only consumer today (and later, Area 4's
  sandbox); making Help/Journal/Map/Character/Stats actually functional
  stays explicitly out of scope for this pass.

**Modify:**
- `GamePanel.keyListen()` and `Main.keyListen()`: migrate both to Key
  Bindings (`InputMap`/`ActionMap`, `WHEN_IN_FOCUSED_WINDOW`) so there's one
  input API repo-wide, matching `Main`'s existing F5 precedent. Movement and
  inventory-toggle become named `Action`s registered against
  `Keybindings` constants.
- Resolve the `GamePanel` → `EastPanel` direct-reference backdoor
  (`Main.java`'s `// wire "I" key to EastPanel's toggle`): extend
  `GameListener` (or add a sibling interface) so the inventory-toggle
  action dispatches the same way state updates do, instead of a
  special-cased field reference.
- `GamePanel`'s `GameListener` broadcast: fire only for actions that
  actually change state, not after every keypress including unmapped ones.
- `NorthPanel`, `SouthPanel`, `MenuPanel`, `InventoryPanel`,
  `PlayerInfoPanel`: extend/compose `TerminalPanel`, delete their private
  `makeLabel` duplicates.
- `MenuPanel`: wire it to `SelectableMenu` so up/down/Enter/Esc work
  against its item list (only `Inventory` needs to keep working end-to-end;
  H/J/M/P/O stay decorative for now, per scope above).

### Area 2 — Data-driven player classes/stats

**Scope (confirmed):** only base-stat *values* become data-driven. `Stats`'
six fixed fields and derived-stat formulas stay Java.

**New:**
- `resources/classes/warrior.json`, `resources/classes/mage.json` — base
  stat values per class, mirroring the shape of
  `resources/buildings/small_house_01.json`.
- `PlayerClassLoader` — mirrors `BuildingLoader`: `getResourceAsStream`,
  Gson parse into a plain data object, throws a new `PlayerClassException`
  on failure. Provides `load(String fileName)` and `loadAll()` (the latter
  is what makes a future class-select menu and Area 4's sandbox possible —
  nothing today can enumerate "all classes").

**Modify:**
- `PlayerClass`: becomes concrete (not abstract) — holds `name` + base
  stats populated by the loader. `applyBaseStats(Stats)` becomes a
  mechanical copy from loaded data instead of an overridden method per
  class.
- Delete `Warrior.java`, `Mage.java`.
- `PlayerInfo`: replace `new Warrior()` with a `PlayerClassLoader`-backed
  default lookup — preserves the existing "defaults to Warrior" behavior
  already covered by `specs/features/default-player-class.feature`.

**Verify no drift:** diff the new JSON values against current
`Warrior.java`/`Mage.java` hardcoded numbers to confirm they match exactly
— this pass moves data, it doesn't rebalance anything.

**Wiki:** per `docs/wiki.md`, class/stat changes are player-visible. Since
values shouldn't change, the Classes/Player Stats wiki pages likely need no
factual edit — but confirm via the diff above, and only touch the wiki if
it did.

### Area 4 — Class/stats sandbox (stat inspector)

**New:** a standalone dev-only entry point (e.g.
`com.swiftfaze.veil.sandbox.ClassSandbox` with its own `main`), not
referenced from `Main.java` and not wired into the packaged/jpackage build.
Run via `mvn compile exec:java -Dexec.mainClass=...` or a dedicated IDE run
configuration.

- Reuses `TerminalPanel` and `SelectableMenu` from Area 3.
- Lists all classes via `PlayerClassLoader.loadAll()` (Area 2).
- Selecting a class instantly displays its computed `Stats` — attack power,
  defense, HP, mana — with no world, no movement, no combat (nothing
  consumes combat stats in gameplay yet, so a playable arena would have
  nothing real to exercise).
- Editing a class's JSON file and re-running the sandbox should reflect the
  new numbers without recompiling Java — this is the actual point: fast
  iteration on class balance.

## Sequencing

One branch, one PR, off `develop`: **`chore/restructure-solid-base`**. All
four areas land together, implemented internally in this order (each still
verified working before moving to the next, per the checkpoints in
"Verification" below) rather than as separate branches:

**Area 1 → Area 3 → Area 2 → Area 4**

- Area 1 before Area 3: Area 1 deletes the `PAGE_UP`/`PAGE_DOWN` debug key
  cases outright. Doing input centralization first would mean building a
  keymap entry for those keys only to delete it again.
- Area 3 before Area 2: no hard dependency, but Area 3 fixes the
  `GamePanel`→`EastPanel` backdoor which is unrelated to stats — ordering
  this way keeps the work self-contained one concern at a time even though
  it all lands in one PR.
- Area 2 before Area 4: the sandbox needs `PlayerClassLoader` to exist.
- Area 4 also reuses Area 3's `TerminalPanel` and selection-model classes.

Each `.feature` file's implementation, acceptance tests, and
mutation-testing pass proceeds internally in this same order, all within
the single `chore/restructure-solid-base` branch/PR:
1. `world-single-floor-rendering.feature`
2. `keyboard-input-and-menu-navigation.feature`
3. `data-driven-player-classes.feature`
4. `class-stats-sandbox.feature`

`docs/architecture.md` gets its full rewrite as part of this same PR
(most of the outdated content is Area 1's; Areas 2 and 3 each add their own
smaller edits to it in the same pass).

## Constraints / non-functional notes

- Packaged jar/installer build (`mvn package`) must still succeed and must
  not include the sandbox as a launchable entry point.
- No player-visible stat changes (Area 2 numeric diff must be zero).
- Class JSON schema uses abbreviated stat field names (`str`, `dex`, `con`,
  `int`, `wis`, `luck`) per the user's preference (see Clarifications).
  `int` is a Java reserved word, so `PlayerClassLoader`'s data class must
  keep a normally-named Java field (e.g. `intelligence`) and map it to the
  `int` JSON key via Gson's `@SerializedName("int")`, rather than naming
  the field `int` directly.

## Critical files

- `src/main/java/com/swiftfaze/veil/game/GamePanel.java`
- `src/main/java/com/swiftfaze/veil/world/WorldScene.java`
- `src/main/java/com/swiftfaze/veil/entities/player/Player.java`
- `src/main/java/com/swiftfaze/veil/GameConst.java`
- `src/main/java/com/swiftfaze/veil/entities/buildings/Building.java` and
  `BuildingLoader.java`
- `src/main/java/com/swiftfaze/veil/ui/*.java`
- `src/main/java/com/swiftfaze/veil/entities/player/classes/PlayerClass.java`,
  `Warrior.java`, `Mage.java`
- `src/main/java/com/swiftfaze/veil/entities/player/PlayerInfo.java`,
  `Stats.java`
- `docs/architecture.md`, `docs/wiki.md`
- `specs/intent/`, `specs/features/`

## Verification

- `mvn verify` after each area's changes land within the branch, before
  moving to the next area (unit + Cucumber + integration tests).
- Area 1: manually run the game (`mvn compile exec:java`), confirm
  movement works with no Z-transition/fog artifacts, `BuildingLoaderIT`
  passes against the flattened schema, new `PlayerTest` cases pass.
- Area 3: manually verify movement + `I` inventory toggle still work
  through the new Action-based dispatch and no longer via the field-
  reference backdoor; up/down/Enter/Esc work on `MenuPanel`.
- Area 2: `PlayerClassLoader` unit tests load both classes correctly;
  numeric diff against pre-refactor `Warrior`/`Mage` values shows zero
  drift; existing `default-player-class.feature` still passes unmodified.
- Area 4: run the sandbox entry point, confirm both classes list and show
  correct computed stats; edit a JSON value and re-run to confirm it picks
  up the change without recompiling.
- Confirm the packaged jar/installer build (`mvn package`) still succeeds
  and does not include the sandbox as a launchable entry point.

## Clarifications

- Q: What keys/focus model drives `SelectableMenu` (up/down/confirm/back)?
  Menu panels are currently non-focusable, and `GamePanel` already owns
  WASD/arrows for movement.
  A: Up/Down arrow keys move the selection, Enter confirms. The menu
  panel takes keyboard focus while shown, so the same physical keys route
  to Key Bindings scoped to whichever panel currently has focus (GamePanel
  vs. the menu) — no separate key set needed.
  Affects: keyboard-input-and-menu-navigation.feature (Navigating a
  selectable menu, Confirming a menu selection)

- Q: At the top/bottom of a menu, does `moveUp()`/`moveDown()` clamp
  (stays put) or wrap (jumps to the other end)?
  A: Wrap — moving up from the first item selects the last, and moving
  down from the last selects the first.
  Affects: keyboard-input-and-menu-navigation.feature (new wrap-around
  scenarios)

- Q: What JSON field names does the class schema use for the six Stats
  attributes (e.g. "strength" vs "str")?
  A: Abbreviated — `str`, `dex`, `con`, `int`, `wis`, `luck`.
  Affects: general (schema/implementation detail for Area 2 — no Gherkin
  scenario wording changes, since scenarios describe behavior rather than
  raw field names; see Constraints above for the `int`/reserved-word note)

- Q: The issue calls for branching off `master` directly, but
  `master-source-check` (added in PR #21, after the issue was filed)
  hard-fails any PR into `master` whose head branch isn't `develop` or
  `hotfix/*`. How should this actually reach `master`?
  A: Retarget through `develop` like any other feature — branch off
  `develop`, PR into `develop`, and let it ride the next `develop`→`master`
  promotion. No CI change.
  Affects: general (Scope decisions, Sequencing — branch target changed
  from `master` to `develop`)

## Open questions

None outstanding.
