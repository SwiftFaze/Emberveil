package com.swiftfaze.veil.sandbox;

import com.swiftfaze.veil.entities.player.Stats;
import com.swiftfaze.veil.input.Keybindings;
import com.swiftfaze.veil.ui.SelectableMenu;
import com.swiftfaze.veil.ui.TerminalPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ClassSandboxPanel extends TerminalPanel {

    private static final Color SELECTED_COLOR = Color.decode("#eeb392");

    private final ClassSandboxModel model;
    private final List<String> names;
    private final SelectableMenu menu;
    private final JLabel[] labels;
    private final JLabel statsLabel;

    public ClassSandboxPanel(ClassSandboxModel model) {
        this.model = model;
        this.names = model.classNames();
        this.menu = new SelectableMenu(names.size());
        this.labels = new JLabel[names.size()];

        setFocusable(true);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(makeLabel("Class Sandbox — Up/Down to select"));
        for (int i = 0; i < names.size(); i++) {
            labels[i] = makeLabel(names.get(i));
            add(labels[i]);
        }
        statsLabel = makeLabel("");
        add(statsLabel);

        bindKeys();
        refresh();
    }

    public JLabel getClassLabel(int index) {
        return labels[index];
    }

    public JLabel getStatsLabel() {
        return statsLabel;
    }

    private void bindKeys() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();

        inputMap.put(Keybindings.MENU_UP, Keybindings.ACTION_MENU_UP);
        inputMap.put(Keybindings.MENU_DOWN, Keybindings.ACTION_MENU_DOWN);

        actionMap.put(Keybindings.ACTION_MENU_UP, new MoveSelectionAction(menu::moveUp));
        actionMap.put(Keybindings.ACTION_MENU_DOWN, new MoveSelectionAction(menu::moveDown));
    }

    private void refresh() {
        for (int i = 0; i < labels.length; i++) {
            labels[i].setForeground(i == menu.selected() ? SELECTED_COLOR : Color.WHITE);
        }
        Stats stats = model.computedStats(names.get(menu.selected()));
        statsLabel.setText(String.format(
                "ATK %d  DEF %d  HP %d  MP %d",
                stats.getAttackPower(), stats.getDefense(), stats.getMaxHp(), stats.getMaxMana()
        ));
    }

    private class MoveSelectionAction extends AbstractAction {
        private final Runnable move;

        MoveSelectionAction(Runnable move) {
            this.move = move;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            move.run();
            refresh();
        }
    }
}
