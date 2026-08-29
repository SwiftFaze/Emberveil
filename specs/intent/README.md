# Intent docs

One `<feature-slug>.md` file per feature, written by a human before any spec
or code exists. This is the source of truth for *why* a feature exists and
what it must do — `/specs/features/<feature-slug>.feature` is generated from
it and never the other way around.

Intent is not written once and frozen: clarifying answers gathered while
drafting the `.feature` file get appended back here, then the `.feature` file
is regenerated from the updated intent.

Copy `TEMPLATE.md` to `<feature-slug>.md` to start a new one. See
`default-player-class.md` for a worked (if retroactive) example filled in.

## Index

Browsable map of which GitHub issue(s) motivated each intent doc.
**Whoever adds, removes, or renames an intent doc updates this table in
the same change** — same convention as `specs/features/README.md`'s own
Index table.

| File | Related issue(s) |
|---|---|
| `default-player-class.md` | — (worked example, predates issue tracking) |
| `restructure-solid-base.md` | [#15](https://github.com/SwiftFaze/Veil/issues/15) |
| `sandbox-dev-console.md` | [#28](https://github.com/SwiftFaze/Veil/issues/28) (follow-up: [#27](https://github.com/SwiftFaze/Veil/issues/27)) |
| `sandbox-spawn-edit.md` | [#27](https://github.com/SwiftFaze/Veil/issues/27) (depends on [#28](https://github.com/SwiftFaze/Veil/issues/28)) |
| `camera-behavior.md` | [#31](https://github.com/SwiftFaze/Veil/issues/31) (split out of [#25](https://github.com/SwiftFaze/Veil/issues/25)) |
| `world-scene-population-and-building-placement.md` | [#32](https://github.com/SwiftFaze/Veil/issues/32) (split out of [#25](https://github.com/SwiftFaze/Veil/issues/25)) |
| `ui-panel-rendering-and-composition.md` | [#33](https://github.com/SwiftFaze/Veil/issues/33) (split out of [#25](https://github.com/SwiftFaze/Veil/issues/25)) |
| `class-sandbox-panel-and-building-exception-coverage.md` | [#43](https://github.com/SwiftFaze/Veil/issues/43) |
| `mod-loader.md` | [#48](https://github.com/SwiftFaze/Veil/issues/48), phase 1 of [#26](https://github.com/SwiftFaze/Veil/issues/26) |
| `data-driven-tile.md` | [#49](https://github.com/SwiftFaze/Veil/issues/49), phase 2 of [#26](https://github.com/SwiftFaze/Veil/issues/26) |
| `data-driven-player-class.md` | [#50](https://github.com/SwiftFaze/Veil/issues/50), phase 3 of [#26](https://github.com/SwiftFaze/Veil/issues/26) |
| `data-driven-item.md` | [#51](https://github.com/SwiftFaze/Veil/issues/51), phase 4 of [#26](https://github.com/SwiftFaze/Veil/issues/26) |
| `data-driven-quest.md` | [#52](https://github.com/SwiftFaze/Veil/issues/52), phase 5 of [#26](https://github.com/SwiftFaze/Veil/issues/26) |
| `installer-mods-bundling.md` | [#62](https://github.com/SwiftFaze/Veil/issues/62) (follow-up to [#48](https://github.com/SwiftFaze/Veil/issues/48)) |
