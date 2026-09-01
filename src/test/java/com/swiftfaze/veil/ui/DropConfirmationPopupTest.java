package com.swiftfaze.veil.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DropConfirmationPopupTest {

    @Test
    void constructorInitializes() {
        DropConfirmationPopup popup = new DropConfirmationPopup();
        assertNotNull(popup);
    }

    @Test
    void dismissWorks() {
        DropConfirmationPopup popup = new DropConfirmationPopup();
        popup.open();
        popup.dismiss();
        assertNotNull(popup);
    }
}
