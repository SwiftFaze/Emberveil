Feature: Building loader failure path
  BuildingLoader.load(...) wraps any failure to read or parse a building
  resource in a BuildingException, so callers get one consistent
  exception type rather than a raw I/O or JSON parsing failure — but
  today nothing verifies that actually happens.

  Scenario: Loading a missing building resource throws BuildingException
    When a building is loaded from the missing resource "does-not-exist.json"
    Then a BuildingException is thrown wrapping the underlying cause

  Scenario: Loading a malformed building resource throws BuildingException
    When a building is loaded from the malformed resource "malformed_building.json"
    Then a BuildingException is thrown wrapping the underlying cause

  # Non-goals:
  #   - Any functional/behavior change to BuildingLoader or
  #     BuildingException — this is test-coverage only, per
  #     specs/intent/class-sandbox-panel-and-building-exception-coverage.md.
  #   - The happy path (loading a valid building resource) — already
  #     covered by BuildingLoaderIT.
  #
  # Open questions:
  #   - None outstanding — see
  #     specs/intent/class-sandbox-panel-and-building-exception-coverage.md.
