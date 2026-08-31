package com.swiftfaze.veil.sandbox;

import com.swiftfaze.veil.entities.player.Stats;
import com.swiftfaze.veil.input.Keybindings;
import com.swiftfaze.veil.ui.TerminalPanel;
import com.swiftfaze.veil.ui.widget.ListWidget;
import com.swiftfaze.veil.ui.widget.WidgetTheme;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ClassSandboxPanel extends TerminalPanel {

    private final ClassSandboxModel model;
    private final List<String> names;
    private final ListWidget<String> listWidget;
    private final JLabel[] labels;
    private final JLabel statsLabel;

    public ClassSandboxPanel(ClassSandboxModel model) {
        this.model = model;
        this.names = model.classNames();
        this.listWidget = new ListWidget<>(s -> s);
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

        listWidget.setItems(names);
        listWidget.setOnConfirm(s -> refresh());

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

        actionMap.put(Keybindings.ACTION_MENU_UP, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listWidget.moveUp();
                refresh();
            }
        });

        actionMap.put(Keybindings.ACTION_MENU_DOWN, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listWidget.moveDown();
                refresh();
            }
        });
    }

    private void refresh() {
        int selectedIndex = listWidget.getSelectedIndex();
        for (int i = 0; i < labels.length; i++) {
            labels[i].setForeground(i == selectedIndex ? WidgetTheme.ACCENT : WidgetTheme.NORMAL_TEXT);
        }
        Stats stats = model.computedStats(names.get(selectedIndex));
        statsLabel.setText(String.format(
                "ATK %d  DEF %d  HP %d  MP %d",
                stats.getAttackPower(), stats.getDefense(), stats.getMaxHp(), stats.getMaxMana()
        ));
    }
}
