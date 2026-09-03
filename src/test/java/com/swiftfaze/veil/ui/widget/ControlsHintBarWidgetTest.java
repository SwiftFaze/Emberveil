package com.swiftfaze.veil.ui.widget;

import com.swiftfaze.veil.ui.widget.ControlsHintBarWidget.Hint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControlsHintBarWidgetTest {

    @Test
    void startsWithNoHints() {
        ControlsHintBarWidget bar = new ControlsHintBarWidget();
        assertTrue(bar.getHints().isEmpty());
    }

    @Test
    void setHintsStoresExactContent() {
        ControlsHintBarWidget bar = new ControlsHintBarWidget();
        bar.setHints(List.of(new Hint("up", "Navigate"), new Hint("down", "Navigate")));
        assertEquals(List.of(new Hint("up", "Navigate"), new Hint("down", "Navigate")), bar.getHints());
    }

    @Test
    void setHintsReplacesPreviousContent() {
        ControlsHintBarWidget bar = new ControlsHintBarWidget();
        bar.setHints(List.of(new Hint("up", "Navigate")));
        bar.setHints(List.of(new Hint("enter", "Select")));
        assertEquals(List.of(new Hint("enter", "Select")), bar.getHints());
    }
}
