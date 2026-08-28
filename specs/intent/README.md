# Intent docs

One `<feature-slug>.md` file per feature, written by a human before any spec
or code exists. This is the source of truth for *why* a feature exists and
what it must do — `/specs/features/<feature-slug>.feature` is generated from
it and never the other way around.

Intent is not written once and frozen: clarifying answers gathered while
drafting the `.feature` file get appended back here, then the `.feature` file
is regenerated from the updated intent.

See `default-player-class.md` for a worked (if retroactive) example.
