Feature: Radio group widget
  A single-select radio group widget built on the shared
  Widget/WidgetTheme framework from ui-component-framework.feature.
  Horizontal (Left/Right) layout by default, using the new MENU_LEFT/
  MENU_RIGHT keybindings shared with the table widget (see
  specs/intent/ui-widget-library.md's Clarifications — a real consumer,
  issue #54's settings screen, needs a Fullscreen/Windowed toggle and a
  font cycler navigated this way).

  Scenario: Navigating a radio group right moves the highlighted option to the next one
    Given a radio group with options "Windowed", "Fullscreen" and "Windowed" highlighted
    And the radio group has keyboard focus
    When the "Right" key is pressed
    Then the highlighted option is "Fullscreen"

  Scenario: Moving left from the first option wraps to the last option
    Given a radio group with options "Windowed", "Fullscreen" and "Windowed" highlighted
    And the radio group has keyboard focus
    When the "Left" key is pressed
    Then the highlighted option is "Fullscreen"

  Scenario: Confirming a radio group's highlighted option with Enter selects it
    Given a radio group with options "Windowed", "Fullscreen" and "Fullscreen" highlighted
    And the radio group has keyboard focus
    When the "Enter" key is pressed
    Then the selected option is "Fullscreen"

  Scenario: Selecting a new option deselects the previous one
    Given a radio group with options "Windowed", "Fullscreen" and "Windowed" selected
    And the radio group has keyboard focus
    When the "Right" key is pressed
    And the "Enter" key is pressed
    Then the selected option is "Fullscreen"
    And "Windowed" is not selected

  # Non-goals:
  #   - Wiring this widget into any real in-game screen — issue #54
  #     (startup welcome menu + settings screen) is the identified real
  #     consumer, but wiring it up is that issue's job, not this one's.
  #   - Vertical (Up/Down) layout — not ruled out, but horizontal is the
  #     proven default per the real consumer found in #54; see
  #     specs/intent/ui-widget-library.md's Clarifications.
  #   - Any mouse/pointer handling — this game is keyboard-only by design.
  #
  # Risks:
  #   - Real Swing focus-transfer is not simulated headlessly here,
  #     matching the existing precedent in ui-component-framework.feature.
  #   - This orientation decision reverses an earlier autonomous call
  #     (vertical-only) made before issue #54 was checked — see
  #     specs/intent/ui-widget-library.md's Clarifications for the full
  #     history; still flagged for human confirmation at Step 3 approval.
  #
  # Open questions:
  #   - None for this widget specifically — issue #54 resolved its
  #     orientation. specs/intent/ui-widget-library.md's remaining open
  #     question (first consumer of the pattern-field widget) doesn't
  #     apply here.
