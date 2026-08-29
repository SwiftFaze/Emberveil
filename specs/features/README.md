# Feature specs (Gherkin)

One `<feature-slug>.feature` file per **distinct concept**, generated from
the matching file in `/specs/intent/`. Do not hand-edit a `.feature` file
ahead of its intent doc — update the intent doc first, then regenerate.

**One feature file, one thing.** If an intent doc covers multiple
unrelated concepts (e.g. a new class *and* a new biome), don't bundle them
into one combined file like `added-class-and-biome.feature` — split into
`class-warrior.feature` and `biome-jungle.feature`, each named for the
specific thing it covers. A single intent doc can produce more than one
`.feature` file when it isn't actually a single cohesive concept.

These files are copied onto the test classpath at build time (see the
`testResources` config in `pom.xml`) and executed by `RunCucumberTest` via
`mvn test`. Step definitions live under
`src/test/java/com/swiftfaze/veil/steps/`. See `docs/testing.md` for
the full test-layer breakdown.

## Index

Concept-based naming (above) means related behavior is spread across
several files instead of one per Java class — this table is the browsable
map of what's covered where. **Whoever adds, removes, or renames a
`.feature` file updates this table in the same change** — it's part of
that change's definition of done, not separate cleanup.

| File | Covers |
|---|---|
| `default-player-class.feature` | New player defaults to the Warrior class |
| `data-driven-player-class.feature` | Data-driven PlayerClass loaded from `mods/core/classes/*.json` with per-level stat growth |
| `class-stats-sandbox.feature` | Dev-only `ClassSandbox` stat display |
| `data-driven-tile.feature` | Tile definitions loaded from JSON + registry |
| `data-driven-item.feature` | Item definitions loaded from JSON + stat registry, minimal InventoryPanel wiring |
| `data-driven-quest.feature` | Quest definitions loaded from JSON + item registry, minimal per-player quest-state tracking |
| `mod-loader.feature` | External `mods/` directory loading (dependency order, overrides) |
| `building-loader-failure-path.feature` | Mod loader failure/error path |
| `world-single-floor-rendering.feature` | Single-floor world rendering |
| `world-scene-population-and-building-placement.feature` | World scene population and building placement |
| `camera-behavior.feature` | Camera follow/scroll behavior |
| `keyboard-input-and-menu-navigation.feature` | Key Bindings input and menu navigation |
| `ui-panel-rendering-and-composition.feature` | UI panel rendering and composition |
| `installer-mods-bundling.feature` | jpackage installer build bundles `mods/core` alongside the executable |
| `ui-component-framework.feature` | Shared terminal-style widget framework (list/button/popup, focus manager, selected-state styling) proven by rebuilding the in-game menu/inventory screen and ClassSandboxPanel; supersedes SelectableMenu-based scenarios previously in `keyboard-input-and-menu-navigation.feature`, `ui-panel-rendering-and-composition.feature`, `class-sandbox-panel-selection.feature`, and `data-driven-item.feature` (pending their migration during implementation) |
