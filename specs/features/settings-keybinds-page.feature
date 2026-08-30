Feature: Settings keybinds page
  A dedicated page (opened from the settings screen's Keybinds item)
  listing every rebindable action and its current key, with a "press any
  key" popup to change a binding's display, and a footer with Go back,
  Reset to Defaults, Cancel, and Apply (left to right, added after Step
  4.5 playtest feedback - Reset to Defaults restores every action's
  default binding and stays on this page, unlike the other three which
  all return to the settings screen). Visual only — no real rebinding
  takes effect (Keybindings.java's constants are unchanged). See
  specs/intent/startup-and-settings-screens.md.

  Background:
    Given the keybinds page is shown

  Scenario: The keybinds page lists every rebindable action with its current key
    Then the keybinds page lists "Move up" bound to "Up"
    And the keybinds page lists "Move down" bound to "Down"
    And the keybinds page lists "Move left" bound to "Left"
    And the keybinds page lists "Move right" bound to "Right"
    And the keybinds page lists "Toggle inventory" bound to "I"

  Scenario: Confirming an action opens a press-any-key popup
    Given "Move up" is highlighted
    When the "Enter" key is pressed
    Then the press-any-key popup is shown

  Scenario: Pressing a key while the popup is open updates that action's displayed keybind
    Given "Move up" is highlighted
    And the press-any-key popup is shown
    When the "W" key is pressed
    Then the press-any-key popup is closed
    And the keybinds page lists "Move up" bound to "W"

  Scenario Outline: Confirming a footer action returns to the settings screen
    Given "<action>" is highlighted in the footer
    When the "Enter" key is pressed
    Then the settings screen is shown

    Examples:
      | action   |
      | Apply    |
      | Cancel   |
      | Go back  |

  Scenario: Confirming Reset to Defaults resets all keybinds without leaving the page
    Given "Move up" is highlighted
    And the press-any-key popup is shown
    When the "W" key is pressed
    And "Reset to Defaults" is highlighted in the footer
    And the "Enter" key is pressed
    Then the keybinds page lists "Move up" bound to "Up"

  # Non-goals:
  #   - Apply/Cancel/Go back actually differing in behavior — decided
  #     autonomously to behave identically in this visual-only pass, see
  #     specs/intent/startup-and-settings-screens.md's Clarifications;
  #     they diverge only once real rebind persistence exists. Reset to
  #     Defaults is the one footer action that does do something real
  #     (restores every action's default binding) since that state is
  #     genuinely local to this page, unlike settings-screen.feature's
  #     own placeholder Reset to Defaults item.
  #   - Actually changing Keybindings.java's real KeyStroke constants, or
  #     any other rebinding side effect — display-only.
  #   - Validating for duplicate/conflicting key assignments — no real
  #     rebinding exists yet to conflict with.
  #   - Any mouse/pointer handling — this game is keyboard-only by
  #     design.
  #
  # Risks:
  #   - Real Swing focus-transfer and real keyboard capture (the
  #     press-any-key popup accepting arbitrary keys, not just
  #     MENU_CONFIRM/MENU_CANCEL) are not simulated headlessly here,
  #     matching this repo's existing Cucumber precedent.
  #
  # Open questions:
  #   - None outstanding for this page specifically.
