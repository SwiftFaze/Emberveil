# Releases

Versioning and changelog generation are automatic — you should not hand-edit
the version in `pom.xml` or write `CHANGELOG.md` entries by hand.

## How a release happens

1. Commits land on `master` (via PR, per branch protection) using
   [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`,
   `fix:`, `feat!:`/`BREAKING CHANGE:`, etc.) — the same style already used
   in this repo.
2. [Release Please](https://github.com/googleapis/release-please) (workflow:
   `.github/workflows/release.yml`) watches `master` and keeps a standing
   `chore(main): release x.y.z` pull request up to date, computing the next
   version from those commits (`fix:` → patch, `feat:` → minor, a `!` or
   `BREAKING CHANGE:` footer → major) and drafting `CHANGELOG.md` from their
   messages.
3. Merging that release PR is the actual release trigger: Release Please
   tags the merge commit (`vX.Y.Z`), bumps the `<version>` in `pom.xml` to
   match (see `release-please-config.json`'s `extra-files` entry), and
   creates the GitHub Release with the changelog as its body.
4. That same workflow then builds the Windows installer (see below) and
   uploads it as an asset on the release that was just created.

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
| `windows-latest` | `Emberveil-<version>.exe` | Built via WiX (preinstalled on the runner). Start Menu shortcut, directory chooser. |
| `ubuntu-latest` | `emberveil_<version>*.deb` | Needs `fakeroot` (installed as a workflow step). Debian/Ubuntu only — no `.rpm` variant yet. |
| `macos-latest` | `Emberveil-<version>.pkg` | **Unsigned** — no Apple Developer account, so first launch shows Gatekeeper's "unidentified developer" warning; the user has to right-click → Open once. |

Each job: checks out the release tag, `mvn -B package` (produces
`target/Emberveil-<version>-app.jar`, a single runnable jar with all
dependencies bundled — see the `maven-shade-plugin` execution in
`pom.xml`), then `jpackage --type <exe|deb|pkg>` wraps that jar with a
bundled JRE — no separate Java install required on the player's machine.
The result is uploaded as a release asset.

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
copy target\Emberveil-0.1.0-app.jar target\jpackage-input\
jpackage --type exe --input target\jpackage-input --dest target\dist `
  --name Emberveil --app-version 0.1.0 --vendor SwiftFaze `
  --main-jar Emberveil-0.1.0-app.jar --main-class com.swiftfaze.emberveil.Main `
  --win-menu --win-shortcut --win-dir-chooser
```

Requires a JDK with `jpackage` (bundled since JDK 14) and, for `--type exe`
specifically, the [WiX Toolset](https://wixtoolset.org/) v3 on `PATH`. Without
WiX, use `--type app-image` instead — it produces a `target\dist\Emberveil\Emberveil.exe`
launcher folder (no installer, just unzip-and-run) and needs no extra tooling.
The same idea applies on Linux/macOS with `--type deb`/`--type pkg` swapped
in, run on that OS — `jpackage` cannot target an OS other than the one
it's running on.
