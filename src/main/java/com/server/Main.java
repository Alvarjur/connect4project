package com.server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Servidor WebSocket amb routing simple de missatges, sense REPL.
 *
 * El servidor arrenca, registra un shutdown hook i es queda a l'espera
 * fins que el procés rep un senyal de terminació (SIGINT, SIGTERM).
 *
 * Missatges suportats:
 *  - bounce: eco del missatge a l’emissor
 *  - broadcast: envia a tots excepte l’emissor
 *  - private: envia a un destinatari pel seu nom
 *  - clients: llista de clients connectats
 *  - error / confirmation: missatges de control
 */
public class Main extends WebSocketServer {

    /** Port per defecte on escolta el servidor. */
    public static final int DEFAULT_PORT = 3000;

    /** Registro de clientes */
    public static ClientRegistry clients;

    /***** Registro de partidas *****/
    public static List<Game> games;

    // Claus JSON
    private static final String K_TYPE = "type";
    private static final String K_MESSAGE = "message";
    private static final String K_ORIGIN = "origin";
    private static final String K_DESTINATION = "destination";
    private static final String K_ID = "id";
    private static final String K_LIST = "list";
    private static final String K_CLIENT_NAME = "clientName";

    // Tipus de missatge
    private static final String T_REGISTER = "register";
    private static final String T_BOUNCE = "bounce";
    private static final String T_BROADCAST = "broadcast";
    private static final String T_PRIVATE = "private";
    private static final String T_CLIENTS = "clients";
    private static final String T_ERROR = "error";
    private static final String T_CONFIRMATION = "confirmation";
    private static final String T_CHALLENGE = "challenge";
    private static final String T_START_MATCH = "startMatch";

    /**
     * Crea un servidor WebSocket que escolta a l'adreça indicada.
     *
     * @param address adreça i port d'escolta del servidor
     */
    public Main(InetSocketAddress address) {
        super(address);
        clients = new ClientRegistry();
        games = new ArrayList<>();
    }

    /**
     * Crea un objecte JSON amb el camp type inicialitzat.
     *
     * @param type valor per a type
     * @return instància de JSONObject amb el tipus establert
     */
    private static JSONObject msg(String type) {
        return new JSONObject().put(K_TYPE, type);
    }

    /**
     * Afegeix clau-valor al JSONObject si el valor no és null.
     *
     * @param o objecte JSON destí
     * @param k clau
     * @param v valor (ignorat si és null)
     */
    private static void put(JSONObject o, String k, Object v) {
        if (v != null) o.put(k, v);
    }

    /**
     * Envia de forma segura un payload i, si el socket no està connectat,
     * el neteja del registre.
     *
     * @param to socket destinatari
     * @param payload cadena JSON a enviar
     */
    private void sendSafe(WebSocket to, String payload) {
        if (to == null) return;
        try {
            to.send(payload);
        } catch (WebsocketNotConnectedException e) {
            String name = clients.cleanupDisconnected(to);
            System.out.println("Client desconnectat durant send: " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Envia la llista actualitzada de clients a tots els clients connectats.
     */
    private void sendClientsListToAll() {
        JSONArray list = clients.currentNames();
        for (Map.Entry<WebSocket, String> e : clients.snapshot().entrySet()) {
            JSONObject rst = msg(T_CLIENTS);
            put(rst, K_ID, e.getValue());
            put(rst, K_LIST, list);
            sendSafe(e.getKey(), rst.toString());
        }
    }



    // ----------------- WebSocketServer overrides -----------------

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("[SERVIDOR] Nuevo cliente conectado");
    }

    /** Elimina el client del registre i notifica la llista actualitzada. */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("[SERVIDOR] Cliente desconectado -> " + clients.nameBySocket(conn));
        clients.remove(conn);
        sendClientsListToAll();
    }

    /***** Procesa el mensaje recibido y actúa según el tipo de mensaje. *****/
    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("[CLIENTE] Mensaje del cliente -> " + message);

        try {
            // Obtener el JSON
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");

            switch (type) {
                // Si es un registro de cliente
                case T_REGISTER:
                    // Registrar nuevo cliente
                    String clientName = json.getString(K_CLIENT_NAME);
                    clients.add(conn, clientName);
                    System.out.println("[SERVIDOR] Cliente registrado -> " + clientName);
                    conn.send("[SERVIDOR] Has sido registrado en el servidor con el nombre: " + clientName);

                    // Enviar nuevo JSON con los clientes actuales a todo el mundo
                    sendClientsListToAll();
                    break;
                
                // Si es un reto de un cliente a otro
                case T_CHALLENGE:
                    String challenger = json.getString("clientName");
                    String challengedPlayer = json.getString("challengedClientName");
                    System.out.println("AAAAAA");
                    System.out.println(String.format("[SERVIDOR] Cliente '%s' ha retado a '%s", challenger, challengedPlayer));
                    
                    // Preparar mensaje y enviar sólo a cliente retado
                    String payload = new JSONObject()
                        .put("type", "challenge")
                        .put("challenger", challenger)
                        .toString();
                    sendSafe(clients.socketByName(challengedPlayer), payload);
                    break;

                // Si un cliente acepta una partida
                case T_START_MATCH:
                    System.out.println("Entro en T_START_MATCH");
                    String player_1 = json.getString("player_1");
                    String player_2 = json.getString("player_2");
                    System.out.println(String.format("Se confirma que empieza la partida! Jugarán %s VS %s", player_1, player_2));
                    
                    // Crea la partida
                    Game game = new Game(player_1, player_2);
                    games.add(game);

                    // TODO Saca a ambos jugadores de la lista de disponibles

                    // Manda confirmación a los jugadores (para que pasen de vista)
                    JSONObject payloadConfirmedGame = new JSONObject();
                    payloadConfirmedGame.put("type", "confirmedGame");
                    System.out.println(payloadConfirmedGame);
                    sendSafe(clients.socketByName(player_1), payloadConfirmedGame.toString());
                    sendSafe(clients.socketByName(player_2), payloadConfirmedGame.toString());
                    
                    break;

                default:
                    conn.send("[SERVIDOR] Tipo de mensaje no controlado."
                        + "\n"
                        + "El mensaje recibido era: " + message
                    );
            }

        } catch (Exception e) {
            conn.send(new JSONObject()
                .put("type", "error")
                .put("message", "Invalid JSON")
                .toString()
            );
            e.printStackTrace();
        }
    }

    /** Log d'error global o de socket concret. */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        conn.send("error");
    }

    /** Arrencada: log i configuració del timeout de connexió perduda. */
    @Override
    public void onStart() {
        System.out.println("Servidor WebSocket engegat al port: " + getPort());
    }

    /**
     * Punt d'entrada: arrenca el servidor al port per defecte i espera senyals.
     *
     * @param args arguments de línia d'ordres (no utilitzats)
     */
    public static void main(String[] args) {
        Main server = new Main(new InetSocketAddress(DEFAULT_PORT));
        server.start();
        System.out.println("Servidor WebSocket en execució al port " + DEFAULT_PORT + ". Prem Ctrl+C per aturar-lo.");
    }
}
