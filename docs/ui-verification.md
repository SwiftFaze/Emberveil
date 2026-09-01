# Verifying a Swing UI change visually

Swing layout/text bugs routinely look fixed by reasoning about the code and
still be wrong on screen — `getPreferredSize()`, `BoxLayout` sizing, and HTML
text wrapping all have non-obvious runtime behavior that reading the source
doesn't reveal. Compiling and passing `mvn test` only proves the code runs,
not that it renders correctly: none of this project's tests assert on pixel
layout or actual rendered text.

This happened concretely while building the compact confirmation popup
(`CompactPopupWidget`, `ResetConfirmationPopup`, `DropConfirmationPopup`):
two separate "fixes" for wrapping long body text were shipped, tests green
both times, and both were still visibly broken — one because this JDK's
Swing HTML renderer silently ignores a CSS pixel `width` when computing
preferred size (confirmed by direct measurement: the "wrapped" size came
back *wider* than the unwrapped text), the other because
`JComponent.getPreferredSize()` returns whatever was last passed to
`setPreferredSize()` verbatim, without recomputing anything, once that's
been called — so a second call with longer text silently kept the first
call's shorter size and clipped a line. Both were only caught by rendering
the actual component and looking at it.

## When to do this

Any time an implementation or fix step (Step 4 in `.claude/workflow.md`, or
a UI-focused change outside that pipeline) touches Swing rendering, layout,
sizing, or text content — not just when a human explicitly asks for a
screenshot. Do it before reporting the change as done, the same way you'd
run the test suite before reporting a logic change as done. This is agent
work, done during/after implementation — it does not replace or overlap
with `CLAUDE.md`'s Step 4.5 manual playtest, which is a human verifying real
interactive *feel* (movement, menu navigation, timing) that no static
render can capture.

## How to do it

There is no screenshot tool for a live desktop window. Instead, render the
real component to an image and inspect that with the `Read` tool, the same
way you'd inspect a user-supplied screenshot.

1. Compile the project first (`mvn compile`), so the class you want to
   render exists in `target/classes`.
2. Write a small throwaway Java class in the scratchpad directory (never in
   `src/` — this is a diagnostic, not project code) that:
   - Builds the real component on the Swing event thread
     (`SwingUtilities.invokeAndWait`), calling it exactly the way
     production code does (e.g. `popup.open("Iron Sword")`, not a
     hand-built stand-in).
   - Adds it to an actual `JFrame` and calls `frame.setVisible(true)` —
     **the frame must really be shown.** Painting an unrealized/never-shown
     frame via `component.paint()` or `component.printAll()` onto a bare
     `BufferedImage` produces misleading results (confirmed this session:
     it silently drops per-line styling on wrapped text) because Swing's
     text/layout pipeline behaves differently without a real native peer.
   - Waits briefly after showing it (`Thread.sleep(500)`–`800`ms) for the
     OS to actually paint before capturing.
   - Captures the frame's real screen bounds with `java.awt.Robot`
     (`robot.createScreenCapture(frame.getBounds())`) and writes that to a
     PNG in the scratchpad with `ImageIO.write`.
   - Disposes the frame immediately after capturing.
3. Compile and run it against the project's classes:
   ```
   javac -cp <project>/target/classes -d . YourDiag.java
   java -cp ".;<project>/target/classes" YourDiag out.png [args...]
   ```
4. Read the PNG with the `Read` tool and actually look at it — don't just
   check that the diagnostic ran without throwing.
5. If something looks wrong and the cause isn't obvious, add a recursive
   component-tree dump (walk `Container.getComponents()`, print each
   child's class, `getBounds()`, `getPreferredSize()`, `getMaximumSize()`,
   and — for a text component — its actual text) before guessing further.
   This is what actually found the stale-`getPreferredSize()` bug above;
   reasoning about the layout math alone had already been wrong twice.
6. Delete or leave the throwaway diagnostic in the scratchpad (it's
   session-local and never committed) once the fix is confirmed.

Re-run this after every attempted fix, not just once per bug — a plausible
explanation for what's wrong is not the same as having verified the new
code actually renders correctly.

## Relaying "done" up the chain

Producing the PNG and looking at it satisfies the *implementing* agent's
own obligation for this step (see "When to do this" above). It does not
by itself let anyone downstream treat "visual verification passed" as a
fact without checking — an orchestrating session handing the result to
another agent or to a human, or a human being told the step is complete.
A subagent's self-report ("I rendered it and it looked correct") is a
claim, not evidence. Before relaying this step as done, open the actual
PNG yourself (regenerate it if it was already cleaned up) and look — the
same "trust but verify" rule that applies to any other subagent output,
not a special case for this step.
