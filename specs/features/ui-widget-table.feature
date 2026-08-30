Feature: Table widget
  A keyboard-navigable table widget (rows/columns of terminal-style cells,
  with an optional header row) built on the shared Widget/WidgetTheme
  framework from ui-component-framework.feature. Also supports a
  non-selectable/non-highlighted mode for purely static data. Proven both
  in isolation and by two real consumers in the rebuilt inventory popup's
  details pane: a static field-value table for the selected item's
  properties, and a row-navigable effects table (see
  specs/intent/ui-widget-library.md, including its Step 4.5 playtest
  Clarification for why the details pane became two tables).

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

  Scenario: The inventory popup's details pane shows an item's effects as a table
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    When an item with effects "+strength (base)", "+agility (base)" is selected
    Then the details pane shows an effects table with 2 rows
    And the effects table's first row is highlighted as selected

  Scenario: The inventory popup's details pane shows an item with no effects as an empty table
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    When an item with no effects is selected
    Then the details pane shows an effects table with 0 rows

  Scenario: The inventory popup's details pane shows every item property in a static field-value table
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    When an item is selected
    Then the details pane shows a field-value table listing the item's ID, Name, Glyph, Type, and Slot
    And the field-value table is not row-highlighted

  Scenario: Pressing Right from the item list moves navigation focus to the effects table
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    And the selected item has effects
    When the "Right" key is pressed
    Then the effects table has navigation focus

  Scenario: Up/Down navigates the effects table once it has navigation focus
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    And the selected item has effects
    And the effects table has navigation focus
    When the "Down" key is pressed
    Then the effects table's selected row is no longer the first row

  Scenario: Pressing Left from the effects table returns navigation focus to the item list
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    And the effects table has navigation focus
    When the "Left" key is pressed
    Then the item list has navigation focus

  Scenario: Pressing Right does nothing when the selected item has no effects
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    And the selected item has no effects
    When the "Right" key is pressed
    Then the item list still has navigation focus

  # Non-goals:
  #   - Cell-level confirm (as opposed to row-level) — decided against in
  #     specs/intent/ui-widget-library.md's Clarifications.
  #   - Scrolling behavior specifics — TerminalScrollBarUI is reused
  #     as-is from the existing framework, nothing new to prove there.
  #   - Any real drop mechanism triggered from the effects table — see
  #     ui-widget-radio-group.feature's "Drop item?" scenarios; that
  #     confirmation is keyboard "D", not something the table itself
  #     does.
  #   - Any mouse/pointer handling — this game is keyboard-only by
  #     design.
  #
  # Risks:
  #   - Real Swing focus-transfer is not simulated headlessly here,
  #     matching the existing precedent in ui-component-framework.feature
  #     — "keyboard focus"/"navigation focus" Given/Then steps model
  #     internal state directly.
  #   - This feature modifies already-shipped code from #36
  #     (InventoryPanel, PopupWidget), not just adding a standalone
  #     widget — PopupWidget gains onLeft()/onRight() hooks alongside its
  #     existing onUp()/onDown(). See
  #     specs/intent/ui-widget-library.md's Constraints.
  #   - The exact column set/labels for the effects table ("Stat"/
  #     "Value") and the no-wrap-at-the-pane-boundary behavion (Left from
  #     the item list, Right from the effects table with no further pane)
  #     were autonomous implementation-level calls during spec drafting,
  #     not separately grilled — flag for confirmation at Step 3 approval
  #     if they matter.
  #
  # Open questions:
  #   - None outstanding for this widget.
