package com.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ControllerPlayerSelection {

    @FXML private Label labelClientName;
    @FXML private Button buttonExit;
    @FXML private VBox yPane;
    private List<String> clients;
    private URL resource = this.getClass().getResource("/assets/listViewAvaliblePlayers.fxml");

    @FXML
    public void setClientName(String clientName) {
        labelClientName.setText(clientName);
    }

    @FXML
    private void exit() {
        Main.closeClient();
    }

    /***** Actualiza la lista de clientes disponibles para retar *****/
    public void updateListOfClients(JSONObject msgObj) {
        try {
            // Preparar nueva lista de clientes
            JSONArray jsonArray = msgObj.getJSONArray("list");
            List<String> newClients = new ArrayList<>();
            for (int i=0 ; i<jsonArray.length() ; i++) {
                newClients.add(jsonArray.getString(i));
            }

            // Actualizar vieja lista
            clients = newClients;

            // Mostrarlos por pantalla
            setAvaliblePlayerPane();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    /***** Añade a la vista los jugadores retables *****/
    private void setAvaliblePlayerPane() throws Exception {
        // Borrar contenido anterior
        yPane.getChildren().clear();

        for (String name : clients) {
            // Si miramos nuestro propio nombre, no nos añadimos
            if (name.equals(Main.clientName)) {
                continue;
            }

            // Carregar el template de 'listItem.fxml'
            FXMLLoader loader = new FXMLLoader(resource);
            Parent itemTemplate = loader.load();
            ControllerListAvaliblePlayers itemController = loader.getController();

            // Nombre y botón
            itemController.setPlayerName(name);
            itemController.getButtonChallenge().setOnAction(e -> {
                challengePlayer(name);
            });

            yPane.getChildren().add(itemTemplate);

        }
    }

    private void challengePlayer(String playerName) {
        System.out.println("prueba! has clickado a " + playerName);
        Main.sendChallenge(playerName);
    }

    public void processChallenge(JSONObject msgObj) {
        String challenger = msgObj.getString("challenger");
        System.out.println("Has recibido un reto de: " + challenger);
        // TODO Aquí debería haber lógica relacionada con dar la posibilidad al cliente de aceptar o rechazar el reto

        // Aceptar el reto
        Main.sendStartMatch(challenger);

        // Pasar a siguiente vista
    }
}
