Feature: Table widget
  A keyboard-navigable, full-width table widget (rows/columns of
  terminal-style cells with visible cell/table borders, and an optional
  header row) built on the shared Widget/WidgetTheme framework from
  ui-component-framework.feature. Also supports a non-selectable/
  non-highlighted mode, used for whichever table isn't currently the
  active navigation target. Proven both in isolation and by two real
  consumers in the rebuilt inventory popup's details pane: a field/value
  table for the selected item's properties, and an effects table — both
  row-navigable, forming one continuous region (Right enters it at the
  fields table, Down/Up falls through between the two tables at their
  ends, Left exits back to the item list). See
  specs/intent/ui-widget-library.md, including its two rounds of Step
  4.5 playtest Clarifications for why the details pane looks and
  navigates this way.

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

  Scenario: Pressing Right from the item list moves navigation focus to the fields table
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    When the "Right" key is pressed
    Then the fields table has navigation focus

  Scenario: Pressing Right works even when the selected item has no effects
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    And the selected item has no effects
    When the "Right" key is pressed
    Then the fields table has navigation focus

  Scenario: Down at the fields table's last row falls through into the effects table
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    And the selected item has effects
    And the fields table has navigation focus
    And the fields table's selected row is its last row
    When the "Down" key is pressed
    Then the effects table has navigation focus

  Scenario: Up/Down navigates the effects table once it has navigation focus
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    And the selected item has effects
    And the effects table has navigation focus
    When the "Down" key is pressed
    Then the effects table's selected row is no longer the first row

  Scenario: Up at the effects table's first row falls back into the fields table's last row
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    And the selected item has effects
    And the effects table has navigation focus
    When the "Up" key is pressed
    Then the fields table has navigation focus

  Scenario: Pressing Left from the fields table returns navigation focus to the item list
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    And the fields table has navigation focus
    When the "Left" key is pressed
    Then the item list has navigation focus

  Scenario: Pressing Left from the effects table returns navigation focus to the item list
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    And the effects table has navigation focus
    When the "Left" key is pressed
    Then the item list has navigation focus

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
  #   - The fields/effects fall-through boundary logic (Down at the last
  #     fields row enters effects; Up at the first effects row returns to
  #     the last fields row) lives in InventoryPanel, not TableWidget
  #     itself — TableWidget only exposes the isAtFirstRow()/isAtLastRow()/
  #     moveToStart()/moveToEnd() primitives a consumer needs to build
  #     this kind of multi-table navigation; it doesn't know about any
  #     other table.
  #
  # Open questions:
  #   - None outstanding for this widget.
