# Intent: Shared list/detail UI contract (Identifiable/DetailDescribable/Inspectable)

- **Slug(s):** shared-list-detail-ui-contract (matches `/specs/features/shared-list-detail-ui-contract.feature`)
- **Author:** rwoolley
- **Date:** 2026-09-02
- **Source:** [GitHub issue #125](https://github.com/SwiftFaze/Veil/issues/125)

## Status

- [x] Intent drafted
- [ ] Spec drafted (`.feature` file) — high-risk path only, see `.claude/workflow.md` (was drafted and even implemented against; deleted along with the composition root it depended on — see Clarifications)
- [ ] Approved by human — high-risk path only
- [x] Implemented
- [x] Manually playtested (human) — see CLAUDE.md's "Repo-specific Step 4.5" (surfaced the PopupWidget Tab focus-traversal bug, fixed in this branch)
- [ ] Acceptance tests passing — blocked, see Clarifications
- [ ] Mutation testing passed — not run this branch; nothing player-facing to re-verify beyond what unit tests already cover, deferred to when the composition root (and this ticket's acceptance tests) are rebuilt
- [x] Documentation updated (`docs/`, and the wiki if player-facing) — no wiki update needed, no player-facing stat/formula/content changed

## Problem

`InventoryPanel` and `CodexPanel` each hand-wrote their own copy of: a
`FieldRow` record, the fields/effects `TableWidget` setup, `updateDetails`,
`buildScrollPane`/`buildBody`, and all Up/Down/Left/Right focus-navigation
logic. Nothing is actually shared — `CodexPanel`'s copy is missing a focus
state entirely (see the linked bug-fix issue). The per-type field mapping
(`CodexPanel.toEntry(Item|Tile|PlayerClass)`, `InventoryPanel.fieldRows(Item)`)
is also hand-written in both panels, so adding a field to `Item` requires
updating two files, and any future catalog type (Quest, Biome) that wants a
Codex tab needs its own hand-written mapping method added to the panel
itself.

Full design (interfaces, guardrails, output/event contract, verified against
the current widget code) is already written up in `docs/components.md` as
general UI-component rules, with this Codex/Inventory case as the worked
example — this issue implements that doc rather than re-deriving the design
here.

## Scope

- In scope:
  - Add `Identifiable`, `DetailDescribable`, `DetailField`, `DetailTable`,
    `Inspectable` per `docs/components.md`.
  - `Item`, `Tile`, `PlayerClass` implement `Inspectable` directly, each
    owning its own `getDetailTables()` (replacing `CodexPanel.toEntry(...)`
    and `InventoryPanel.fieldRows(...)`).
  - Extract a shared details-pane widget (rendering + Up/Down/Left/Right
    focus routing across however many `DetailTable`s an `Inspectable`
    returns) used by both `InventoryPanel` and `CodexPanel`, replacing each
    panel's private copy.
  - `CodexPanel.refreshEntries()` stops calling any `toEntry` method — it
    just selects the already-typed list.
  - Implement the `DetailTable` label-as-data design and the
    `TableWidget.onSelectionChange` decision already resolved below (and
    reflected in `docs/components.md`) — no re-deriving needed during
    implementation.
  - Fixes the underlying bug in the linked bug-fix issue as a side effect,
    though that issue ships independently and faster.
- Out of scope:
  - Expanding what `PlayerClass`'s detail view actually shows (still just
    ID/Name today) — content gap, not this issue's architecture change.
  - Building a Quests tab in the Codex, or a player stats screen (`P` key,
    already scoped under milestone "6. Intro Quest & UI") — those are
    future consumers of this contract, not built here. Mentioned in
    `docs/components.md` only to validate the design generalizes.
  - Reflection/annotation-based automapping — considered and rejected;
    doesn't remove per-field customization (conditional fields, formatted
    fields) and isn't needed once each type owns its own mapping directly.

## Actors

Any player using Inventory (`I`) or Codex (`X`); any future contributor
adding a new mod-loaded catalog type or a new list/detail UI panel.

## Desired behavior

