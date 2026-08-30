Feature: Table widget
  A keyboard-navigable table widget (rows/columns of terminal-style cells)
  built on the shared Widget/WidgetTheme framework from
  ui-component-framework.feature. No real in-game screen consumes it yet
  (see specs/intent/ui-widget-library.md) — proven through isolated
  widget-level scenarios, same style as the list/button scenarios in
  ui-component-framework.feature.

  Scenario: Navigating a table widget down moves the selection to the next row
    Given a table widget with rows "Sword", "Shield", "Potion" and row 1 selected
    And the table widget has keyboard focus
    When the "Down" key is pressed
    Then the selected row is 2

  Scenario: Navigating a table widget right moves the selection to the next column
    Given a table widget with columns "Name", "Type", "Value" and column 1 selected
    And the table widget has keyboard focus
    When the "Right" key is pressed
    Then the selected column is 2

  Scenario: Moving up from the first row wraps to the last row
    Given a table widget with rows "Sword", "Shield", "Potion" and row 1 selected
    And the table widget has keyboard focus
    When the "Up" key is pressed
    Then the selected row is 3

  Scenario: Moving right from the last column wraps to the first column
    Given a table widget with columns "Name", "Type", "Value" and column 3 selected
    And the table widget has keyboard focus
    When the "Right" key is pressed
    Then the selected column is 1

  Scenario: A table widget can be configured to stop instead of wrap
    Given a table widget with rows "Sword", "Shield", "Potion" and row 1 selected
    And the table widget's wrap-around is disabled
    And the table widget has keyboard focus
    When the "Up" key is pressed
    Then the selected row is 1

  Scenario: Confirming a table widget's selection with Enter confirms the whole row
    Given a table widget with rows "Sword", "Shield", "Potion" and row 2 selected
    And the table widget has keyboard focus
    When the "Enter" key is pressed
    Then the confirmed row is "Shield"

  # Non-goals:
  #   - Wiring this widget into any real in-game screen — no consumer
  #     exists yet (see specs/intent/ui-widget-library.md's Constraints).
  #   - Cell-level confirm (as opposed to row-level) — decided against in
  #     specs/intent/ui-widget-library.md's Clarifications.
  #   - Scrolling behavior specifics — TerminalScrollBarUI is reused
  #     as-is from the existing framework, nothing new to prove there.
  #   - Any mouse/pointer handling — this game is keyboard-only by design.
  #
  # Risks:
  #   - Real Swing focus-transfer is not simulated headlessly here,
  #     matching the existing precedent in ui-component-framework.feature
  #     — "keyboard focus" Given/Then steps model the widget's internal
  #     selection state directly.
  #   - This widget has no real consumer to validate its shape against;
  #     the row-confirm/wrap-default decisions above were made
  #     autonomously (see specs/intent/ui-widget-library.md's
  #     Clarifications) and should be revisited once a real screen needs
  #     this widget, per the intent doc's Constraints. The new
  #     MENU_LEFT/MENU_RIGHT keybindings this widget needs are shared
  #     with the radio group widget, which does have a real consumer
  #     (issue #54) — see ui-widget-radio-group.feature.
  #
  # Open questions:
  #   - See specs/intent/ui-widget-library.md's Open questions — the
  #     first real consumer of these widgets is still unidentified.
