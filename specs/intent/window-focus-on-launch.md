# Intent: Window Focus On Launch

- **Slug(s):** window-focus-on-launch (matches `/specs/features/window-focus-on-launch.feature`)
- **Author:** rwoolley
- **Date:** 2026-09-04
- **Source:** [GitHub issue #159](https://github.com/SwiftFaze/Veil/issues/159)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file) — N/A, standard path; no new `.feature`
      scenario added, see Clarifications
- [ ] Approved by human — N/A, standard path (no blocking gate)
- [x] Implemented
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5";
      already verified via automated screenshot+SendKeys testing during
      diagnosis (see Clarifications), but still needs a plain human
      confirmation pass
- [x] Acceptance tests passing — N/A, no new `.feature` scenario added; this
      class of bug (real Swing focus/window activation) isn't simulable
      headlessly per this repo's existing precedent (see Clarifications).
      `mvn test`'s existing 388 tests still pass unchanged
- [x] Mutation testing passed — N/A, `Main` is excluded from pitest's
      `targetClasses` in `pom.xml`
- [ ] Documentation updated (`docs/`, and the wiki if player-facing)

## Problem

The game window never reliably receives real Swing keyboard focus on
launch. `Main.configureAndShowFrame()` called
`((JComponent) cardPanel.getComponent(0)).requestFocusInWindow()` after
`cardLayout.show(cardPanel, "title")` — but `cardPanel.getComponent(0)` is
whichever card was added to the container *first* (the "game" card, added
in `buildGameCard()` before `buildUIScreens()` adds "title"), not
whichever card `CardLayout` is currently showing. Requesting focus on
that hidden, non-showing component silently fails, so
`KeyboardFocusManager.getFocusOwner()` stays `null` indefinitely and the
title screen's Up/Down/Enter menu navigation does nothing, even though
the window itself is the OS-active/foreground window (confirmed via
`GetForegroundWindow()` during diagnosis — this is not a window-activation
issue).

## Scope

- In scope: fix the focus request in `Main.configureAndShowFrame()` to
  target the actual "title" card via `cards.get("title")` instead of the
  positionally-wrong `cardPanel.getComponent(0)`.
- Out of scope: any change to which component receives focus *within* a
  screen once it's shown (already correct) or to the pause-menu/settings
  focus-restore paths, which use `cards.get(...)`/`navigateTo()` correctly
  already and weren't affected by this bug.

## Actors

Any player launching the game.

## Desired behavior

Immediately after the game window is shown, the title screen should have
real Swing keyboard focus so Up/Down/Enter menu navigation works without
the player needing to click, alt-tab, or minimize/restore the window.

## Constraints / non-functional notes

None beyond the usual.

## Open questions

None — root cause identified and verified live (see Clarifications).

## Clarifications

- Diagnosed live via a temporary `javax.swing.Timer` logging
  `KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()`
  every second, confirming it was `null` forever both before and after
  two earlier (reverted) speculative fixes — an always-on-top window
  activation toggle, and a `SwingUtilities.invokeLater`/`windowOpened`
  listener wrapper. Neither addressed the real bug (`getComponent(0)`
  pointing at the wrong card); the `invokeLater` wrap around `main()` was
  kept since it's correct Swing practice regardless, but the
  `windowOpened` listener workaround was removed once the actual fix
  landed.
- Verified end-to-end via Windows automation (PowerShell + `SendKeys` +
  `SetForegroundWindow`/`GetForegroundWindow`, screenshots captured and
  visually inspected): confirmed the window has genuine OS foreground
  focus, confirmed Up/Down now move the title menu highlight
  (Continue → New → Load), and confirmed selecting Exit now actually
  terminates the process — this exercises both this fix and #147's Exit
  wiring together.
- Acceptance scenario not yet written — this class of bug (real Swing
  focus/activation) isn't simulated by this repo's existing headless
  Cucumber steps (see `startup-welcome-screen.feature`'s Risks note: "Real
  Swing focus-transfer and real window launch are not simulated
  headlessly"). Whether a `.feature` file adds value here, or whether this
  is better covered by documenting the fragile `getComponent(0)` idiom
  and relying on manual playtest, is still open — deciding this is part
  of finishing Step 5.
