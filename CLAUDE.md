# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

@development-workflow.md

## Project

Veil is a 2D ASCII-tile desktop RPG built with Java 17 Swing (no game engine). Rendering draws Unicode/ASCII glyphs with `Graphics2D.drawString` onto a `JPanel`; there is no sprite/texture pipeline for tiles.

## Build & run

- Build: `mvn compile`
- Unit tests + Cucumber acceptance tests: `mvn test`
- Everything, including integration tests: `mvn verify`
- Run a single unit test: `mvn test -Dtest=PlayerTest#movingRightIncreasesX`
- Run a single Cucumber scenario: `mvn test -Dcucumber.filter.name="A newly created player starts as a Warrior"`
- Run a single integration test: `mvn verify -Dit.test=BuildingLoaderIT`
- See `docs/testing.md` for the full breakdown of the three test layers (unit / acceptance / integration) and why they're separated.
- Run the game from source: `mvn compile exec:java` (no packaging or version number needed).
- Run the dev-only class/stats sandbox instead of the game: `mvn compile exec:java -Dexec.mainClass=com.swiftfaze.veil.sandbox.ClassSandbox` (not part of the packaged build — see `docs/architecture.md`).
- `mvn package` also produces `target/Veil-<version>-app.jar`, a runnable fat jar (`java -jar` it directly) — see `docs/release.md` for how CI turns that into Windows/Linux/macOS installers on release.
- Press **F5** while the game window is focused to hot-reset the scene (`Main.resetGame` disposes the `JFrame` and rebuilds it from scratch — see `Main.java`).
- Mutation testing (workflow Step 6): `mvn org.pitest:pitest-maven:mutationCoverage` — report lands in `target/pit-reports/`. See `docs/testing.md`.

## CI / releases

- `.github/workflows/ci.yml` runs `mvn verify` on every PR/push to `master`/`develop`; it's a required status check on both (branch protection).
- Versioning and changelog generation are fully automatic via Release Please — see `docs/release.md`. Don't hand-edit `pom.xml`'s `<version>` or write `CHANGELOG.md` entries by hand; they're derived from Conventional Commits.
- Two release channels: `master` → stable (`vX.Y.Z`), `develop` → beta prereleases (`vX.Y.Z-beta.N`, marked "Pre-release" on GitHub). Merging `develop` into `master` is what promotes accumulated beta work into the next stable release.
- **No direct commits to `master`, ever — including for admins.** Branch protection has `enforce_admins` on, so this is enforced, not just a convention. The only two ways changes reach `master`:
  - A PR from `develop` (a release promotion), or
  - A PR from a `hotfix/*` branch (an urgent fix that can't wait for the next promotion — branch off `master`, fix, PR back to `master`, then bring the same fix into `develop` too so it isn't lost on the next promotion).
  - The `master-source-check` CI job (in `.github/workflows/ci.yml`) enforces this mechanically: it fails any PR into `master` whose head branch isn't `develop`, `hotfix/*`, or Release Please's own `release-please--branches--master`.

## Architecture

See `docs/architecture.md` for the full write-up (entry point/window assembly, the `GamePanel` render loop, the flat single-layer `WorldScene`/`Tile` world model, JSON building blueprints, player movement, Key Bindings-based input, the `TerminalPanel`/`SelectableMenu` UI shell, JSON-driven player classes/stats, the class/stats sandbox, and the `DrawableAsciiEntity` rendering contracts).

## Spec-first workflow layout

This repo follows the intent → spec → approval → implementation pipeline (see the global development workflow instructions):

- `/specs/intent/<feature-slug>.md` — human-written intent docs, source of truth for *why*. Copy `specs/intent/TEMPLATE.md` to start one. Starting from an existing GitHub issue instead? Use the `spec-intent` skill (`.claude/skills/spec-intent/`) — give it the issue number and it creates/links a branch, moves the tracker item to In progress on the VEIL project board, and derives `intent.md` from the issue's description. Brainstorming a not-yet-ready idea straight into an issue (no repo file) instead? Use the `brainstorm-issue` skill (`.claude/skills/brainstorm-issue/`).
- `/specs/features/<feature-slug>.feature` — Gherkin specs generated from the matching intent doc, executed by Cucumber via `mvn test` (see `RunCucumberTest`). **One `.feature` file per distinct concept** — an intent covering multiple unrelated things (a new class *and* a new biome) produces multiple `.feature` files (`class-warrior.feature`, `biome-jungle.feature`), never one bundled file. See `specs/features/README.md`.
- `docs/` — narrative/reference documentation (architecture, testing) kept up to date as part of each feature's definition of done, not left to be reverse-engineered from diffs.
- `specs/intent/default-player-class.md` + `specs/features/default-player-class.feature` are a worked example proving the intent → spec → Cucumber pipeline runs end-to-end; use their shape as the template for the next real feature rather than editing them.

**Step 7 (Documentation) for this repo also covers the player-facing [GitHub wiki](https://github.com/SwiftFaze/Veil/wiki)**, not just `docs/`: any change to a class's base stats, a new class/attribute, a changed combat formula, or other player-visible game data must update the matching wiki page in the same change. See `docs/wiki.md` for what's covered and how to edit it (it's a separate git repo, no PR needed).

### Repo-specific Step 4.5 — Manual playtest (Human)

Inserted between the global pipeline's Step 4 (Implementation) and Step 5
(Acceptance tests): after implementation lands, the human runs the game
(`mvn compile exec:java`) and actually plays through the changed behavior
before acceptance tests get wired up.

- **Why this exists:** `mvn verify` and Cucumber can confirm the code does
  what the spec says, but not whether movement, menu navigation, or
  rendering actually *feel* right — that's a judgment call only a human
  playtesting the running game can make.
- **Model:** no model — a human decision gate, like Step 3.
- **For a multi-area change** (e.g. a restructure spanning several
  `.feature` files), playtest each area right after it's implemented, not
  only once at the end — this mirrors the per-area `mvn verify`
  checkpoints already used in intent docs' Verification sections.
- Note what was tested and any issues found (PR description or a status
  note) so Step 7 documentation and PR review can see it.
- No feature is "done" without this playtest, same as Steps 5-7 aren't
  optional per the global workflow's notes for the agent.
