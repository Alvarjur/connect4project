package com.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.shared.*;

public class Main extends WebSocketServer {

    // Puerto por defecto
    public static final int DEFAULT_PORT = 3000;

    // Nombres de jugadores
    private static List<String> playersNames = new ArrayList<String>();

    // Colores disponibles para los jugadores
    private static final List<String> PLAYER_COLORS = Arrays.asList(
        "RED", "YELLOW"
    );

    // Número de clientes necesarios para iniciar la partida
    private static final int REQUIRED_CLIENTS = 2;

    // TODO: Claves JSON

    // TODO: Tipos de mensajes
    // private static final String T_CLIENT_MOUSE_MOVING = "clientMouseMoving";  // client -> server
    // private static final String T_CLIENT_OBJECT_MOVING = "clientObjectMoving";// client -> server
    // private static final String T_SERVER_DATA = "serverData";                 // server -> clients
    // private static final String T_COUNTDOWN = "countdown";                    // server -> clients

    // Registro de clientes
    private final ClientRegistry clients;

    // Mapa de estado para clientes (source of truth del servidor). Clave = nombre/id
    private final Map<String, ClientData> clientsData = new HashMap<>();

    // Mapa de objetos seleccionables
    private final Map<String, GameObject> gameObjects = new HashMap<>();

    private volatile boolean countdownRunning = false;

    // Frecuencia de envío del estado de juego
    private static final int SEND_FPS = 60;
    private final ScheduledExecutorService ticker;

    public Main(InetSocketAddress adress) {
        super(adress);
        this.clients = new ClientRegistry(playerNames);
        initializeGameObjects();

        ThreadFactory tf = r -> {
            Thread t = new Thread(r, "ServerTicket")
        }
    }

    public static void main(String[] args) {

    }

    @Override
    public void onClose(WebSocket arg0, int arg1, String arg2, boolean arg3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onClose'");
    }

    @Override
    public void onError(WebSocket arg0, Exception arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onError'");
    }

    @Override
    public void onMessage(WebSocket arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onMessage'");
    }

    @Override
    public void onOpen(WebSocket arg0, ClientHandshake arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onOpen'");
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onStart'");
    }

}
