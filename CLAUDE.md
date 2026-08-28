# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Emberveil is a 2D ASCII-tile desktop RPG built with Java 17 Swing (no game engine). Rendering draws Unicode/ASCII glyphs with `Graphics2D.drawString` onto a `JPanel`; there is no sprite/texture pipeline for tiles (a `grass.png` resource and a `DrawableImageEntity` interface exist but are currently unused by any scene).

## Build & run

- Build: `mvn compile`
- Unit tests + Cucumber acceptance tests: `mvn test`
- Everything, including integration tests: `mvn verify`
- Run a single unit test: `mvn test -Dtest=PlayerTest#movingRightIncreasesX`
- Run a single Cucumber scenario: `mvn test -Dcucumber.filter.name="A newly created player starts as a Warrior"`
- Run a single integration test: `mvn verify -Dit.test=BuildingLoaderIT`
- See `docs/testing.md` for the full breakdown of the three test layers (unit / acceptance / integration) and why they're separated.
- Run the game from source: `mvn compile exec:java` (no packaging or version number needed).
- `mvn package` also produces `target/Emberveil-<version>-app.jar`, a runnable fat jar — see `docs/release.md` for how CI turns that into Windows/Linux/macOS installers on release.
- Press **F5** while the game window is focused to hot-reset the scene (`Main.resetGame` disposes the `JFrame` and rebuilds it from scratch — see `Main.java`).

## CI / releases

- `.github/workflows/ci.yml` runs `mvn verify` on every PR/push to `master`/`develop`; it's a required status check on both (branch protection).
- Versioning and changelog generation are fully automatic via Release Please — see `docs/release.md`. Don't hand-edit `pom.xml`'s `<version>` or write `CHANGELOG.md` entries by hand; they're derived from Conventional Commits.
- Two release channels: `master` → stable (`vX.Y.Z`), `develop` → beta prereleases (`vX.Y.Z-beta.N`, marked "Pre-release" on GitHub). Merging `develop` into `master` is what promotes accumulated beta work into the next stable release.

## Architecture

See `docs/architecture.md` for the full write-up (entry point/window assembly, the `GamePanel` render loop and Z-level/fog handling, the `WorldScene`/`Tile` world model, JSON building blueprints, player movement, the RPG stats/class strategy pattern, and the `DrawableAsciiEntity` rendering contracts).

## Spec-first workflow layout

This repo follows the intent → spec → approval → implementation pipeline (see the global development workflow instructions):

- `/specs/intent/<feature-slug>.md` — human-written intent docs, source of truth for *why*.
- `/specs/features/<feature-slug>.feature` — Gherkin specs generated from the matching intent doc, executed by Cucumber via `mvn test` (see `RunCucumberTest`).
- `docs/` — narrative/reference documentation (architecture, testing) kept up to date as part of each feature's definition of done, not left to be reverse-engineered from diffs.
- `specs/intent/default-player-class.md` + `specs/features/default-player-class.feature` are a worked example proving the intent → spec → Cucumber pipeline runs end-to-end; use their shape as the template for the next real feature rather than editing them.

**Step 7 (Documentation) for this repo also covers the player-facing [GitHub wiki](https://github.com/SwiftFaze/Emberveil/wiki)**, not just `docs/`: any change to a class's base stats, a new class/attribute, a changed combat formula, or other player-visible game data must update the matching wiki page in the same change. See `docs/wiki.md` for what's covered and how to edit it (it's a separate git repo, no PR needed).