See `docs/components.md` in full. Summary: a type implements `Inspectable`
once; any list/detail panel that receives instances of it needs no per-type
mapping code of its own. Up/Down within a list live-updates the details pane
(confirmed behavior, matches what's shipped today). Right/Left move focus
between the list and however many detail tables exist, with fallthrough
between tables exactly like `InventoryPanel`'s current Fields→Effects
behavior.

## Constraints / non-functional notes

None beyond the usual (see CLAUDE.md function-length/complexity/coverage
budgets). Note `docs/components.md`'s guardrails: the contract stays
data-only (no Swing types in any interface signature), and is opt-in per
type (e.g. `Building` doesn't implement it).

## Open questions

None remaining — the two carried over from `docs/components.md` were
resolved during intent drafting:

- `TableWidget` does not gain `onSelectionChange`. Nothing in this case
  needs live-preview *within* a table (unlike `ListWidget`, where the
  details pane needs to update as the list scrolls); adding it would be
  wiring an output "just in case," which Rule 4 already warns against.
  The asymmetry with `ListWidget` is intentional.
- `DetailTable` gains a `label` field (`DetailTable(String label,
  List<String> columnHeaders, List<List<String>> rows)`) instead of the
  widget hardcoding section names by table position. A positional
  convention would move Rule 1's violation up one level and breaks for
  `PlayerClass` (one table) or any future type that doesn't fit the
  current two-table shape. `""` renders no heading, matching
  `InventoryPanel`'s current unlabeled Fields table.

`docs/components.md` has been updated to match both decisions.

## Clarifications

- Q: (not a spec question — a mid-implementation scope addition from the
  author) Can we also remove `EastPanel`/`NorthPanel`/`SouthPanel`/
  `PlayerInfoPanel`/`TerminalPanel` on this branch? They're early
  scaffolding from the very start of the project and aren't being used
  — want to reimplement them properly later.
  A: Confirmed, full cascade, on this branch, after the orchestrator
  flagged the actual blast radius (these classes turned out to be the
  live game's composition root, not dead code — `EastPanel` specifically
  wired `InventoryPanel`/`CodexPanel` into the game and was the only way
  the acceptance-test harness could construct/open them).
  Affects: general — not a Gherkin scenario change, a removal of the
  scenarios/harness entirely. Consequences:
  - `InventoryPanel`, `CodexPanel`, and `DetailsPaneWidget` (this issue's
    own deliverables) are untouched as classes and keep their unit test
    coverage. Initially left unwired entirely; the author then asked for
    the I/X toggle behavior back (same open/dismiss/mutual-exclusion/
    focus-restore as before), so `Main.buildGameCard`/`wirePopups` plus a
    new small `ui/PopupToggleListener` class now do that wiring — no
    sidebar, no player-info display, no `NorthPanel`/`SouthPanel` chrome.
    (First attempt nested `PopupToggleListener` inside `Main` — failed
    `ModuleDependencyTest`'s ArchUnit gate, since a nested class isn't
    `equivalentTo(Main.class)` and so isn't covered by `Main`'s
    UI-dependency exemption; moved to `ui/` to fix.) This is explicitly a
    stopgap, not the "proper reimplementation" the author still intends to
    do later.
  - This issue's own acceptance spec,
    `specs/features/shared-list-detail-ui-contract.feature`, had to be
    deleted rather than wired (Step 5) — it depended entirely on
    `EastPanel` to construct/open `CodexPanel` in tests. The "Acceptance
    tests passing" status box above stays unchecked until the
    composition root exists again and this spec (or its replacement) can
    actually be wired.
  - `codex-ui.feature`, `ui-panel-rendering-and-composition.feature` were
    deleted outright (entirely `EastPanel`-dependent); `ui-widget-table.
    feature`, `ui-widget-radio-group.feature`, `ui-component-framework.
    feature`, `keyboard-input-and-menu-navigation.feature` had their
    `EastPanel`-dependent scenarios surgically removed, keeping every
    scenario that tests a widget/panel in isolation. See each file's own
    trailing "Removal note" comment.
  - `ClassSandboxPanel` (the dev-only sandbox) lost its `TerminalPanel`
    base; its two lines of shared construction logic and its
    `makeLabel()` helper were inlined directly into it.
  - `GameWindow.buildContentArea` dropped its `EastPanel` parameter —
    it now just wraps `GamePanel` in a `JLayeredPane`, ready for
    whatever the reimplemented composition root adds later.
