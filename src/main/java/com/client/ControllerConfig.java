package com.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ControllerConfig {
    @FXML
    public TextField textFieldProtocol, textFieldHost, textFieldPort;
    @FXML
    public Button buttonConnect, buttonLocal, buttonProxmox;
    @FXML
    public Label labelMessage;

    @FXML
    private void connectToServer() {
        Main.connectToServer();
    }

    @FXML
    private void setConfigLocal() {
        textFieldProtocol.setText("ws");
        textFieldHost.setText("localhost");
        textFieldPort.setText("3000");
    }

    @FXML
    private void setConfigProxmox() {
        textFieldProtocol.setText("wss");
        textFieldHost.setText("user.ieti.site");
        textFieldPort.setText("443");
    }
}
