# Intent: PMD and JaCoCo as CI-enforced quality gates

- **Slug(s):** pmd-jacoco-quality-gates (matches `/specs/features/pmd-jacoco-quality-gates.feature`)
- **Author:** rwoolley
- **Date:** 2026-09-01

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [x] Approved by human
- [x] Implemented
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5" — **still pending, needs the human**: run `mvn compile exec:java`, open the in-game Codex and Inventory (list+detail layout was consolidated behind a new shared `ListDetailLayoutUtility`) and Settings/Keybinds screens, confirm navigation still feels right
- [x] Acceptance tests passing — N/A by design: `.feature` file is tagged `@manual-verification` (build-pipeline concern, no Java code path — same precedent as `installer-mods-bundling.feature`)
- [x] Mutation testing passed — ran `mvn org.pitest:pitest-maven:mutationCoverage`: 76% mutation score, 80% test strength on PIT's existing target classes (informational per Step 6, not a gate; not degraded by this change)
- [x] Documentation updated (`docs/`, and the wiki if player-facing) — `.claude/workflow.md` coverage wording corrected, `docs/testing.md` gained a "Code quality gates" section; no wiki update needed (no player-facing game data changed)

## Problem

`.claude/workflow.md` documents mechanical budgets for AI-produced code —
max function length (40 lines), max cyclomatic complexity (8), max 4
params, and 85% coverage on changed files — and states these are
"enforced by CI, not by asking the agent to try to keep things clean." In
reality, nothing enforces them: `pom.xml` has no PMD, JaCoCo, Checkstyle,
or SpotBugs plugin, and `ci.yml`'s `build-and-test` job only runs
`mvn verify` (Surefire unit + Cucumber, Failsafe integration) with no
complexity/coverage/duplication gate. PIT mutation testing is configured
in `pom.xml` but is a manual Step 6 command, never run by CI. The doc's
claim and the actual build have drifted apart.

## Scope

- In scope:
  - Add PMD as a Maven plugin bound to `mvn verify`, configured to fail
    the build on: cyclomatic complexity > 8, function length > 40 lines,
    more than 4 parameters, and CPD duplicate-code detection at the
    default 100-token threshold.
  - Add JaCoCo as a Maven plugin bound to `mvn verify`, configured to
    fail the build if repo-wide line coverage drops below 85%.
  - Both gates are enforced immediately (not report-only) — fix any
    violations PMD/JaCoCo turn up against the existing codebase as part
    of this issue, so `mvn verify` is green with the gates active by the
    time it's done.
  - Exclude pure Swing layout/wiring classes with no real branching logic
    from both PMD's complexity/coverage-relevant rules and JaCoCo's
    coverage requirement. Derive the exclusion list via the hybrid method
    settled in Clarifications below: start from PIT's existing
    `<targetClasses>` allowlist in `pom.xml`, but pull back in (i.e. do
    NOT exclude) any class that already has a dedicated `*Test.java` file
    — a written test is itself proof the class has real logic worth
    covering. Untested classes only join the exclusion list after
    confirming they're genuinely pure layout, per the same reasoning PIT
    already uses: classes like `NorthPanel` are pure construction/layout
    with no logic to unit test, so a unit test asserting on them would be
    a hollow re-statement of the source, not a real check. Visual
    correctness for that class of code is already covered separately
    (image-render check + Step 4.5 human playtest per
    `docs/ui-verification.md`).
  - Drive-by fix while touching this exact area of `pom.xml`: remove
    PIT's two stale `<targetClasses>` entries (`com.swiftfaze.veil.ui.SelectableMenu`,
    `com.swiftfaze.veil.ui.MenuPanel`) — both classes no longer exist in
    the codebase, superseded by the widget-framework work in
    `ui-component-framework.md`. This does not change PIT's behavior
    (referencing a nonexistent class was already a no-op), it just stops
    the config from lying about what it covers.
  - Correct `.claude/workflow.md`'s coverage wording: it currently says
    "85% coverage on changed files," but plain JaCoCo enforces coverage
    repo-wide, not via a diff against changed files (true
    changed-files-only enforcement needs extra tooling, out of scope
    here). Update the doc to say repo-wide so it matches what's actually
    enforced.
- Out of scope:
  - Moving PIT mutation testing into CI as a gate — stays a manual Step 6
    command, untouched by this issue.
  - CRAP score (complexity × coverage composite) — no clean modern tool
    for it; Crap4j is unmaintained and built for Cobertura, not JaCoCo.
    Not pursued.
  - SpotBugs, SonarQube, or any other static analysis tool beyond PMD —
    judged as more setup/maintenance cost than this project's size
    justifies.
  - True changed-files-only coverage enforcement (would need a
    diff-coverage tool beyond JaCoCo) — repo-wide is the accepted
    interpretation for this issue.
  - Writing new unit tests for pure-Swing-layout classes just to satisfy
    a coverage number on classes with no real logic — that's a separate,
    bigger initiative if ever pursued, not a side effect of this issue.

## Actors

Whoever's writing or reviewing code in this repo (human or AI agent) —
this issue makes the complexity/size/coverage/duplication budgets already
documented in `.claude/workflow.md` actually mechanically checked,
instead of relying on the agent's own judgment to follow them.

## Desired behavior

`mvn verify` fails if any (non-excluded) function exceeds cyclomatic
complexity 8, exceeds 40 lines, takes more than 4 parameters, if PMD's
CPD finds a duplicate block at or above the default 100-token threshold,
or if repo-wide line coverage falls below 85%. Pure Swing layout/wiring
classes (matching the same effective exclusion PIT's `<targetClasses>`
allowlist already produces) are excluded from the complexity and coverage
checks the same way they're already excluded from mutation testing.
`.claude/workflow.md`'s coverage line reads "85% repo-wide line
coverage" (or equivalent), not "on changed files."

## Constraints / non-functional notes

None beyond the usual function-length/complexity/coverage budgets this
issue is itself implementing.

## Open questions

None — scope settled via a grilling session on 2026-09-01 (see the
source issue), plus a second grilling round during spec drafting (see
Clarifications below).

## Clarifications

- Q: PIT's `<targetClasses>` allowlist in `pom.xml` (the thing this
  intent doc says to mirror for the new exclusion list) is stale and
  contradicts the "pure layout, no real branching logic" exclusion
  criterion — `ListWidget` (146 lines), `TableWidget` (351 lines),
  `RadioGroupWidget` (285 lines), and `SliderWidget` (91 lines) all
  already have dedicated `*Test.java` files (real logic being tested
  today) yet none are in PIT's list, and it also still names two classes
  that no longer exist (`SelectableMenu`, `MenuPanel`). How should the
  actual PMD/JaCoCo exclusion list be derived: (a) literally mirror PIT's
  list as-is, (b) build a fresh list via per-class judgment across the
  whole codebase, or (c) hybrid — start from PIT's list but pull back in
  any class that already has a dedicated unit test file, excluding
  untested classes only after confirming they're genuinely pure layout?
  A: (c), the hybrid approach.
  Affects: Scope (exclusion-list derivation), the "Excluded pure-layout
  classes are not held to either gate" scenario in
  `pmd-jacoco-quality-gates.feature`.

- Q: Should this issue also fix PIT's two stale `<targetClasses>` entries
  (`SelectableMenu`, `MenuPanel`) while touching this exact area of
  `pom.xml`, or leave PIT's own config untouched and file a follow-up
  instead?
  A: Fix it here — drive-by deletion of the two dead entries, since this
  issue is already editing this exact list for the new exclusion-list
  work.
  Affects: Scope (in-scope list now includes the PIT config drive-by
  fix).
