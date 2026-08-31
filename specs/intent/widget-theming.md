# Intent: Mod-driven color theming for the UI widget library

- **Slug(s):** widget-theming (matches `/specs/features/widget-theming.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-31

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [x] Approved by human (approved implementing directly per user request, 2026-08-31 — see Clarifications)
- [ ] Implemented — reopened 2026-08-31: scope expanded to all UI colors (see Clarifications); the
      first implementation pass (10 WidgetTheme colors + Settings screen row only) is done, the
      wider sweep is not yet
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"; not yet done, still required before this ships
- [ ] Acceptance tests passing — needs re-verification after the wider sweep
- [ ] Mutation testing passed — needs re-run after the wider sweep
- [ ] Documentation updated (`docs/`, and the wiki if player-facing) — needs a pass after the wider sweep

## Problem

Widget colors are hardcoded as `static final Color` constants in
`WidgetTheme` (`src/main/java/com/swiftfaze/veil/ui/widget/WidgetTheme.java`),
so reskinning the UI widget library requires editing and recompiling Java.
Mod content (tiles, buildings, classes, items, quests) is already fully
JSON-driven and loaded via `ModLoader` with a consistent
id/collision/`overrides` model (see `ModLoader.load()` and
`registerWithCollisionCheck`), but color theming has no equivalent, despite
the widget library being the natural next candidate for that same pattern.

## Scope

- In scope:
  - A mod-shaped theme file loaded from `mods/<modid>/themes/*.json` — a
    directory of files, one theme per file, matching the `tiles/`/
    `items/`/`quests/` directory-of-many-files convention (not the
    `stats.json` singleton, superseding an earlier draft of this doc that
    proposed a single `theme.json` per mod) — loaded by `ModLoader`
    alongside the existing content types, following the same
    id/collision/`"overrides"` rules as tiles/buildings/classes/items/
    quests (`registerWithCollisionCheck`).
  - A default theme shipped in `mods/core/themes/default.json` (file id
    `core:default`) defining **all 11** colors `WidgetTheme` exposes:
    `SELECTED_HIGHLIGHT`, `SELECTED_TEXT`, `NORMAL_TEXT`, `DIMMED_TEXT`,
    `BACKGROUND`, `INVALID_HIGHLIGHT`, `VALID_HIGHLIGHT`,
    `TABLE_HEADER_BACKGROUND`, `BORDER`, `SCROLLBAR_THUMB`, `ACCENT` —
    using the same `{r, g, b}` JSON color shape tiles already use
    (`ModLoader.readColor`). No color may be left hardcoded in any UI file
    once this lands; every one of the 11 must have an entry in the default
    theme.
    - Note: the originating issue (#106) named only 7 colors, all still
      present under their original names except `TABLE_BORDER` (see the
      rename below). It was filed mid-day on 2026-08-30, before
      `DIMMED_TEXT`, `VALID_HIGHLIGHT`, and `SCROLLBAR_THUMB` were added to
      `WidgetTheme` in later commits the same day. Confirmed via grilling
      session on 2026-08-31: migrate all 10 colors that existed at
      implementation time, not just the 7 named in the issue.
    - **`TABLE_BORDER` renamed to `BORDER`** (2026-08-31, see
      Clarifications): the color was never table-specific — it's the
      general widget-library border color, already used by
      `CompactPopupWidget` and `RadioGroupWidget` before this rename, and
      now also used across every panel border in scope below. Keeping the
      name `TABLE_BORDER` while broadening its use this far would be a
      misleading name.
    - **`ACCENT` added** (2026-08-31, see Clarifications): the widget
      library's original `SELECTED_HIGHLIGHT` color (`#eeb392`) before an
      earlier commit changed selection highlighting to neutral gray. That
      same `#eeb392` value is still hardcoded as an accent color in two
      places brought into scope below (`NorthPanel`'s title,
      `ClassSandboxPanel`'s selected-row color) — this captures it as its
      own named theme color rather than leaving it as a hardcoded
      leftover.
  - **Every hardcoded UI color in `src/main/java/.../ui/**` and
    `.../ui/widget/**`, plus the dev `ClassSandboxPanel`, is replaced with
    the matching `WidgetTheme` field** (2026-08-31, see Clarifications —
    this supersedes the "legacy screen-chrome panels" exclusion
    originally below). Gameplay/world rendering
    (`entities/player/Player.java`, `world/WorldScene.java`,
    `game/GamePanel.java`) is explicitly excluded — those colors are
    gameplay content, not UI chrome, and stay as they are.
  - `WidgetTheme` is populated from the loaded default theme at startup
    instead of hardcoding `Color` constants; every consumer keeps
    referencing its static fields exactly as before (same field names —
    only where the values come from changes, plus the `TABLE_BORDER` ->
    `BORDER` rename and new `ACCENT` field above).
  - Any mod (including a user-authored one) can define its own file(s)
    under `themes/` following the same schema, proving the "user can
    create their own theme" pattern end-to-end, even without a way to
    activate a non-default one yet.
  - A **purely visual** "Theme" entry on the main Settings screen
    (`SettingsScreenPanel`): a selector listing the loaded theme(s) by
    id/name from the new theme registry. Selecting a non-default entry
    does not actually switch anything — there's no settings/config
    persistence system to make that stick (see the activation note below,
    carried over unchanged from issue #106). This is a placeholder
    affordance the user asked for directly (2026-08-31) alongside the
    intent doc draft, distinct from real activation/switching, which
    stays out of scope.
- Out of scope:
  - Fonts — colors only for this issue.
  - Gameplay/world rendering colors (`Player`'s glyph color, `WorldScene`'s
    fallback tile color, `GamePanel`'s own viewport chrome) — not UI, per
    the user's 2026-08-31 clarification narrowing "everything" to "UI"
    specifically.
  - Theme activation/switching (choosing which loaded theme is actually
    applied) — no settings/config system exists yet anywhere in the
    codebase. v1 only needs the default theme to load and render correctly
    through the new pipeline, plus the visual-only settings entry above.

## Actors

- The game engine at startup (loads the default theme via `ModLoader`).
- Modders/players authoring their own color theme as a mod.

## Desired behavior

On startup, `ModLoader` loads every `*.json` file under each mod's
`themes/` directory the same way it loads tiles/classes/items/quests,
building a theme registry keyed by theme id (namespaced like existing
content, e.g. `core:default`) with the same collision/`overrides`
semantics as `registerWithCollisionCheck`. The core mod's default theme
(`mods/core/themes/default.json`, id `core:default`) — defining all 11
widget colors — loads and applies to the widget library so rendering is
unchanged from today's hardcoded output (a visual no-op migration), and
every in-scope UI file (see Scope) reads its colors from `WidgetTheme`
instead of hardcoding a literal. A second mod could ship its own file
under `themes/` (same schema, distinct id) and it would load without
error, proving custom themes are possible, even though nothing yet picks
a non-default one to actually display.

On the Settings screen, a new "Theme" row lists the theme registry's
entries (initially just `core:default`) via the existing widget library
(e.g. a `RadioGroupWidget`, consistent with other settings rows). Choosing
an entry other than the currently-applied one has no functional effect —
no config system exists to persist or apply the choice — so this row is
explicitly cosmetic until a real settings/config system and activation
mechanism exist.

Failure handling follows the existing `ModLoadException` convention used by
`loadTile`/`loadClass`/`loadItem`/`loadQuest`: a malformed theme file
(missing a required color key, bad `{r,g,b}` shape, etc.) throws
`ModLoadException` identifying the offending file, the same as any other
mod content type.

## Constraints / non-functional notes

None beyond the usual (see CLAUDE.md function-length/complexity/coverage
budgets).

## Open questions

None — see the Clarifications section for every fork raised so far
(color-key set, settings-screen Theme row, theme file layout, and UI
color-cleanup scope) and how each was resolved.

## Clarifications

- Q: Migrate only the 7 `WidgetTheme` colors literally named in issue
  #106, or all 10 colors the class currently defines (3 were added in
  commits after the issue was filed)?
  A: Migrate all 10.
  Affects: general (default theme's required color-key set; the "no
  color may be left hardcoded" scenario in widget-theming.feature)

- Q: (raised directly by the user, not via grilling) Should the Settings
  screen get a visual "Theme" entry?
  A: Yes — add it, but purely visual; it doesn't need to actually switch
  anything, matching the existing "Font" row's precedent (a cycling
  `RadioGroupWidget` value with no real backing system yet — see
  `specs/features/settings-screen.feature`'s Non-goals). Implemented as a
  fixed placeholder list ("Default", "Midnight", "Sunrise") built the same
  way as the Font row's fixed list, not wired to the real mod-driven theme
  registry from this same feature — that registry only has one entry
  (`core:default`) until a second theme mod actually ships, and real
  activation is out of scope regardless (see Scope's "Out of scope").
  Affects: settings-screen.feature (new "Theme" settings item, inserted
  after "Font"; the "eleven settings items" scenario supersedes the
  earlier "ten settings items" one)

- Q: (raised directly by the user, 2026-08-31) File layout for theme
  content — a single `theme.json` per mod (this doc's original draft,
  matching issue #106's literal wording and the `stats.json` singleton
  pattern), or a `themes/` directory of files per mod (matching tiles/
  items/quests' directory-of-many-files convention)?
  A: A `themes/` directory; the core mod's default theme file is named
  `mods/core/themes/default.json` (id `core:default`).
  Affects: general (ModLoader's theme-loading shape moves from a
  loadStatRegistry-style single-file check to a loadTiles-style directory
  scan); widget-theming.feature's Given steps and its Risks note about
  the loader shape

- Q: (raised directly by the user, 2026-08-31, after the first
  implementation pass landed) "All colors need to be replaced ... I still
  see a lot of Color.BLACK for example" — how far should the hardcoded-
  color cleanup go? Options ranged from just the widget-library core
  (`Widget`/`PopupWidget`, which hardcode `Color.BLACK` instead of
  referencing `WidgetTheme.BACKGROUND`) up to literally every `Color`
  reference in the codebase, including gameplay/world rendering.
  A: "Everything that is UI" — every file under `ui/` and `ui/widget/`,
  plus the dev `ClassSandboxPanel`. Explicitly NOT gameplay/world
  rendering (`Player`, `WorldScene`, `GamePanel`) when those were named as
  an example of going too far.
  Affects: general — this reverses the earlier "legacy screen-chrome
  panels are out of scope" exclusion above, renames `TABLE_BORDER` to
  `BORDER` (it was already used well beyond tables —
  `CompactPopupWidget`, `RadioGroupWidget` — before this change), and adds
  a new `ACCENT` color (`#eeb392`) to capture a hardcoded leftover from
  before `SELECTED_HIGHLIGHT` changed to neutral gray, still used by
  `NorthPanel` and `ClassSandboxPanel`. widget-theming.feature's color
  count/list and `mods/core/themes/default.json` both move from 10 keys
  to 11.
