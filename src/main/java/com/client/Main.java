package com.client;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.shared.ClientData;
import com.shared.GameObject;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    public static UtilsWS wsClient;

    public static String clientName = "";
    // TODO Sustituir por los objetos finales que utilizaremos
    // public static List<ClientData> clients;
    // public static List<GameObject> objects;

    public static ControllerConfig controllerConfig;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        final int windowWidth = 400;
        final int windowHeight = 300;

        UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");
        UtilsViews.addView(getClass(), "ViewConfig", "/assets/viewConfig.fxml");
        UtilsViews.addView(getClass(), "ViewPlayerSelection", "/assets/viewPlayerSelection.fxml");
        UtilsViews.addView(getClass(), "ViewWait", "/assets/viewWait.fxml");
        UtilsViews.addView(getClass(), "ViewCountdown", "/assets/viewCountdown.fxml");
        UtilsViews.addView(getClass(), "ViewGame", "/assets/viewGame.fxml");

        controllerConfig = (ControllerConfig) UtilsViews.getController("ViewConfig");
        // TODO Añadir el resto de controladores cuando estén listos

        Scene scene = new Scene(UtilsViews.parentContainer);
        
        stage.setScene(scene);
        stage.onCloseRequestProperty(); // Call close method when closing window
        stage.setTitle("JavaFX");
        stage.setMinWidth(windowWidth);
        stage.setMinHeight(windowHeight);
        stage.show();

        // Add icon only if not Mac
        if (!System.getProperty("os.name").contains("Mac")) {
            Image icon = new Image("file:/icons/icon.png");
            stage.getIcons().add(icon);
        }
    }

    public static void connectToServer() {

        controllerConfig.labelMessage.setTextFill(Color.BLACK);
        controllerConfig.labelMessage.setText("Connecting ...");
    
        pauseDuring(1500, () -> { // Give time to show connecting message ...

            String protocol = controllerConfig.textFieldProtocol.getText();
            String host = controllerConfig.textFieldHost.getText();
            String port = controllerConfig.textFieldPort.getText();
            wsClient = UtilsWS.getSharedInstance(protocol + "://" + host + ":" + port);
    
            wsClient.onMessage((response) -> { Platform.runLater(() -> { wsMessage(response); }); });
            wsClient.onError((response) -> { Platform.runLater(() -> { wsError(response); }); });
        });
    }

    private static void wsMessage(String response) {
        
        // System.out.println(response);
        
        JSONObject msgObj = new JSONObject(response);
        switch (msgObj.getString("type")) {
            case "serverData":
                clientName = msgObj.getString("clientName");

                JSONArray arrClients = msgObj.getJSONArray("clientsList");
                List<ClientData> newClients = new ArrayList<>();
                for (int i = 0; i < arrClients.length(); i++) {
                    JSONObject obj = arrClients.getJSONObject(i);
                    newClients.add(ClientData.fromJSON(obj));
                }
                clients = newClients;

                JSONArray arrObjects = msgObj.getJSONArray("objectsList");
                List<GameObject> newObjects = new ArrayList<>();
                for (int i = 0; i < arrObjects.length(); i++) {
                    JSONObject obj = arrObjects.getJSONObject(i);
                    newObjects.add(GameObject.fromJSON(obj));
                }
                objects = newObjects;

                if (clients.size() == 1) {

                    ctrlWait.txtPlayer0.setText(clients.get(0).name);

                } else if (clients.size() > 1) {

                    ctrlWait.txtPlayer0.setText(clients.get(0).name);
                    ctrlWait.txtPlayer1.setText(clients.get(1).name);
                    ctrlPlay.title.setText(clients.get(0).name + " vs " + clients.get(1).name);
                }
                
                if (UtilsViews.getActiveView().equals("ViewConfig")) {
                    UtilsViews.setViewAnimating("ViewWait");
                }

                break;
            
            case "countdown":
                int value = msgObj.getInt("value");
                String txt = String.valueOf(value);
                if (value == 0) {
                    UtilsViews.setViewAnimating("ViewPlay");
                    txt = "GO";
                }
                ctrlWait.txtTitle.setText(txt);
                break;
        }
    }

    private static void wsError(String response) {
        String connectionRefused = "Connection refused";
        if (response.indexOf(connectionRefused) != -1) {
            ctrlConfig.txtMessage.setTextFill(Color.RED);
            ctrlConfig.txtMessage.setText(connectionRefused);
            pauseDuring(1500, () -> {
                ctrlConfig.txtMessage.setText("");
            });
        }
    }
}
