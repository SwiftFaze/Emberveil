package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.ui.widget.CompactPopupWidget;
import com.swiftfaze.veil.ui.widget.RadioGroupWidget;
import com.swiftfaze.veil.ui.widget.WidgetTheme;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DropConfirmationPopup extends CompactPopupWidget {
    private final RadioGroupWidget<String> choice;

    public DropConfirmationPopup() {
        super("Drop Item");

        JLabel questionLabel = new JLabel("Drop item?");
        questionLabel.setForeground(WidgetTheme.NORMAL_TEXT);
        questionLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        questionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        questionLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        addContent(questionLabel);

        choice = new RadioGroupWidget<>(s -> s, true);
        choice.setOptions(List.of("No", "Yes"));
        choice.setOnConfirm(selected -> dismiss());
        addContent(choice);
    }

    @Override
    public void open() {
        super.open();
        choice.requestFocusInWindow();
    }

    public RadioGroupWidget<String> getChoiceWidget() {
        return choice;
    }
}
