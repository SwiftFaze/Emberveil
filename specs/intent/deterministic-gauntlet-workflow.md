# Intent: Align agentic workflow with Uncle Bob's deterministic-gauntlet approach

- **Slug(s):** deterministic-gauntlet-workflow (matches `/specs/features/deterministic-gauntlet-workflow.feature`)
- **Author:** rwoolley
- **Date:** 2026-09-01
- **Source:** [GitHub issue #128](https://github.com/SwiftFaze/Veil/issues/128)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [x] Approved by human
- [x] Implemented
- [x] Manually playtested (human) — N/A, no game behavior changed
- [x] Acceptance tests passing — N/A by design (`.feature` file tagged `@manual-verification`, same as `pmd-jacoco-quality-gates.feature`)
- [x] Mutation testing passed — mutation score 76% (344/450 mutations killed, unaffected by this change)
- [x] Documentation updated (`docs/testing.md`, `docs/architecture.md`, `.claude/workflow.md`, `CLAUDE.md`, `specs/intent/README.md`; no wiki update needed)

## Problem

`.claude/workflow.md` and root `CLAUDE.md` define this repo's agentic
pipeline (intent -> Gherkin spec -> human approval -> Haiku implementation
-> acceptance tests -> mutation testing -> docs). An analysis against
Robert C. Martin's own description of how he actually runs agentic coding
(deterministic gauntlets over steering docs, CRAP/mutation-test gates,
module dependency enforcement, agile slice-by-slice over upfront
spec-complete planning) found several gaps and a few structural
divergences worth correcting. This issue tracks bringing the workflow
closer to that model, on top of the PMD/JaCoCo gate work in
`pmd-jacoco-quality-gates.md` (#123), which has already landed on
`develop`.

## Scope

- In scope — additions (things he does that we don't yet):
  - A "run PMD -> fix -> rerun until clean" loop wired explicitly into the
    Step 4 implementation handoff — not just a one-time self-check against
    the `uncle-bob-craft` checklist, but a deterministic loop the agent
    must satisfy before moving to acceptance tests. PMD/JaCoCo are already
    CI-enforced gates as of #123, so this item is about documenting and
    wiring the loop into the Step 4 handoff prompt/process, not about
    adding the gates themselves.
  - A module/package dependency-direction check (e.g. ArchUnit) enforcing
    the layering `docs/architecture.md` already documents (engine /
    widgets / screens), so a violation fails the build instead of relying
    on the SLAP rule, which is function-level only and not mechanically
    checked at the package level.
  - Documented guidance for running independent Step 4 implementations in
    parallel (separate worktrees / separate fresh Haiku agents) when
    multiple tickets are ready at once, rather than only as an edge-case
    exception.
  - Explicit note that the complexity/coverage thresholds in the
    Constraints table are a tunable dial for agent-authored code (he moved
    his own CRAP threshold from 4, tuned for humans, toward 6-8 for
    agents, and says he hasn't found the ceiling) rather than a fixed
    constant — document the rationale so future changes to these numbers
    are a deliberate experiment, not drift.
- In scope — corrections/removals (things he explicitly doesn't do, that
  our workflow currently assumes):
  - Reconsider requiring a fully human-approved Gherkin spec before any
    implementation code is written for every non-trivial feature. He
    tried this in 2025 and calls it "always a disaster" — the plan
    doesn't survive contact with implementation. Evaluate a lighter agile
    loop (implement a slice, look at the result, reconcile the spec,
    continue) as an option for smaller features, keeping the full intent
    -> spec -> approval gate for genuinely high-risk work
    (auth/payments/data-integrity/public API, matching the existing Opus-
    bump criteria).
  - Reduce the ceremony around `specs/intent/*.md` as a permanent, indexed
    artifact. He treats specs as ephemeral — "there is no equivalent to
    source code" for a persisted plan doc — and the thing that should
    persist is the deterministic check, not the prose. `specs/features/*.feature`
    stays as-is since it's executable via Cucumber (a real deterministic
    check, not documentation), but `specs/intent/*.md` doesn't need the
    same long-term maintenance weight.
- Out of scope:
  - Any actual game feature work.
  - The PMD/JaCoCo gate implementation itself (already delivered by
    `pmd-jacoco-quality-gates.md` / #123).

## Actors

Whoever runs this repo's agentic pipeline (currently just the maintainer,
via Claude Code sessions).

## Desired behavior

`.claude/workflow.md` and `CLAUDE.md` reflect the corrected pipeline:

- A real fix-loop for static analysis (PMD) wired into Step 4, run before
  acceptance tests begin, not just a one-time self-check.
- A mechanically-enforced module dependency rule (e.g. ArchUnit) alongside
  PMD/JaCoCo/PITest, checking the engine/widgets/screens layering that
  `docs/architecture.md` documents.
- Documented parallel fan-out guidance for running multiple Step 4
  implementations concurrently (separate worktrees/agents) as a normal
  option, not just an exception.
- A documented rationale in the Constraints section explaining that the
  complexity/coverage thresholds are a tunable dial for agent-authored
  code, not a fixed constant, with reference to why they might move over
  time.
- A lighter-weight path for smaller features that doesn't require a fully
  pre-approved `.feature` spec before any code exists, while still
  requiring the full intent -> spec -> approval gate for auth/payments/
  data-integrity/public-API work.
- `specs/intent/` no longer treated as a permanently indexed artifact
  requiring the same long-term maintenance as before (e.g. the
  `specs/intent/README.md` Index table's update-in-lockstep requirement is
  relaxed or removed), while `specs/features/*.feature` keeps its current
  weight as an executable, indexed artifact.

## Constraints / non-functional notes

Builds on `pmd-jacoco-quality-gates.md` (#123) — already merged into
`develop`, so the fix-loop item here is unblocked. No game feature or
player-visible behavior changes as part of this issue.

## Open questions

None outstanding — settled via a grilling round during spec drafting on
2026-09-01 (see Clarifications below). The `.feature` file is
`@manual-verification`-tagged, matching the `pmd-jacoco-quality-gates.feature`
precedent: no Cucumber step defs (the ArchUnit module-dependency check is
a plain JUnit test, and the rest of this issue is process-doc content a
human reviews directly), consistent with the "no Java code path to
exercise via Cucumber glue" pattern `RunCucumberTest` already documents.

## Clarifications

- Q: Given the real package layout has no literal "screens" package
  (screen classes — `TitleScreenPanel`, `SettingsScreenPanel`,
  `CodexPanel`, `InventoryPanel`, `SettingsKeybindsPanel`, `GameWindow`,
  `SettingsWindow`, and panel-composition classes — sit flat in
  `com.swiftfaze.veil.ui`, alongside the `ui.widget` subpackage), how
  should the ArchUnit rule actually be specified?
  A: Treat "screens" as "classes directly in `com.swiftfaze.veil.ui`,
  excluding the `ui.widget` subpackage." Enforce: screens may depend on
  widgets, widgets must not depend on screens, and "engine" (everything
  outside `ui` — `game`, `world`, `entities`, `input`, `mods`,
  `exceptions`, `sandbox`) must not depend on `ui` at all.
  Affects: Scope (ArchUnit rule definition), the module-dependency
  scenarios in `deterministic-gauntlet-workflow.feature`.

- Q: Mechanically, how should the lighter-weight path replace/coexist
  with the current Step 1-3 gate in `.claude/workflow.md`?
  A: Flip the default — the slice-by-slice agile loop (implement a slice,
  look at the result, reconcile the spec, continue) becomes the default
  path for anything not matching the high-risk criteria
  (auth/payments/data-integrity/public API). The full intent -> spec ->
  human-approval gate becomes the documented exception, required only for
  that high-risk category (same category that already triggers the
  Opus-5 bump for Steps 1-2). Reinforced directly by the primary-source
  interview transcript (Robert C. Martin, cross-referenced against this
  issue): he tried full upfront planning "this week" and called it
  "always a disaster," then deliberately switched to "let them do a story
  or two, then we'll look at the architecture... manually get involved...
  a few more stories."
  Affects: Scope, Desired behavior (default pipeline shape),
  `.claude/workflow.md`'s Step order and Step 3's framing.

- Q: Does the lighter path still require writing a
  `specs/intent/<slug>.md` at all, or does small-feature work skip even
  that?
  A: Still write it — the lighter path keeps a short intent doc as a
  starting point for implementation, it just isn't required to be
  permanently indexed or kept up to date forever the way it is today.
  Affects: Scope, Desired behavior, `specs/intent/README.md`'s framing of
  what the Index table covers.

- Q: For `specs/intent/README.md`'s Index table, should the "update in
  the same change" requirement be removed entirely, or loosened but kept
  as a courtesy?
  A: Removed entirely. `specs/intent/README.md` stops requiring the Index
  table to track every intent doc in lockstep; existing entries can stay
  as historical record but new/small-feature intent docs are not required
  to be added. Reinforced directly by the transcript: "the specifications
  are ephemeral... they go away... there is no equivalent to source
  code... I do not [keep a list of specs in the repo]" — matching this
  issue's own "there is no equivalent to source code" framing verbatim.
  Affects: `specs/intent/README.md`'s Index section wording.

- Q: Should the already-existing indexed intent docs in
  `specs/intent/README.md` be cleaned up as part of this issue?
  A: No — leave them alone as historical record. Out of scope, matching
  the issue's own "no game feature work" boundary.
  Affects: Nothing (confirms no action).

- Q: The primary-source interview transcript also shows Robert C. Martin
  explicitly declining to impose strict red/green/refactor TDD discipline
  on agents ("I cannot and will not enforce that on the agents... I don't
  think it makes any sense") — letting them write a function then its
  test rather than test-first, line by line. Issue #128's own Scope
  section doesn't mention TDD discipline at all. Is this in scope?
  A: No — confirmed out of scope. Checked `.claude/workflow.md` and root
  `CLAUDE.md`: neither currently mandates strict test-first TDD for
  agent-authored code (Step 4 just says "implement the feature and write
  unit tests," no ordering mandate); the only TDD mentions in the repo
  are background/reference material in the `uncle-bob-craft` skill
  (phrased conditionally, "write tests first *when doing TDD*"), not a
  procedural requirement Step 4 invokes. Nothing to relax.
  Affects: Nothing (confirms no action).
