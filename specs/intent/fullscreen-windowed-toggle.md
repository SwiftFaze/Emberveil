# Intent: Wire Settings screen's Fullscreen/Windowed toggle to the real game window

- **Slug(s):** fullscreen-windowed-toggle (matches `/specs/features/fullscreen-windowed-toggle.feature`)
- **Author:** rwoolley
- **Date:** 2026-09-04
- **Source:** [GitHub issue #136](https://github.com/SwiftFaze/Veil/issues/136)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file) — high-risk path only, see `.claude/workflow.md`
- [ ] Approved by human — high-risk path only
- [ ] Implemented
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
- [ ] Acceptance tests passing
- [ ] Mutation testing passed
- [ ] Documentation updated (`docs/`, and the wiki if player-facing)

## Problem

`SettingsScreenPanel` already has a working Windowed/Fullscreen `RadioGroupWidget`
(constructed at `SettingsScreenPanel.java:133-136`), but `moveLeft`/`moveRight`
(`SettingsScreenPanel.java:179-197`) only mutate the widget's own display state
and refresh the row's label — no callback fires anywhere, so toggling it does
nothing to the actual window. `Main.configureAndShowFrame` (`Main.java:79-90`)
also hardcodes `frame.setResizable(false)`, so the frame can never be resized
regardless of the setting. `settings-screen.feature`'s own non-goals list
already states plainly: "no rendering/audio/config-persistence system exists
yet; this screen is visual/input shape only" — this issue is what makes that
non-goal no longer true for the Fullscreen/Windowed row specifically.

## Scope

- In scope:
  - Wire the Fullscreen/Windowed radio's change to a real callback threaded
    from `SettingsScreenPanel` through to `Main.java`, applied to the live
    `JFrame`.
  - **Windowed:** frame is resizable (decorated, normal title bar).
    `GamePanel`'s `Camera` (currently constructed with a fixed viewport of
    `GAME_WINDOW_WIDTH`/`GAME_WINDOW_HEIGHT`, see `GamePanel.java:24` and
    `Camera.java`) becomes viewport-size-aware, deriving its viewport
    dimensions from the panel's live pixel size instead of the fixed
    constants. `WorldScene.renderWorld` already draws the entire map with no
    clipping to a fixed viewport, so once the camera's viewport tracks the
    panel's actual size, resizing the window naturally reveals more (or
    less) of the generated map around the player, who stays centered — no
    letterboxing, no content scaling/stretching.
  - **Fullscreen:** a borderless fullscreen window — an undecorated `JFrame`
    maximized to the current `GraphicsDevice`'s screen bounds — not
    exclusive fullscreen (`GraphicsDevice.setFullScreenWindow`). Borderless
    was chosen over exclusive fullscreen for robustness across alt-tab and
    multi-monitor setups.
  - Remove the F5 hot-reset dev feature entirely (`Main.resetGame`,
    `Main.keyListen`, and the F5 keybinding wiring, `Main.java:188-208`) —
    superseded by this issue's decision to apply the persisted window mode
    at every real launch (see Clarifications below), which made F5's
    dispose/rebuild cycle both redundant as a "pick up the latest setting"
    mechanism and an awkward extra path to keep the live window-mode
    application logic correct on.
- Out of scope (tracked in [#135](https://github.com/SwiftFaze/Veil/issues/135)):
  persisting the chosen window mode (or any other setting) across full
  application restarts. No config-persistence system exists anywhere in this
  codebase yet — that's net-new infrastructure, not a small addition here.
  This issue only makes the toggle live-apply for the current run.

## Actors

The player, via the Settings screen's keyboard-navigated Fullscreen/Windowed
row.

## Desired behavior

From the Settings screen, moving the Fullscreen/Windowed row's highlight
left/right immediately applies to the live game window: moving right switches
Windowed -> Fullscreen and the window becomes an undecorated, screen-filling
borderless window; moving left switches back to a resizable, decorated
Windowed frame. Resizing the Windowed frame changes how much of the map is
visible around the player, with the player staying centered. The F5 hot-reset
dev feature is removed entirely as part of this change (see Clarifications) —
every real launch already applies the persisted window mode directly. No
setting survives a full app restart yet (see
[#135](https://github.com/SwiftFaze/Veil/issues/135)).

## Constraints / non-functional notes

None beyond the usual (see CLAUDE.md function-length/complexity/coverage
budgets). `Camera`/`GamePanel` changes must keep `ModuleDependencyTest`'s
engine/UI dependency direction intact — `Camera` lives outside
`com.swiftfaze.veil.ui` today and must stay engine-only.

## Open questions

None remaining — scope settled via a grilling session on 2026-09-02.

## Clarifications

- Q: Does the app's initial launch (including the F5 hot-reset rebuild) apply
  the already-persisted Fullscreen/Windowed setting to the real window
  immediately, or does every launch always start Windowed regardless of the
  persisted value until the player touches the row in that session?
  A: Apply on launch. `Main.loadGame()` reads `SettingsConfig.getFullscreen()`
  and configures the real `JFrame` to match at construction time, for both a
  fresh launch and an F5 rebuild. Since F5 rebuilds via the same `loadGame()`
  path against the same `settings.json`, this also satisfies the F5-survival
  requirement with no separate in-memory state-tracking needed — one source
  of truth (the persisted config) drives both.
  Affects: general (frame construction), F5 hot-reset scenario.
- Q: Should Settings screen's "Reset to Defaults" also live-apply the window
  mode (switch the real window back to Windowed immediately) when it resets
  Fullscreen, not just persist/display the reset value?
  A: Yes. The live-window-mode callback is wired into `resetAllToDefaults()`
  as well as `moveLeft`/`moveRight`'s `syncAndPersist()`, so the displayed
  value, the persisted value, and the actual window state can never disagree.
  Affects: Reset to Defaults scenario.
- Q: Should the Windowed frame enforce a minimum size, so resizing it down
  can never shrink the camera's viewport to 0 or a negative tile count?
  A: Yes, enforce a small sane minimum (e.g. `JFrame.setMinimumSize` covering
  a handful of tiles in each dimension) so the viewport is never degenerate.
  Affects: windowed resize edge-case scenario.
- Q: (Mid-implementation follow-up, asked by the user directly, not part of
  the original grilling round) "is the F5 key feature removed? it should be" —
  confirmed via follow-up: remove the F5 hot-reset dev feature
  (`Main.resetGame`, `Main.keyListen`, the F5 keybinding, `Main.java:188-208`)
  entirely.
  A: Removed. This supersedes the first Clarification entry above's mention
  of F5-survival: there is no more F5 hot-reset to survive across, since the
  feature no longer exists. That entry's core decision — initial launch (and
  every real launch) applies the persisted `SettingsConfig.getFullscreen()`
  value to the real window — still stands and is what actually made F5 both
  redundant (relaunching already picks up the latest setting) and awkward to
  keep correct (an extra dispose/rebuild path duplicating logic that launch
  already does once).
  Affects: Scope (F5-survival requirement dropped, replaced by F5-removal),
  Desired behavior, any scenario that would have exercised F5.
  Note: this deviates from GitHub issue #136's original written text, which
  explicitly required F5-survival. The issue itself has not been edited to
  match — flagged for the user to decide whether to update it.
