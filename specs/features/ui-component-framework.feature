Feature: Terminal-style UI component framework
  A shared component framework — a base widget contract, a keyboard
  focus/navigation manager, and one consistent "selected" highlight style
  — replaces each panel's hand-rolled InputMap/ActionMap wiring and ad hoc
  highlight color. Three concrete widgets (list, button, popup/modal) are
  built on it and proven end-to-end by rebuilding the existing in-game
  inventory screen (opened directly via the keyboard inventory toggle, no
  navigable menu widget — see Clarifications), and by migrating
  ClassSandboxPanel off the deleted SelectableMenu, on top of it.

  Scenario: Navigating a list widget down moves the selection to the next item
    Given a list widget with items "Inventory", "Help", "Journal" and "Inventory" selected
    And the list widget has keyboard focus
    When the "Down" key is pressed
    Then the selected item is "Help"

  Scenario: Moving up from the first item wraps to the last item
    Given a list widget with items "Inventory", "Help", "Journal" and "Inventory" selected
    And the list widget has keyboard focus
    When the "Up" key is pressed
    Then the selected item is "Journal"

  Scenario: Moving down from the last item wraps to the first item
    Given a list widget with items "Inventory", "Help", "Journal" and "Journal" selected
    And the list widget has keyboard focus
    When the "Down" key is pressed
    Then the selected item is "Inventory"

  Scenario: Confirming a list widget's selection with Enter
    Given a list widget with items "Inventory", "Help", "Journal" and "Inventory" selected
    And the list widget has keyboard focus
    When the "Enter" key is pressed
    Then the confirmed item is "Inventory"

  Scenario: A list widget's items come from a pluggable data source, not a hardcoded list
    Given a list widget backed by a data source currently containing "Iron Sword", "Plain Shield"
    When the data source's contents change to "Iron Sword", "Plain Shield", "Health Potion"
    And the list widget is refreshed
    Then the list widget's items are "Iron Sword", "Plain Shield", "Health Potion"

  Scenario: Confirming a button widget invokes its action
    Given a button widget labeled "Close" with an action registered
    And the button widget has keyboard focus
    When the "Enter" key is pressed
    Then the button's action was invoked

  Scenario: Toggling the inventory open focuses the popup's Close button
    Given the rebuilt in-game inventory screen
    When the inventory is toggled open
    Then the inventory popup is open
    And the popup's Close button has keyboard focus

  Scenario: Dismissing the popup with Escape closes it and restores game focus
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    When the "Escape" key is pressed
    Then the inventory popup is closed
    And the restore-game-focus action was invoked

  Scenario: Confirming the popup's Close button closes it and restores game focus
    Given the rebuilt in-game inventory screen
    And the inventory is toggled open
    When the popup's Close button is confirmed
    Then the inventory popup is closed
    And the restore-game-focus action was invoked

  Scenario: The rebuilt inventory popup displays real loaded item data, with no per-item selection
    Given the rebuilt in-game inventory screen
    When the inventory is toggled open
    Then the inventory popup lists the item "Iron Sword"
    And none of the inventory popup's items are highlighted as selected

  Scenario: The inventory popup is layered above the game view and sidebar, not inside them
    Given the game window's layered content area
    Then the inventory popup's layer is above the game and sidebar content's layer

  Scenario: ClassSandboxPanel's initial selection is highlighted and its stats shown
    Given a class sandbox panel is showing
    Then the first class's label is colored "#eeb392"
    And the stats label shows the first class's computed stats

  Scenario: Moving ClassSandboxPanel's selection down highlights the next class
    Given a class sandbox panel is showing
    When the down-bound action fires
    Then the previously selected class's label is white
    And the newly selected class's label is colored "#eeb392"
    And the stats label shows the newly selected class's computed stats

  Scenario: Moving ClassSandboxPanel's selection up from the first class wraps to the last
    Given a class sandbox panel is showing
    When the up-bound action fires
    Then the last class's label is colored "#eeb392"
    And the stats label shows the last class's computed stats

  # Non-goals:
  #   - Testing the base widget contract/interface directly — it has no
  #     independent runtime behavior; it's exercised indirectly through the
  #     concrete list/button/popup scenarios above.
  #   - Left-right / tab-style movement between widgets — nothing in the
  #     real screen being rebuilt has a natural left-right layout, so
  #     there's no concrete case to prove.
  #   - A navigable menu widget in the rebuilt screen at all (see
  #     Clarifications) — the inventory popup opens directly off the
  #     keyboard inventory toggle now, no Up/Down/Enter-through-a-menu
  #     step in between, so there's no menu<->popup focus transition or
  #     modal-capture-blocks-the-menu-behind-it behavior to prove either.
  #   - Disabled-widget styling — the shared style/theme constants support
  #     it as a convention hook, but no widget in the rebuilt real screen is
  #     ever disabled, so there's nothing concrete to prove end-to-end.
  #   - Real keyboard navigation/selection *within* the inventory popup's
  #     item list — it stays a static, non-interactive display; per-item
  #     inventory interaction is milestone "6. Intro Quest & UI" (#7)'s job.
  #   - Table widget, radio group widget, pattern-validated text fields —
  #     tracked separately in #35.
  #   - Any mouse/pointer handling — this game is keyboard-only by design.
  #   - Wiring new item data or changing how items load — #26 phase 4
  #     (data-driven-item, #51) already shipped this; the rebuilt list
  #     widget just needs to keep rendering it.
  #   - A deeper redesign of ClassSandboxPanel beyond the mechanical
  #     SelectableMenu -> list-widget swap — tracked in milestone "3. Dev
  #     sandbox framework".
  #   - Any new panel features, layout, or visual redesign beyond what's
  #     needed to prove the framework against this real screen — this
  #     rebuilt screen is an explicitly disposable skeleton, expected to be
  #     fully deleted and rebuilt once milestone "6. Intro Quest & UI" (#7)
  #     lands. The one exception: the inventory popup is layered above the
  #     whole game window (JLayeredPane, GameWindow.buildContentArea), not
  #     just centered in the sidebar — added after Step 4.5 manual playtest
  #     showed the popup sitting inline in EastPanel's own layout didn't
  #     read as a popup at all. That's a real behavior gap, not visual
  #     polish, so it's in scope despite the disposable-skeleton framing
  #     above.
  #
  # Risks:
  #   - This feature supersedes scenarios in four existing files, which
  #     must be removed/updated in the same change (not just added here) or
  #     mvn verify goes red: keyboard-input-and-menu-navigation.feature (4
  #     SelectableMenu scenarios), ui-panel-rendering-and-composition.feature
  #     (3 MenuPanel cancel/confirm-through-EastPanel scenarios),
  #     class-sandbox-panel-selection.feature (3 ClassSandboxPanel
  #     scenarios, all three carried over unchanged above since the visible
  #     behavior doesn't change, only what's underneath it), and
  #     data-driven-item.feature (1 "EastPanel wires real core item data"
  #     scenario). specs/features/README.md's rows for all four need
  #     updating in the same change, not just the new row for this file.
  #   - UiPanelRenderingAndCompositionSteps.java's inventory-item step defs
  #     (itsInventoryPanelDisplaysTheItem,
  #     itsInventoryPanelNoLongerDisplaysThePlaceholderText) are also used
  #     by data-driven-item.feature's scenario, not just
  #     ui-panel-rendering-and-composition.feature's — don't delete them
  #     wholesale when migrating the latter; both files' migrations need to
  #     land together.
  #   - MenuPanel itself (the sidebar's I/H/J/M/P/O list) was deleted
  #     entirely after Step 4.5 manual playtest, on top of the popup's
  #     layered-overlay rework above — decided too late to avoid a second
  #     pass over ui-panel-rendering-and-composition.feature's EastPanel
  #     composition scenario (menu-panel assertion dropped) and
  #     EastPanelTest (menu-cancel tests dropped). The inventory toggle
  #     (the "I" key, GamePanel -> EastPanel.toggleInventory) is untouched
  #     and still the only way to open/close it; see
  #     specs/intent/ui-component-framework.md's Clarifications for why.
  #   - Real Swing focus-transfer (does a widget actually become the AWT
  #     focus owner) is not simulated headlessly here, matching the
  #     existing precedent in keyboard-input-and-menu-navigation.feature —
  #     "keyboard focus" Given/Then steps model the framework's internal
  #     focus-manager state directly; true cross-component focus transfer
  #     is exercised via the manual playtest (this repo's Step 4.5).
  #
  # Open questions:
  #   - None outstanding — see
  #     specs/intent/ui-component-framework.md's Clarifications section.
