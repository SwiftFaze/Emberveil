package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.ui.widget.PopupWidget;
import com.swiftfaze.veil.ui.widget.RadioGroupWidget;
import com.swiftfaze.veil.ui.widget.WidgetTheme;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DropConfirmationPopup extends PopupWidget {
    private final RadioGroupWidget<String> choice;

    public DropConfirmationPopup() {
        JLabel questionLabel = new JLabel("Drop item?");
        questionLabel.setForeground(WidgetTheme.NORMAL_TEXT);
        questionLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
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
