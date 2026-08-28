# Feature specs (Gherkin)

One `<feature-slug>.feature` file per feature, generated from the matching
file in `/specs/intent/`. Do not hand-edit a `.feature` file ahead of its
intent doc — update the intent doc first, then regenerate.

These files are copied onto the test classpath at build time (see the
`testResources` config in `pom.xml`) and executed by `RunCucumberTest` via
`mvn test`. Step definitions live under
`src/test/java/com/swiftfaze/emberveil/steps/`. See `docs/testing.md` for
the full test-layer breakdown.
