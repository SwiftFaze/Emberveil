Feature: Smaller confirmation-style popup variant
  A centered, content-sized PopupWidget presentation (as opposed to
  InventoryPanel's existing full-screen one), proven by a concrete Yes/No
  confirmation dialog wired to the settings screen's "Reset to Defaults"
  item. Reuses the existing PopupWidget base (Close/Escape, onUp/onDown)
  and #35's radio group widget for the Yes/No choice — no new dialog
  control is built. See specs/intent/confirmation-popup-variant.md.

  Scenario: Confirming Reset to Defaults opens the Yes/No confirmation popup
    Given the settings screen is shown
    And "Reset to Defaults" is highlighted
    When the "Enter" key is pressed
    Then the confirmation popup is shown
    And the confirmation popup is not full-screen

  Scenario: Choosing No on the confirmation popup dismisses it without resetting
    Given the confirmation popup is shown
    And "No" is highlighted
    When the "Enter" key is pressed
    Then the confirmation popup is closed
    And the settings screen is shown

  Scenario: Choosing Yes on the confirmation popup dismisses it
    Given the confirmation popup is shown
    And "Yes" is highlighted
    When the "Enter" key is pressed
    Then the confirmation popup is closed
    And the settings screen is shown

  Scenario: Left/Right moves the highlighted choice between Yes and No
    Given the confirmation popup is shown
    And "No" is highlighted
    When the "Left" key is pressed
    Then "Yes" is highlighted

  Scenario: Escape dismisses the confirmation popup without resetting
    Given the confirmation popup is shown
    When the "Escape" key is pressed
    Then the confirmation popup is closed
    And the settings screen is shown

  # Non-goals:
  #   - Reset to Defaults's "Yes" choice actually resetting anything —
  #     no setting persists real state yet, matching #54's own
  #     out-of-scope framing for that item.
  #   - A bigger real-world trigger for Yes/No confirmation (e.g. NPC
  #     dialogue) — explicitly out of scope for issue #99 itself.
  #   - Any mouse/pointer handling — this game is keyboard-only by
  #     design.
  #
  # Risks:
  #   - Depends on #54's settings screen (Reset to Defaults item, not
  #     yet approved/implemented) as this feature's trigger, and on
  #     #35's radio group widget (not yet approved/implemented) for the
  #     Yes/No choice control. This feature cannot be fully implemented
  #     until both land.
  #   - The choice of "Reset to Defaults" as the concrete trigger (over
  #     a dev-only sandbox demo) was an autonomous decision — see
  #     specs/intent/confirmation-popup-variant.md's Constraints —
  #     flagged for confirmation at Step 3 approval.
  #   - Real Swing focus-transfer is not simulated headlessly here,
  #     matching this repo's existing Cucumber precedent
  #     (ui-component-framework.feature).
  #
  # Open questions:
  #   - None outstanding.
