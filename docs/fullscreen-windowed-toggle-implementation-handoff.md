# Fullscreen/Windowed toggle — Step 4/5/7 implementation handoff

This is the exact handoff prompt given to the fresh Claude Haiku 4.5 agent
that implemented `specs/intent/fullscreen-windowed-toggle.md` /
`specs/features/fullscreen-windowed-toggle.feature` (GitHub issue #136),
per `.claude/workflow.md`'s Step 4 guidance (a self-contained,
excerpt-rich prompt so the implementing agent doesn't need to explore the
codebase itself). Saved here as a record of the design decisions made
during the handoff — not a maintained reference doc; if it drifts from
what was actually implemented, the code and `.feature` files are the
source of truth, not this file.

---

You are implementing Steps 4 (Implementation), 5 (Acceptance tests), and 7 (Documentation) of a spec-first pipeline for the Veil project (2D ASCII-tile Java 17 Swing game, `com.swiftfaze.veil`), on branch `feat/fullscreen-windowed-toggle`. All design/architecture decisions below have already been made by the orchestrating session — do NOT redesign, do NOT explore the codebase beyond the files and line numbers listed here, and do NOT read `specs/intent/fullscreen-windowed-toggle.md` or the `.feature` files unless a specific instruction below tells you to (their content relevant to you is already excerpted here). If something you need turns out to be missing, wrong, or insufficient, STOP and report exactly what's missing rather than grepping/exploring around for it.

## Goal

Wire the Settings screen's Fullscreen/Windowed radio row to the real game `JFrame`. Currently toggling it only updates its own display + persists to `settings.json` (already-existing `SettingsStore`/`SettingsConfig` infra from a prior feature) — nothing ever applies it to the real window. Also: make the Windowed frame's `Camera` viewport track the panel's live pixel size (so resizing the window reveals more/less map), and **delete the F5 hot-reset dev feature entirely** (this was requested mid-session by the user, superseding the original GitHub issue's text — the PR description will explain this deviation, you don't need to justify it further, just do it).

## Verified facts you don't need to re-derive

- Cucumber tests in this repo run headless and NEVER construct a real `JFrame` (see `UiComponentFrameworkSteps.java` around line 838-842, comment: "a live JFrame isn't constructed in headless tests"). So none of your acceptance-test scenarios touch real window decoration/fullscreen/resize — they only assert a mocked callback receives the right string. Real-window behavior is manual-playtest-only (see Step 4.5 note at the end).
- PMD's actual enforced parameter-count threshold (I verified this empirically by running `mvn org.apache.maven.plugins:maven-pmd-plugin:3.28.0:check` against a throwaway probe method) is: **5 parameters is fine, 6+ fails the build.** (CLAUDE.md says "max 4" but the real configured/enforced gate in `.pmd-minimal.xml` — `ExcessiveParameterList` with `minimum=5` — only fires at 6+; I confirmed this directly, don't second-guess it.) Every signature below stays at ≤5 params for exactly this reason — do not add more.
- `GameWindow.buildContentArea`'s `JLayeredPane` + `FillLayout` (in `com.swiftfaze.veil.ui.widget.FillLayout`) already stretches `GamePanel` to whatever live size its container has on every layout pass (`child.setBounds(0, 0, parent.getWidth(), parent.getHeight())`), and `CardLayout`/`BorderLayout.CENTER` already propagate a live JFrame resize all the way down to `GamePanel`. **You do not need to touch `GameWindow.java` or `FillLayout.java` at all** — `GamePanel.getWidth()`/`getHeight()` will already correctly reflect the live window size once the frame becomes resizable. Your only job on the Camera side is making `Camera`'s viewport mutable and having `GamePanel` feed it the live size each paint.

## File 1: `src/main/java/com/swiftfaze/veil/Camera.java` (full current content, 21 lines)

