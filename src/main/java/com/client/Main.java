package com.client;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    public static UtilsWS wsClient;

    public static String clientName = "";
    // TODO Sustituir por los objetos finales que utilizaremos
    // public static List<ClientData> clients;
    // public static List<GameObject> objects;

    public static ControllerConfig controllerConfig;
    public static ControllerPlayerSelection controllerPlayerSelection;

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
        // UtilsViews.addView(getClass(), "ViewWait", "/assets/viewWait.fxml");
        // UtilsViews.addView(getClass(), "ViewCountdown", "/assets/viewCountdown.fxml");
        // UtilsViews.addView(getClass(), "ViewGame", "/assets/viewGame.fxml");

        controllerConfig = (ControllerConfig) UtilsViews.getController("ViewConfig");
        controllerPlayerSelection = (ControllerPlayerSelection) UtilsViews.getController("ViewPlayerSelection");
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

    public static void pauseDuring(long milliseconds, Runnable action) {
        PauseTransition pause = new PauseTransition(Duration.millis(milliseconds));
        pause.setOnFinished(event -> Platform.runLater(action));
        pause.play();
    }

    private static void wsMessage(String response) {
        Platform.runLater(()->{ 
            // Fer aquí els canvis a la interficie
            if (UtilsViews.getActiveView() != "ViewPlayerSelectio") {
                UtilsViews.setViewAnimating("ViewPlayerSelectio");
            }
            // JSONObject msgObj = new JSONObject(response);
            // controllerPlayerSelection.receiveMessage(msgObj);
        });
    }

    private static void wsError(String response) {

        String connectionRefused = "Connection refused";
        if (response.indexOf(connectionRefused) != -1) {
            controllerConfig.labelMessage.setTextFill(Color.RED);
            controllerConfig.labelMessage.setText(connectionRefused);
            pauseDuring(1500, () -> {
                controllerConfig.labelMessage.setText("");
            });
        }
    }
}
