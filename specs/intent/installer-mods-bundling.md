# Intent: Installer Build Bundles mods/ Folder Alongside the Executable

- **Slug(s):** installer-mods-bundling (matches `/specs/features/installer-mods-bundling.feature`)
- **Author:** rwoolley
- **Date:** 2026-08-29

## Status

- [x] Intent drafted
- [x] Spec drafted (`.feature` file)
- [x] Approved by human
- [x] Implemented
- [x] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5"
  — Windows direct-`.exe` launch verified locally (see Clarifications) and
  confirmed by the human. Start Menu/desktop shortcut and the Debian/macOS
  installers weren't verified against a real `jpackage --type exe|deb|pkg`
  build (needs WiX or their native OS, not available in this environment)
  — human accepted this as sufficient rather than blocking on it.
- [x] Acceptance tests passing — N/A, this feature is manual-verification-only, see the `.feature` file's header
- [x] Mutation testing passed — N/A, no Java code changed
- [x] Documentation updated (`docs/`, and the wiki if player-facing)

## Problem

Closed issue #48 (mod-loader phase 1) explicitly scoped: "The jpackage
installer build (`docs/release.md`) needs to bundle a `mods/` folder
alongside the executable, not just the fat jar" — and the matching intent
doc (`specs/intent/mod-loader.md`) has this checked off as implemented.
It wasn't. The `build-installers` job in `.github/workflows/release.yml`
("Stage jpackage input" step) only copies `target/Veil-*-app.jar` into
`target/jpackage-input/` before running `jpackage` — no `mods/` directory
is ever staged, for any of the three platforms (Windows `.exe`, Debian
`.deb`, macOS `.pkg`).

`ModLoader.load()` always resolves mods relative to the JVM's working
directory (`Paths.get("mods")` — see `PlayerInfo.java`,
`TileTestScene2.java`, `ClassSandboxModel.java`), and per the mod-loader
intent's own clarification, a missing `mods/` directory is a broken
install, not a supported fallback. So a real install to e.g.
`C:\Program Files\Veil` currently ships with no `mods/core/` content at
all — no buildings, tiles, classes, or stats can load. This works today
only by accident of running from the repo root during development
(`mvn compile exec:java`), where `mods/core/` is checked into the repo.

## Scope