```java
package com.swiftfaze.veil;

public class Camera {
    private int x;
    private int y;
    private final int viewportWidth;
    private final int viewportHeight;

    public Camera(int viewportWidth, int viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public void centerOn(int targetX, int targetY) {
        x = targetX - viewportWidth / 2;
        y = targetY - viewportHeight / 2;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
```

**Change to:** make `viewportWidth`/`viewportHeight` non-final, add a `resizeViewport(int, int)` method, and clamp both the constructor and `resizeViewport` to a minimum of 5 tiles in each dimension (a `private static final int MIN_VIEWPORT_TILES = 5;` constant), via `Math.max(MIN_VIEWPORT_TILES, ...)`. This exact 5-tile floor is dictated by `specs/features/camera-behavior.feature`'s new scenarios (see File 6 below) — don't pick a different number.

## File 2: `src/main/java/com/swiftfaze/veil/game/GamePanel.java`

Relevant current lines:
```java
22: private final Player player = new Player(DEFAULT_PLAYER_START_X, DEFAULT_PLAYER_START_Y);
23: private TileTestScene2 scene = new TileTestScene2(DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT);
24: private final Camera camera = new Camera(GAME_WINDOW_WIDTH, GAME_WINDOW_HEIGHT);
...
88: @Override
89: protected void paintComponent(Graphics g) {
90:     super.paintComponent(g);
91:     Graphics2D g2d = (Graphics2D) g;
92:     camera.centerOn(player.getX(), player.getY());
93:
94:     scene.renderWorld(
```
`TILE_WIDTH`/`TILE_HEIGHT` are statically imported from `GameConst` (`import static com.swiftfaze.veil.GameConst.*;` already present at the top of the file).

**Change:** insert one line before `camera.centerOn(player.getX(), player.getY());` (line 92):
```java
camera.resizeViewport(getWidth() / TILE_WIDTH, getHeight() / TILE_HEIGHT);
```
No other change needed to this file. (It's fine/expected that the very first paint before layout completes might briefly compute a 0-sized viewport that gets clamped to the 5-tile floor — this self-corrects on the next repaint once real layout size is known; don't add extra guard logic for it.)

## File 3: `src/main/java/com/swiftfaze/veil/ui/SettingsScreenPanel.java` (full current content, 337 lines — read it yourself, it's already in your working tree at this path; the excerpts below are the only parts that change)

