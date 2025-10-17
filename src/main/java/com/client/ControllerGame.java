package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import com.project.CanvasTimer;
import com.server.GameMatch;

public class ControllerGame implements Initializable {
    
    private static double mouse_x, mouse_y;
    private static boolean dragging;
    private static String name = Main.clientName;
    private static int playerNum;
    @FXML
    private Canvas canvas;
    private static GraphicsContext gc;
    
    // public ControllerGame(Canvas canv, GraphicsContext grapCont) {
    //     setCanvas(canv);
    //     setGraphicsContext(grapCont);
    // }

    // public static void initializeResources(Canvas canv, GraphicsContext context) {
    //     setCanvas(canv);
    //     setGraphicsContext(context);
    // }
    // public static void setCanvas(Canvas canv) {
    //     canvas = canv;
    // }

    // public static void setGraphicsContext(GraphicsContext grapCont) {
    //     gc = grapCont;
    // }

    public static void setPlayerInfo(String nombre, int numero, boolean draggea) {
        name = Main.clientName;
        playerNum = numero;
        dragging = draggea;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = canvas.getGraphicsContext2D();
        // canvas_width = canvas.getWidth();
        // canvas_height = canvas.getHeight();
        // game = new Game();

        // canvas.widthProperty().bind(root.widthProperty());
        // canvas.heightProperty().bind(root.heightProperty());

        // canvas.widthProperty().addListener(evt -> updateWindowSize());
        // canvas.heightProperty().addListener(evt -> updateWindowSize());

        canvas.setOnMouseMoved(event -> {
            // GameMatch.setMousePos(event.getSceneX(), event.getSceneY());
            Main.sendPlayerMousePosInfo(Main.clientName, event.getSceneX(), event.getSceneY(), dragging);
            // update();

            //TODO Aquí hacer que mande un json con la info necesaria
        });

        canvas.setOnMouseDragged(event -> {
            // GameMatch.setMousePos(event.getSceneX(), event.getSceneY());
            // game.setPlayersDragging(true);
            Main.sendPlayerMousePosInfo(Main.clientName, event.getSceneX(), event.getSceneY(), dragging);
            dragging = true;
            // update();

            //TODO Aquí hacer que mande un json con la info necesaria
        });

        canvas.setOnMouseReleased(event -> {
            // game.setPlayersDragging(false);
            Main.sendPlayerMousePosInfo(Main.clientName, event.getSceneX(), event.getSceneY(), dragging);
            dragging = false;
            // game.checkReleases();

            //TODO Aquí hacer que mande un json con la info necesaria

        });
}
}

