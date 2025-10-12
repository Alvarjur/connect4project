package com.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ControllerPlayerSelection {

    @FXML private Label labelClientName;
    @FXML private Button buttonExit;

    @FXML
    public void setClientName(String clientName) {
        labelClientName.setText(clientName);
    }

    @FXML
    private void exit() {
        Main.closeClient();
    }
}
