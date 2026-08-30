# Changelog

## [0.5.0-beta.2](https://github.com/SwiftFaze/Veil/compare/v0.5.0-beta.1...v0.5.0-beta.2) (2026-08-30)


### Features

* add uncle-bob-craft skill and wire it into Step 4 self-check ([034fd34](https://github.com/SwiftFaze/Veil/commit/034fd348402a27ef8bd18c788fe58d3341ebe8a0))

## [0.5.0-beta.1](https://github.com/SwiftFaze/Veil/compare/v0.5.0-beta...v0.5.0-beta.1) (2026-08-30)


### Features

* draft Gherkin specs for table, radio group, and pattern-field widgets ([92fefe4](https://github.com/SwiftFaze/Veil/commit/92fefe491083df1b0c95ef5f738429f94fff8065))
* implement UI widget library (table, radio group, pattern field) ([cc4cc57](https://github.com/SwiftFaze/Veil/commit/cc4cc57309cb15201c125ed5e4063507c9126d79))
* pattern field gets a Material-style outlined-field look ([c4ddf37](https://github.com/SwiftFaze/Veil/commit/c4ddf3754cf2d5b797ccf72c0bad72f4af3b4b4a))
* pattern field gets a real cursor, Left/Right movement, Ctrl+A ([ae4b641](https://github.com/SwiftFaze/Veil/commit/ae4b6417f7e72f592b6af578abc9e58c5e7c787c))
* pattern field widget gets a validity-colored, focus-aware border ([1b0ef5e](https://github.com/SwiftFaze/Veil/commit/1b0ef5e1f34da23ae15ecbad0b58d45e18f0e9e5))
* radio group gets a confirmed-state border, highlight goes neutral ([1c47f0c](https://github.com/SwiftFaze/Veil/commit/1c47f0c96bff07d762040e174a0014c70782184a))
* real InventoryPanel consumer for the table and radio group widgets ([e57396a](https://github.com/SwiftFaze/Veil/commit/e57396a504ae533fb4e574aaeef316b32433e2ae))
* restructure inventory details pane into field/effects tables ([daed315](https://github.com/SwiftFaze/Veil/commit/daed315b3a88374f86cbf0b347de1be82f375601))


### Bug Fixes

* block cursor made the letter under it invisible ([63d8fc6](https://github.com/SwiftFaze/Veil/commit/63d8fc60aed7e85316a5a7bcc25a59467172166f))
* block cursor wasn't blinking - blink rate was never actually set ([0e17d5c](https://github.com/SwiftFaze/Veil/commit/0e17d5c7071a28935387960a3ae31f90535f731e))
* confirming a radio option truncated its text and shifted position ([c6d150c](https://github.com/SwiftFaze/Veil/commit/c6d150cd6e032e96930ff411e6d60d5289a6b1d5))
* correct radio group widget to horizontal Left/Right per real consumer ([5cdb828](https://github.com/SwiftFaze/Veil/commit/5cdb82808979ea4e9dc8aaa55a3d4ac3f4d5cd1c))
* correct table widget off-by-one indexing and step definition issues ([5bff70f](https://github.com/SwiftFaze/Veil/commit/5bff70f61bc2e0056b8537adafc491d9330d639a))
* Enter typed a literal newline instead of moving focus ([0ac0148](https://github.com/SwiftFaze/Veil/commit/0ac01483f2577db85e78e9591925d4245b290723))
* gray scrollbar, reset scroll on rebuild, centralize highlight logic ([9a11e19](https://github.com/SwiftFaze/Veil/commit/9a11e19c14eb27594033e033c267759a764392fa))
* pattern field border thickness squeezed the label's text to nothing ([98826ec](https://github.com/SwiftFaze/Veil/commit/98826eca0c34d444d2e5ee3a036e7fe684fba5d0))
* radio group confirm timing, vertical width, default bottom border ([19d62e3](https://github.com/SwiftFaze/Veil/commit/19d62e364cf03e00f77e5c08f31bd3d5745f27ec))
* resolve remaining Cucumber failures in the UI widget library suite ([1388f62](https://github.com/SwiftFaze/Veil/commit/1388f622cad20655e602ca8a9c8632cab4e66a40))
* scroll the details pane, highlight the whole row not just text ([65bdb05](https://github.com/SwiftFaze/Veil/commit/65bdb05f6b1ef5937082d60bc2bd303cbb4bb37c))
* scrolling to row 0 didn't guarantee the header scrolled into view ([ad1df91](https://github.com/SwiftFaze/Veil/commit/ad1df9165a37b36810800c042a7a88188a01ac60))
* tables rendering completely empty (rows collapsed to zero height) ([c647509](https://github.com/SwiftFaze/Veil/commit/c6475090c14a2ea8aeb875e61ecf087426ddca68))
* unify details-pane navigation, fix stray highlight, bordered tables ([bfbebb8](https://github.com/SwiftFaze/Veil/commit/bfbebb8b61d294ee442cf2ae9110b9a17e07fe69))

## [0.5.0-beta](https://github.com/SwiftFaze/Veil/compare/v0.4.0...v0.5.0-beta) (2026-08-30)


### Features

* delete MenuPanel, open inventory only via the keyboard toggle ([f721373](https://github.com/SwiftFaze/Veil/commit/f7213731153ad8c8fff8c8595770aa59325f0b3c))
* Implement terminal-style UI component framework ([2927d50](https://github.com/SwiftFaze/Veil/commit/2927d504dfa70f11e179b49d0f15d989f1ec3ecc))
* inventory popup's item list stops at the ends instead of wrapping ([b553b8b](https://github.com/SwiftFaze/Veil/commit/b553b8b112fc42561e236b206d892551fe507b9d))
* keyboard Up/Down navigation in the inventory popup, more items ([b722d0f](https://github.com/SwiftFaze/Veil/commit/b722d0f307def0ff0f736dc2f40711d5067d437a))
* layer the inventory popup over the game view instead of the sidebar ([d5f5174](https://github.com/SwiftFaze/Veil/commit/d5f5174246d0b1180f35d3d48a0743ac04cd8dbd))
* scrollable inventory popup with more test items ([32edf47](https://github.com/SwiftFaze/Veil/commit/32edf47aee9d10e2316cd9fc5cf610f0f08f091a))
* split inventory popup into item list + details pane, style scrollbar ([0d392b6](https://github.com/SwiftFaze/Veil/commit/0d392b6a253678ba535262e2adb070ceae220280))
* terminal-style UI component framework ([#36](https://github.com/SwiftFaze/Veil/issues/36)) ([f0bcfa3](https://github.com/SwiftFaze/Veil/commit/f0bcfa388640ce90b0e40daa7edf3de93cce7e03))


### Bug Fixes

* **ci:** bump build-installers JDK to 21 for jpackage --app-content ([bb91b74](https://github.com/SwiftFaze/Veil/commit/bb91b7468ddc40af63744c912cfdd56417a32c4e))
* **ci:** bump build-installers JDK to 21 for jpackage --app-content ([3236ec4](https://github.com/SwiftFaze/Veil/commit/3236ec4f7749ea75ef9e73b0716c72456669c590))
* **ci:** use a PAT for release-please, not GITHUB_TOKEN ([7ac9fbb](https://github.com/SwiftFaze/Veil/commit/7ac9fbb950fd6aeae5e5407ef8d18123b33ddffe))
* **ci:** use a PAT for release-please, not GITHUB_TOKEN ([0daeda2](https://github.com/SwiftFaze/Veil/commit/0daeda27e09b8e8b00cfdef526e83b10c0b83a97))
* define missing restore-game-focus assertion step ([3c02edf](https://github.com/SwiftFaze/Veil/commit/3c02edf6d1437964ca8d324ef6899ebec78cb887))
* make popup modal focus capture actually block the menu ([2478843](https://github.com/SwiftFaze/Veil/commit/2478843f22580d1dd2be244dc85c1ba993cc8a0c))

## [0.3.0-beta.12](https://github.com/SwiftFaze/Veil/compare/v0.3.0-beta.11...v0.3.0-beta.12) (2026-08-30)


### Features

* delete MenuPanel, open inventory only via the keyboard toggle ([f721373](https://github.com/SwiftFaze/Veil/commit/f7213731153ad8c8fff8c8595770aa59325f0b3c))
* Implement terminal-style UI component framework ([2927d50](https://github.com/SwiftFaze/Veil/commit/2927d504dfa70f11e179b49d0f15d989f1ec3ecc))
* inventory popup's item list stops at the ends instead of wrapping ([b553b8b](https://github.com/SwiftFaze/Veil/commit/b553b8b112fc42561e236b206d892551fe507b9d))
* keyboard Up/Down navigation in the inventory popup, more items ([b722d0f](https://github.com/SwiftFaze/Veil/commit/b722d0f307def0ff0f736dc2f40711d5067d437a))
* layer the inventory popup over the game view instead of the sidebar ([d5f5174](https://github.com/SwiftFaze/Veil/commit/d5f5174246d0b1180f35d3d48a0743ac04cd8dbd))
* scrollable inventory popup with more test items ([32edf47](https://github.com/SwiftFaze/Veil/commit/32edf47aee9d10e2316cd9fc5cf610f0f08f091a))
* split inventory popup into item list + details pane, style scrollbar ([0d392b6](https://github.com/SwiftFaze/Veil/commit/0d392b6a253678ba535262e2adb070ceae220280))
* terminal-style UI component framework ([#36](https://github.com/SwiftFaze/Veil/issues/36)) ([f0bcfa3](https://github.com/SwiftFaze/Veil/commit/f0bcfa388640ce90b0e40daa7edf3de93cce7e03))


### Bug Fixes

* define missing restore-game-focus assertion step ([3c02edf](https://github.com/SwiftFaze/Veil/commit/3c02edf6d1437964ca8d324ef6899ebec78cb887))
* make popup modal focus capture actually block the menu ([2478843](https://github.com/SwiftFaze/Veil/commit/2478843f22580d1dd2be244dc85c1ba993cc8a0c))

## [0.3.0-beta.11](https://github.com/SwiftFaze/Veil/compare/v0.3.0-beta.10...v0.3.0-beta.11) (2026-08-29)


### Bug Fixes

* **ci:** bump build-installers JDK to 21 for jpackage --app-content ([bb91b74](https://github.com/SwiftFaze/Veil/commit/bb91b7468ddc40af63744c912cfdd56417a32c4e))
* **ci:** bump build-installers JDK to 21 for jpackage --app-content ([3236ec4](https://github.com/SwiftFaze/Veil/commit/3236ec4f7749ea75ef9e73b0716c72456669c590))

## [0.3.0-beta.10](https://github.com/SwiftFaze/Veil/compare/v0.3.0-beta.9...v0.3.0-beta.10) (2026-08-29)


### Bug Fixes

* **ci:** exempt develop from the branch-name check ([1e583d0](https://github.com/SwiftFaze/Veil/commit/1e583d0ad8a70662ee954dc2968a259eefc916ea))
* **ci:** exempt develop from the branch-name check ([444d450](https://github.com/SwiftFaze/Veil/commit/444d45006645ac23e6dd07152d6b55cbe1db92ee))

## [0.3.0-beta.9](https://github.com/SwiftFaze/Veil/compare/v0.3.0-beta.8...v0.3.0-beta.9) (2026-08-29)


### Features

* add close-milestone skill ([54eb5e5](https://github.com/SwiftFaze/Veil/commit/54eb5e5ef40244fc9884ec499a58c870bf0d4399))
* bundle mods/core into jpackage installer builds ([9442dbb](https://github.com/SwiftFaze/Veil/commit/9442dbb6dce23a03f78bba0e3f34885488a80ec3))
* bundle mods/core into jpackage installer builds ([20eb545](https://github.com/SwiftFaze/Veil/commit/20eb54592153ba7ccfe0e188a448c88b4fb93d2d)), closes [#62](https://github.com/SwiftFaze/Veil/issues/62)


### Bug Fixes

* exclude [@manual-verification](https://github.com/manual-verification) features from Cucumber execution ([e7d5ccf](https://github.com/SwiftFaze/Veil/commit/e7d5ccf88cd011debb81c737a7d56df9dea6757c))

## [0.3.0-beta.8](https://github.com/SwiftFaze/Veil/compare/v0.3.0-beta.7...v0.3.0-beta.8) (2026-08-29)


### Features

* add data-driven quest schema + minimal quest-state tracking ([6a95ea1](https://github.com/SwiftFaze/Veil/commit/6a95ea12091338077ac3485031742a5b107d5b10))
* add data-driven quest schema + minimal quest-state tracking ([30d70fd](https://github.com/SwiftFaze/Veil/commit/30d70fd777ea36f4f7212620e5ee5179bc48bb85)), closes [#52](https://github.com/SwiftFaze/Veil/issues/52)

## [0.3.0-beta.7](https://github.com/SwiftFaze/Veil/compare/v0.3.0-beta.6...v0.3.0-beta.7) (2026-08-29)


### Features

* add audit-project skill for GitHub project board/milestone health checks ([d76baa3](https://github.com/SwiftFaze/Veil/commit/d76baa39a0ea38c7fe7b21a0d3fbb1223f4e2744))
* add audit-project skill for project board/milestone health checks ([f29f92c](https://github.com/SwiftFaze/Veil/commit/f29f92cff5933cec1592372ffc94e31f56dcf729))

## [0.3.0-beta.6](https://github.com/SwiftFaze/Veil/compare/v0.3.0-beta.5...v0.3.0-beta.6) (2026-08-29)


### Features

* data-drive items via JSON schema, wire into InventoryPanel ([4532263](https://github.com/SwiftFaze/Veil/commit/4532263f686bd81608478c7fa9b745a46ef1020c))
* data-drive items via JSON schema, wire into InventoryPanel ([#51](https://github.com/SwiftFaze/Veil/issues/51)) ([e436470](https://github.com/SwiftFaze/Veil/commit/e436470cf702d18b87adb042fdc3251d4f587016))

## [0.3.0-beta.5](https://github.com/SwiftFaze/Veil/compare/v0.3.0-beta.4...v0.3.0-beta.5) (2026-08-29)


### Features

* data-drive PlayerClass via JSON stat-growth curves ([e635b0c](https://github.com/SwiftFaze/Veil/commit/e635b0c5bcaf524b93eba8525c439f14c8900117))
* data-drive PlayerClass via JSON stat-growth curves ([#50](https://github.com/SwiftFaze/Veil/issues/50)) ([c012baa](https://github.com/SwiftFaze/Veil/commit/c012baad0bb9d626f6805dc70578fc19496f333c))

## [0.3.0-beta.4](https://github.com/SwiftFaze/Veil/compare/v0.3.0-beta.3...v0.3.0-beta.4) (2026-08-29)


### Features

* data-drive Tile via JSON definitions + registry ([20d76fc](https://github.com/SwiftFaze/Veil/commit/20d76fc9d70acc82cfd12655088844543392e6be))
* data-drive Tile via JSON definitions + registry ([b2dc66e](https://github.com/SwiftFaze/Veil/commit/b2dc66ec2ca62b78b017b06afa32fdd3ba4b24cf))

## [0.3.0-beta.3](https://github.com/SwiftFaze/Veil/compare/v0.3.0-beta.2...v0.3.0-beta.3) (2026-08-29)


### Features

* generalize BuildingLoader into an external mods/ ModLoader ([01e8f17](https://github.com/SwiftFaze/Veil/commit/01e8f170a06e4a59f267ded7c4478a6c90f1d939))
* generalize BuildingLoader into an external mods/ ModLoader ([67387f4](https://github.com/SwiftFaze/Veil/commit/67387f48b2fed22cfdc63ce32ca80d00d26a470b))

## [0.3.0-beta.2](https://github.com/SwiftFaze/Veil/compare/v0.3.0-beta.1...v0.3.0-beta.2) (2026-08-29)


### Bug Fixes

* correct spec-intent skill's branch prefix from feat/ to feature/ ([b547d85](https://github.com/SwiftFaze/Veil/commit/b547d85d80be9f019c7f5983dd51bc33109e441f))

## [0.3.0-beta.1](https://github.com/SwiftFaze/Veil/compare/v0.3.0-beta...v0.3.0-beta.1) (2026-08-29)


### Features

* add class/stats sandbox (Area 4) ([7a8a769](https://github.com/SwiftFaze/Veil/commit/7a8a769f0954a73a1e8c2235929b95de66221d0f))


### Bug Fixes

* repair README.md corrupted by rename, add missing branch-name CI job ([5608d80](https://github.com/SwiftFaze/Veil/commit/5608d800cbf34270ed049cf730477ef962ce2644))
* repair README.md corrupted by the Emberveil-&gt;Veil rename, add missing branch-name CI job to master ([1af0bd6](https://github.com/SwiftFaze/Veil/commit/1af0bd6ec479f00411359e01cfc25bfe77a7d2e6))
* stop the inventory menu from permanently stealing keyboard focus ([aaea64a](https://github.com/SwiftFaze/Veil/commit/aaea64a72ea291f73b9340b8f3587d03ed59a12e))

## [0.3.0-beta](https://github.com/SwiftFaze/Emberveil/compare/v0.2.0...v0.3.0-beta) (2026-08-28)


### Features

* **ci:** add develop beta channel, fix skipped installer job ([921a6bc](https://github.com/SwiftFaze/Emberveil/commit/921a6bcc7d18ee1c657353a7cf0e89b3610771b4))
* **ci:** build Linux and macOS installers alongside Windows ([ec1f659](https://github.com/SwiftFaze/Emberveil/commit/ec1f659eb816bf3ca8984806e1c1deb2f0d0237e))
* **ci:** build Linux and macOS installers alongside Windows ([76e8835](https://github.com/SwiftFaze/Emberveil/commit/76e8835e6c3d8cbe8ef8bb1e6eb012faa0d7c1fc))
* **ci:** cross-platform installers + develop beta channel ([d2b1cb7](https://github.com/SwiftFaze/Emberveil/commit/d2b1cb701ed9fc88b789f123032fef6c7f72bb75))


### Bug Fixes

* **ci:** correct beta release versioning config field ([f44d9a1](https://github.com/SwiftFaze/Emberveil/commit/f44d9a124819f8ce6f57573d57736b541e4a4324))
* **ci:** correct beta release versioning config field ([358eed6](https://github.com/SwiftFaze/Emberveil/commit/358eed62b82397fc61f68ddb676a7e5aae6abeb4))
* **ci:** exempt release-please branches from branch-name check ([c187629](https://github.com/SwiftFaze/Emberveil/commit/c1876294e6ebb7a968caa6c847a1b966d36dbf25))
* **ci:** exempt release-please branches from branch-name check ([58637dd](https://github.com/SwiftFaze/Emberveil/commit/58637ddc6e13b60f314dec793701a24ca716f9f5))
* **ci:** work around jpackage rejecting major version 0 on macOS ([31aa380](https://github.com/SwiftFaze/Emberveil/commit/31aa380173e00d3dc22405bb1909086add53bb5c))

## [0.3.0](https://github.com/SwiftFaze/Emberveil/compare/v0.2.0...v0.3.0) (2026-08-28)


### Features

* **ci:** add develop beta channel, fix skipped installer job ([921a6bc](https://github.com/SwiftFaze/Emberveil/commit/921a6bcc7d18ee1c657353a7cf0e89b3610771b4))
* **ci:** build Linux and macOS installers alongside Windows ([ec1f659](https://github.com/SwiftFaze/Emberveil/commit/ec1f659eb816bf3ca8984806e1c1deb2f0d0237e))
* **ci:** build Linux and macOS installers alongside Windows ([76e8835](https://github.com/SwiftFaze/Emberveil/commit/76e8835e6c3d8cbe8ef8bb1e6eb012faa0d7c1fc))
* **ci:** cross-platform installers + develop beta channel ([d2b1cb7](https://github.com/SwiftFaze/Emberveil/commit/d2b1cb701ed9fc88b789f123032fef6c7f72bb75))


### Bug Fixes

* **ci:** work around jpackage rejecting major version 0 on macOS ([31aa380](https://github.com/SwiftFaze/Emberveil/commit/31aa380173e00d3dc22405bb1909086add53bb5c))

## [0.2.0](https://github.com/SwiftFaze/Emberveil/compare/v0.1.0...v0.2.0) (2026-08-28)


### Features

* **ci:** add build/release pipeline with Windows installer ([000d523](https://github.com/SwiftFaze/Emberveil/commit/000d52342d2f02cca01bb118a75f53faf5c839d9))
* **ci:** add build/release pipeline with Windows installer ([503aa56](https://github.com/SwiftFaze/Emberveil/commit/503aa5642d3b79a82c7b3b5e71f4bc35f78da724))
