# Intent: Add pause screen (ESC): Resume / Settings / Exit to Main Menu

- **Slug(s):** pause-screen-esc (matches `/specs/features/pause-screen-esc.feature`)
- **Author:** rwoolley
- **Date:** 2026-09-04
- **Source:** [GitHub issue #148](https://github.com/SwiftFaze/Veil/issues/148)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [ ] Approved by human — high-risk path only
- [x] Implemented
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [x] Acceptance tests passing
- [ ] Mutation testing passed — `mvn org.pitest:pitest-maven:mutationCoverage` ran clean
      (72% overall kill rate, no regression), but none of this feature's new
      classes (`GamePanel`, `PauseMenuPopup`, `PauseToggleListener`,
      `SettingsScreenPanel`, `Main`) are in `pom.xml`'s curated `targetClasses`
      list — consistent with that list's existing convention of excluding
      Swing view/composition classes (only `Keybindings`, touched here for a
      new constant only, is in scope). Left unchecked since the run doesn't
      actually cover this feature's own logic; not a regression, just no
      signal either way.
- [x] Documentation updated (`docs/`, and the wiki if player-facing)

## Problem

Pressing Escape during gameplay does nothing today. `Keybindings.MENU_CANCEL`
(VK_ESCAPE) already exists and is used elsewhere (Settings' cancel action,
`PopupWidget`'s dismiss binding), but `GamePanel.bindKeys()` never registers
it. Players have no way to pause, reach Settings, or return to the main menu
mid-game.

## Scope

- In scope:
  - Bind ESC in `GamePanel` to open a pause menu overlay, following the same
    non-destructive overlay pattern already used for Inventory/Codex: a
    `PopupWidget` subclass containing a `ListWidget<String>` of Resume /
    Settings / Exit to Main Menu, added to the existing `JLayeredPane` at
    `POPUP_LAYER`, toggled via a new `GameListener` hook (mirrors
    `PopupToggleListener`).
  - **Resume**: dismisses the popup (ESC again also dismisses it, same as
    other popups since `PopupWidget` already binds `MENU_CANCEL` to dismiss)
    and gameplay continues immediately.
  - **Settings**: opens the existing `SettingsScreenPanel`. Its back/cancel
    action currently hardcodes returning to `"title"`
    (`SettingsScreenPanel.java:213`) — this needs to be parameterized so
    Settings opened from the pause menu returns to the pause menu (game
    remains paused underneath), not to the title screen.
  - **Exit to Main Menu**: navigates back to the title card (reusing
    `Main.navigateTo`) and resets `GamePanel`'s `Player`/`WorldScene` state,
    so a subsequent New/Continue doesn't inherit stale state from the
    exited session.
  - **Explicit acceptance criterion**: while paused, player movement and any
    game-loop ticking must actually freeze. `GamePanel`'s movement bindings
    are `WHEN_IN_FOCUSED_WINDOW`, so a popup merely capturing Swing focus is
    not guaranteed to stop WASD/arrow input from firing underneath the
    pause menu — this needs explicit verification/handling, not an
    assumption that focus capture alone is sufficient.
- Out of scope (tracked in #147): implementing the title screen's own
  "Exit" menu item (quit the application) — different screen, different
  action, no shared implementation assumed.

## Actors

Player during active gameplay.

## Desired behavior

- Press ESC during gameplay → pause menu overlay appears on top of the (now
  frozen) game view, matching how Inventory/Codex popups already overlay
  the game.
- Press ESC again, or otherwise dismiss → pause menu closes, gameplay
  resumes immediately with no stale/queued input.
- Select **Resume** → same effect as dismissing.
- Select **Settings** → Settings screen opens; going back returns to the
  pause menu (not the title screen), game remains paused throughout.
- Select **Exit to Main Menu** → returns to the title screen; in-progress
  player/world state is reset.

## Constraints / non-functional notes

None beyond the usual (see CLAUDE.md function-length/complexity/coverage
budgets).

## Open questions

- Interaction with Inventory/Codex popups already being open when ESC is
  pressed: **RESOLVED**. Swing's key-dispatch priority (`WHEN_FOCUSED` →
  `WHEN_ANCESTOR_OF_FOCUSED_COMPONENT` → `WHEN_IN_FOCUSED_WINDOW`) means
  pressing ESC while Inventory/Codex holds focus closes that popup (their own
  dismiss binding wins), and pressing ESC again (now that GamePanel has focus)
  opens the pause menu. No extra code needed to prevent conflict.

- Exit to Main Menu's reset mechanism: originally implemented by reusing
  `Main.resetGame()` (the same dispose-and-reload-everything approach the F5
  hot-reset dev feature used). While this PR was open, `feat/fullscreen-
  windowed-toggle` merged into `develop` and deliberately removed F5 hot-reset
  and `resetGame()` entirely (window-mode switching now happens in-place via
  `Main.applyWindowMode`, not by tearing down and reloading). Resolved during
  the resulting merge conflict by adding `GamePanel.resetState()` instead: an
  in-place reset (fresh `Player`/`WorldScene`, cleared/rebuilt
  `entitiesToDraw`, `paused` cleared) that doesn't depend on disposing the
  frame. This also required changing `GamePanel.player` from `final` to a
  reassignable field, and changing its movement `Action`s from bound method
  references (`player::moveUp`) to lambdas that read the `player` field
  dynamically — a bound reference would have kept moving the discarded
  pre-reset `Player` instance forever. See `docs/screens.md`'s `PauseMenuPopup`
  section for the current mechanism.

Scope otherwise settled via a grilling session on 2026-09-02.
