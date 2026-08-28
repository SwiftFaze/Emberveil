# Veil

Veil is a 2D ASCII-style open-world RPG

## Getting the game

Grab the latest installer for your platform from the
[Releases page](https://github.com/SwiftFaze/Veil/releases) —
Windows (`.exe`), Linux (`.deb`), and macOS (`.pkg`, unsigned — right-click
→ Open past the first Gatekeeper warning) are all supported. Prereleases
tagged `-beta.N` are newer, less stable builds; anything without a beta
tag is the current stable release.

## Running from source

Requires JDK 17+ and Maven.

```
mvn compile exec:java
```

See `CLAUDE.md` and `docs/` for the full architecture, testing, and
release documentation.
