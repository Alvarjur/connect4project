package com.client;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
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
        try {
            JSONArray jsonArray = msgObj.getJSONArray("list");
            List<String> newClients = new ArrayList<>();

            for (int i=0 ; i<jsonArray.length() ; i++) {
                newClients.add(jsonArray.getString(i));
            }

            clients = newClients;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
