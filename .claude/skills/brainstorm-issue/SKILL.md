---
name: brainstorm-issue
description: Brainstorm a feature idea with the user and capture it directly as a GitHub issue (or two, if scope splits), skipping specs/intent/ entirely. Use when the user wants to think out loud about an idea for later — not spec it through the full intent -> .feature -> approval pipeline.
---

This is the *pre-intent* version of Step 1 in the repo's spec-first
workflow (see root `CLAUDE.md` / `.claude/workflow.md`): same shape of
thinking, but the destination is a GitHub
issue, not `specs/intent/<slug>.md`. Never write to `specs/intent/` or
`specs/features/` in this skill — if the user wants that, they'll ask for
`/spec-feature` instead (or say so mid-brainstorm; if they do, stop and
hand off rather than writing the file yourself).

## Step 1 — Ground the brainstorm in what actually exists

Before asking the user anything, check what the idea depends on: search
the codebase for the systems/classes it would touch or extend. Note
explicitly what already exists (reuse/extend) versus what doesn't exist
yet (net-new — often means the idea is bigger, or partly blocked, or
needs its own separate issue). Don't skip this — a brainstorm grounded in
"here's what's already there" produces a much tighter scope discussion
than one that starts from the idea in the abstract.

## Step 2 — Scope the brainstorm with the user

Use `AskUserQuestion` (not free-form prose) to settle, at minimum:

- **What's in scope for a first version** versus what should be split
  into a separate follow-up issue. Offer a recommended option — usually
  the smallest coherent slice that doesn't depend on anything unbuilt.
- Any other genuinely open fork in the idea (entry point, UI shape,
  who/what triggers it) that materially changes what the issue should
  say. Don't ask about things you can just decide reasonably.

If the answers reveal a natural split (some of the idea is buildable now,
some depends on systems that don't exist yet or is clearly later work),
plan on two issues, not one bloated issue — see Step 3.

## Step 3 — File the issue(s)

If the brainstorm split into "do now" + "later", create the **follow-up /
backlog issue first**, so its number is known and the primary issue can
link to it. If it didn't split, there's just one issue — skip straight to
writing it.

Write each issue body in intent-doc shape, adapted for an issue (no
frontmatter, no `Source:`/`Slug:` fields, no `## Status` checklist — those
only make sense once there's a file to check items off in):

```
## Problem
<what's broken/missing/manual today, why it matters now>

## Scope
**In scope:** ...
**Out of scope (tracked in #<n>, if split):** ...

## Actors
<who triggers/uses this>

## Desired behavior
<plain-language happy path + the edge cases that already matter>

## Constraints / non-functional notes
<anything beyond the repo's standard budgets, or "none beyond the usual">

## Open questions
<anything genuinely undecided — don't silently pick for the user here>
```

A backlog/follow-up issue can be shorter — Problem + Scope + a short
"why this is separate/blocked" note is enough; it doesn't need the full
shape if most sections would just say "n/a, blocked."

Create with:

```
gh issue create --title "<title>" --label enhancement --body "$(cat <<'EOF'
...
EOF
)"
```

Cross-link: the primary issue's Scope references the follow-up issue
number in "Out of scope"; if useful, the follow-up issue can note which
primary issue it was split from.

## Step 4 — Add to the project board

Every issue created this way goes on the VEIL project board — this is a
standing repo convention, not optional:

```
gh project item-add 2 --owner SwiftFaze --url <issue-url>
```

Do this for each issue created in Step 3.

## Step 5 — Report back

Tell the user the issue URL(s) and, in one line each, what's in scope for
the primary issue and what got split into the follow-up (if any). Don't
propose next steps beyond what was asked — if this brainstorm is likely
to turn into a real intent later, that's the user's call to make, not
something to push in this skill's output.
