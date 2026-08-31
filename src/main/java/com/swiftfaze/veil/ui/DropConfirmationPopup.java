package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.ui.widget.CompactPopupWidget;
import com.swiftfaze.veil.ui.widget.RadioGroupWidget;
import com.swiftfaze.veil.ui.widget.WidgetTheme;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DropConfirmationPopup extends CompactPopupWidget {
    private static final String DEFAULT_ITEM_NAME = "this item";

    private final RadioGroupWidget<String> choice;
    private final JLabel questionLabel;

    public DropConfirmationPopup() {
        super("Drop Item");

        questionLabel = new JLabel(questionTextFor(DEFAULT_ITEM_NAME));
        questionLabel.setForeground(WidgetTheme.NORMAL_TEXT);
        questionLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        questionLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        addContent(questionLabel);

        addContent((JComponent) Box.createVerticalGlue());

        choice = new RadioGroupWidget<>(s -> s, true);
        choice.setFillWidth(true);
        choice.setOptions(List.of("No", "Yes"));
        choice.setOnConfirm(selected -> dismiss());
        choice.setAlignmentX(Component.CENTER_ALIGNMENT);
        choice.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        addContent(choice);
    }

    @Override
    public void open() {
        open(DEFAULT_ITEM_NAME);
    }

    /**
     * Opens the popup naming the specific item being dropped, e.g. "Drop Iron Sword" as the
     * title and "Are you sure you want to drop Iron Sword?" as the body — the item name isn't
     * known until an item is actually selected in the inventory, so it can't be baked into the
     * constructor like the rest of this dialog's static text.
     */
    public void open(String itemName) {
        setTitleText("Drop " + itemName);
        questionLabel.setText(questionTextFor(itemName));
        super.open();
        choice.resetSelection();
        choice.requestFocusInWindow();
    }

    public RadioGroupWidget<String> getChoiceWidget() {
        return choice;
    }

    private static String questionTextFor(String itemName) {
        return "Are you sure you want to drop " + itemName + "?";
    }
}
