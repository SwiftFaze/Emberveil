package com.swiftfaze.veil.ui;

import com.swiftfaze.veil.ui.widget.CompactPopupWidget;
import com.swiftfaze.veil.ui.widget.RadioGroupWidget;
import com.swiftfaze.veil.ui.widget.WidgetTheme;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A Yes/No confirmation dialog for the "Reset to Defaults" settings action.
 * Extends CompactPopupWidget to provide a smaller, centered presentation with
 * a title bar. Uses RadioGroupWidget for the choice, with "No" as the default
 * (safe-default convention matching DropConfirmationPopup).
 */
public class ResetConfirmationPopup extends CompactPopupWidget {
    private final RadioGroupWidget<String> choice;
    private final String questionText;

    public ResetConfirmationPopup() {
        super("Confirm Reset");
        this.questionText = "Reset all settings to their defaults?";

        JLabel questionLabel = new JLabel(questionText);
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

    public String getTitle() {
        return "Confirm Reset";
    }

    public String getQuestionText() {
        return questionText;
    }
}
