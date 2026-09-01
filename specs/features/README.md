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
| `ui-component-framework.feature` | Shared terminal-style widget framework (list/button/popup, focus manager, selected-state styling) proven by rebuilding the in-game inventory popup — opened directly via the keyboard toggle, no menu widget — and ClassSandboxPanel; supersedes SelectableMenu-based scenarios previously in `keyboard-input-and-menu-navigation.feature`, `ui-panel-rendering-and-composition.feature`, `class-sandbox-panel-selection.feature`, and `data-driven-item.feature` |
| `ui-widget-table.feature` | Keyboard-navigable, full-width, bordered table widget (row/column selection, wrap-around, row-level confirm, header row, non-selectable mode); real consumers: the inventory popup's field/value and effects tables, forming one continuous navigable region |
| `ui-widget-radio-group.feature` | Single-select radio group widget (vertical by default, optional horizontal); real consumer: the inventory popup's "Drop item?" confirmation |
| `ui-widget-pattern-field.feature` | Regex-pattern-validated text field widget (valid/invalid state via a new WidgetTheme color) — no real consumer yet, proven in isolation |
| `startup-welcome-screen.feature` | Title screen shown on launch (VEIL title + Continue/New/Load/Settings/Exit menu), replacing direct-to-world startup |
| `settings-screen.feature` | Settings screen shell (brightness/fullscreen/font/theme/volume/keybinds/folders/about/reset), visual/input shape only |
| `settings-keybinds-page.feature` | Dedicated keybind-rebinding sub-page (action list, press-any-key popup, Apply/Cancel/Go back), display only |
| `ui-widget-slider.feature` | Bounded-value slider widget (Left/Right adjusts within [min, max], no wrap) — no real backing system, proven in isolation |
| `confirmation-popup-variant.feature` | Centered, content-sized PopupWidget presentation, proven by a Yes/No confirm dialog on the settings screen's Reset to Defaults |
| `widget-theming.feature` | Mod-driven `theme.json` loaded via `ModLoader` (id/collision/overrides), populating all 10 `WidgetTheme` colors from `mods/core/theme.json`; a second mod's theme loads without activating |
| `codex-ui.feature` | In-game Codex overlay (X key) with a tab switcher across Items/Tiles/Classes, each tab a list+detail split mirroring InventoryPanel; no locking/gating |
| `pmd-jacoco-quality-gates.feature` | `mvn verify` fails on PMD complexity/length/parameter/duplication violations or sub-85% repo-wide JaCoCo coverage, with pure-layout Swing classes excluded from both; build-pipeline concern, no Java code path (`@manual-verification`) |
