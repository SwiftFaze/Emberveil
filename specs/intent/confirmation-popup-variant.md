# Intent: Smaller confirmation-style popup variant (Yes/No dialogs)

- **Slug(s):** confirmation-popup-variant (matches
  `/specs/features/confirmation-popup-variant.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-30
- **Source:** [GitHub issue #99](https://github.com/SwiftFaze/Veil/issues/99)

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

`PopupWidget` (`src/main/java/com/swiftfaze/veil/ui/widget/PopupWidget.java`)
currently has exactly one presentation: full-screen, layered above the
whole game view via `GameWindow.buildContentArea`'s `JLayeredPane` +
`FillLayout` (proven by `InventoryPanel`). There's no smaller, centered
variant for content that doesn't need the full screen — e.g. a Yes/No
confirmation. Surfaced during #36's manual playtest and deliberately
deferred (see `specs/intent/ui-component-framework.md`'s Clarifications)
since no concrete consumer existed yet.

## Scope

- In scope:
  - A smaller/centered `PopupWidget` presentation: sized to its content
    instead of stretched full-screen. Still layered via the same
    `JLayeredPane`/`POPUP_LAYER` mechanism `GameWindow.buildContentArea`
    already provides — just without `FillLayout` forcing full-bounds
    sizing for this variant.
  - Reuses the existing `PopupWidget` base directly (Close button,
    Escape-to-dismiss via `MENU_CANCEL`, `onUp()`/`onDown()` hooks) —
    not a parallel popup implementation.
  - A concrete Yes/No confirm dialog built on this smaller variant, with
    a real (if minimal) in-game trigger to prove it against — see
    Constraints for which one and why.
- Out of scope:
  - Whatever bigger feature eventually needs its own Yes/No prompt (e.g.
    NPC dialogue) — this issue only needs *a* real trigger, not that
    trigger's own feature work.
  - Any mouse/pointer handling — this game is keyboard-only by design.

## Actors

- The player, when an in-game action needs a Yes/No confirmation before
  proceeding.

## Desired behavior

- A developer can open a smaller, centered popup (Yes/No or similar)
  without it stretching to the full game view, reusing the same
  `PopupWidget` API (`open()`, `dismiss()`, `setOnDismiss`, `onUp()`/
  `onDown()`) as the existing full-screen `InventoryPanel`.
- Concretely: the settings screen's "Reset to Defaults" item (see
  Constraints) opens this popup asking for confirmation; Yes and No both
  dismiss it (Yes doesn't actually reset anything yet — no setting
  persists real state, matching #54's own out-of-scope framing); Escape
  also dismisses it, same as any other popup.
- Left/Right (not Up/Down) moves the highlighted choice between Yes and
  No, reusing #35's radio group widget (a 2-option horizontal choice is
  exactly what that widget already models) rather than building a
  separate Yes/No-specific control.

## Constraints / non-functional notes

- Must fit the existing complexity budgets (40-line functions, cyclomatic
  complexity 8, 4 params max, SLAP).
- Stays Swing-based, no new UI framework/dependency.
- **Chosen concrete trigger, and why:** issue #99 requires "a real
  consumer, not just the widget in isolation," but explicitly rules out
  building a bigger feature (like NPC dialogue) just to host it. The
  settings screen's "Reset to Defaults" item
  (`specs/intent/startup-and-settings-screens.md`, #54) is a real,
  already-scoped UI element that's a natural fit for a Yes/No confirm —
  and since #54 itself already made "Reset to Defaults doesn't actually
  reset anything yet" an explicit non-goal, wiring a confirm dialog onto
  it stays honest: the dialog is real, its "Yes" action is a no-op, same
  honesty level as every other settings item in #54. This was decided
  autonomously (no human available) — flag for confirmation at Step 3
  approval; the alternative (a dev-only sandbox demo, matching #35's
  resolution for its own no-consumer widgets) remains available if this
  coupling to #54 is rejected.
- **Dependency chain**: this issue depends on #54's settings screen (for
  its chosen trigger) and #35's radio group widget (for the Yes/No
  choice itself), neither approved/implemented yet. This mirrors the
  same "depends on the prior framework piece landing first" shape
  already established by #35 depending on #36, and #54 depending on both
  #36 and #35.

## Open questions

None — the trigger choice and control reuse were the only real ambiguity,
resolved above.
