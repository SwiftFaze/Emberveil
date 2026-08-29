# Intent: Sandbox dev-console framework

- **Slug(s):** `sandbox-dev-console` (matches `/specs/features/sandbox-dev-console.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-29
- **Source:** [GitHub issue #28](https://github.com/SwiftFaze/Veil/issues/28)
- **Follow-up:** [GitHub issue #27](https://github.com/SwiftFaze/Veil/issues/27) tracks spawning/editing
  entities, items, and live stats — deliberately out of scope here.

## Status

- [x] Intent drafted
- [ ] Spec drafted (`.feature` file)
- [ ] Approved by human
- [ ] Implemented
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [ ] Acceptance tests passing
- [ ] Mutation testing passed
- [ ] Documentation updated (`docs/`, and the wiki if player-facing)

## Problem

`ClassSandbox` (Area 4 of the solid-base restructure) only knows how to
browse player classes and their computed stats — the menu, the arrow-key
wiring, and the stats display are all hardcoded to that one concern. There
is no game system in the codebase yet for items, skills, combat, or
quests, but several are planned, and each one will eventually want its own
debug/inspection panel. Today, adding a second panel means duplicating
`ClassSandboxPanel`'s menu/keybinding/rendering boilerplate rather than
plugging into anything reusable, and there's no way to jump straight to a
panel by name — you scroll a flat list.

## Scope

**In scope:**
- Generalize the sandbox into a small, searchable dev-console shell: a
  top-level menu of registered "providers" that can be filtered by typing
  (substring match), navigated with the existing up/down keybindings, and
  opened with Enter.
- Define a minimal provider abstraction (name + the panel it opens) so a
  future provider (items, monsters, quests, ...) is a small addition, not
  a rewrite of the menu/search/keybinding plumbing.
- Refactor `ClassSandboxModel`/`ClassSandboxPanel` to fit that provider
  shape as the framework's first (and for now only) registered provider,
  preserving its current behavior (class list, computed stats display)
  exactly — no behavior change to what it already does.
- Remains launched only via the existing standalone `Sandbox.run.xml` run
  configuration, entirely outside `Main`/the real game entry point.

**Out of scope:**
- Spawning or editing anything (players, items, stats, monsters), combat
  simulation, or quest triggering — tracked in issue #27.
- Any in-game hotkey/overlay access — this stays a separate dev-only
  entry point, never reachable from a running game session.
- Persisting sandbox state, or any interaction with save files.
- New item/skill/combat/quest systems — none exist yet and none are
  introduced here.

## Actors

Solo developer (rwoolley), running the sandbox outside the game to inspect
class/stat data today, and to have a place to plug in inspection panels
for future systems (items, monsters, quests) without re-deriving the
menu/search/keybinding scaffolding each time.

## Desired behavior

- Launching the sandbox shows a searchable list of provider names (today:
  just "Classes"). Typing filters the visible list by substring match,
  case-insensitively; Up/Down (existing `Keybindings.MENU_UP`/`MENU_DOWN`)
  moves the selection within the filtered results; Enter opens the
  selected provider's panel, replacing the list view.
- The opened "Classes" provider behaves exactly as `ClassSandboxPanel`
  does today: Up/Down browses class names, and the computed
  ATK/DEF/HP/MP stats for the selected class are shown below the list.
- An escape/back action returns from a provider's panel to the top-level
  search list (exact key TBD — see Open questions).
- With zero results matching the current search text, the list is empty
  and no selection/Enter action does anything (no crash, no exception).
- Adding a second provider later means implementing the provider
  interface and registering it — no changes to the search/filter/menu
  code itself.

## Constraints / non-functional notes

- Java 17 Swing, consistent with the rest of the codebase — no new UI
  toolkit or dependency.
- Must not touch `Main.java` or any code path reachable from the actual
  game window; the sandbox's isolation from production code is a
  deliberate existing property (see restructure-solid-base intent, Area
  4) and this change must preserve it.
- Standard function/complexity budgets from the global workflow apply (max
  40 lines, complexity 8, 4 params, 85% line coverage on changed files).

## Open questions

- Exact keybinding for "back" from an open provider panel to the
  top-level search list (reuse an existing `Keybindings` entry, e.g. the
  one the inventory menu uses to close, or add a new one?).
- Should the provider list show anything when there's only one provider
  (today), or should a single-provider sandbox open straight into it and
  only show the searchable list once a second provider is registered?
