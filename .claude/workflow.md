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
    - **A `/fork` always runs on the parent session's model — a model
      override is ignored.** This means "inherit full context" and "run on
      Haiku 4.5" are not both achievable when the parent session isn't
      already on Haiku. Confirmed against the tool's actual behavior
      2026-08-29; this corrects earlier guidance here that assumed a fork
      could be switched to a different model.
    - **Default to `/fork`, not a named Task subagent, for this handoff,
      accepting that it runs on the parent session's model instead of
      Haiku 4.5.** A fork inherits the full conversation history —
      including every file already read during the Spec step — so the
      implementer starts with the context it needs at zero re-read cost.
      This is usually the better trade: re-deriving context from scratch
      typically costs more than the difference in per-token price.
    - **Use a fresh agent pinned to Haiku 4.5 instead of a fork only when
      true Haiku-cost savings matter enough to justify losing inherited
      context** — e.g. a large, well-isolated area of a multi-part change.
      In that case, the orchestrator must paste the actually-relevant file
      excerpts (not just file paths or module names) directly into the
      subagent's task prompt, and explicitly instruct it not to re-explore
      the codebase beyond what's provided unless something is missing or
      doesn't match. A subagent given only a path or ticket reference will
      re-read files from scratch, which is the exact problem this section
      exists to prevent.
    - **Also use a named subagent instead of a fork when true isolation is
      needed** — e.g. multiple tickets being implemented in parallel across
      separate worktrees, where forks aren't the right tool. The same
      excerpt-pasting rule above applies.
    - Keep every function within the complexity budget below. These are
      enforced by CI, not by your judgment alone.
    - See "Context & session management" below for checkpoint guidance —
      this step is the most common place sessions run long.
    - See "Model selection" below — this step uses Claude Haiku 4.5.
5. **Acceptance tests** — wire the approved `.feature` file to the project's
   test runner so it's executable, not just documentation.
    - Continue in the same fork used for Step 4, or open a new `/fork` from
      the point right after implementation if a fresh context is preferred.
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
    - Continue in the fork from Steps 4-5 rather than a blank-context
      subagent. This step needs to know precisely what changed to write an
      accurate doc update — handing it a ticket reference alone forces it
      to re-derive that from the diff, the same cost problem Step 4 had.
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

### Signs a checkpoint is overdue (don't wait for these — they mean you're

already late)

- Repeating a fix already tried earlier in the same session
- Referencing a file structure that's since changed
- Losing track of which files in a multi-file change have been touched
- Responses feeling more generic or hedged than earlier in the session

## Context handoff rule

Before delegating work in Steps 4, 5, or 7 to a cheaper model, ask: does
this need true isolation (parallel work, separate worktree), or is it a
sequential handoff where inherited context is pure upside? Default to
staying in the same `/fork` across Steps 4→5→7 for a single ticket's
implementation, test-wiring, and documentation — they all need to know
what the previous step actually did. Reserve named subagents for cases
that actually need isolation, and when you do use one, treat "what
context does it need" as something you must construct explicitly — it
inherits nothing automatically.

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
- **Step 3 (Human approval)** — no model. This is a human decision gate,
  not agent work.
- **Steps 4-5 (Implementation, Acceptance tests)** — Claude Haiku 4.5 is
  the target model, but a `/fork` cannot actually be switched to it (see
  Step 4 above) — a fork always runs on the parent session's model. Use
  Haiku 4.5 only via a fresh, non-fork agent with manually-provided
  context; default to a same-model `/fork` otherwise.
- **Step 6 (Mutation testing)** — no model. This step is tooling
  (running the mutation test suite), not agent judgment.
- **Step 7 (Documentation)** — Claude Haiku 4.5, continuing in the same
  fork as Steps 4-5, since it only needs to describe what already
  changed rather than make new judgment calls.

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
