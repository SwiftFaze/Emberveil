# Intent: Default player class

New players must start with a playable class already assigned. There is no
character-creation menu yet, and combat math (attack power, defense, HP/mana
pools) depends on a class's base stats being applied immediately, so the
player can never exist in a classless state.

## Requirements

- A newly created `PlayerInfo` has a non-null `PlayerClass`.
- The default class is Warrior.
- The Warrior's base stats (HP, mana, attributes) are applied to the
  player's `Stats` as soon as the class is assigned.

## Status

Already implemented (`PlayerInfo`'s constructor). This intent doc and its
matching `.feature` file exist as the worked example proving the
intent -> spec -> Cucumber pipeline end-to-end (see `docs/testing.md`) —
use it as the template for the next real feature, not as a change to make.
