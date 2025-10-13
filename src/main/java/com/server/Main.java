package com.server;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
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
    public static ClientRegistry clientRegistry;

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

    /**
     * Crea un servidor WebSocket que escolta a l'adreça indicada.
     *
     * @param address adreça i port d'escolta del servidor
     */
    public Main(InetSocketAddress address) {
        super(address);
        clientRegistry = new ClientRegistry();
    }

    // ----------------- WebSocketServer overrides -----------------

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("[SERVIDOR] Nuevo cliente conectado");
        // conn.send("[SERVIDOR] Te has conectado al servidor");
    }

    /** Elimina el client del registre i notifica la llista actualitzada. */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("[SERVIDOR] Cliente desconectado -> " + clientRegistry.nameBySocket(conn));
        clientRegistry.remove(conn);
    }

    /***** Procesa el mensaje recibido y actúa según el tipo de mensaje. *****/
    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("[CLIENTE] Mensaje del cliente -> " + message);

        try {
            // Obtener el 
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");

            switch (type) {
                // Si es un registro de cliente
                case T_REGISTER:
                    String clientName = json.getString(K_CLIENT_NAME);
                    clientRegistry.add(conn, clientName);
                    System.out.println("[SERVIDOR] Nombre del nuevo cliente -> " + clientName);
                    // TODO Enviar nuevo JSON con los clientes actuales a todo el mundo
                    conn.send("[SERVIDOR] Has sido registrado en el servidor con el nombre: " + clientName);
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
