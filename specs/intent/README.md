# Intent docs

One `<feature-slug>.md` file per feature, written by a human (or derived
from a GitHub issue via the `spec-intent` skill) before implementation
starts. This is the source of *why* a feature exists and what it must do.

See `.claude/workflow.md` for the high-risk vs. standard path split: on
the high-risk path (auth/payments/data-integrity/public API),
`/specs/features/<feature-slug>.feature` is generated from this doc and
reviewed/approved before any code is written. On the standard path, this
doc still gets written first, but feeds directly into an agile
implementation loop instead — no fully-approved `.feature` file is
required before code exists.

Intent is not written once and frozen: clarifying answers gathered while
drafting or implementing get appended back here.

Copy `TEMPLATE.md` to `<feature-slug>.md` to start a new one. See
`default-player-class.md` for a worked (if retroactive) example filled in.

These docs are a starting point for implementation, not a permanently
maintained artifact. Unlike `specs/features/*.feature` — which stays a
real, executable check via Cucumber — an intent doc has no equivalent
once a feature is built: the code itself, and its `.feature` file where
one exists, are what actually persist. The Index below is a best-effort
historical map, not something every future change is required to keep
current.

## Index

Browsable map of which GitHub issue(s) motivated each intent doc that
happened to get indexed. Adding a row here is optional, not a required
part of any change.

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
| `ui-component-framework.md` | [#36](https://github.com/SwiftFaze/Veil/issues/36) (table/radio/form-fields split into [#35](https://github.com/SwiftFaze/Veil/issues/35); couples with [#26](https://github.com/SwiftFaze/Veil/issues/26) phase 4) |
| `ui-widget-library.md` | [#35](https://github.com/SwiftFaze/Veil/issues/35) (split out of [#36](https://github.com/SwiftFaze/Veil/issues/36); builds on its framework) |
| `startup-and-settings-screens.md` | [#54](https://github.com/SwiftFaze/Veil/issues/54) (depends on [#36](https://github.com/SwiftFaze/Veil/issues/36)'s framework and [#35](https://github.com/SwiftFaze/Veil/issues/35)'s radio group widget) |
| `confirmation-popup-variant.md` | [#99](https://github.com/SwiftFaze/Veil/issues/99) (deferred from [#36](https://github.com/SwiftFaze/Veil/issues/36)'s playtest; depends on [#35](https://github.com/SwiftFaze/Veil/issues/35)'s radio group widget and [#54](https://github.com/SwiftFaze/Veil/issues/54)'s Reset to Defaults as its trigger) |
| `widget-theming.md` | [#106](https://github.com/SwiftFaze/Veil/issues/106) |
| `codex-ui.md` | [#113](https://github.com/SwiftFaze/Veil/issues/113) (follow-ups split into [#112](https://github.com/SwiftFaze/Veil/issues/112) discovery gating, [#121](https://github.com/SwiftFaze/Veil/issues/121) Enemies/Biomes) |
| `pmd-jacoco-quality-gates.md` | [#123](https://github.com/SwiftFaze/Veil/issues/123) |
| `deterministic-gauntlet-workflow.md` | [#128](https://github.com/SwiftFaze/Veil/issues/128) (builds on [#123](https://github.com/SwiftFaze/Veil/issues/123)) |
| `shared-list-detail-ui-contract.md` | [#125](https://github.com/SwiftFaze/Veil/issues/125) (implements the contract already designed in `docs/components.md`) |
| `controls-hint-bar.md` | [#134](https://github.com/SwiftFaze/Veil/issues/134) (contextual world-action hints out of scope, tracked in [#133](https://github.com/SwiftFaze/Veil/issues/133)) |
