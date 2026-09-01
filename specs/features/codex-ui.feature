Feature: Codex — browsable in-game reference screen for mods/core content
  An in-game overlay (X key, currently unbound) that opens over the live
  game, following the same PopupWidget/JLayeredPane overlay pattern
  InventoryPanel already uses. A Tab/Shift+Tab tab switcher across three
  mods/core categories (Items, Tiles, Classes); each tab shows a
  scrollable list of that category's entries on the left (ListWidget)
  with a detail pane on the right showing the selected entry's data,
  mirroring InventoryPanel's list+detail split. Reachable only via the X
  key (no menu entry), same as Inventory's I key; opening Codex while
  Inventory is open (or vice versa) closes the other popup first, so only
  one is ever visible. All entries are shown immediately — no locking/
  discovery gating. Buildings and Quests tabs are deferred (not in this
  pass). See specs/intent/codex-ui.md.

  Background:
    Given the game world is running

  Scenario: Pressing the codex key opens the codex overlay on the Items tab
    When the "X" key is pressed
    Then the codex overlay is shown
    And a tab switcher for "Items", "Tiles", "Classes" is shown
    And the "Items" tab is selected
    And the detail pane shows the first Items entry's data

  Scenario: Pressing the codex key again closes the codex overlay
    Given the codex overlay is open
    When the "X" key is pressed
    Then the codex overlay is not shown
    And the restore-game-focus action was invoked

  Scenario: Escape also closes the codex overlay
    Given the codex overlay is open
    When the "Escape" key is pressed
    Then the codex overlay is not shown
    And the restore-game-focus action was invoked

  Scenario: Opening the codex while the inventory is open closes the inventory first
    Given the inventory popup is open
    When the "X" key is pressed
    Then the inventory popup is closed
    And the codex overlay is shown

  Scenario: Opening the inventory while the codex is open closes the codex first
    Given the codex overlay is open
    When the "I" key is pressed
    Then the codex overlay is not shown
    And the inventory popup is shown

  Scenario: Reopening the codex always resets to the Items tab
    Given the codex overlay was previously closed while showing the "Tiles" tab
    When the "X" key is pressed
    Then the "Items" tab is selected

  Scenario Outline: Selecting a tab shows a scrollable list of that category's entries
    Given the codex overlay is open
    When the "<Category>" tab is opened
    Then the codex list shows one entry per mod-defined <Category>
    And the detail pane shows the first <Category> entry's data

    Examples:
      | Category |
      | Items    |
      | Tiles    |
      | Classes  |

  Scenario Outline: Selecting an entry in the list populates the detail pane with its data
    Given the codex overlay is open
    And the "<Category>" tab is opened
    When an entry is selected from the list
    Then the detail pane shows that entry's data

    Examples:
      | Category |
      | Items    |
      | Tiles    |
      | Classes  |

  Scenario Outline: A category with no mod-defined entries shows an empty list
    Given the codex overlay is open
    And no mods define any <Category>
    When the "<Category>" tab is opened
    Then the codex list is empty
    And the detail pane shows "(no item selected)"

    Examples:
      | Category |
      | Items    |
      | Tiles    |
      | Classes  |

  Scenario: Tab cycles forward to the next tab
    Given the codex overlay is open
    And the "Items" tab is opened
    When the "Tab" key is pressed
    Then the "Tiles" tab is selected

  Scenario: Tab from the last tab wraps to the first tab
    Given the codex overlay is open
    And the "Classes" tab is opened
    When the "Tab" key is pressed
    Then the "Items" tab is selected

  Scenario: Shift+Tab cycles backward to the previous tab
    Given the codex overlay is open
    And the "Tiles" tab is opened
    When the "Shift+Tab" key is pressed
    Then the "Items" tab is selected

  # Non-goals:
  #   - Buildings and Quests tabs — deferred; not in this pass, and not
  #     currently tracked in a separate issue (see
  #     specs/intent/codex-ui.md's Clarifications). `Building` has a real
  #     bug found while scoping this (its JSON already has name/type
  #     fields that ModLoader.loadBuilding silently drops) — noted there
  #     for whoever picks Buildings back up.
  #   - Enemy/Biome tabs and their underlying mod data categories — no
  #     schema/loader/registry exists yet; tracked in #121.
  #   - Discovery/unlock gating (locking entries until first encountered)
  #     — no persistence for "what the player has seen" exists yet;
  #     tracked in #112.
  #   - A menu entry for opening the codex — X-key-only, same as
  #     Inventory's I key.
  #   - Any mouse/pointer handling — this game is keyboard-only by design.
  #
  # Risks:
  #   - ListWidget.setItems() always auto-selects index 0 (and fires
  #     onSelectionChange immediately) whenever the list is non-empty —
  #     confirmed from ListWidget.java and InventoryPanel's existing use
  #     of it. So a freshly-selected non-empty tab shows its first
  #     entry's data immediately, not "(no item selected)"; that text
  #     only appears for a genuinely empty category. Corrected in this
  #     revision after drafting the first version against a wrong
  #     assumption — not a new open question.
  #   - Tab-cycling wrap-around (last tab -> Tab wraps to first; first tab
  #     -> Shift+Tab wraps to last) is an assumption, not a confirmed
  #     answer from the 2026-08-31 grilling session — it matches common
  #     Tab-cycling UX and ListWidget's own default wrap-around behavior,
  #     but wasn't explicitly asked. Flag at Step 3 human approval if this
  #     isn't the desired feel.
  #   - GamePanel.bindKeys() currently binds Inventory's toggle at
  #     WHEN_IN_FOCUSED_WINDOW; making I/X mutually exclusive (this
  #     feature's "closes the other popup first" scenarios) requires each
  #     toggle action to know about and dismiss the other popup, which
  #     doesn't exist yet — likely lands in EastPanel (which already owns
  #     InventoryPanel) alongside the new CodexPanel.
  #
  # Open questions:
  #   - None outstanding — four rounds of design questions resolved via
  #     grilling sessions on 2026-08-31 (see specs/intent/codex-ui.md's
  #     Clarifications), including the late scope narrowing from five
  #     categories down to three (Items/Tiles/Classes).
  #
  # Post-approval wording fix (2026-09-01, orchestrator, no behavior change):
  #   - "the "<Category>" tab is selected" was originally reused for both a
  #     setup action (switch tabs) and an assertion (verify current tab) in
  #     the same scenarios. Cucumber matches step text irrespective of
  #     Given/When/Then, so one shared step definition can't safely serve
  #     both roles - a mutating implementation would make the Tab-cycling
  #     Then-assertions vacuously true, an assertion-only one would fail the
  #     Outline scenarios' setup. Setup/action usages were reworded to
  #     "the "<Category>" tab is opened"; "... is selected" is now
  #     assertion-only. Caught before implementation; no scenario's meaning
  #     changed.
