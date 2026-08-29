# Releases

Versioning and changelog generation are automatic — you should not hand-edit
the version in `pom.xml` or write `CHANGELOG.md` entries by hand.

## Two channels: stable (`master`) and beta (`develop`)

- **`master` → stable releases** (`vX.Y.Z`). This is what most players
  should install.
- **`develop` → beta prereleases** (`vX.Y.Z-beta.N`). GitHub marks these
  releases as a "Pre-release" automatically (Release Please's
  `prerelease: true` config). Merging `develop` into `master` is what
  "promotes" accumulated beta work into the next real stable release.

Both are handled by the same `.github/workflows/release.yml`, via two
independent Release Please jobs (`release-please-stable`,
`release-please-beta`) gated on which branch was pushed to, each with its
own config/manifest (`release-please-config.json` +
`.release-please-manifest.json` for stable, the `-beta` variants of both
for beta) so the two version lines and changelogs don't collide.

## How a release happens

1. Commits land on `master` or `develop` (via PR, per branch protection)
   using [Conventional Commits](https://www.conventionalcommits.org/)
   (`feat:`, `fix:`, `feat!:`/`BREAKING CHANGE:`, etc.) — the same style
   already used in this repo.
2. [Release Please](https://github.com/googleapis/release-please) watches
   both branches and keeps a standing `chore(<branch>): release x.y.z`
   pull request up to date on each, computing the next version from those
   commits (`fix:` → patch, `feat:` → minor, a `!` or `BREAKING CHANGE:`
   footer → major) and drafting `CHANGELOG.md` from their messages.
3. Merging that release PR is the actual release trigger: Release Please
   tags the merge commit (`vX.Y.Z` on `master`, `vX.Y.Z-beta.N` on
   `develop`), bumps the `<version>` in `pom.xml` to match (see the
   `extra-files` entry in the relevant config file), and creates the
   GitHub Release with the changelog as its body.
4. That same workflow then builds the installers (see below) and uploads
   them as assets on the release that was just created.

The merge in step 3 happens automatically — no human click needed. Right
after Release Please creates/updates the PR, the same job asks GitHub to
auto-merge it (`gh pr merge --auto`), which waits for the branch's
required status checks to pass and then merges it for you. This exists
because leaving the release PR sitting open was actively harmful: every
other PR that merged into `develop`/`master` in the meantime kept
updating that same standing PR, and if it sat unmerged for a while before
finally being merged, it was easy to lose track of which commits were and
weren't actually reflected in `CHANGELOG.md` for that version. Auto-merge
means each release goes out the moment the commit that should trigger it
lands and CI is green, so there's no window where the release PR can go
stale.

Nothing about this requires a human to decide "what's the next version
number" — that's entirely derived from commit messages. If a change
shouldn't trigger a release at all, use a non-triggering type (`chore:`,
`docs:`, `test:`, `style:`) or `fix:`/`feat:` as appropriate — Release
Please only reacts to types that map to a version bump.

## Installer builds

Triggered only when Release Please reports a release was actually created
(i.e. on merge of the release PR, not on every push to `master`). The
`build-installers` job in `.github/workflows/release.yml` runs as a matrix
across three runners, since `jpackage` only builds for the OS it runs on
— there's no cross-compiling a Windows installer from Linux, etc.:

| Runner | Output | Notes |
|---|---|---|
| `windows-latest` | `Veil-<version>.exe` | Built via WiX (preinstalled on the runner). Start Menu shortcut, directory chooser. |
| `ubuntu-latest` | `Veil-<version>.deb` | Needs `fakeroot` (installed as a workflow step). Debian/Ubuntu only — no `.rpm` variant yet. |
| `macos-latest` | `Veil-<version>.pkg` | **Unsigned** — no Apple Developer account, so first launch shows Gatekeeper's "unidentified developer" warning; the user has to right-click → Open once. |

Each job: checks out the release tag, `mvn -B package` (produces
`target/Veil-<version>-app.jar`, a single runnable jar with all
dependencies bundled — see the `maven-shade-plugin` execution in
`pom.xml`), then `jpackage --type <exe|deb|pkg>` wraps that jar with a
bundled JRE — no separate Java install required on the player's machine —
and the "Normalize installer filename" step renames whatever `jpackage`
produced (its own per-OS naming conventions vary, e.g. Debian's
underscore-separated scheme) to a consistent `Veil-<version>.<ext>`.
The result is uploaded as a release asset.

**Beta version numbers**: `jpackage`'s Windows/macOS installers require a
clean numeric `--app-version` (no `-beta.N` suffix accepted), so the
workflow strips the suffix for that flag specifically
(`APP_VERSION=${full_version%%-*}`) while the uploaded filename still uses
the full version including the suffix (`FULL_VERSION`), so e.g. a
`v0.3.0-beta.2` release uploads `Veil-0.3.0-beta.2.exe` even though
the installer's own internal version metadata just says `0.3.0`.

**Pre-1.0 versions on macOS**: `jpackage --type pkg` additionally rejects a
major version of `0` ("The first number in an app-version cannot be zero
or negative"). While the project is still `0.x.y`, the macOS job bumps
just its own internal bundle version metadata (`0.2.0` → `1.2.0`) — the
uploaded filename is unaffected and still shows the real version. This
step becomes a no-op once the project reaches `1.0.0`.

### Testing the installer build without cutting a release

The workflow also accepts `workflow_dispatch` (Actions tab → Release →
Run workflow, or `gh workflow run release.yml`). This runs the same
`build-installers` matrix against the current commit and uploads each
platform's installer as a workflow artifact instead of a release asset —
useful for validating a jpackage change (e.g. a new platform, new
`jpackage` flags) without waiting for or faking an actual release.

### Building an installer locally

```powershell
mvn -B clean package
mkdir target\jpackage-input
copy target\Veil-0.1.0-app.jar target\jpackage-input\
jpackage --type exe --input target\jpackage-input --dest target\dist `
  --name Veil --app-version 0.1.0 --vendor SwiftFaze `
  --main-jar Veil-0.1.0-app.jar --main-class com.swiftfaze.veil.Main `
  --win-menu --win-shortcut --win-dir-chooser
```

Requires a JDK with `jpackage` (bundled since JDK 14) and, for `--type exe`
specifically, the [WiX Toolset](https://wixtoolset.org/) v3 on `PATH`. Without
WiX, use `--type app-image` instead — it produces a `target\dist\Veil\Veil.exe`
launcher folder (no installer, just unzip-and-run) and needs no extra tooling.
The same idea applies on Linux/macOS with `--type deb`/`--type pkg` swapped
in, run on that OS — `jpackage` cannot target an OS other than the one
it's running on.
