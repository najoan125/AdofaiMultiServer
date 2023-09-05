package com.hyfata.adofai.server;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Room {
    private String id;
    private String title;
    private String password;
    private final ArrayList<String> players = new ArrayList<>();
    private final HashMap<String, PrintWriter> socketOutput = new HashMap<>();
    private String ownerId;
    private String customLevelName;
    private String customLevelUrl;

    public Room(String title, String password, String ownerId) {
        this.title = title;
        this.password = password;
        this.ownerId = ownerId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<String> getPlayers() {
        return players;
    }

    public void addPlayer(String clientId) {
        this.players.add(clientId);
    }

    public void removePlayer(int index) {
        this.players.remove(index);
    }

    public void removePlayer(String clientId) {
        this.players.remove(clientId);
    }

    public HashMap<String, PrintWriter> getSocketOutput() {
        return socketOutput;
    }

    public void putSocketOutput(String clientId, PrintWriter output) {
        socketOutput.put(clientId, output);
    }

    public void removeSocketOutput(String clientId){
        socketOutput.remove(clientId);
    }

    public String getCustomLevelName() {
        return customLevelName;
    }

    public void setCustomLevelName(String customLevelName) {
        this.customLevelName = customLevelName;
    }

    public String getCustomLevelUrl() {
        return customLevelUrl;
    }

    public void setCustomLevelUrl(String customLevelUrl) {
        this.customLevelUrl = customLevelUrl;
    }
}
