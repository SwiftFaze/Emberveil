Feature: Keyboard input and menu navigation
  Keyboard input is dispatched through one Key Bindings-based system
  (replacing the mix of raw KeyListener and Key Bindings that coexisted
  before), and terminal-style menus support keyboard selection through a
  shared, small selection model.

  Scenario: A movement key updates the player and notifies listeners
    Given a game panel with a player at position (5, 5)
    And tile (5, 4) is walkable
    When the "move up" action fires
    Then the player's position is (5, 4)
    And registered game listeners are notified

  Scenario: An unmapped key does not notify listeners
    Given a game panel with a player at position (5, 5)
    When a key with no bound action is pressed
    Then registered game listeners are not notified

  Scenario: Toggling inventory dispatches through the listener interface
    Given the inventory panel is hidden
    When the "toggle inventory" action fires
    Then the inventory panel becomes visible
    And the toggle did not use a direct field reference to EastPanel

  Scenario: Navigating a selectable menu with the arrow keys
    Given a selectable menu with items "Inventory", "Help", "Journal" and "Inventory" selected
    And the menu has keyboard focus
    When the "Down" key is pressed
    Then the selected item is "Help"

  Scenario: Moving up from the first item wraps to the last item
    Given a selectable menu with items "Inventory", "Help", "Journal" and "Inventory" selected
    And the menu has keyboard focus
    When the "Up" key is pressed
    Then the selected item is "Journal"

  Scenario: Moving down from the last item wraps to the first item
    Given a selectable menu with items "Inventory", "Help", "Journal" and "Journal" selected
    And the menu has keyboard focus
    When the "Down" key is pressed
    Then the selected item is "Inventory"

  Scenario: Confirming a menu selection with Enter
    Given a selectable menu with items "Inventory", "Help", "Journal" and "Inventory" selected
    And the menu has keyboard focus
    When the "Enter" key is pressed
    Then the confirmed item is "Inventory"

  # Non-goals:
  #   - Making Help/Journal/Map/Character/Stats do anything when selected —
  #     only Inventory needs to work end-to-end; the rest stay decorative.
  #   - A generic rebindable keymap UI — Keybindings is a constants class,
  #     not a settings screen.
  #
  # Risks:
  #   - GamePanel's existing WASD/arrow keys are already bound to movement;
  #     since the menu reuses the same Up/Down/Enter keys, focus must
  #     actually move to the menu panel while it's shown, or key events
  #     will keep routing to GamePanel's movement Actions instead.
  #
  # Open questions:
  #   - None outstanding — see specs/intent/restructure-solid-base.md's
  #     Clarifications section (up/down + Enter, focus-scoped Key
  #     Bindings, wrap-around at boundaries).
