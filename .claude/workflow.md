# Development workflow (Uncle Bob-style agentic pipeline)

This project uses a spec-first, constraint-first workflow for AI-assisted
development. Follow this order for every non-trivial feature. Do not skip
steps 1-3 for anything touching auth, payments, data integrity, or public APIs.

## Step order

1. **Intent** — a short written description of the feature already exists
   in `/specs/intent/<feature-slug>.md` before any code or spec is written.
   If it doesn't exist, ask the user for it instead of guessing.
    - If the intent already lives in a GitHub issue instead of being
      dictated fresh, use the `spec-intent` skill instead — give it the
      issue number and it creates/links a branch, moves the tracker item
      to in-progress, and derives `intent.md` from the issue's own
      description.
    - Intent is not written once and frozen — clarifying answers gathered
      during Step 2 get appended back here (see the `spec-feature`
      skill's Clarifications section). `.feature` is always derived from
      the current state of this file, never the other way around.
    - See "Model selection" below — this step uses Claude Sonnet 5.
2. **Spec** — generate a Gherkin feature file from the intent doc and save
   it to `/specs/features/<feature-slug>.feature`. Use the `spec-feature`
   skill (`.claude/skills/spec-feature/`) for this. Do not write
   implementation code in this step.
    - This step loops with Step 1: every clarifying answer updates
      `intent.md` first, then regenerates the `.feature` file from the
      updated intent, before asking what's still open. See the
      `spec-feature` skill for the full loop.
    - See "Context & session management" below — checkpoint after each
      round of revision, especially once the spec settles.
    - See "Model selection" below — this step uses Claude Sonnet 5 (or
      Claude Opus 5 for auth/payments/data-integrity/public-API specs).
3. **Human approval** — stop and wait for the human to approve the `.feature`
   file before writing any implementation code. Do not proceed on your own.
    - See "Context & session management" below — checkpoint right after
      approval, since that's a clean boundary worth starting Step 4 from.
    - See "Model selection" below — this step is a human decision, no
      model involved.
4. **Implementation** — once the spec is approved, hand off to Claude
   Haiku 4.5 to implement the feature and write unit tests.
    - **Default to a fresh agent pinned to Haiku 4.5, not `/fork`, for
      this handoff — the goal is minimum token spend, and Haiku's
      per-token cost plus a tight, self-contained prompt beats a fork's
      "free" inherited context running on the pricier parent model.**
      The orchestrator (the Sonnet 5 session running Steps 1-3) must do
      the exploration once and compress it directly into the handoff
      prompt: an explicit list of every file path it needs (with line
      numbers), the actual code being referenced (not just its name), and
      the exact reasoning/decisions already made (e.g. "don't redefine
      step X, it already exists at Y and collides") — everything the
      agent would otherwise have to rediscover by reading files.
    - **Tell the agent explicitly not to scan or explore the codebase
      beyond the files listed.** If something it needs turns out to be
      missing, wrong, or insufficient, it should stop and report exactly
      what's missing rather than grepping/globbing around for it — the
      orchestrator can then supply the missing piece and resume it. A
      prompt with a complete file list plus this instruction is what
      makes the cheaper model actually cheaper; without it, the agent
      falls back to exploring the codebase itself.
    - **Fall back to `/fork` (accepting it runs on the parent's model,
      not Haiku) only when the context genuinely can't be compressed into
      a prompt economically** — e.g. the relevant material is too
      sprawling, exploratory, or spread across too many files/decisions
      to excerpt without the prompt-writing itself costing nearly as much
      as just forking. This should be the exception, not the default.
    - **A fresh Haiku agent is also the right choice, not just the cheap
      one, whenever true isolation is needed** — e.g. multiple tickets
      being implemented in parallel across separate worktrees, where a
      fork's shared-context model isn't appropriate anyway. Same
      excerpt-pasting rule applies.
    - Keep every function within the complexity budget below. These are
      enforced by CI, not by your judgment alone.
    - See "Context & session management" below for checkpoint guidance —
      this step is the most common place sessions run long.
    - See "Model selection" below — this step uses Claude Haiku 4.5.
5. **Acceptance tests** — wire the approved `.feature` file to the project's
   test runner so it's executable, not just documentation.
    - Continue in the same fresh Haiku agent from Step 4 rather than
      starting over — see "Context handoff rule" below for when isolation
      (a new agent, or the `/fork` fallback) is actually warranted instead.
      Either way, this step needs to know exactly what was implemented —
      don't hand it off as a bare ticket reference to a blank-context
      subagent, or it will re-explore the diff to figure out what changed.
    - See "Context & session management" below — checkpoint per scenario,
      not just at the end of the ticket.
    - See "Model selection" below — this step uses Claude Haiku 4.5.
6. **Mutation testing** — run the mutation test suite against new/changed
   code before considering the feature done. This is the check on the unit
   tests, since they won't be manually reviewed.
    - See "Model selection" below — tooling only, no model involved.
7. **Documentation** — update the codebase docs affected by this change
   before marking the feature done. This is not optional cleanup, it's part
   of the definition of done:
    - Continue in the same fresh Haiku agent from Steps 4-5 rather than a
      blank-context subagent (see "Context handoff rule" below). This step
      needs to know precisely what changed to write an accurate doc update
      — handing it a ticket reference alone forces it to re-derive that
      from the diff, the same cost problem Step 4 had.
    - If the change adds/changes a public API endpoint, method, or
      configuration property, update the relevant reference doc (OpenAPI
      description, `docs/architecture.md`, public API doc, or Javadoc for a
      library's public surface).
    - If the change introduces a new domain concept, non-obvious design
      decision, or deviates from an existing pattern, add or update an entry
      in `docs/` explaining it — don't just leave it discoverable only by
      reading the diff.
    - If the change is a library, update the CHANGELOG.
    - If nothing user-facing or architecturally significant changed (e.g. a
      bugfix with no behavior/contract change), state explicitly that no doc
      update was needed rather than skipping the step silently.
    - Do not put narrative domain explanations into CLAUDE.md itself — that
      file stays behavioral/instructional. Narrative and reference
      documentation belongs in `docs/`, with CLAUDE.md linking to it only if
      the agent needs to be pointed there.
    - See "Model selection" below — this step uses Claude Haiku 4.5.

## Context & session management

Claude Code auto-compacts by default once context usage gets high (around
83% capacity). That's a safety net, not a strategy — it fires reactively,
regardless of whether you're mid-edit or mid-thought, and can produce a
worse summary than a checkpoint taken at a clean boundary. Don't rely on
it as your only mechanism; checkpoint proactively at natural breakpoints
in every step below, not just when a limit warning appears.

### Steps 2-3 (Spec drafting + human approval)

This is a back-and-forth, human-driven step — revising the Gherkin spec
and intent doc across several rounds of questions and edits — and it
accumulates context the same as any agent-heavy step, even when most of
the turns are the human talking directly to the main session rather than
delegating to a subagent. Checkpoint with `/compact` after each round of
revision is settled (not mid-edit), and especially right after the spec
is approved — that approval is a clean, meaningful boundary worth
starting the next step from fresh.

### Steps 4-5 (Implementation + acceptance tests)

These are the steps most likely to run long. Checkpoint with `/compact`
after each sub-unit of work is verified working — not just once at the end
of the whole ticket:

- In Step 4: after each file or logical unit passes its own tests, before
  moving to the next one.
- In Step 5: after each scenario from the `.feature` file is wired and
  passing, before wiring the next scenario.

Do not wait until output feels sluggish or repetitive to compact — by
then the session is already in degraded mode. Compacting at a clean
boundary (tests green, nothing mid-edit) produces a much better summary
than compacting mid-failure with half-applied changes in flight.

### Steps 6-7 and elsewhere

Usually short enough that this rarely matters. If a session does run long
here too, apply the same principle: checkpoint at a clean boundary, not
reactively.

### Between pipeline steps

Prefer starting a fresh session (`/clear` or a new session) over
continuing indefinitely, once a step is fully complete and its artifact is
committed to disk (approved `.feature` file, working implementation,
passing acceptance tests, updated docs). The next step's real memory is
that artifact, not the conversation — so nothing is lost by starting
clean, and the new session runs faster with a smaller window to manage.

Before clearing or starting fresh, write a short status note either in the
ticket/PR description or as a commit message, stating: which step just
finished, which files were touched, and what the next step should do
first. This is what a `/resume` or a fresh session should read first to
reconstruct state — don't rely on the agent re-deriving this from a full
diff read.

### On `/resume`

When resuming a session (new terminal, next day, after a `/clear`), first
read: the ticket's current git diff/log, the approved `.feature` file for
this ticket, and any status note left per the paragraph above. Do this
before touching any other file — it's the fastest way to reconstruct
exactly where the previous session left off without re-exploring the
whole codebase.

### Signs a checkpoint is overdue (don't wait for these — they mean you're already late)

- Repeating a fix already tried earlier in the same session
- Referencing a file structure that's since changed
- Losing track of which files in a multi-file change have been touched
- Responses feeling more generic or hedged than earlier in the session

## Context handoff rule

Steps 4, 5, and 7 are a single continuous handoff, not three separate
delegations: stay in the same fresh Haiku agent across 4→5→7 for one
ticket rather than re-briefing a new one at each step (see Step 4 above
for the full default-vs-`/fork` reasoning). The one question worth asking
before any of the three is whether this specific piece of work needs true
isolation — parallel work in a separate worktree — since that's the one
case where a fresh, separately-briefed agent (or `/fork`) is actually the
right call instead of continuing the existing one.

## Model selection

Not every step needs the same model. Pin concrete models per step rather
than leaving it to whoever happens to be running the session — this keeps
cost predictable and avoids under-provisioning judgment-heavy steps.

- **Steps 1-2 (Intent, Spec)** — Claude Sonnet 5. Writing the intent doc
  and generating the Gherkin spec involves judgment calls and ambiguity
  resolution that a human will review, so use the balanced model, not the
  cheapest one. Bump to Claude Opus 5 for specs touching auth, payments,
  data integrity, or public APIs — the same class of feature Step 3
  won't let skip human approval.
- **Extended thinking for Steps 1-2** — a separate lever from model choice.
  Use it for the judgment-heavy parts of these steps: resolving scope
  boundaries, deciding what's in/out per the intent doc, designing
  scenarios that actually catch bugs (e.g. an asymmetric fixture to catch
  an x/y transposition, not a symmetric one that would pass either way).
  Trigger it with "think hard" / "think harder" in the prompt, or a
  session-wide thinking setting. Skip it for the mechanical parts of these
  steps (reading files, running `gh` commands) — no ambiguity to reason
  through, so it's just added latency.
- **Step 3 (Human approval)** — no model. This is a human decision gate,
  not agent work.
- **Steps 4-5 (Implementation, Acceptance tests)** — Claude Haiku 4.5, via
  a fresh, non-fork agent with a self-contained, excerpt-rich prompt — see
  Step 4 above and "Context handoff rule" above for the full default vs.
  `/fork` reasoning.
- **Step 6 (Mutation testing)** — no model. This step is tooling
  (running the mutation test suite), not agent judgment.
- **Step 7 (Documentation)** — Claude Haiku 4.5, continuing in the same
  fresh Haiku agent as Steps 4-5 (see "Context handoff rule" above),
  since it only needs to describe what already changed rather than make
  new judgment calls.

If the model lineup changes, update the names here rather than reverting
to vague relative terms — a stale name is easier to spot and fix than a
permanently vague policy.

## Constraints (mechanically enforced, not optional)

- Max function length: 40 lines
- Max cyclomatic complexity: 8
- Max function parameters: 4
- Minimum line coverage on changed files: 85%
- No function may call more than one level of abstraction below itself
  (Single Level of Abstraction principle)

These are enforced by the linter/CI config in this repo, not by asking the
agent to "try to keep things clean." If a change can't meet these limits,
stop and flag it rather than disabling the check.

## What gets reviewed vs. not

| Artifact                         | Written by | Reviewed by human?                                                |
|-----------------------------------|------------|--------------------------------------------------------------------|
| Intent doc                       | Human      | N/A (it's the source)                                             |
| Gherkin acceptance spec          | Agent      | Yes — always, before implementation starts                        |
| QA procedures                    | Agent      | Yes — rigor scales with criticality                               |
| Implementation code              | Agent      | No, by design — leverage comes from not reading it                |
| Unit tests                       | Agent      | No, by design                                                     |
| Mutation test results            | Tooling    | Human skims summary only                                          |
| Codebase docs (docs/, CHANGELOG) | Agent      | Spot check — rigor scales with how public/critical the surface is |

## Notes for the agent

- If the intent doc is missing or ambiguous, ask one clarifying question
  rather than inventing scope.
- If a requested change would violate a constraint above, say so explicitly
  and propose a decomposition instead of quietly exceeding the limit.
- Never mark a feature "done" without the acceptance tests passing against
  the approved `.feature` file.
- Never mark a feature "done" without completing the documentation step
  (Step 7), even if the conclusion is "no doc update needed" — say so
  explicitly rather than omitting the step.
