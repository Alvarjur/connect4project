package com.server;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.java_websocket.WebSocket;
import org.json.JSONArray;

final class ClientRegistry {
    /** Mapa de sockets a noms de client. */
    private final Map<WebSocket, String> bySocket = new ConcurrentHashMap<>();

    /** Mapa de noms de client a sockets. */
    private final Map<String, WebSocket> byName = new ConcurrentHashMap<>();

    /** Cua de noms disponibles per assignar. */
    private final Queue<String> pool = new ConcurrentLinkedQueue<>();

    /** Llista base de noms per reomplir el pool quan s'esgoti. */
    private final List<String> seedNames;

    /**
     * Crea un nou registre amb el conjunt inicial de noms disponibles.
     *
     * @param seedNames llista inicial de noms per al pool
     */
    ClientRegistry(List<String> seedNames) {
        this.seedNames = seedNames;
        resetPool();
    }

    /**
     * Reinicia el pool de noms amb la llista inicial.
     * Aquest mètode és sincronitzat per evitar condicions de cursa durant el buidat i reompliment.
     */
    private synchronized void resetPool() {
        pool.clear();
        pool.addAll(seedNames);
    }

    /**
     * Extreu un nom disponible del pool. Si el pool està buit, es reinicia i es torna a intentar.
     *
     * @return un nom lliure extret del pool
     */
    private String takeOrRecycle() {
        String name = pool.poll();
        if (name == null) {
            resetPool();
            name = pool.poll();
        }
        return name;
    }

    /**
     * Retorna un nom al pool de disponibles.
     *
     * @param name el nom a retornar; si és null no es fa res
     */
    private void giveBack(String name) {
        if (name != null) {
            pool.offer(name);
        }
    }

    /**
     * Afegeix un client nou i li assigna un nom lliure.
     *
     * @param socket socket del client connectat
     * @return el nom assignat al client
     */
    String add(WebSocket socket) {
        String name = takeOrRecycle();
        bySocket.put(socket, name);
        byName.put(name, socket);
        return name;
    }

    /**
     * Elimina un client del registre i retorna el nom al pool.
     *
     * @param socket socket del client a eliminar
     * @return el nom que estava assignat, o null si no existia
     */
    String remove(WebSocket socket) {
        String name = bySocket.remove(socket);
        if (name != null) {
            byName.remove(name);
            giveBack(name);
        }
        return name;
    }

    /**
     * Obté el socket associat a un nom de client.
     *
     * @param name nom del client
     * @return socket associat o null si no existeix
     */
    WebSocket socketByName(String name) {
        return byName.get(name);
    }

    /**
     * Obté el nom associat a un socket.
     *
     * @param socket socket del client
     * @return nom del client o null si no existeix
     */
    String nameBySocket(WebSocket socket) {
        return bySocket.get(socket);
    }

    /**
     * Retorna la llista actual de noms de clients connectats en format JSONArray.
     *
     * @return JSONArray amb els noms dels clients actius
     */
    JSONArray currentNames() {
        JSONArray arr = new JSONArray();
        for (String n : byName.keySet()) {
            arr.put(n);
        }
        return arr;
    }

    /**
     * Neteja el registre per a un socket desconnectat.
     * Equivalent a remove(socket).
     *
     * @param socket socket desconnectat
     * @return nom del client eliminat o null si no existia
     */
    String cleanupDisconnected(WebSocket socket) {
        return remove(socket);
    }

    /**
     * Retorna una còpia immutable de l'estat actual del mapa socket a nom.
     * Útil per iteracions fora del lock intern sense risc de ConcurrentModification.
     *
     * @return mapa immutable de WebSocket a nom
     */
    Map<WebSocket, String> snapshot() {
        return Map.copyOf(bySocket);
    }
}
