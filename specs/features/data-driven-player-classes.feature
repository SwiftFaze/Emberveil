@pending
Feature: Data-driven player classes
  Player class base stats come from JSON resources rather than one
  hardcoded Java subclass per class, so adding or tuning a class doesn't
  require a code change. Only base-stat values move to data — Stats'
  fields and its attack power/defense formulas stay plain Java.

  Scenario: Loading the Warrior class from JSON
    When the "warrior" class is loaded
    Then the class name is "Warrior"
    And the base max HP is 120
    And the base max mana is 20

  Scenario: Loading the Mage class from JSON
    When the "mage" class is loaded
    Then the class name is "Mage"
    And the base max HP is 70
    And the base max mana is 100

  Scenario: Loading all classes enumerates every class file
    When all player classes are loaded
    Then the result includes classes named "Warrior" and "Mage"

  Scenario: Loading a missing class file fails clearly
    When class file "not_a_real_class.json" is loaded
    Then loading fails with a PlayerClassException

  # Non-goals:
  #   - Changing any class's numbers — this pass moves data, it doesn't
  #     rebalance (see intent doc's "Verify no drift").
  #   - A fully dynamic attribute map, or moving the attack power/defense
  #     formulas out of Stats.
  #   - Re-testing "a new player defaults to Warrior" — that's already
  #     covered by specs/features/default-player-class.feature, which must
  #     keep passing unmodified against the new PlayerClassLoader-backed
  #     PlayerInfo.
  #
  # Risks:
  #   - warrior.json/mage.json values must exactly match today's
  #     Warrior.java/Mage.java numbers (str/dex/con/int/wis/luck, HP, mana)
  #     or this introduces an unintended balance change.
  #
  # Open questions:
  #   - None outstanding — see specs/intent/restructure-solid-base.md's
  #     Clarifications section: JSON uses abbreviated field names (str,
  #     dex, con, int, wis, luck). This is a schema/implementation detail
  #     that doesn't change the scenario wording above, since scenarios
  #     describe behavior rather than raw field names.
