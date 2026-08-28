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

## Windows installer build

Triggered only when Release Please reports a release was actually created
(i.e. on merge of the release PR, not on every push to `master`):

1. Checks out the new tag.
2. `mvn -B package` — produces `target/Emberveil-<version>-app.jar`, a
   single runnable jar with all dependencies bundled (see the
   `maven-shade-plugin` execution in `pom.xml`).
3. `jpackage --type exe` wraps that jar with a bundled JRE into
   `EmberveilInstaller-<version>.exe` (built via WiX, preinstalled on the
   `windows-latest` GitHub Actions runner) — a self-contained Windows
   installer with a Start Menu shortcut, no separate Java install required
   on the player's machine.
4. The installer is uploaded as an asset on the GitHub Release.

### Building the installer locally

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
