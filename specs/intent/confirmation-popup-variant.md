# Intent: Smaller confirmation-style popup variant (Yes/No dialogs)

- **Slug(s):** confirmation-popup-variant (matches
  `/specs/features/confirmation-popup-variant.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-30
- **Source:** [GitHub issue #99](https://github.com/SwiftFaze/Veil/issues/99)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
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
  - A smaller, fixed-size, centered `PopupWidget` presentation (see
    Clarifications Q5 — a fixed size, not a tight content-wrap), with a
    decorative bordered frame and arrow-accented title bar (Clarifications
    Q3) — nothing like this exists in the codebase today; current popups
    only have a plain bottom-line divider and a plain title label.
  - `PopupWidget.isFullScreen()` (Clarifications Q2): `true` for today's
    existing full-screen popups, overridden `false` by the new compact
    variant — the headless-testable signal for "not full-screen."
  - A new `JLayeredPane` host for the settings card specifically,
    mirroring `GameWindow.buildContentArea`'s pattern (Clarifications
    Q1) — `GameWindow`'s own instance is scoped to the game card
    (`GamePanel` + `EastPanel`) and can't be literally reused, since
    `SettingsScreenPanel` is currently just a plain `CardLayout` card
    with no layering of its own.
  - Reuses the existing `PopupWidget` base's core mechanism (Escape-to-
    dismiss via `MENU_CANCEL`, `onUp()`/`onDown()`/`onLeft()`/`onRight()`
    hooks) — not a parallel popup implementation — but this dialog does
    NOT use the inherited "Close" button footer (Clarifications Q4): the
    Yes/No `RadioGroupWidget` (Left/Right + Enter) is its only
    confirm/cancel mechanism, matching the reference image's single
    footer action rather than a separate Close button alongside it.
  - The frame/compact-presentation mechanism itself must stay reusable
    for a different footer shape later (Clarifications Q4) — e.g. a
    future Close-only "alert" popup — even though building that alert
    popup is not part of this issue.
  - A concrete Yes/No confirm dialog built on this smaller variant, with
    a real (if minimal) in-game trigger to prove it against — see
    Constraints for which one and why.
- Out of scope:
  - Whatever bigger feature eventually needs its own Yes/No prompt (e.g.
    NPC dialogue) — this issue only needs *a* real trigger, not that
    trigger's own feature work.
  - The future Close-only "alert" popup itself (Clarifications Q4) —
    this issue only needs the underlying mechanism to not preclude it.
  - A general-purpose popup host usable by every card/screen — this
    issue only wires a layered host for the settings card specifically
    (Clarifications Q1), not a shared mechanism for other screens.
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
- The popup shows a bordered, arrow-accented title bar (e.g. matching
  the `>> Title <<` style of the reference image in Clarifications Q3)
  above the "Reset all settings to their defaults?" question and the
  Yes/No choice — exact title wording is a minor copy detail for the
  human to adjust at Step 3 approval, not an architectural question.

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
  choice itself). Both have since landed on `develop` (#54 merged via
  PR #114; `RadioGroupWidget` at
  `src/main/java/com/swiftfaze/veil/ui/widget/RadioGroupWidget.java`,
  the settings screen's "Reset to Defaults" row at
  `src/main/java/com/swiftfaze/veil/ui/SettingsScreenPanel.java`), so
  this feature is no longer blocked on either landing first.

## Clarifications

- Q: `GameWindow.buildContentArea`'s `JLayeredPane`/`FillLayout` mechanism
  is scoped to the game card (`GamePanel` + `EastPanel`, assembled in
  `Main.java`'s `loadGame()`); `SettingsScreenPanel` is a plain
  `CardLayout` card with no layering of its own. How should the new
  popup actually get composited above the settings screen?
  A: Wrap `SettingsScreenPanel` in its own new `JLayeredPane` host in
  `Main.java`, mirroring `GameWindow.buildContentArea`'s pattern for the
  settings card specifically (not a literal reuse of `GameWindow`'s own
  instance, and not a shared host for every card).
  Affects: Scope (host mechanism), general (implementation file layout).

- Q: `FillLayout` only stretches children to full bounds during a real
  Swing layout pass, which this repo's headless Cucumber steps don't
  run. What should "the confirmation popup is not full-screen"
  concretely assert?
  A: Add `PopupWidget.isFullScreen()` — `true` for today's existing
  full-screen popups, overridden `false` by the new compact/centered
  variant. Testable by direct method call, no layout pass needed.
  Affects: Scenario "Confirming Reset to Defaults opens the Yes/No
  confirmation popup".

- Q: Should the popup adopt the decorative ASCII frame (double-line
  border, arrow-accented title bar) shown in the user's reference image,
  or reuse today's plain bottom-line-divider + plain-title-label style?
  A: Build the frame now, not deferred — "this is what I'd like the
  popup to resemble," not "roughly like."
  Affects: Scope, Desired behavior (title bar), Scenario "Confirming
  Reset to Defaults opens the Yes/No confirmation popup".

- Q: `PopupWidget` always renders an inherited "Close" button footer
  today (kept, redundantly, alongside the Yes/No choice by
  `DropConfirmationPopup`). Should this dialog keep it, replace it with
  a single-key footer like the reference image's `[Z] Exit`, or drop it?
  A: Drop it — the Yes/No `RadioGroupWidget` (Left/Right + Enter) is the
  only confirm/cancel mechanism, Escape still cancels. But the
  underlying frame/compact-presentation mechanism must stay reusable for
  a different footer shape later (e.g. a future Close-only "alert"
  popup) — building that alert popup is not part of this issue, only
  not precluding it.
  Affects: Scope (footer), Out of scope (future alert popup).

- Q: intent.md's original Scope said the popup is "sized to its content"
  (a tight wrap), but the reference image shows generous padding around
  short content rather than a snug fit. Should the compact presentation
  be one fixed size for every popup using it, or sized to each popup's
  actual content?
  A: One fixed size (a constant) — matches the reference image, keeps
  every popup using this presentation visually consistent, and gives
  the headless test a concrete constant to assert against.
  Affects: Scope (sizing model).

## Open questions

None — all open questions from the Step 2 clarification round were
resolved above.
