package com.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ControllerWait {
    @FXML private Label labelChallengedName;

    @FXML
    public void updateChallengedName(String challengedName) {
        labelChallengedName.setText(challengedName);
    }
}
