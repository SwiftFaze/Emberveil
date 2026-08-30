Feature: UI panel rendering and composition
  The terminal-style UI shell displays player stats and composes its
  panels consistently: TerminalPanel's shared styling contract,
  PlayerInfoPanel's stat display, NorthPanel/SouthPanel's fixed layout,
  and EastPanel's composition of PlayerInfoPanel/MenuPanel (the inventory
  popup is layered above the whole game window instead — see
  ui-component-framework.feature).

  Scenario: A panel extending TerminalPanel uses the shared terminal styling
    Given a new instance of a panel extending TerminalPanel
    Then the panel's background is black
    And the panel is not focusable
    And a label made by the panel is white and monospaced
    And a label made by the panel without an explicit alignment is left-aligned

  Scenario: A newly created PlayerInfoPanel shows placeholder text
    Given a new PlayerInfoPanel
    Then the name label reads "Name:"
    And the level label reads "LV:"
    And the position label reads "Pos: "

  Scenario: Updating PlayerInfoPanel with a player shows their stats
    Given a new PlayerInfoPanel
    And a player named "Aria Blackwood" at level 3 with 40 XP at position (5, 8)
    When the panel is updated with that player
    Then the name label reads "Aria Blackwood | Warrior"
    And the level label reads "LV 3 | 40.0/115 XP"
    And the position label reads "Pos: (5, 8)"

  Scenario: PlayerInfoPanel has a bottom light-gray border with padding
    Given a new PlayerInfoPanel
    Then the player info panel's border has a 2px light-gray bottom line and 10px padding on all sides

  Scenario: NorthPanel composes a centered title
    Given a new NorthPanel
    Then the panel's preferred width equals the game window width
    And the panel's preferred height equals 4 times the tile height
    And the panel has a light-gray border
    And the panel displays a centered title reading "Veil"
    And the title is colored "#eeb392"

  Scenario: SouthPanel composes as a bordered placeholder
    Given a new SouthPanel
    Then the panel's preferred width equals the game window width
    And the panel's preferred height equals 4 times the game window height
    And the panel has a light-gray border

  Scenario: EastPanel composes PlayerInfoPanel and MenuPanel
    Given a new EastPanel
    Then its player info panel is in the north of the layout
    And its menu panel is in the south of the layout
    And its inventory panel is not part of EastPanel's own layout

  Scenario: EastPanel itself is a fixed-size, bordered, non-focusable panel
    Given a new EastPanel
    Then EastPanel's preferred size is 500 wide and the game window height times the tile height tall
    And EastPanel's background is black
    And EastPanel is not focusable
    And EastPanel has a light-gray border

  Scenario: Updating EastPanel with a player delegates to its player info panel
    Given a new EastPanel
    And a player named "Aria Blackwood" at level 3 with 40 XP at position (5, 8)
    When EastPanel is updated with that player
    Then its player info panel's name label reads "Aria Blackwood | Warrior"


  # Non-goals:
  #   - Keyboard navigation/dispatch and the inventory-toggle listener
  #     path (the key binding that invokes toggleInventory) — already
  #     covered by keyboard-input-and-menu-navigation.feature.
  #   - Calling toggleInventory() directly and asserting its visibility
  #     flip / focus request-restore — already covered in detail by the
  #     existing EastPanelTest unit test (src/test/java/.../ui/EastPanelTest.java).
  #     MenuPanel's CancelAction/ConfirmSelectionAction ARE in scope here
  #     (see the cancel/confirm scenarios above) since they prove EastPanel's
  #     real composition/wiring, not just toggleInventory()'s internals —
  #     see specs/intent/ui-panel-rendering-and-composition.md's
  #     Clarifications for why this isn't the same exclusion as before.
  #   - MenuPanel and InventoryPanel's own internal rendering/behavior
  #     (e.g. SelectableMenu highlight color, item list) beyond what's
  #     needed to exercise the cancel/confirm scenarios above.
  #   - Any new panel features, layout changes, or visual redesign — this
  #     is spec coverage for existing behavior only.
  #
  # Risks:
  #   - Level.getXp() returns a double, so PlayerInfoPanel's level label
  #     always renders a trailing ".0" for whole-number XP (e.g. "40.0"),
  #     and Level.getMaxXp() is derived as (currentLevel + 20) * 5, not an
  #     independently settable value — step definitions must compute
  #     expected text from the real formula rather than picking an
  #     arbitrary maxXp.
  #   - The cancel/confirm scenarios overlap with EastPanelTest's existing
  #     unit coverage (cancelMenuClosesOpenInventoryAndRestoresGameFocus,
  #     cancelMenuRestoresGameFocusEvenWhenInventoryAlreadyClosed) — this
  #     is intentional per the Clarifications above, not accidental
  #     duplication to clean up.
  #
  # Open questions:
  #   - None outstanding — see specs/intent/ui-panel-rendering-and-composition.md.
