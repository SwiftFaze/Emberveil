# Component contracts

This documents a convention, not (yet) a fully-built one: how a shared
Swing UI component (a list, a table, a details pane) should receive its
data and report what the player does with it, so a new mod-loaded content
type (Item, Tile, PlayerClass, Quest, and future types like Biome) never
requires the *component* to be taught that type's shape. The type teaches
itself.

**Why this exists**: `CodexPanel` and `InventoryPanel` each hand-wrote
their own `toEntry(Item)`/`fieldRows(Item)` mapping methods, duplicated
between the two panels, and `CodexPanel` separately duplicated
`InventoryPanel`'s focus-navigation logic and dropped a state doing it
(no `EFFECTS` focus, so the Effects table was rendered but keyboard-
unreachable). Both problems come from the same root cause: the panel
knew how to describe a domain type, instead of the type describing
itself. This isn't a new idea in this codebase — `Positionable` →
`DrawableAsciiEntity` (see `architecture.md`'s "Rendering contracts")
already does this for world rendering, where `GamePanel` iterates
`entitiesToDraw` generically instead of switching on entity type. This
doc extends the same principle to list/table/detail UI.

## Input contract

```java
public interface Identifiable {
    String getId();
    String getName();
}

public record DetailField(String label, String value) {}

public record DetailTable(List<String> columnHeaders, List<List<String>> rows) {}

public interface DetailDescribable {
    List<DetailTable> getDetailTables();
}

public interface CodexInspectable extends Identifiable, DetailDescribable {}
```

- **`Identifiable`** is for anything that appears in a browsable list —
  `ListWidget<T>` already accepts any `T` plus a name-renderer function;
  types implementing `Identifiable` let that renderer default to
  `Identifiable::getName` instead of every call site repeating its own
  lambda (`Item::getName`, `CodexEntry::name`, `s -> s`, ...).
- **`DetailDescribable`** is for anything that has a details pane, list
  or no list. `Item`, `Tile`, `PlayerClass`, `Quest` implement
  `CodexInspectable` (both list-selectable and detail-renderable). A
  future player stats screen (`entities/player/Stats.java`) would
  implement `DetailDescribable` only — there's exactly one player, so
  there's nothing to list, only a details pane to render directly.
- **`DetailTable`** replaces the current hardcoded "Fields table + a
  second table typed as `List<Item.Effect>`" shape. `Item` returns two
  tables (Fields, Effects); `Tile`/`PlayerClass` return one; a stats
  screen could return one (Stats) or two (Stats + Active Effects, once
  buffs/debuffs exist) — same generic shape, no `Item`-specific type
  leaking into the contract.

**Guardrail — keep this data-only.** No Swing type may appear in any of
these signatures, and no method may vary its output by which component
is asking. The moment two consumers want to display the same type
differently, that's a sign a new field or a narrower method belongs on
the type's own implementation — not that a component should go back to
hand-mapping "just this once."

**Guardrail — opt-in, not universal.** Not everything `ModRegistry`
holds is Codex-shaped. `Building` (`entities/buildings/Building.java`)
is a placed world instance (`worldX`/`worldY` + a blueprint), not a
catalog entry, and implements neither interface.

## Output contract (events a component reports)

Checked against the current widgets, not assumed:

- **`ListWidget<T>.moveUp()`/`moveDown()`** already fire
  `onSelectionChange(T)` on *every* cursor move (`ListWidget.java:144`,
  inside `refreshHighlight()`) — this is what currently drives
  `updateDetails()` in both panels as the player cursors through the
  list, not something new to add.
- **`onConfirm(T)`** fires only on the confirm key (Enter), separately
  from selection change — used by menu-style lists (`TitleScreenPanel`,
  `ClassSandboxPanel`) where moving the cursor shouldn't trigger the
  action.
- **`TableWidget<T>` has `onConfirm` but no `onSelectionChange`**,
  unlike `ListWidget` — an existing asymmetry. Any shared details widget
  built against `DetailTable` should standardize both widgets on the
  same input/output shape rather than carry this forward.
- **Left/Right (moving between list and details, or between detail
  tables) is not an event at all today** — `PopupWidget.onUp/onDown/
  onLeft/onRight` (`PopupWidget.java:75-85`) are empty template-method
  hooks a subclass overrides directly; `CodexPanel`/`InventoryPanel`
  each mutate their own private `Focus` field in response. This is the
  structural layer that coordinates *which* child widget currently owns
  Up/Down — it's a different mechanism from `onSelectionChange`/
  `onConfirm`, and it's exactly the piece that got copy-pasted (and, in
  `CodexPanel`'s case, copy-pasted incompletely) between the two panels.
  A shared details-pane component should own this coordination once,
  keyed off however many `DetailTable`s the current `DetailDescribable`
  returned, instead of each popup re-deriving its own `Focus` enum.

## Open questions (not yet decided)

- Should `TableWidget` gain `onSelectionChange` for parity with
  `ListWidget`, or is "only fires on confirm" intentional for tables and
  the asymmetry is fine?
- Does the shared details widget render one `TableWidget` per
  `DetailTable`, or does `DetailTable` need a section label
  (e.g. "Effects:") as part of its own data rather than a label the
  widget hardcodes?
