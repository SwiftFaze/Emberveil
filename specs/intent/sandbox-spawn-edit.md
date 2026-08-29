# Intent: Sandbox entity spawning and live stat editing

- **Slug(s):** `sandbox-spawn-edit` (matches `/specs/features/sandbox-spawn-edit.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-29
- **Source:** [GitHub issue #27](https://github.com/SwiftFaze/Veil/issues/27)
- **Depends on:** [GitHub issue #28](https://github.com/SwiftFaze/Veil/issues/28) /
  `specs/intent/sandbox-dev-console.md` — this intent adds a second
  provider to that framework, so it can't be spec'd against real code
  until the provider abstraction exists.

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

Once the sandbox can browse class/stat data (#28), the next gap is that
you still can't put an actual `Player` in front of you to poke at —
today's sandbox only shows the *computed* stats for a class in the
abstract, not a live entity whose fields you can change and watch update.
Checking "what does a Warrior look like at HP 40 instead of 50" currently
means editing JSON, rebuilding, and relaunching the sandbox.

Item, combat, and quest systems don't exist in this codebase yet (there is
no `items`, `combat`, or `quests` package), so "spawn an item," "test
fighting," and "trigger a quest" — all mentioned when this idea first came
up — aren't implementable against anything real today. This intent
therefore only covers the one piece that is implementable now: spawning a
player entity and live-editing its stats. The rest stays a named backlog
item until those systems land (see Out of scope).

## Scope

**In scope:**
- A new "Player" provider in the dev-console framework (#28): spawns a
  `Player` with a chosen class at a chosen starting position, as a second
  registered provider alongside "Classes".
- Live editing of the spawned player's `Stats` fields (attack power,
  defense, max HP, max mana) from the provider panel, with the display
  updating immediately as values change.
- Editing is in-memory only, scoped to the running sandbox instance.

**Out of scope:**
- Item spawning or equipping — no item system exists in the codebase.
- Combat simulation between spawned entities — no combat system exists.
- Quest triggering/inspection — no quest system exists.
- Monster/NPC spawning — no monster entity type exists yet (`Player` is
  currently the only spawnable entity).
- Any persistence of a spawned/edited player to a save file.
- Anything reachable from the real game (`Main`) — stays confined to the
  standalone `Sandbox.run.xml` entry point, same constraint as #28.

The item/combat/quest pieces are not being deferred as "later work within
this intent" — they need their own intent docs once those game systems
exist, since there's nothing yet to spawn or fight. Re-scope this doc (or
split a new one) at that point rather than stretching it to cover them.

## Actors

Solo developer (rwoolley), using the sandbox to inspect and tune a live
player entity's stats without playing the game or hand-editing JSON.

## Desired behavior

- Selecting "Player" from the dev-console's search list opens a panel to
  choose a class and a starting position, then spawns a `Player` with
  that class's base stats applied.
- The panel lists the spawned player's editable `Stats` fields (attack
  power, defense, max HP, max mana); navigating to a field and adjusting
  it (exact input method TBD — see Open questions) changes that field's
  value and the displayed value updates immediately.
- Editing a stat only affects the in-memory spawned player; nothing is
  written to disk and no other part of the sandbox is affected.
- Returning to the top-level search list (same back action as #28)
  discards the spawned player — nothing is preserved between sandbox
  sessions or between opening/closing the provider.

## Constraints / non-functional notes

- Cannot be implemented before #28's provider framework lands — this
  intent's `.feature` file should not be spec'd against real code until
  that dependency is merged.
- Java 17 Swing, no new dependency.
- Standard function/complexity budgets from the global workflow apply (max
  40 lines, complexity 8, 4 params, 85% line coverage on changed files).

## Open questions

- Input method for editing a numeric stat field: increment/decrement with
  Left/Right (mirrors the existing Up/Down list navigation) vs. typing a
  value directly?
- Should starting position be a free-form coordinate entry, or a picker
  constrained to valid tiles in whatever world/scene is loaded?
- Are attributes (`Stats`' six fixed fields, per the restructure-solid-base
  intent) all editable, or only the four derived combat stats shown today
  (ATK/DEF/HP/MP)?
- Does spawning a player need a world/scene loaded at all, or can it work
  against a bare `Stats`/`Player` object with no map underneath?
