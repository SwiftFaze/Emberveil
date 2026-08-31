Feature: Mod-driven color theming for the UI widget library
  Widget colors move from hardcoded WidgetTheme Color constants to a
  mod-shaped theme.json, loaded through the same ModLoader/mods/
  mechanism tiles/buildings/classes/items/quests already use, so
  reskinning the widget library no longer requires editing Java.

  Scenario: Loading the core mod's default theme populates all ten widget colors
    Given a mods directory containing the "core" mod with a theme declaring id "core:default" and all ten widget colors
    When the mods directory is loaded
    Then a theme with ID "core:default" is available
    And WidgetTheme's colors match the "core:default" theme's colors exactly

  Scenario: A second mod can ship its own theme without activating it
    Given a mods directory containing the "core" mod with a theme declaring id "core:default" and all ten widget colors
    And the mods directory also contains mod "midnight-pack" with a theme declaring id "midnight-pack:midnight" and all ten widget colors
    When the mods directory is loaded
    Then a theme with ID "core:default" is available
    And a theme with ID "midnight-pack:midnight" is available
    And WidgetTheme's colors still match the "core:default" theme's colors

  Scenario: A mod declaring a colliding theme ID without an override field fails to load
    Given a mods directory containing the "core" mod with a theme declaring id "core:default" and all ten widget colors
    And the mods directory also contains mod "retexture-pack" with a theme declaring id "core:default" and no "overrides" field
    When the mods directory is loaded
    Then loading fails with a ModLoadException naming the colliding ID "core:default" and both mods "core" and "retexture-pack"

  Scenario: A mod declaring a colliding theme ID with an explicit override replaces the earlier definition
    Given a mods directory containing the "core" mod with a theme declaring id "core:default" and all ten widget colors
    And the mods directory also contains mod "retexture-pack" with a theme declaring id "core:default", a "SELECTED_HIGHLIGHT" color of (10, 20, 30), and the rest of the ten widget colors, whose "overrides" field names "core:default"
    When the mods directory is loaded
    Then a theme with ID "core:default" is available
    And its "SELECTED_HIGHLIGHT" color is (10, 20, 30)

  Scenario: A theme missing a required color key fails to load
    Given a mods directory containing mod "broken-pack" with a theme declaring id "broken-pack:incomplete" that omits "TABLE_BORDER"
    When the mods directory is loaded
    Then loading fails with a ModLoadException naming the missing color key "TABLE_BORDER" and the file for theme "broken-pack:incomplete"

  Scenario: A theme with a malformed color resource throws ModLoadException
    Given a mods directory containing mod "broken-pack" with a malformed theme file
    When the mods directory is loaded
    Then a ModLoadException is thrown wrapping the underlying cause

  # Non-goals:
  #   - Fonts — colors only, see specs/intent/widget-theming.md.
  #   - Legacy screen-chrome panels (EastPanel, InventoryPanel, NorthPanel,
  #     PlayerInfoPanel, SouthPanel, TerminalPanel) — not part of the
  #     widget library; they keep their existing hardcoded colors.
  #   - Theme activation/switching (choosing which loaded theme actually
  #     applies to WidgetTheme) — no settings/config persistence system
  #     exists yet. WidgetTheme always applies whichever theme owns ID
  #     "core:default" once loaded; a second mod's theme loading without
  #     error is what proves the pattern, not that it renders.
  #   - The visual-only "Theme" entry on the Settings screen — a fixed
  #     placeholder cycle (not sourced from this theme registry, see
  #     settings-screen.feature's Risks), part of the existing Settings
  #     screen concept rather than a new scenario here.
  #
  # Risks:
  #   - WidgetTheme's fields are `static final` Color constants today;
  #     they become mutable statics populated once at startup from the
  #     loaded "core:default" theme, but ListWidget/TableWidget/
  #     PopupWidget/Widget keep referencing them by the same static field
  #     names — no call-site changes needed there.
  #   - Themes live under mods/<modid>/themes/*.json — a directory of
  #     files, one theme per file, matching the tiles/items/quests
  #     convention (not the stats.json singleton) — so ModLoader's new
  #     loadThemes/loadTheme functions are shaped like loadTiles/loadTile,
  #     still routed through registerWithCollisionCheck for id/overrides
  #     parity with every other content type. The core mod's default
  #     theme file is mods/core/themes/default.json (id "core:default").
  #   - The 7-vs-10-color scope question (issue #106 named only 7;
  #     WidgetTheme now has 10) was resolved via grilling — see
  #     specs/intent/widget-theming.md's Clarifications. All 10 must be
  #     present in mods/core/theme.json.
  #
  # Open questions:
  #   None outstanding — see specs/intent/widget-theming.md's
  #   Clarifications section.
