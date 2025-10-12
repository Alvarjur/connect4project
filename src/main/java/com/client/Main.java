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

    // Inicializar instancia de WebSocket antes de usarla
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
        stage.setTitle("Connect 4 - Client");
        stage.setMinWidth(windowWidth);
        stage.setMinHeight(windowHeight);
        stage.show();

        // Cerrar correctamente el cliente al cerrar ventana
        stage.setOnCloseRequest(event -> {
            closeClient();
        });

        // Add icon only if not Mac
        if (!System.getProperty("os.name").contains("Mac")) {
            Image icon = new Image("file:/icons/icon.png");
            stage.getIcons().add(icon);
        }
    }

    /***** Conexión al servidor (se llama en ViewConfig) *****/
    public static void connectToServer() {

        // Cambiar label en ViewConfig
        controllerConfig.labelMessage.setTextFill(Color.BLACK);
        controllerConfig.labelMessage.setText("Connecting ...");
    
        pauseDuring(1500, () -> { // Give time to show connecting message ...

            // Obtiene la URI introducida en ViewConfig
            String protocol = controllerConfig.textFieldProtocol.getText();
            String host = controllerConfig.textFieldHost.getText();
            String port = controllerConfig.textFieldPort.getText();

            // Generar instancia de wsClient con la URI registrada
            UtilsWS.resetSharedInstance(); // Asegura que si falla un intento de conexión (URI incorrecta), luego puede hacer otro intento correcto
            wsClient = UtilsWS.getSharedInstance(protocol + "://" + host + ":" + port);
    
            // Realiza una acción dependiendo del mensaje devuelto por Server
            wsClient.onMessage((response) -> { Platform.runLater(() -> { wsMessage(response); }); });
            wsClient.onError((response) -> { Platform.runLater(() -> { wsError(response); }); });

            wsClient.connect(); // Hay que hacer la conexión después de definir los handler para mensaje y error, sino utiliza el error definido en UtilsWS
        });
    }

    /***** Detiene el programa durante X milisegundos, y luego realiza un Runnable *****/
    public static void pauseDuring(long milliseconds, Runnable action) {
        PauseTransition pause = new PauseTransition(Duration.millis(milliseconds));
        pause.setOnFinished(event -> Platform.runLater(action));
        pause.play();
    }

    /***** Realiza una acción cuando recibe un mensaje del servidor *****/
    private static void wsMessage(String response) {
        Platform.runLater(()->{ 
            // Cambio de ViewConfig a ViewPlayerSelection
            if (UtilsViews.getActiveView() == "ViewConfig") { // TODO Hacer algo menos chapuza
                UtilsViews.setViewAnimating("ViewPlayerSelection");
                clientName = controllerConfig.getUsername();
                controllerPlayerSelection.setClientName(clientName);
            }

            // JSONObject msgObj = new JSONObject(response);
            // controllerPlayerSelection.receiveMessage(msgObj);
        });
    }

    /***** Realiza una acción cuando hay un error de red o protocolo (p.ej. desconexión) *****/
    private static void wsError(String response) {

        System.out.println("Estoy en wsError");
        String connectionRefused = "S’ha refusat la connexió";
        if (response.indexOf(connectionRefused) != -1) {
            controllerConfig.labelMessage.setTextFill(Color.RED);
            controllerConfig.labelMessage.setText(connectionRefused);
            pauseDuring(1500, () -> {
                controllerConfig.labelMessage.setText("");
            });
        }
    }

    /***** Cierra el cliente *****/
    public static void closeClient() {
        System.out.println("Cerrando aplicación...");
    
    // Cierra el WebSocket si está abierto
    if (wsClient != null) {
        wsClient.forceExit();
    }

    Platform.exit();
    System.exit(0);
    }
}
