# Intent: Persist windowed game window size in settings

- **Slug(s):** persist-windowed-window-size (matches `/specs/features/persist-windowed-window-size.feature`)
- **Author:** rwoolley
- **Date:** 2026-09-04
- **Source:** [GitHub issue #163](https://github.com/SwiftFaze/Veil/issues/163)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file) — high-risk path only, see `.claude/workflow.md`
- [ ] Approved by human — high-risk path only
- [x] Implemented
- [x] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [x] Acceptance tests passing
- [ ] Mutation testing passed
- [x] Documentation updated (`docs/`, and the wiki if player-facing)

## Problem

The Windowed-mode game window has no memory of its size — `Main.java`'s
`configureAndShowFrame()` always `pack()`s to the content's natural
preferred size on launch, and `MIN_WINDOW_WIDTH`/`MIN_WINDOW_HEIGHT` only
enforce a floor, not a remembered value. Every time a player resizes the
window, that shape is lost on next launch.

## Scope

- In scope:
  - Capture the current window width/height when the app closes/exits
    while in Windowed mode, and persist it via a new scalar field on
    `SettingsConfig` (plain Gson POJO, no merge machinery needed — see
    `SettingsRepository`).
  - Restore the saved size on next launch, applied wherever
    `configureAndShowFrame()`/`applyWindowMode()` currently packs to a
    default size.
  - Also restore the saved size when switching Fullscreen → Windowed
    mid-session (reusing the existing re-`pack()` path in
    `Main.applyWindowMode()`), so the transition doesn't produce an
    arbitrary size.
  - If the app quits while in Fullscreen mode, leave whatever Windowed
    size was last saved from a previous Windowed session untouched
    (nothing new to capture that session).
  - Clamp the restored size to the current screen's bounds (alongside the
    existing `MIN_WINDOW_WIDTH`/`MIN_WINDOW_HEIGHT` clamp) so a size
    saved on a bigger/since-reconfigured monitor doesn't produce an
    off-screen or oversized window.
- Out of scope:
  - Persisting window *position*, only size.
  - Persisting Fullscreen-mode dimensions (fullscreen always fills the
    current display; nothing to remember there).

## Actors

The player, resizing the Windowed-mode game window during normal play.

## Desired behavior

A player resizes the game window while in Windowed mode, closes the
game, and relaunches — the window comes back at the same size.
Switching from Fullscreen back to Windowed mid-session restores that
same saved size instead of an arbitrary pack size. If the saved size no
longer fits the current display, it's clamped to fit rather than
appearing off-screen or larger than the monitor.

## Constraints / non-functional notes

None beyond the usual (see CLAUDE.md function-length/complexity/coverage
budgets).

## Open questions

None — scope settled via a grilling session on 2026-09-04 (see issue
#163).

## Clarifications

- Q: Should this feature's Cucumber scenarios be scoped to just the
  settings.json round-trip of the new width/height field(s) (mirroring
  `settings-persistence.feature`'s Given/Then pattern on
  `SettingsConfig`, no live JFrame involved), with the live-window
  capture/restore/clamp behavior treated as a Non-goal deferred to
  manual playtest (Step 4.5) + the implementing agent's render-and-
  inspect check — the same precedent `fullscreen-windowed-toggle.feature`
  already set? Or is a testable seam/fake wanted specifically for this?
  A: Scope Cucumber to the settings.json round-trip only, same as the
  existing precedent.
  Affects: general (drives which scenarios the .feature file can contain)

- Q: Field shape on `SettingsConfig` — two int fields
  (`windowWidth`/`windowHeight`), or one combined field?
  A: Two int fields (`windowWidth`, `windowHeight`), matching the
  existing flat-scalar convention.
  Affects: general

- Q: Default/sentinel when nothing has been saved yet (fresh install, or
  a pre-feature settings.json) — fall back to today's `pack()` behavior,
  with what sentinel value?
  A: Yes, fall back to today's `pack()` behavior; sentinel is `0` for
  either field. Gson's default int field value on a missing/old
  settings.json is already `0`, so this falls out of existing "missing
  value" handling for free.
  Affects: "A fresh install with no saved window size uses today's
  pack()-based default size" scenario

- Q: Capture trigger mechanism — leave the exact wiring (shutdown hook
  vs. WindowListener + explicit pre-exit call) as a Step 4 detail, or
  lock in a preference now?
  A: Lock in a JVM shutdown hook now, so any future exit path is covered
  automatically without needing to remember to wire capture into it.
  Affects: general (Step 4 implementation note, not a scenario)

- Q: Clamping precedence — clamp the restored size independently on each
  axis (`[MIN_WINDOW_WIDTH, screenBounds.width]` /
  `[MIN_WINDOW_HEIGHT, screenBounds.height]`), no aspect-ratio
  preservation?
  A: Yes, independent-axis clamping.
  Affects: "A saved size larger than the current screen is clamped to
  fit" scenario
