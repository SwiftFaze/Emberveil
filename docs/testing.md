# Testing

Three test layers, run at different points, kept in separate file-naming
conventions so Maven can tell them apart automatically:

## Unit tests

- Location: `src/test/java/**/*Test.java`
- Runner: Surefire, bound to `mvn test`
- Fast, no real I/O — the norm for new production code.
- Run a single test: `mvn test -Dtest=PlayerTest#movingRightIncreasesX`

## Acceptance tests (Cucumber)

- `.feature` files (Gherkin) live in `/specs/features/`, one per feature,
  each generated from the matching `/specs/intent/<slug>.md`. They are
  copied onto the test classpath at build time (`testResources` config in
  `pom.xml`) so Cucumber can discover them via `@SelectPackages("features")`.
- Step definitions live in `src/test/java/com/swiftfaze/veil/steps/`.
- `RunCucumberTest` (`src/test/java/com/swiftfaze/veil/RunCucumberTest.java`)
  is a JUnit 5 `@Suite` that includes the Cucumber engine — because its name
  matches Surefire's `*Test.java` pattern, the whole Cucumber suite runs
  under plain `mvn test` alongside the unit tests.
- Run a single scenario: `mvn test -Dcucumber.filter.name="A newly created player starts as a Warrior"`
- `specs/features/default-player-class.feature` +
  `steps/DefaultPlayerClassSteps.java` are a worked example proving this
  wiring end-to-end — copy their shape for the next real feature.
- A `.feature` file generated ahead of its implementation (e.g. an intent
  covering several areas, spec-drafted all at once but implemented one
  area at a time) should be tagged `@pending` at the top, and excluded via
  `RunCucumberTest`'s `not @pending` tag filter — otherwise `mvn test`
  fails on undefined steps for scenarios that don't have an implementation
  yet. Remove the tag from a file only once its step definitions exist.

## Integration tests

- Location: `src/test/java/**/*IT.java`
- Runner: Failsafe, bound to `integration-test`/`verify` — **not** run by
  plain `mvn test**. Run them with `mvn verify`.
- Reserved for tests that need real I/O or cross-class wiring that unit
  tests shouldn't pay for on every run (e.g. `ModLoaderIT`, which loads
  actual mod content off disk instead of mocking the file read).
- Run a single integration test: `mvn verify -Dit.test=ModLoaderIT`

## Everything together

`mvn verify` runs all three layers: unit tests and the Cucumber suite via
Surefire, then integration tests via Failsafe.

## Mutation testing (workflow Step 6)

- Runner: PIT (`org.pitest:pitest-maven`, with `pitest-junit5-plugin` so it
  runs through the JUnit Platform and picks up both plain unit tests and
  Cucumber scenarios).
- Run it: `mvn org.pitest:pitest-maven:mutationCoverage` — HTML report at
  `target/pit-reports/index.html` (per-package/per-class breakdowns,
  including a *line* coverage figure distinct from mutation score).
- `<targetClasses>`/`<targetTests>` in `pom.xml` scope this to classes with
  real unit tests — pure Swing view/wiring classes with no meaningful unit
  coverage (`Main`, layout-only panels, `ClassSandbox`'s UI entry point)
  are excluded rather than left to report a wall of untested mutants.
- This is the check on the unit tests themselves (CLAUDE.md's changed-file
  coverage constraint is easy to satisfy with weak assertions; mutation
  score catches that) — not a substitute for acceptance tests or the
  Step 4.5 manual playtest.
