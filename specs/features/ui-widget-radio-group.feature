Feature: Radio group widget
  A single-select radio group widget built on the shared
  Widget/WidgetTheme framework from ui-component-framework.feature.
  Vertical (Up/Down) layout only, reusing the existing MENU_UP/MENU_DOWN/
  MENU_CONFIRM keybindings — no real in-game screen consumes it yet (see
  specs/intent/ui-widget-library.md).

  Scenario: Navigating a radio group down moves the highlighted option to the next one
    Given a radio group with options "Warrior", "Mage", "Rogue" and "Warrior" highlighted
    And the radio group has keyboard focus
    When the "Down" key is pressed
    Then the highlighted option is "Mage"

  Scenario: Moving up from the first option wraps to the last option
    Given a radio group with options "Warrior", "Mage", "Rogue" and "Warrior" highlighted
    And the radio group has keyboard focus
    When the "Up" key is pressed
    Then the highlighted option is "Rogue"

  Scenario: Confirming a radio group's highlighted option with Enter selects it
    Given a radio group with options "Warrior", "Mage", "Rogue" and "Mage" highlighted
    And the radio group has keyboard focus
    When the "Enter" key is pressed
    Then the selected option is "Mage"

  Scenario: Selecting a new option deselects the previous one
    Given a radio group with options "Warrior", "Mage", "Rogue" and "Warrior" selected
    And the radio group has keyboard focus
    When the "Down" key is pressed
    And the "Enter" key is pressed
    Then the selected option is "Mage"
    And "Warrior" is not selected

  # Non-goals:
  #   - Wiring this widget into any real in-game screen — no consumer
  #     exists yet (see specs/intent/ui-widget-library.md's Constraints).
  #   - Horizontal (Left/Right) layout — deferred per
  #     specs/intent/ui-widget-library.md's Clarifications; only the
  #     table widget gets new MENU_LEFT/MENU_RIGHT keybindings.
  #   - Any mouse/pointer handling — this game is keyboard-only by design.
  #
  # Risks:
  #   - Real Swing focus-transfer is not simulated headlessly here,
  #     matching the existing precedent in ui-component-framework.feature.
  #   - This widget has no real consumer to validate its shape against;
  #     the vertical-only-layout decision above was made autonomously
  #     (see specs/intent/ui-widget-library.md's Clarifications) and
  #     should be revisited if a future consumer needs horizontal layout.
  #
  # Open questions:
  #   - See specs/intent/ui-widget-library.md's Open questions — the
  #     first real consumer of these widgets is still unidentified.