Current constructor (lines 50-86) takes `(Consumer<String> onBack, Consumer<String> onOpenFolder, ControlsHintBarWidget hintBar, SettingsStore settingsStore)` — **do not change this signature** (it's already at the safe 4-param count and multiple call sites depend on it).

**Add a new field** (near the other fields, e.g. after `private final SettingsStore settingsStore;`):
```java
private Consumer<String> onWindowModeChanged = mode -> { };
```
(default no-op so any code path that never calls the new setter below — including every *existing* Cucumber scenario that doesn't care about window mode — stays safe with zero behavior change).

**Add a new public method** (a setter that also immediately applies the current persisted value — this is what makes "launching with an already-persisted Fullscreen value applies immediately" work, both in real `Main.java` usage and in tests):
```java
public void setOnWindowModeChanged(Consumer<String> onWindowModeChanged) {
    this.onWindowModeChanged = onWindowModeChanged;
    onWindowModeChanged.accept(settingsStore.config().getFullscreen());
}
```

**Modify `syncAndPersist`** (current lines 212-222):
```java
private void syncAndPersist(SettingsRow row) {
    switch (row.name) {
        case "Brightness" -> settingsStore.config().setBrightness(((SliderWidget) row.widget).getValue());
        case "Fullscreen" -> settingsStore.config().setFullscreen(String.valueOf(((RadioGroupWidget<?>) row.widget).getHighlightedOption()));
        case "Font" -> settingsStore.config().setFont(String.valueOf(((RadioGroupWidget<?>) row.widget).getHighlightedOption()));
        case "Theme" -> settingsStore.config().setTheme(String.valueOf(((RadioGroupWidget<?>) row.widget).getHighlightedOption()));
        case "Volume" -> settingsStore.config().setVolume(((SliderWidget) row.widget).getValue());
        default -> { return; }
    }
    settingsStore.persist();
}
```
Add, after `settingsStore.persist();`, a call that fires the window-mode callback specifically when the changed row was "Fullscreen":
```java
if ("Fullscreen".equals(row.name)) {
    onWindowModeChanged.accept(settingsStore.config().getFullscreen());
}
```

**Modify `resetAllToDefaults`** (current lines 242-247):
```java
private void resetAllToDefaults() {
    settingsStore.config().resetToDefaults();
    initializeRows();
    refresh();
    settingsStore.persist();
}
```
Add one line at the end:
```java
onWindowModeChanged.accept(settingsStore.config().getFullscreen());
```

That's the entire change to this file. `Consumer` is already imported (`import java.util.function.Consumer;` at the top).

## File 4: `src/main/java/com/swiftfaze/veil/Main.java` (full current content, 211 lines — already in your working tree)

Current relevant methods:
```java
42: public static void main(String[] args) {
43:     loadGame();
44: }
45:
46: private static void loadGame() {
47:     JFrame frame = new JFrame("Veil");
48:     CardLayout cardLayout = new CardLayout();
49:     JPanel cardPanel = new JPanel(cardLayout);
50:     loadAndApplyDefaultTheme();
51:     Map<String, JComponent> cards = new HashMap<>();
52:     ControlsHintBarWidget hintBar = new ControlsHintBarWidget();
53:
54:     GamePanel gamePanel = buildGameCard(cardPanel, cards, hintBar);
55:     buildUIScreens(cardLayout, cardPanel, cards, gamePanel, hintBar);
56:     configureAndShowFrame(frame, cardPanel, cardLayout, hintBar);
57: }
```
```java
103: private static void buildUIScreens(CardLayout cardLayout, JPanel cardPanel, Map<String, JComponent> cards,
104:                                     GamePanel gamePanel, ControlsHintBarWidget hintBar) {
105:     TitleScreenPanel titleScreen = new TitleScreenPanel(menuItem -> {
106:         handleMenuSelection(menuItem, cardLayout, cardPanel, cards, gamePanel);
107:         if ("New".equals(menuItem)) {
108:             hintBar.setHints(GAME_HINTS);
109:         }
110:     }, hintBar);
111:     SettingsStore settingsStore = new SettingsStore(Path.of("").toAbsolutePath());
112:     SettingsScreenPanel settingsScreen = new SettingsScreenPanel(
113:             screen -> navigateTo(cardLayout, cardPanel, cards, screen), Main::openFolder, hintBar, settingsStore);
114:     SettingsKeybindsPanel keybindsScreen = new SettingsKeybindsPanel(
115:             screen -> navigateTo(cardLayout, cardPanel, cards, screen), hintBar, settingsStore);
116:     cards.put("title", titleScreen);
117:     cards.put("settings", settingsScreen);
118:     cards.put("keybinds", keybindsScreen);
119:     cardPanel.add(titleScreen, "title");
120:     cardPanel.add(SettingsWindow.buildContentArea(settingsScreen), "settings");
121:     cardPanel.add(SettingsKeybindsWindow.buildContentArea(keybindsScreen), "keybinds");
122: }
```
**Do not change `buildUIScreens`'s signature or body at all** — it's already at 5 params, adding a 6th (e.g. `frame`) would fail PMD. `SettingsScreenPanel`'s constructor call at line 112-113 is also unchanged (4 args, as today).

```java
135: private static void configureAndShowFrame(JFrame frame, JPanel cardPanel, CardLayout cardLayout,
136:                                            ControlsHintBarWidget hintBar) {
137:     frame.setLayout(new BorderLayout());
138:     frame.add(cardPanel, BorderLayout.CENTER);
139:     frame.add(hintBar, BorderLayout.SOUTH);
140:     frame.pack();
141:     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
142:     frame.setResizable(false);
143:     frame.setLocationRelativeTo(null);
144:     frame.setVisible(true);
145:     cardLayout.show(cardPanel, "title");
146:     ((JComponent) cardPanel.getComponent(0)).requestFocusInWindow();
147:     keyListen(frame);
148: }
```
```java
188: private static void keyListen(JFrame frame) {
189:     JRootPane rootPane = frame.getRootPane();
190:     rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
191:             .put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "resetGame");
192:     rootPane.getActionMap().put("resetGame", new AbstractAction() {
193:         @Override
194:         public void actionPerformed(java.awt.event.ActionEvent e) {
195:             resetGame(frame);
196:         }
197:     });
198: }
199:
200: private static void resetGame(JFrame oldFrame) {
201:     try {
202:         oldFrame.dispose(); // closes old window, releases its listeners
203:         loadGame();
204:         logger.info("Scene Reset");
205:     } catch (Exception e) {
206:         logger.error("Reset failed", e); // pass the Throwable, not e.getMessage()
207:     }
208: }
```

**Changes to Main.java:**

1. **Delete `keyListen` and `resetGame` entirely** (lines 188-208) — the F5 hot-reset feature is being removed.

2. **In `configureAndShowFrame`:**
   - Change the signature to add one more parameter, `Map<String, JComponent> cards` (5 params total — safe): `private static void configureAndShowFrame(JFrame frame, JPanel cardPanel, CardLayout cardLayout, ControlsHintBarWidget hintBar, Map<String, JComponent> cards)`.
   - Remove the `frame.setResizable(false);` line (window mode now owns resizable state).
   - Remove the `keyListen(frame);` line (F5 removed).
   - Insert a call to a new `wireWindowMode(frame, cards);` method **right after `frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);` and before `frame.setLocationRelativeTo(null);`** — i.e. after `frame.pack()` has already run (this ordering matters: the initial window-mode application, if Fullscreen, needs to override the packed size with the full screen bounds, not the other way around).
   - Update the call site in `loadGame()` (line 56) to pass `cards`: `configureAndShowFrame(frame, cardPanel, cardLayout, hintBar, cards);`.

3. **Add three new private static methods** (place them near `configureAndShowFrame`):

```java
private static void wireWindowMode(JFrame frame, Map<String, JComponent> cards) {
    SettingsScreenPanel settingsScreen = (SettingsScreenPanel) cards.get("settings");
    settingsScreen.setOnWindowModeChanged(mode -> {
        applyWindowMode(frame, mode);
        settingsScreen.requestFocusInWindow();
    });
}
```
(The `requestFocusInWindow()` call matters for the *live* toggle case — see below, `applyWindowMode` disposes/re-shows the frame's native peer, which drops Swing's focus owner; refocusing the settings screen restores keyboard input. It's a harmless no-op at initial-launch time since the settings card isn't the visible one yet — `configureAndShowFrame`'s existing `cardLayout.show(cardPanel, "title")` + its own `requestFocusInWindow()` call, which run right after, correctly override it for the real startup case.)

```java
private static final int MIN_WINDOW_WIDTH = 400;
private static final int MIN_WINDOW_HEIGHT = 300;

private static void applyWindowMode(JFrame frame, String mode) {
    boolean fullscreen = "Fullscreen".equals(mode);
    boolean wasDisplayable = frame.isDisplayable();
    if (wasDisplayable) {
        frame.dispose(); // Swing requires this before setUndecorated on an already-shown frame
    }
    frame.setUndecorated(fullscreen);
    frame.setResizable(!fullscreen);
    if (fullscreen) {
        frame.setBounds(currentScreenBounds(frame));
    } else {
        frame.setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));
        frame.pack();
    }
    if (wasDisplayable) {
        frame.setVisible(true);
    }
}

private static Rectangle currentScreenBounds(JFrame frame) {
    GraphicsConfiguration config = frame.getGraphicsConfiguration();
    GraphicsDevice device = config != null
            ? config.getDevice()
            : GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    return device.getDefaultConfiguration().getBounds();
}
```
(`Dimension`, `Rectangle`, `GraphicsConfiguration`, `GraphicsDevice`, `GraphicsEnvironment` are all covered by the existing `import java.awt.*;` wildcard import already at the top of `Main.java` — no new imports needed for these.)

4. **Remove the now-unused `import java.awt.event.KeyEvent;`** (only used by the deleted `keyListen`) — check the rest of the file doesn't use `KeyEvent` elsewhere before removing (it shouldn't).

**Why this design (context, not something to second-guess):** `applyWindowMode` is called from two places: (a) `wireWindowMode`, once at every real launch (fresh or — previously — F5, now just fresh, since F5 is gone) with whatever `SettingsConfig.getFullscreen()` already says, satisfying "launch already in the persisted mode"; (b) implicitly again on every live toggle via the same `onWindowModeChanged` callback `SettingsScreenPanel` now fires. The `wasDisplayable` branching handles both cases correctly with one method: at initial launch the frame isn't shown yet (no dispose/re-show needed, flags/bounds just get set once before `setVisible(true)` runs later in `configureAndShowFrame`); for a live toggle while already playing, Swing requires `dispose()` before `setUndecorated()` can change on an already-displayable frame, then `setVisible(true)` recreates the peer — the existing Swing component tree (cardPanel, gamePanel, all game state) is untouched by this, only the native window peer is recreated.

## File 5: `src/test/java/com/swiftfaze/veil/steps/UiComponentFrameworkSteps.java`

Current relevant excerpt (fields, around line 106-108):
```java
    // Settings persistence test support
    private Path tempDir;
    private SettingsStore settingsStore;
```

Current `@Before` hook (lines 45-69) resets shared fields each scenario, including `settingsScreenPanel = null;` and `settingsStore = null;`.

Current `theSettingsScreenIsShown()` (lines 633-654):
```java
    @Given("the settings screen is shown")
    public void theSettingsScreenIsShown() throws Exception {
        if (settingsScreenPanel != null) {
            return;
        }
        if (tempDir == null) {
            tempDir = Files.createTempDirectory("veil-test-");
        }
        if (settingsStore == null) {
            settingsStore = new SettingsStore(tempDir);
        }
        settingsScreenPanel = new SettingsScreenPanel(
            screen -> {
                // Menu action callback
            },
            folder -> {
                // Open folder callback
            },
            hintBar,
            settingsStore
        );
    }
```

**Changes:**

1. Add a new field near `settingsStore`: `private String lastWindowMode;`
2. Add `lastWindowMode = null;` to the `@Before beforeScenario()` reset block (alongside the other resets).
3. In `theSettingsScreenIsShown()`, after the `settingsScreenPanel = new SettingsScreenPanel(...)` call, add:
```java
settingsScreenPanel.setOnWindowModeChanged(mode -> lastWindowMode = mode);
```
4. Add a new step definition method anywhere sensible in the file (near the other settings-screen `@Then` steps, e.g. right after `itemsValueIs`):
```java
@Then("the game window switches to {string} mode")
public void theGameWindowSwitchesToMode(String expectedMode) {
    assertEquals(expectedMode, lastWindowMode);
}
```

**This is a shared step-definitions file backing several `.feature` files** (`settings-screen.feature`, `settings-persistence.feature`, `settings-keybinds-page.feature`, `confirmation-popup-variant.feature`, and now `fullscreen-windowed-toggle.feature`). Per this repo's workflow, after your change here, **run `mvn clean test` (or `mvn clean verify`) TWICE in a row and require identical results** before considering this step done — a real problem in shared test infra can show up as flaky/order-dependent failures instead of a clean deterministic one.

## File 6: `src/test/java/com/swiftfaze/veil/steps/CameraBehaviorSteps.java` (full current content, 35 lines)

```java
package com.swiftfaze.veil.steps;

import com.swiftfaze.veil.Camera;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CameraBehaviorSteps {

    private Camera camera;

    @Given("a camera with a viewport {int} tiles wide and {int} tiles tall")
    public void aCameraWithAViewportTilesWideAndTilesTall(int width, int height) {
        camera = new Camera(width, height);
    }

    @When("the camera centers on position \\({int}, {int})")
    public void theCameraCentersOnPosition(int x, int y) {
        camera.centerOn(x, y);
    }

    @Given("the camera has centered on position \\({int}, {int})")
    public void theCameraHasCenteredOnPosition(int x, int y) {
        camera.centerOn(x, y);
    }

    @Then("the camera's offset is \\({int}, {int})")
    public void theCamerasOffsetIs(int x, int y) {
        assertEquals(x, camera.getX());
        assertEquals(y, camera.getY());
    }
}
```

**Add one new step method** (matching the `.feature` file's new scenarios — see below):
```java
    @When("the viewport is resized to {int} tiles wide and {int} tiles tall")
    public void theViewportIsResizedToTilesWideAndTilesTall(int width, int height) {
        camera.resizeViewport(width, height);
    }
```

## The two `.feature` files (already written and finalized by the orchestrator — DO NOT edit their content, they're the acceptance criteria your step defs above must satisfy)

`specs/features/fullscreen-windowed-toggle.feature` (already exists in your working tree) has 4 scenarios exercising: toggling Fullscreen right/left, launching with an already-persisted Fullscreen value, and Reset to Defaults — all via the `settingsScreenPanel`/`lastWindowMode` wiring above. Read it if you want to see exact step text, but the step defs above already implement everything it needs.

`specs/features/camera-behavior.feature` (already exists, extended) has 2 new scenarios:
```gherkin
  Scenario: Resizing the viewport changes subsequent centering offsets
    When the viewport is resized to 20 tiles wide and 16 tiles tall
    And the camera centers on position (100, 100)
    Then the camera's offset is (90, 92)

  Scenario: Resizing the viewport below the minimum clamps to a 5x5 floor
    When the viewport is resized to 2 tiles wide and 1 tiles tall
    And the camera centers on position (50, 50)
    Then the camera's offset is (48, 48)
```
(Note: "1 tiles tall" — not a typo, deliberately not pluralization-aware to match a single step pattern; don't "fix" it.)

## Verification loop (do this, don't skip)

1. `mvn compile` — confirm it builds.
2. `mvn test` — confirm all Cucumber scenarios pass, including the new ones in `fullscreen-windowed-toggle.feature` and `camera-behavior.feature`, and that nothing in `settings-screen.feature`/`settings-persistence.feature`/`settings-keybinds-page.feature`/`confirmation-popup-variant.feature` regressed (they share `UiComponentFrameworkSteps.java`).
3. Run `mvn clean test` a second time and confirm identical results (per the shared-step-file guidance above).
4. `mvn org.apache.maven.plugins:maven-pmd-plugin:3.28.0:check` (or just `mvn verify`, which runs it) — fix any violation, rerun until clean. Also confirms `ModuleDependencyTest` (ArchUnit, part of `mvn test`) still passes — it should, since `Camera` stays outside `com.swiftfaze.veil.ui` and nothing you're adding creates an engine→ui dependency.
5. Self-check every new/changed method against: ≤40 lines, cyclomatic complexity ≤8, ≤5 parameters, small single-purpose functions, no duplicated logic beyond what's already justified above. Everything in this plan was sized to fit comfortably under budget — if something you wrote doesn't, you likely deviated from the plan; reconcile back to it rather than suppressing a check.
6. **Visual verification** (`docs/ui-verification.md`'s technique — render the real component to an image via a throwaway diagnostic class in the scratchpad, inspect with the Read tool): specifically verify the Camera/GamePanel resize behavior — render `GamePanel` at two different sizes (e.g. 750x750 and 1200x900) and confirm the larger render shows visibly more of the map around the player (not stretched/scaled content, more *tiles*). Real JFrame decoration/undecoration/fullscreen bounds can't be usefully verified this way (no live window in this environment) — that's expected, it's covered by the Step 4.5 human playtest instead, not something you need to fake-verify.

## Step 7 — Documentation (same continuous handoff, not a separate agent)

Update these exact locations:

1. **Root `CLAUDE.md`**, "## Build & run" section — **delete** the bullet: `- Press **F5** while the game window is focused to hot-reset the scene (\`Main.resetGame\` disposes and rebuilds the \`JFrame\` from scratch — see \`Main.java\`).` (F5 no longer exists.)

2. **`docs/architecture.md`**, lines 34-38 (the `Camera` description): currently reads "`Camera` (`Camera.java`) is a plain offset holder — `centerOn(x, y)` sets its top-left offset to the target position minus half the viewport, with no smoothing between calls and no clamping to the map's bounds, so the viewport can extend past the map edge when the player is near one." — add a sentence noting the viewport is now resizable after construction (`resizeViewport`), tracks `GamePanel`'s live pixel size every paint so a resizable Windowed frame reveals more/less map on resize, and clamps to a 5-tile-per-dimension floor.

3. **`docs/architecture.md`**, line ~147 (in the "Keyboard input" paragraph): currently says Key Bindings is "the same mechanism `Main.java` already used for F5/reset" — reword to remove the F5/reset reference (it no longer exists); just describe Key Bindings' own mechanism without that comparison.

4. **`docs/screens.md`**, lines 114-115: currently ends "...this page's `Map<String,String>` is a persisted display-only label, not real key binding. F5 still resets the entire game (back to the title screen) — settings on disk are unaffected either way." — remove the F5 sentence entirely (keep the rest about the display-only label).

5. **`docs/screens.md`**, near line 48 (Fullscreen radio row description) or near line 117+ (Settings persistence section) — add a short note that the Fullscreen/Windowed row now also live-applies to the real game window (undecorated+maximized borderless for Fullscreen, resizable+decorated for Windowed, with the Camera viewport tracking the live panel size) — pick whichever location reads more naturally once you see the surrounding paragraph; this is your call, no need to ask.

If you judge any other doc passage is now stale because of this change, fix it too — but don't go looking for unrelated cleanup.

## What NOT to do

- Don't touch `GameWindow.java`, `FillLayout.java`, `WorldScene.java`, `GameConst.java`, `SettingsConfig.java`, `SettingsStore.java`, `SettingsRepository.java` — none of them need changes.
- Don't add a config-persistence mechanism for window mode across full app restarts beyond what already exists (`SettingsStore` already handles that; out of scope here per `specs/intent/fullscreen-windowed-toggle.md`).
- Don't try to simulate/test real `JFrame` fullscreen/decoration behavior in Cucumber — it's explicitly not testable headlessly in this repo (see "Verified facts" above) and covered by manual playtest instead.
- Don't touch the GitHub issue (#136) — the orchestrator already decided to leave it as-is and explain the F5 deviation in the PR description instead.

## When you're done

Report back with: confirmation `mvn verify` is clean (paste the final BUILD SUCCESS or any remaining failure verbatim), confirmation the double `mvn clean test` run was identical, confirmation of the visual-verification render-and-inspect result for the Camera/GamePanel resize behavior, and the full list of files you touched. Do NOT claim the feature is fully "done" — a human still needs to run `mvn compile exec:java` and manually playtest the real Fullscreen/Windowed toggle, window resizing, and Reset to Defaults behavior (CLAUDE.md's "Repo-specific Step 4.5"); say so explicitly in your report rather than omitting it. Do not commit, push, or open a PR — the orchestrator handles that after independently verifying your work.
