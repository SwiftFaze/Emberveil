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

## Integration tests

- Location: `src/test/java/**/*IT.java`
- Runner: Failsafe, bound to `integration-test`/`verify` — **not** run by
  plain `mvn test**. Run them with `mvn verify`.
- Reserved for tests that need real I/O or cross-class wiring that unit
  tests shouldn't pay for on every run (e.g. `BuildingLoaderIT`, which
  loads an actual JSON fixture off disk instead of mocking the file read).
- Run a single integration test: `mvn verify -Dit.test=BuildingLoaderIT`

## Everything together

`mvn verify` runs all three layers: unit tests and the Cucumber suite via
Surefire, then integration tests via Failsafe.
