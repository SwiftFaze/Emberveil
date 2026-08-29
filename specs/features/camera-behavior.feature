Feature: Camera behavior
  The camera centers the viewport on a target world position. It holds no
  state beyond the last computed offset, and performs no clamping to map
  bounds — the offset it produces is used as-is by every renderer that
  translates world coordinates into screen coordinates.

  Background:
    Given a camera with a viewport 10 tiles wide and 10 tiles tall

  Scenario: Centering on a target position offsets the viewport by half its size
    When the camera centers on position (20, 20)
    Then the camera's offset is (15, 15)

  Scenario Outline: Centering offset follows target minus half the viewport
    When the camera centers on position (<targetX>, <targetY>)
    Then the camera's offset is (<offsetX>, <offsetY>)

    Examples:
      | targetX | targetY | offsetX | offsetY |
      | 20      | 20      | 15      | 15      |
      | 25      | 13      | 20      | 8       |
      | 0       | 0       | -5      | -5      |

  Scenario: Re-centering replaces the previous offset with no smoothing
    Given the camera has centered on position (20, 20)
    When the camera centers on position (30, 30)
    Then the camera's offset is (25, 25)

  Scenario: Centering near a map edge is not clamped to the map bounds
    When the camera centers on position (2, 2)
    Then the camera's offset is (-3, -3)

  # Non-goals:
  #   - Edge-of-map clamping, zoom, panning, or floor/depth-aware behavior —
  #     out of scope per specs/intent/camera-behavior.md; the scenario above
  #     documents the current unclamped behavior, it does not request
  #     clamping be added.
  #   - Pixel-level Swing rendering output — only the Camera domain object's
  #     offset math is covered, not Graphics2D calls.
  #
  # Risks:
  #   - None identified; Camera has no external dependencies and no branching
  #     logic beyond the two arithmetic assignments in centerOn.
  #
  # Open questions:
  #   - None. Camera has no validation/error paths (no exceptions, no bounds
  #     checks) — there is no meaningful failure/error scenario to add here;
  #     the unclamped-offset scenario above is the closest analogue to an
  #     edge case this class has.
