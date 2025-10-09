package com.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;

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
    private static final String K_TYPE = "type";
    private static final String K_VALUE = "value";
    private static final String K_CLIENT_NAME = "clientName";
    private static final String K_CLIENTS_LIST = "clientsList";             
    private static final String K_OBJECTS_LIST = "objectsList"; 

    // TODO: Tipos de mensajes
    private static final String T_CLIENT_MOUSE_MOVING = "clientMouseMoving";  // client -> server
    private static final String T_CLIENT_OBJECT_MOVING = "clientObjectMoving";// client -> server
    private static final String T_SERVER_DATA = "serverData";                 // server -> clients
    private static final String T_COUNTDOWN = "countdown";                    // server -> clients

    // Registro de clientes
    private final ClientRegistry clients;

    // Mapa de estado para clientes (source of truth del servidor). Clave = nombre/id
    private final Map<String, ClientData> clientsData = new HashMap<>();

    // Mapa de objetos seleccionables
    private final Map<String, GameObject> gameObjects = new HashMap<>();

    private volatile boolean countdownRunning = false;

    // Frecuencia de envío del estado de juego
    private static final int SEND_FPS = 30;
    private final ScheduledExecutorService ticker;

    public Main(InetSocketAddress adress) {
        super(adress);
        this.clients = new ClientRegistry(playersNames);
        // initializeGameObjects();

        ThreadFactory tf = r -> {
            Thread t = new Thread(r, "ServerTicket");
            t.setDaemon(true);
            return t;
        };
        this.ticker = Executors.newSingleThreadScheduledExecutor(tf);
    }

    public static void main(String[] args) {
        Main server = new Main(new InetSocketAddress(DEFAULT_PORT));
        server.start();
        registerShutdownHook(server);

        System.out.println("Server running on port " + DEFAULT_PORT + ". Press Ctrl+C to stop it.");
        awaitForever();
    }

    // private void initializegameObjects() {
    //     String objId = "O0";
    //     GameObject obj0 = new GameObject(objId, 300, 50, 4, 1);
    //     gameObjects.put(objId, obj0);

    //     objId = "O1";
    //     GameObject obj1 = new GameObject(objId, 300, 100, 1, 3);
    //     gameObjects.put(objId, obj1);
    // }

    // private synchronized String getColorForName(String name) {
    //     int idx = PLAYER_NAMES.indexOf(name);
    //     if (idx < 0) idx = 0; // fallback si el nom no està a la llista
    //     return PLAYER_COLORS.get(idx % PLAYER_COLORS.size());
    // }

    private void sendCountdown() {
        synchronized (this) {
            if (countdownRunning) return;
            if (clients.snapshot().size() != REQUIRED_CLIENTS) return;
            countdownRunning = true;
        }

        new Thread(() -> {
            try {
                for (int i = 5; i >= 0; i--) {
                    // Si durant el compte enrere ja no hi ha els clients requerits, cancel·la
                    if (clients.snapshot().size() < REQUIRED_CLIENTS) {
                        break;
                    }

                    sendCountdownToAll(i);
                    if (i > 0) Thread.sleep(750); // ritme del compte enrere
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } finally {
                countdownRunning = false;
            }
        }, "CountdownThread").start();
    }

    // ----------------- Helpers JSON -----------------

    private static JSONObject msg(String type) {
        return new JSONObject().put(K_TYPE, type);
    }

    private void sendSafe(WebSocket to, String payload) {
        if (to == null) return;
        try {
            to.send(payload);
        } catch (WebsocketNotConnectedException e) {
            String name = clients.cleanupDisconnected(to);
            clientsData.remove(name);
            System.out.println("Client desconnectat durant send: " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastExcept(WebSocket sender, String payload) {
        for (Map.Entry<WebSocket, String> e : clients.snapshot().entrySet()) {
            WebSocket conn = e.getKey();
            if (!Objects.equals(conn, sender)) sendSafe(conn, payload);
        }
    }

    // private void broadcastStatus() {

    //     JSONArray arrClients = new JSONArray();
    //     for (ClientData c : clientsData.values()) {
    //         arrClients.put(c.toJSON());
    //     }

    //     JSONArray arrObjects = new JSONArray();
    //     for (GameObject obj : gameObjects.values()) {
    //         arrObjects.put(obj.toJSON());
    //     }

    //     JSONObject rst = msg(T_SERVER_DATA)
    //                     .put(K_CLIENTS_LIST, arrClients)
    //                     .put(K_OBJECTS_LIST, arrObjects);

    //     for (Map.Entry<WebSocket, String> e : clients.snapshot().entrySet()) {
    //         WebSocket conn = e.getKey();
    //         String name = clients.nameBySocket(conn);
    //         rst.put(K_CLIENT_NAME, name);
    //         sendSafe(conn, rst.toString());
    //     }
    // }

    private void sendCountdownToAll(int n) {
        JSONObject rst = msg(T_COUNTDOWN).put(K_VALUE, n);
        broadcastExcept(null, rst.toString());
    }

    // ----------------- WebSocketServer overrides -----------------

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

    // ----------------- Lifecycle util -----------------

    /** Registra un shutdown hook per aturar netament el servidor en finalitzar el procés. */
    private static void registerShutdownHook(Main server) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Aturant servidor (shutdown hook)...");
            try {
                server.stopTicker();      // <- atura el bucle periòdic
                server.stop(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            System.out.println("Servidor aturat.");
        }));
    }

    /** Bloqueja el fil principal indefinidament fins que sigui interromput. */
    private static void awaitForever() {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }


    // ----------------- Ticker util -----------------

    private void startTicker() {
        long periodMs = Math.max(1, 1000 / SEND_FPS);
        ticker.scheduleAtFixedRate(() -> {
            try {
                // Opcional: si no hi ha clients, evita enviar
                if (!clients.snapshot().isEmpty()) {
                    // broadcastStatus();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, periodMs, TimeUnit.MILLISECONDS);
    }

    private void stopTicker() {
        try {
            ticker.shutdownNow();
            ticker.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

}
