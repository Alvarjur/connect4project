package com.client;

import java.util.List;

import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ControllerPlayerSelection {

    @FXML private Label labelClientName;
    @FXML private Button buttonExit;
    private List<String> clients;

    @FXML
    public void setClientName(String clientName) {
        labelClientName.setText(clientName);
    }

    @FXML
    private void exit() {
        Main.closeClient();
    }

    public void updateListOfClients(JSONObject msgObj) {
        String jsonList = msgObj.getString("list");
        ObjectMapper mapper = new ObjectMapper();
        this.clients = mapper.readValue(jsonList, new TypeReference<List<String>>() {});
    }
}
