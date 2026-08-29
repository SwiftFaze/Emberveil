# Intent: Test coverage for ClassSandboxPanel and BuildingException failure path

- **Slug(s):** class-sandbox-panel-selection, building-loader-failure-path
  (matches `/specs/features/class-sandbox-panel-selection.feature` and
  `/specs/features/building-loader-failure-path.feature`) — two distinct,
  unrelated concepts covered by one issue, so they produce two separate
  `.feature` files rather than one bundled file.
- **Author:** rwoolley
- **Date:** 2026-08-29
- **Source:** [GitHub issue #43](https://github.com/SwiftFaze/Veil/issues/43)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [x] Approved by human (user directed full autonomous pipeline for this
      issue, waiving the normal human-approval pause)
- [x] Implemented
- [x] Manually playtested (human) — not applicable: no observable
      behavior changed (test-coverage only), and the user directed the
      full autonomous pipeline for this issue, waiving this human gate
- [x] Acceptance tests passing
- [x] Mutation testing passed
- [x] Documentation updated (`docs/`, and the wiki if player-facing) — no
      update needed; no public API, domain concept, or player-facing
      behavior changed

## Problem

Two unrelated areas of the codebase have zero test coverage for real,
existing behavior — found while auditing test coverage during the
ui-panel-rendering-and-composition work (#33), but out of that issue's
scope:

- `ClassSandboxPanel` (`src/main/java/com/swiftfaze/veil/sandbox/ClassSandboxPanel.java`)
  is a Swing panel with its own key-binding wiring (`bindKeys()`, Up/Down
  moving `SelectableMenu`'s selection) and display logic (`refresh()`,
  which recolors the selected class label to `#eeb392` and formats the
  stats line). None of this is exercised by any test —
  `class-stats-sandbox.feature` and `ClassStatsSandboxSteps.java` only
  call `ClassSandboxModel` directly, never the panel itself. No
  `ClassSandboxPanelTest` exists.
- `BuildingException` (`src/main/java/com/swiftfaze/veil/exceptions/BuildingException.java`)
  is thrown by `BuildingLoader.load(...)` (`BuildingLoader.java:44-46`)
  when a building resource fails to load, but is never thrown or asserted
  in any test. `BuildingLoaderIT` only covers the happy path — no test
  feeds it a missing/malformed resource to verify the exception actually
  fires.

Both are silent gaps: a regression in either area (broken key handling in
the sandbox, or a swallowed/changed exception in the loader) would pass
`mvn verify` today with nothing catching it.

## Scope

- In scope:
  - Test coverage for `ClassSandboxPanel`: pressing the Up/Down-bound
    actions (`Keybindings.ACTION_MENU_UP`/`ACTION_MENU_DOWN`, wired via
    `bindKeys()`) moves `SelectableMenu`'s selection, and `refresh()`
    recolors the newly selected label to the accent color (`#eeb392`),
    reverts the previously selected label to white, and updates the
    stats label's text to the newly selected class's computed stats
    (`ATK %d  DEF %d  HP %d  MP %d` format).
  - Test coverage for `BuildingLoader`'s failure path: loading a missing
    or malformed building resource throws `BuildingException` (wrapping
    the underlying cause), via a new unit test.
- Out of scope:
  - `DrawableAsciiEntity.render(Graphics2D, ...)` on `Player` and
    `WorldScene`/`Tile` — inherent to being a pixel-drawing method, not
    worth chasing the same way as the two gaps above.
  - Any functional/behavior change to `ClassSandboxPanel` or
    `BuildingLoader` — this is test-coverage only.
  - UI panel composition/rendering gaps (EastPanel's own panel
    properties, `PlayerInfoPanel`'s border, `NorthPanel`'s title color
    override, `cancelMenu()` acceptance coverage) — those are tracked
    under #33's own scope discussion, not here.

## Actors

- Developers using the class/stats sandbox tool (`ClassSandbox`) to check
  class balance — a regression in selection or stat display would go
  unnoticed without this coverage.
- Developers loading building blueprints via `BuildingLoader` — a broken
  or malformed building JSON should reliably throw `BuildingException`,
  not silently misbehave or throw something else.

## Desired behavior

- Given the class sandbox panel showing "Warrior", "Mage", ... labels
  with "Warrior" selected (white text, "Warrior" itself at `#eeb392`),
  when the Down-bound action fires, then "Mage"'s label becomes
  `#eeb392`, "Warrior"'s label reverts to white, and the stats label
  shows Mage's computed stats.
- Given the class sandbox panel with some class other than the first
  selected, when the Up-bound action fires, then the selection moves to
  the previous class in the list (wrapping to the last class if already
  at the first), and labels/stats update the same way.
- Given a missing building resource file, when `BuildingLoader.load(...)`
  is called with it, then a `BuildingException` is thrown wrapping the
  underlying cause.
- Given a malformed (unparseable) building resource file, when
  `BuildingLoader.load(...)` is called with it, then a
  `BuildingException` is thrown wrapping the underlying cause.

## Constraints / non-functional notes

None beyond the repo's standard testing/complexity budgets. Both existing
classes (`ClassSandboxPanel`, `BuildingLoader`) are unchanged by this
work — tests only.

## Open questions

None — both are straightforward test-coverage additions for existing,
unchanged behavior.
