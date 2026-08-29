Feature: Class sandbox panel selection
  ClassSandboxPanel wires Up/Down key bindings to SelectableMenu and
  reflects the current selection in its labels and stats display, so the
  dev-only class/stats sandbox tool is actually usable, not just backed
  by a correct model.

  Scenario: The initially selected class is highlighted and its stats shown
    Given a class sandbox panel is showing
    Then the first class's label is colored "#eeb392"
    And the stats label shows the first class's computed stats

  Scenario: Moving the selection down highlights the next class
    Given a class sandbox panel is showing
    When the down-bound action fires
    Then the previously selected class's label is white
    And the newly selected class's label is colored "#eeb392"
    And the stats label shows the newly selected class's computed stats

  Scenario: Moving the selection up from the first class wraps to the last
    Given a class sandbox panel is showing
    When the up-bound action fires
    Then the last class's label is colored "#eeb392"
    And the stats label shows the last class's computed stats

  # Non-goals:
  #   - Any functional/behavior change to ClassSandboxPanel — this is
  #     test-coverage only, per specs/intent/class-sandbox-panel-and-building-exception-coverage.md.
  #   - ClassSandboxModel's own stat computation — already covered by
  #     class-stats-sandbox.feature; here the model is treated as a
  #     correct dependency and only the panel's wiring/display is checked.
  #
  # Open questions:
  #   - None outstanding — see
  #     specs/intent/class-sandbox-panel-and-building-exception-coverage.md.
