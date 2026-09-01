# Intent: Codex — browsable in-game reference screen for mods/core content

- **Slug(s):** codex-ui (matches `/specs/features/codex-ui.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-31
- **Source:** [GitHub issue #113](https://github.com/SwiftFaze/Veil/issues/113)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [x] Approved by human
- [x] Implemented
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [x] Acceptance tests passing
- [x] Mutation testing passed
- [x] Documentation updated (`docs/`, and the wiki if player-facing)

## Problem

There's no way for a player to look up what an item, tile, class, building,
or quest actually does without external documentation — mods/core already
defines 27 items, 21 tiles, 2 classes, 1 building, and 1 quest, but none of
it is browsable in-game. This is manual today (reading JSON/wiki), and
there's no in-game precedent for a pure reference/lookup screen.

## Scope

- In scope:
  - An in-game overlay screen (X key, currently unbound) that opens over
    the live game, following the same overlay pattern `InventoryPanel`
    already uses (`PopupWidget`/`JLayeredPane` over `GamePanel`, wired in
    `GamePanel.bindKeys()`).
  - A tab/category switcher across 3 of the existing mods/core categories:
    Items, Tiles, Classes.
  - Selecting a tab shows a scrollable list of that category's entries on
    the left (reusing `ListWidget`), with a detail pane on the right
    showing the selected entry's full data — mirroring `InventoryPanel`'s
    list+detail split.
  - Reuse `ModRegistry.getAllItems()` / `getAllPlayerClasses()` directly
    for those 2 tabs. Add a `getAllTiles()` accessor to `ModRegistry`
    (currently only single-lookup `getTile` exists) for the third.
  - Codex is reachable only via the X key (no menu entry) — same as
    Inventory's `I` key today.
  - All entries are shown immediately; no locking/discovery gating.
- Out of scope:
  - Buildings and Quests tabs — deferred for now; not currently tracked
    in a separate issue. (Note: `Building` has a real bug found while
    scoping this — `mods/core/buildings/*.json` already has `name`/`type`
    fields that `ModLoader.loadBuilding` silently drops — see
    Clarifications below for whoever picks this up later.)
  - Enemy/Biome tabs and their underlying mod data categories (neither
    exists as mod data yet — would need new schema/loader/registry) —
    tracked in [#121](https://github.com/SwiftFaze/Veil/issues/121).
  - Discovery/unlock gating (would need new persistence tracking what the
    player has seen) — tracked in
    [#112](https://github.com/SwiftFaze/Veil/issues/112).

## Actors

The player, browsing the codex mid-game as a reference (not from the
title screen).

## Desired behavior

Player presses X during gameplay. A codex overlay opens over the live
game showing a tab switcher for Items/Tiles/Classes.
Selecting a tab populates a scrollable list of that category's entries on
the left; moving through the list updates a detail pane on the right with
the selected entry's full data (name, description, stats, whatever fields
that data type has). Pressing X again (or the standard menu-cancel key)
closes the overlay and returns to the live game, same as closing
Inventory does.

## Constraints / non-functional notes

None beyond the usual (see CLAUDE.md function-length/complexity/coverage
budgets).

## Open questions

None currently — scoped via a grilling session on 2026-08-30 (per issue
#113). The original follow-up issue #112 was split on 2026-08-31 into
Enemies/Biomes data+tabs ([#121](https://github.com/SwiftFaze/Veil/issues/121),
unscheduled) and discovery/unlock gating
([#112](https://github.com/SwiftFaze/Veil/issues/112), narrowed), both
still out of scope for this intent. Four further design questions raised
while drafting the `.feature` file were resolved via a grilling session on
2026-08-31 — see Clarifications below.

## Clarifications

- Q: If the player presses X while Inventory is already open (or I while
  Codex is open), what should happen?
  A: Auto-close the popup that's currently open, then open the one just
  requested — only one popup live at a time, I and X treated
  symmetrically.
  Affects: Pressing the codex key opens the codex overlay; a new
  simultaneous-popup scenario.

- Q: What key(s) cycle between the five Codex tabs, given Up/Down already
  move the entry list and Left/Right already move focus between the list
  and the detail pane?
  A: A dedicated `Tab` (forward) / `Shift+Tab` (backward) binding —
  unclaimed today; reusing Left/Right would collide with their existing
  list<->detail-pane meaning.
  Affects: Selecting a tab shows a scrollable list of that category's
  entries.

- Q: Does the Codex always reopen on the Items tab (first entry), or does
  it remember whichever tab/entry was last viewed within the same play
  session?
  A: Always reset to the Items tab, first entry, on every open — no
  other part of the v1 Codex persists state (locking/discovery
  persistence is explicitly deferred to #112).
  Affects: Pressing the codex key opens the codex overlay.

- Q: `Building` has no id/name/description field today — only a
  `Tile[][] blueprint` and world placement coordinates. What should the
  Buildings tab's list label and detail pane show?
  A: Fix the underlying bug: `mods/core/buildings/*.json` already has
  `id`, `name`, and `type` fields, but `ModLoader.loadBuilding` only
  parses `id` and `tiles` today, silently dropping `name`/`type`. Add
  `name` and `type` fields (and getters) to `Building`, and fix
  `ModLoader.loadBuilding` to parse and pass them through — matching
  every other category's id+name convention. List label = `name` (same
  convention as Items). Detail pane = a field-value table with `ID`,
  `Name`, `Type`, and a computed `Blueprint Size` (e.g. "7 x 7", width x
  height derived from the blueprint array's dimensions — not a stored
  field, since it's already derivable).
  Affects: Selecting a tab shows a scrollable list of that category's
  entries; Selecting an entry in the list populates the detail pane with
  its data.

- Q: Should Buildings actually be a Codex tab in this pass?
  A: No — drop Buildings from scope entirely for now (supersedes the
  previous answer's Buildings-tab design, which is preserved above for
  whoever picks Buildings back up later). Not carved into a separate
  tracked issue at this time.
  Affects: Scope (5 categories -> 3); the tab-list and detail-pane
  scenarios' Examples tables; removes the `getAllBuildings()` accessor
  and `Building` id/name/type fix from this intent's scope.

- Q: With Buildings dropped, should Quests stay in scope?
  A: No — drop Quests too. This pass covers only Items, Tiles, Classes.
  Not carved into a separate tracked issue at this time.
  Affects: Scope (3 categories: Items, Tiles, Classes); the tab-list and
  detail-pane scenarios' Examples tables; removes the `getAllQuests()`
  reuse from this intent's scope.
