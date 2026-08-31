Feature: Radio group widget
  A single-select radio group widget built on the shared
  Widget/WidgetTheme framework from ui-component-framework.feature.
  Vertical (Up/Down) layout by default, matching every other widget's
  convention; an optional horizontal (Left/Right) variant is available
  for callers that need it, sharing the new MENU_LEFT/MENU_RIGHT
  keybindings with the table widget. Proven by a real consumer: the
  rebuilt inventory popup's "Drop item?" confirmation (a horizontal
  Yes/No instance), opened via a new "D" keybinding. See
  specs/intent/ui-widget-library.md.

  Scenario: Navigating a vertical radio group down moves the highlighted option to the next one
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

  Scenario: A horizontal radio group navigates with Left/Right instead of Up/Down
    Given a horizontal radio group with options "Windowed", "Fullscreen" and "Windowed" highlighted
    And the radio group has keyboard focus
    When the "Right" key is pressed
    Then the highlighted option is "Fullscreen"

  Scenario: Pressing D on a selected inventory item opens the drop-confirmation popup
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    And an item is selected
    When the "D" key is pressed
    Then the drop-confirmation popup is shown
    And the drop-confirmation popup asks "Drop item?"
    And "No" is highlighted in the drop-confirmation popup

  Scenario: The drop-confirmation popup opens regardless of which inventory pane has focus
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    And the effects table has navigation focus
    When the "D" key is pressed
    Then the drop-confirmation popup is shown

  Scenario: Left/Right moves the highlighted choice on the drop-confirmation popup
    Given the drop-confirmation popup is shown
    And "No" is highlighted in the drop-confirmation popup
    When the "Left" key is pressed
    Then "Yes" is highlighted in the drop-confirmation popup

  Scenario Outline: Confirming either choice on the drop-confirmation popup closes it without dropping the item
    Given the drop-confirmation popup is shown
    And "<choice>" is highlighted in the drop-confirmation popup
    When the "Enter" key is pressed
    Then the drop-confirmation popup is closed
    And the inventory popup is shown
    And the item was not removed

    Examples:
      | choice |
      | Yes    |
      | No     |

  Scenario: Escape dismisses the drop-confirmation popup without dropping the item
    Given the drop-confirmation popup is shown
    When the "Escape" key is pressed
    Then the drop-confirmation popup is closed
    And the item was not removed

  # Non-goals:
  #   - Actually removing the item from inventory — no real drop
  #     mechanism exists yet; "Yes" is a no-op, matching every other
  #     placeholder action established this session (#54, #99).
  #   - A bigger real-world Yes/No consumer (e.g. NPC dialogue) — not
  #     needed here; the drop-confirmation popup is a real, if minimal,
  #     use site.
  #   - Any mouse/pointer handling — this game is keyboard-only by
  #     design.
  #
  # Risks:
  #   - Real Swing focus-transfer is not simulated headlessly here,
  #     matching the existing precedent in ui-component-framework.feature.
  #   - This orientation question was answered three times over the
  #     course of drafting (vertical-only, then horizontal-only after
  #     #54, then vertical-by-default-with-a-horizontal-option here) —
  #     see specs/intent/ui-widget-library.md's Clarifications for the
  #     full history.
  #   - The drop-confirmation popup now nests a compact, centered
  #     CompactPopupWidget (#99's smaller/centered variant) on top of the
  #     already-open full-screen inventory popup, rather than the
  #     full-screen PopupWidget it started as before #99 landed.
  #   - "No" as the default-highlighted choice was a low-risk autonomous
  #     UX call (safer default for a destructive-sounding action), not
  #     separately grilled.
  #
  # Open questions:
  #   - None outstanding for this widget.
