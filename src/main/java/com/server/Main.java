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

import com.server.GameMatch.Game;

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
    public static List<GameMatch> gameMatches;
    public int game_id = 0;
    

    /* Countdowns que maneja el servidor (1 por partida) */
    private List<Countdown> countdowns;

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
    private static final String T_REFUSED_MATCH = "refusedMatch";
    private static final String T_PLAYER_MOUSE_INFO = "playerMouseInfo";
    private static final String T_KOTLIN_ADD_CHIP = "kotlinAddChip";
    private static final String T_START_COUNTDOWN = "startCountdown";
    private static final String T_REMAINING_COUNTDOWN = "remainingCountdown";

    /**
     * Crea un servidor WebSocket que escolta a l'adreça indicada.
     *
     * @param address adreça i port d'escolta del servidor
     */
    public Main(InetSocketAddress address) {
        super(address);
        clients = new ClientRegistry();
        gameMatches = new ArrayList<>();
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
    private static void sendSafe(WebSocket to, String payload) {
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

    public static void sendUpdateOrder(int id) {
        JSONObject objeto = new JSONObject();
        objeto.put("type", "drawOrder");
        // System.out.println(objeto);
        GameMatch gameMatch = gameMatches.get(id);
        Game game = gameMatch.game;
        String player1 = game.player1.name;
        String player2 = game.player2.name;

        // Posicion de los jugadores
        objeto.put("pos_x_1", game.player1.x);
        objeto.put("pos_y_1", game.player1.y);
        objeto.put("pos_x_2", game.player2.x);
        objeto.put("pos_y_2", game.player2.y);

        // Board serializada
        int[][] grid = game.board.grid;
        JSONArray chipGridPositions = new JSONArray();
        // grid[2][3] = 1;
        // grid[1][1] = 2;
        for(int i = 0; i < grid.length; i++) {
            for(int j = 0; j < grid[0].length; j++) {
                if(grid[i][j] != 0) {
                    chipGridPositions.put(i + " " + j + " " + grid[i][j]);
                }
            }
        }
        objeto.put("grid", chipGridPositions);
        objeto.put("board_pos_x", game.board.x);
        objeto.put("board_pos_y", game.board.y);

        // Draggable chips
        objeto.put("red_chip_dragg_x", game.draggableChips_red_x);
        objeto.put("red_chip_dragg_y", game.draggableChips_red_y);
        objeto.put("yellow_chip_dragg_x", game.draggableChips_yellow_x);
        objeto.put("yellow_chip_dragg_y", game.draggableChips_yellow_y);

        // Current chip draggeada
        if(game.currentChip != null) {
            objeto.put("current_chip", game.currentChip.x + " " + game.currentChip.y + " " + game.currentChip.player);
            // Possible moves 
            objeto.put("possible_moves", game.possibleMoves);
        } else {
            objeto.put("current_chip", "none");
            objeto.put("possible_moves", "none");
        }

        // Falling chip animation status
        if(gameMatch.animChip != null) {
            objeto.put("animChip", gameMatch.animating + " " + gameMatch.animX + " " + gameMatch.animY + " " + gameMatch.animChip.player);
        } else {
            objeto.put("animChip", "none");
        }
        
        
        // Ganador y línea ganadora
        if(game.winner != null) {
            objeto.put("winner", game.winner.name);
            objeto.put("winner_line", gameMatch.winner_start_x + " " + gameMatch.winner_end_x + " " +
                                            gameMatch.winner_start_y + " " + gameMatch.winner_end_y);
        } else {
            objeto.put("winner", "none");
            objeto.put("winner_line", "none");
        }

        objeto.put("turn", game.currentPlayer);
        


        sendSafe(clients.socketByName(player1), objeto.toString());
        sendSafe(clients.socketByName(player2), objeto.toString());
        // JSONArray clientNames = clients.currentNames();

        // for (int i = 0; i < clientNames.length(); i++) {
        //     String name = clientNames.getString(i);

        //     sendSafe(clients.socketByName(name), objeto.toString());
        // }


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
                    System.out.println("Entro en case T_START_MATCH");
                    String player_1 = json.getString("player_1");
                    String player_2 = json.getString("player_2");
                    System.out.println(String.format("Se confirma que empieza la partida! Jugarán %s VS %s", player_1, player_2));

                    // TODO Saca a ambos jugadores de la lista de disponibles
                    
                                       
                    // TODO Hacer que players vayan a vista Countdown
                    int startSeconds = 3;

                    JSONObject payloadStartCountdown = new JSONObject();
                    payloadStartCountdown.put("type", T_START_COUNTDOWN);
                    payloadStartCountdown.put("player_1", player_1);
                    payloadStartCountdown.put("player_2", player_2);
                    payloadStartCountdown.put("value", startSeconds);
                    sendSafe(clients.socketByName(player_1), payloadStartCountdown.toString());
                    sendSafe(clients.socketByName(player_2), payloadStartCountdown.toString());

                    // Pongo en marcha el Countdown
                    Countdown countdown = new Countdown(startSeconds);
                    System.out.println("He creado el objeto Countdown");
                    
                    countdown.setOnTick((remaining) -> {
                        System.out.println("Entro en countdown.setOnTick con remaining=" + remaining);
                        JSONObject msg = new JSONObject()
                            .put("type", T_REMAINING_COUNTDOWN)
                            .put("value", remaining);
                        sendSafe(clients.socketByName(player_1), msg.toString());
                        sendSafe(clients.socketByName(player_2), msg.toString());
                    });

                    countdown.setOnFinished(() -> {
                        // Pongo en marcha la partida
                        GameMatch gameMatch = new GameMatch(game_id, player_1, player_2);
                        System.out.println("He creado el GameMatch con game_id=" + game_id);
                        game_id += 1;
                        gameMatches.add(gameMatch);

                        // Avisar a los clientes de que empieza la partida
                        JSONObject payloadConfirmedGame = new JSONObject();
                        payloadConfirmedGame.put("type", "startGame");
                        payloadConfirmedGame.put("player_1", player_1);
                        payloadConfirmedGame.put("player_2", player_2);
                        sendSafe(clients.socketByName(player_1), payloadConfirmedGame.toString());
                        sendSafe(clients.socketByName(player_2), payloadConfirmedGame.toString());
                    });

                    countdown.startCountdown();

                    break;

                // Si un cliente rechaza una partida
                case T_REFUSED_MATCH:
                    System.out.println("Entro en T_REFUSED_MATCH");
                    String refusedMatchChallenger = json.getString("challenger");

                    // Enviar payload
                    JSONObject payloadRefusedGame = new JSONObject();
                    payloadRefusedGame.put("type", T_REFUSED_MATCH);
                    sendSafe(clients.socketByName(refusedMatchChallenger), payloadRefusedGame.toString());
                    break;
                
                case T_PLAYER_MOUSE_INFO:
                    System.out.println("Mensaje recibido, movimiento en el ratón");
                    String player1 = json.getString("player");
                    double pos_x = json.getDouble("pos_x");
                    double pos_y = json.getDouble("pos_y");
                    boolean dragging = json.getBoolean("dragging");
                        for(GameMatch gm : gameMatches) {
                            if (gm.game.player1.name.equals(player1) || gm.game.player2.name.equals(player1)) {
                                gm.updatePlayerMousePos(player1, pos_x, pos_y);
                                gm.updatePlayerMouseState(player1, dragging);
                            }
                        }
                        
                     
                    
                    
                    break;
                
                case T_KOTLIN_ADD_CHIP:
                    
                    int col = json.getInt("message");
                    String videojugador = json.getString("clientName");
                    System.out.println("\n\n\n\n\n\n\n\n\n\n\n\nMensaje recibido de kotlin, añadir chip\n\n\n\n\n\n\n\n\n\n\n"+col);
                    for(GameMatch gm : gameMatches) {
                            if (gm.game.player1.name.equals(videojugador) ) {
                                gm.game.board.addChip(1, col);
                            } else if (gm.game.player2.name.equals(videojugador)) {
                                gm.game.board.addChip(2, col);
                            }
                        }

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

    public static void log(String message) {
        System.out.println(message);
    }
}
