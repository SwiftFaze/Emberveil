# UI widget framework

The reusable Swing widget library in `ui/widget/`, and the theming system
that colors it. For how these compose into actual game screens, see
`docs/screens.md`; for the self-describing data contract screens use to
feed list/detail widgets, see `docs/components.md`.

**Widget theming** (`mods/<modid>/themes/*.json`): a directory of files, one
theme per file, matching the `tiles/`/`items/`/`quests/` directory-of-many-files
convention (not the `stats.json` singleton) — each file defining all 11 colors
`WidgetTheme` (see below) exposes as static fields: `SELECTED_HIGHLIGHT`,
`SELECTED_TEXT`, `NORMAL_TEXT`, `DIMMED_TEXT`, `BACKGROUND`, `INVALID_HIGHLIGHT`,
`VALID_HIGHLIGHT`, `TABLE_HEADER_BACKGROUND`, `BORDER`, `SCROLLBAR_THUMB`,
`ACCENT` (`WidgetColorTheme.REQUIRED_KEYS`), each an `{r, g, b}` object using
the same color shape tiles already use (`ModLoader.readColor`). `BORDER` was
named `TABLE_BORDER` until the UI color-cleanup sweep below broadened its use
well beyond tables (every panel border in `ui/`) — the old name was misleading
once `CompactPopupWidget`/`RadioGroupWidget` and then general panel chrome all
started reusing it, so it was renamed to the general-purpose `BORDER`. `ACCENT`
(`#eeb392`) was added in that same sweep to capture a hardcoded leftover: it's
the widget library's original `SELECTED_HIGHLIGHT` color from before an earlier
commit switched selection highlighting to neutral gray, and `NorthPanel`'s
title and `ClassSandboxPanel`'s selected-row color still used that literal
directly. Loaded by `ModLoader.loadThemes`/`loadTheme` — shaped like
`loadTiles`/`loadTile`'s directory scan, still routed through
`registerWithCollisionCheck` for id/`overrides` parity with every other content
type — into a `WidgetColorTheme` (id + `Map<String, Color>`,
`WidgetColorTheme.color(key)`) held in `ModRegistry` (`getTheme`/`getAllThemes`),
keyed by namespaced ID. The core mod's default theme lives at
`mods/core/themes/default.json` (id `core:default`); a file's name doesn't need
to match its id, same as tiles/items/quests. A theme missing a required color
key, or with a malformed `{r,g,b}` value, throws `ModLoadException` the same as
any other content type. `Main.loadGame()` loads the mod registry once at
startup and calls `WidgetTheme.applyTheme(...)` with whichever theme owns ID
`core:default`, before any screen/widget is constructed (they read
`WidgetTheme`'s statics at construction time). No settings/config persistence
system exists yet to pick a non-default theme — the Settings screen's "Theme"
row (see `docs/screens.md`) is a purely visual placeholder, not wired to this
registry. Theming coverage isn't limited to the original widget-library files
either: every hardcoded `Color` literal across `ui/`, `ui/widget/`, and the dev
`sandbox/ClassSandboxPanel` was swept to reference a `WidgetTheme` field instead
(gameplay/world rendering — `Player`, `WorldScene`, `GamePanel` — stays
hardcoded, since those colors are game content, not UI chrome). This is the
widget-theming initiative; see `specs/intent/widget-theming.md`.

A small reusable widget framework lives in `ui/widget/`: `Widget` (base
`JPanel` — themed background via `WidgetTheme.BACKGROUND`, focusable),
`FocusManager` (a modal-open flag a popup's content can consult), `WidgetTheme`
(11 mutable `static Color` fields — `SELECTED_HIGHLIGHT`/`SELECTED_TEXT`/
`NORMAL_TEXT`/`DIMMED_TEXT`/`BACKGROUND`/`INVALID_HIGHLIGHT`/`VALID_HIGHLIGHT`/
`TABLE_HEADER_BACKGROUND`/`BORDER`/`SCROLLBAR_THUMB`/`ACCENT` — hardcoded as
field initializers so any widget built without `ModLoader` ever running still
gets sane defaults, but overwritten from a loaded `WidgetColorTheme` via
`applyTheme` at startup; see "Widget theming" above), `ListWidget<T>` (a
keyboard-navigable,
optionally non-wrapping list over a pluggable data source, with
`onConfirm`/`onSelectionChange` callbacks and auto-scroll-into-view of the
selected row), `ButtonWidget` (an Enter-confirmable label), `TableWidget<T>`
(a keyboard-navigable row/column table with row-level confirm; `updateRow()`
replaces one row's data and re-renders just its cells without resetting
selection, unlike `setRows()`; `setSelectedRowAccentColor()` and
`setOtherRowsDimmed()` let a consumer flag the selected row as additionally
"armed" for some other in-progress action, with every other row dimmed to
match — the accent outline paints inside each cell's existing padding rather
than adding new border thickness, so a cell's insets never change between
accented and un-accented (an earlier version reserved extra space instead,
which stopped the whole table resizing on selection but shifted the grid
lines inward and opened a visible gap between rows); used by
`SettingsKeybindsPanel`, see `docs/screens.md`), `RadioGroupWidget<T>`
(a single-select radio group, vertical by default or horizontal on demand),
`PatternFieldWidget` (a text-input field validating its content against a
caller-supplied regex pattern), `PopupWidget` (a dismissible overlay,
keyboard-only like the rest of this game — no Close button, since it never
responded to anything but a click; `open()`/`dismiss()` manage visibility and
focus, Escape dismisses it, and `onUp()`/`onDown()`/`onLeft()`/`onRight()`
hooks — bound at `WHEN_ANCESTOR_OF_FOCUSED_COMPONENT`, so they fire whether
the popup itself or a descendant has real Swing focus — let a subclass wire
keyboard navigation to
its own content; `isFullScreen()` returns true by default, but subclasses can
return false to be centered at their preferred size instead of stretched);
`SliderWidget` (a bounded numeric slider with left/right adjustment within a
[min, max] range by fixed steps, with hard bounds—no wrap-around), `FillLayout`
(a `LayoutManager` stretching every child to the parent's full bounds by
default, for `JLayeredPane` overlays; now respects `PopupWidget.isFullScreen()`
to center non-full-screen popups at their preferred size instead), and
`TerminalScrollBarUI` (a flat black-track/solid-thumb `BasicScrollBarUI`
replacing the platform look-and-feel's default scrollbar chrome).