- In scope:
  - Stage `mods/core/` (the repo's checked-in core mod content) into
    `target/jpackage-input/mods/core/` before the `jpackage` invocation, so
    it lands inside the installed application's own directory alongside
    the executable — for all three matrix targets (Windows/Ubuntu/macOS),
    since it's the same "Stage jpackage input" step for all of them.
  - Verify (as part of implementation, not left to guesswork) that the app
    actually launches with its working directory set to the install
    directory in each launch path jpackage produces (Start Menu shortcut,
    desktop shortcut, direct `.exe` launch) — `jpackage`'s Windows
    packaging doesn't unconditionally guarantee this, and if it doesn't,
    the fix needs to also address working-directory resolution, not just
    staging the files.
  - Update `docs/release.md`'s "Installer builds" section and its
    "Building an installer locally" walkthrough to reflect the `mods/`
    staging step.
- Out of scope:
  - Any change to where `ModLoader` looks for `mods/` at runtime, or a
    fallback search path (e.g. resolving relative to the jar's own
    location instead of cwd) — only pursue this if the verification above
    shows the cwd assumption doesn't hold; don't preemptively rearchitect
    it.
  - Bundling anything beyond `mods/core/` — third-party mods are a
    player-installed concern, not something the installer ships.
  - In-game mod management UI — already out of scope per the original
    mod-loader intent.

## Actors

- The release build pipeline (`.github/workflows/release.yml`'s
  `build-installers` job).
- Players installing the game fresh via any of the three installers.

## Desired behavior

- After running an installer (Windows `.exe`, Debian `.deb`, or macOS
  `.pkg`) produced by this pipeline, the install directory (e.g.
  `C:\Program Files\Veil` on Windows) contains a `mods/core/` folder with
  the same contents as the repo's `mods/core/`.
- Launching the installed game via its normal entry point (Start Menu
  shortcut on Windows, applications menu entry on Linux/macOS) finds and
  loads `mods/core/` successfully — buildings, tiles, classes, and stats
  all present, matching a dev run from the repo root.
- `mvn compile exec:java` (dev run) is unaffected — it already works via
  the repo's checked-in `mods/core/` relative to the project root.

## Constraints / non-functional notes

- Follows the repo's existing intent → spec → approval → implementation
  pipeline (see root `CLAUDE.md`).
- No embedded scripting or mod-format changes — this is purely a build
  pipeline / packaging fix.

## Open questions

- Does jpackage's Windows Start Menu shortcut set the process's working
  directory to the install directory by default, or does it need an
  explicit `--win-shortcut`-adjacent flag / launcher config to do so?
  Needs to be verified against the actual installer output, not assumed
  — see the "Scope" verification bullet above.

## Clarifications

- Q: How should this feature's Gherkin scenarios map onto this repo's
     test layers, given none of them have an existing Java code path
     (unlike every other `.feature` file in this repo)?
  A: Keep `specs/features/installer-mods-bundling.feature` as the
     human-reviewed spec of record (per root `CLAUDE.md`'s review table),
     but never wire Cucumber step definitions for any of it —
     verification happens by actually building and running each
     installer, as an extension of the repo's Step 4.5 manual-playtest
     gate, not via `mvn test`/`mvn verify`. Matches the precedent already
     set by `specs/features/mod-loader.feature`, which listed this exact
     concern ("the jpackage installer bundling a mods/ folder alongside
     the executable") as a non-goal, "verified manually, not exercised
     via Cucumber."
  Affects: general (testing approach for the whole feature)

- Q: The "stage mods/core into target/jpackage-input" scenario doesn't
     need an actual jpackage/installer run, unlike the OS-launch
     scenarios — should it get a real automated check anyway, or is it
     manual-only too?
  A: Manual-only too, uniformly with the rest of the file — carving out
     one scenario for automation while the rest stays manual would add a
     second testing story to this one feature for a build step that's
     easy to eyeball in CI logs, which isn't worth the added abstraction.
  Affects: "Release pipeline stages core mod content alongside the
  packaged jar" scenario

- Q: Should a failure-case scenario be added for `mods/core` being
     unexpectedly missing/empty from the checkout at staging time?
  A: No — `mods/core` is permanently checked into the repo, so this can't
     happen in practice; not worth a scenario for an unreachable case.
  Affects: general (no failure-case scenario added)

- Q: Does jpackage's Windows Start Menu shortcut set the process's
     working directory to the install directory by default, or does it
     need explicit configuration?
  A: Not resolvable from Oracle's published jpackage docs (checked
     directly — `--input`/`--app-content` placement and shortcut working
     directory aren't specified there). Leave as "verify during Step 4
     implementation" per this doc's existing Scope verification bullet —
     only building and inspecting a real installer will answer this.
  Affects: general (open question remains open, resolved empirically
  during implementation rather than now)

- Q: (Self-resolved during implementation) What actually needs to change
     to get `mods/core` onto disk where `ModLoader` can find it in a real
     install?
  A: Not just staging `mods/core` into `target/jpackage-input` alongside
     the jar — verified locally (via `jpackage --type app-image`, no WiX
     needed) that `jpackage --input` content lands inside an `app/`
     subfolder of the installed application (e.g. `Veil/app/...`), while
     `ModLoader` resolves `mods/` relative to the running game's working
     directory, which a direct `Veil.exe` launch confirmed is the
     top-level install directory (`Veil/`), not `app/`. Fixed by passing
     `--app-content mods` to `jpackage` instead, which places the repo's
     `mods/` directory at the top level, alongside the executable —
     confirmed by launching the resulting `Veil.exe` directly, which
     loaded every tile/building/class/item/quest from `mods/core`. Start
     Menu/desktop shortcuts and the Debian/macOS installers weren't
     re-verified beyond this (see Status above) since they need tooling
     not available in this environment, but nothing about `--app-content`
     placement is shortcut- or installer-type-specific — it's the same
     `jpackage` invocation for all three matrix targets.
  Affects: general (this is now how `.github/workflows/release.yml` and
  `docs/release.md` bundle `mods/`); the "Scope" verification bullet
  above ("does the app launch with cwd = install directory") turned out
  to hold for direct-exe Windows launches, so `ModLoader`'s runtime
  resolution logic stayed untouched per the original out-of-scope note.

## Source

[GitHub issue #62](https://github.com/SwiftFaze/Veil/issues/62)

Follows up on closed issue #48, whose own stated scope ("jpackage
installer build ... needs to bundle a mods/ folder alongside the
executable") was not actually delivered.
