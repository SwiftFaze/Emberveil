# Intent: Title Screen Exit

- **Slug(s):** title-screen-exit (matches `/specs/features/title-screen-exit.feature`)
- **Author:** rwoolley
- **Date:** 2026-09-04
- **Source:** [GitHub issue #147](https://github.com/SwiftFaze/Veil/issues/147)

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file) — standard path; scenario added to the
      existing `startup-welcome-screen.feature` rather than a new file, see
      Clarifications below
- [ ] Approved by human — N/A, standard path (no blocking gate)
- [x] Implemented
- [ ] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5";
      still needs a human to run `mvn compile exec:java` and confirm
      selecting Exit actually quits the app
- [x] Acceptance tests passing
- [x] Mutation testing passed — N/A, `Main` is excluded from pitest's
      `targetClasses` in `pom.xml` (Swing view/composition-root classes with
      no meaningful unit coverage); no other production code changed
- [x] Documentation updated (`docs/`, and the wiki if player-facing) —
      `docs/screens.md` updated; no wiki update needed, this isn't
      player-visible game data (class stats/attributes/combat formulas)

## Problem

The title screen's "Exit" menu item is currently a no-op — `TitleScreenPanel`
lists it (`TitleScreenPanel.java:42`) alongside Continue/New/Load/Settings,
but `Main.handleMenuSelection` (`Main.java:136-146`) only branches on `"New"`
and `"Settings"`, so selecting "Exit" does nothing.

## Scope

- In scope: wire the title screen's "Exit" selection to actually quit the
  application (clean shutdown, e.g. disposing the `JFrame`/calling
  `System.exit`).
- Out of scope: the pause menu's "Exit to Main Menu" action (tracked in
  [#148](https://github.com/SwiftFaze/Veil/issues/148)) — that's a different
  screen and a different action (return to title vs. quit the app entirely);
  no shared implementation is assumed between the two.

## Actors

Player on the title screen.

## Desired behavior

Selecting "Exit" from the title screen closes the application cleanly, the
same as closing the window via its title bar would.

## Constraints / non-functional notes

None beyond the usual.

## Open questions

None — scope settled via a grilling session on 2026-09-02 (per the source
issue).

## Clarifications

- The acceptance scenario for this feature landed in the existing
  `specs/features/startup-welcome-screen.feature` rather than a new
  `title-screen-exit.feature`. That file already owns the title screen's
  menu (New/Settings/Continue/Load scenarios) and its Non-goals section
  already anticipated Exit ("Exit's actual window-close behavior beyond
  'the game exits' — standard JFrame close, nothing new to prove"), so
  Exit is the same concept, not a distinct one.
- The scenario proves the menu wiring dispatches "Exit" to
  `TitleScreenPanel`'s callback (mirroring how "New"/"Settings" are
  tested) — it does not invoke `Main.handleMenuSelection`'s real `Exit`
  branch or assert `System.exit` actually runs, since that would kill the
  test JVM. `Main.class` is already excluded from JaCoCo coverage
  (`pom.xml`), consistent with the pre-existing `"New"`/`"Settings"`
  branches also having no direct unit test.
