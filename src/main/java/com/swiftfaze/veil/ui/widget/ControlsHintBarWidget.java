package com.swiftfaze.veil.ui.widget;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Persistent bar docked at the bottom of the game window, rendering the
 * currently-valid key bindings pushed in by whichever screen currently has
 * focus. Each hint's key renders as a literal, reverse-video "keycap"
 * (background/foreground swapped from the theme's normal text colors) so it
 * reads like a terminal help bar (nano's status line), wrapping into a
 * compact grid instead of one ever-widening line.
 */
public class ControlsHintBarWidget extends JPanel {
    private static final Font HINT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);
    private static final int MAX_COLUMNS = 3;
    private static final int KEY_HORIZONTAL_PADDING = 4;
    private static final int CELL_GAP = 6;
    private static final int GRID_HGAP = 20;
    private static final int GRID_VGAP = 2;

    /**
     * A single "key does action" pair. The widget owns turning {@code key}
     * ("up", "escape", "shift+tab") into its displayed keycap label -
     * callers pass the raw key identifier, not a formatted string.
     */
    public record Hint(String key, String action) {
    }

    private List<Hint> hints = List.of();

    public ControlsHintBarWidget() {
        setBackground(WidgetTheme.BACKGROUND);
        setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, WidgetTheme.BORDER));
        setLayout(new GridLayout(1, 1));
    }

    public void setHints(List<Hint> newHints) {
        hints = new ArrayList<>(newHints);
        rebuild();
        revalidate();
        repaint();
    }

    public List<Hint> getHints() {
        return List.copyOf(hints);
    }

    private void rebuild() {
        removeAll();
        if (hints.isEmpty()) {
            setLayout(new GridLayout(1, 1));
            return;
        }
        int columns = Math.min(MAX_COLUMNS, hints.size());
        int rows = (int) Math.ceil(hints.size() / (double) columns);
        setLayout(new GridLayout(rows, columns, GRID_HGAP, GRID_VGAP));

        int keyWidth = widestKeyWidth();
        FontMetrics metrics = getFontMetrics(HINT_FONT);
        for (Hint cell : columnMajorOrder(rows, columns)) {
            add(buildCell(cell, keyWidth, metrics.getHeight()));
        }
    }

    private List<Hint> columnMajorOrder(int rows, int columns) {
        Hint[] cells = new Hint[rows * columns];
        for (int i = 0; i < hints.size(); i++) {
            int col = i / rows;
            int row = i % rows;
            cells[row * columns + col] = hints.get(i);
        }
        return Arrays.asList(cells);
    }

    private JPanel buildCell(Hint hint, int keyWidth, int keyHeight) {
        JPanel cell = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, CELL_GAP, 0));
        cell.setBackground(WidgetTheme.BACKGROUND);
        if (hint == null) {
            return cell;
        }

        JLabel key = new JLabel(keycapText(hint.key()));
        key.setOpaque(true);
        key.setBackground(WidgetTheme.NORMAL_TEXT);
        key.setForeground(WidgetTheme.BACKGROUND);
        key.setFont(HINT_FONT);
        key.setHorizontalAlignment(SwingConstants.LEFT);
        key.setPreferredSize(new Dimension(keyWidth, keyHeight));

        JLabel action = new JLabel(hint.action());
        action.setForeground(WidgetTheme.NORMAL_TEXT);
        action.setFont(HINT_FONT);

        cell.add(key);
        cell.add(action);
        return cell;
    }

    private int widestKeyWidth() {
        FontMetrics metrics = getFontMetrics(HINT_FONT);
        int max = 0;
        for (Hint hint : hints) {
            max = Math.max(max, metrics.stringWidth(keycapText(hint.key())));
        }
        return max + KEY_HORIZONTAL_PADDING;
    }

    // Literal keycap label, capitalized the way it'd be printed on a
    // keyboard/legend - no glyph substitution. "escape"/"enter" are
    // special-cased since they're not just a capitalized first letter.
    private static String keycapText(String key) {
        return switch (key) {
            case "escape" -> "Esc";
            case "enter" -> "Enter";
            default -> Character.toUpperCase(key.charAt(0)) + key.substring(1);
        };
    }
}
