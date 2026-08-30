Feature: Keyboard input and menu navigation
  Keyboard input is dispatched through one Key Bindings-based system
  (replacing the mix of raw KeyListener and Key Bindings that coexisted
  before), and terminal-style menus support keyboard selection through a
  shared, small selection model.

  Scenario: A movement key updates the player and notifies listeners
    Given a game panel with a player at position (5, 5)
    When the "move up" action fires
    Then the game panel's player is at position (5, 4)
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

  # Non-goals:
  #   - A generic rebindable keymap UI — Keybindings is a constants class,
  #     not a settings screen.
  #
  # Note: the sidebar menu this file originally described (Up/Down/Enter
  # through a MenuPanel, with Help/Journal/Map/Character/Stats staying
  # decorative) was deleted entirely in ui-component-framework.feature —
  # see that file and specs/intent/ui-component-framework.md's
  # Clarifications. The "toggle inventory" scenario below is unaffected,
  # since it goes through GamePanel's own "I" binding, not a menu.
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
