---
name: spec-feature
description: Turn an approved intent doc into a reviewable Gherkin acceptance spec, looping between specs/intent/<slug>.md and specs/features/<slug>.feature until nothing's left ambiguous. Use for Step 2 of the spec-first workflow — after an intent doc exists (hand-written, or via spec-intent), before any implementation code is written.
---

You are turning a feature intent into a reviewable Gherkin acceptance
spec, through an iterative clarification loop. Do not write any
implementation code at any point in this skill.

Takes one input: a feature slug (from `args`). If not given, ask for it —
don't guess which intent doc this is for.

## Step 1 — Read intent, including prior clarifications

Read `/specs/intent/<slug>.md`. If it does not exist, stop and tell the
user — do not create it yourself. Either they write it by hand (copy
`specs/intent/TEMPLATE.md`), or, if the intent already lives in a GitHub
issue, run the `spec-intent` skill to derive it from the issue instead.

If the file already has a `## Clarifications` section (format below),
read it fully before doing anything else. Do not ask a question that's
already been answered there.

## Step 2 — Generate or regenerate the .feature file from intent.md only

Write `/specs/features/<slug>.feature`, derived entirely from the current
state of `intent.md` (including its Clarifications section) — never from
anything you were told that isn't reflected in `intent.md` yet. If you're
about to write something into the .feature file that came from an answer
not yet recorded in intent.md, stop and do Step 3/4 first.

Include:

- **Feature**: name + one-line description
- **Background** (if multiple scenarios share setup)
- **Scenarios** in Given/When/Then form — happy path, at least one edge
  case, at least one failure/error case
- **Scenario Outline + Examples table** where behavior varies by input
- A trailing comment block listing **Non-goals**, **Risks**, and **Open
  questions** — anything still ambiguous, for the human reviewer

## Step 3 — Ask remaining questions, doubts, or problems

After generating/updating the spec, list anything still unclear: open
questions, ambiguous edge cases, conflicts you noticed between the intent
and the existing codebase. Ask them now, together, rather than one at a
time if there are several.

## Step 4 — On receiving an answer, close the loop before continuing

When the user answers:

1. **Update `intent.md` first.** Append the Q&A to the `## Clarifications`
   section (format below). This step is not optional and not
   deferrable — do it before touching the `.feature` file.
2. **Then regenerate `.feature` from the updated `intent.md`** (repeat
   Step 2), so the spec reflects the answer.
3. **Then re-run Step 3** — ask whatever's still open, which may now be a
   shorter list, or may include new questions the answer just raised.

Repeat Steps 3-4 until there are no remaining open questions.

## Clarifications section format (in intent.md)

Append to `intent.md` under a `## Clarifications` heading, creating it if
it doesn't exist:

```
## Clarifications

- Q: [question as asked]
  A: [answer as given]
  Affects: [scenario name(s) or "general", if it changes a Gherkin scenario]
```

Keep entries in chronological order. Never delete or rewrite a past entry
— if a later answer contradicts an earlier one, add a new entry noting
the change rather than silently editing history.

## Stopping condition

Once there are no remaining open questions, stop. Tell the user the spec
is ready for review at `/specs/features/<slug>.feature`, and that you
will not begin implementation until they approve it. Do not start writing
code, even partially, in this same turn.
