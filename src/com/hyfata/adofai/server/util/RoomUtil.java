package com.hyfata.adofai.server.util;

import com.hyfata.adofai.server.Room;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Objects;

public class RoomUtil {
    private static final HashMap<String,Room> rooms = new HashMap<>(); //title, Room
    private static final HashMap<String,String> joinedRoomTitles = new HashMap<>(); //clientId, roomTitle
    private final PrintWriter out;
    private final String clientId;

    public RoomUtil(String clientId, PrintWriter out) {
        this.clientId = clientId;
        this.out = out;
    }

    public String getAllRoomsInfoMessage() {
        if (rooms.isEmpty()) {
            return JsonMessageUtil.getStatusMessage("!exist");
        }

        JSONObject object = new JSONObject();
        for (Room room : rooms.values()) {
            object.put(room.getTitle(), getSimpleRoomInfoJson(room));
        }

        JSONObject result = new JSONObject();
        result.put("rooms",object);
        return result.toString();
    }

    public String getRoomInfoMessage(Room room) {
        JSONObject result = new JSONObject();
        result.put("roomInfo", getRoomInfoJson(room));
        return result.toString();
    }

    private JSONObject getRoomInfoJson(Room room) {
        JSONObject object = new JSONObject();
        object.put("title", room.getTitle());
        object.put("players", room.getPlayers());
        object.put("readyPlayers", room.getReadyPlayers());
        object.put("owner", room.getOwnerId());
        object.put("customName", room.getCustomLevelName());
        object.put("customUrl", room.getCustomLevelUrl());
        return object;
    }

    private JSONObject getSimpleRoomInfoJson(Room room) {
        JSONObject result = new JSONObject();
        result.put("players", room.getPlayers().size()+1);
        result.put("playing",room.isPlaying());
        result.put("password", room.getPassword() != null);
        return result;
    }

    public void createRoom(JSONObject received) {
        JSONObject room = received.getJSONObject("createRoom");

        String title = room.getString("title").trim();
        if (rooms.containsKey(title)) {
            out.println(JsonMessageUtil.getStatusMessage("exist"));
            out.flush();
            return;
        }
        String password = room.optString("password", null);

        Room createdRoom = new Room(title, password, clientId);
        createdRoom.putSocketOutput(clientId, out);
        registerRoom(createdRoom);
        registerUser(createdRoom);

        System.out.println(clientId+"님이 "+title+" 방을 만듦");
        out.println(getRoomInfoMessage(createdRoom));
        out.flush();
    }

    public void changePassword(String password) {
        if (!isUserJoined(clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        Room room = getUserRoom(clientId);
        if (!isOwner(room, clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("!owner"));
            out.flush();
            return;
        }
        room.setPassword(password);
        registerRoom(room);
        out.println(JsonMessageUtil.getStatusMessage("success"));
        out.flush();
    }

    public void joinRoom(JSONObject received) {
        JSONObject object = received.getJSONObject("joinRoom");
        String title = object.getString("title");
        String password = object.optString("password", null);

        if (!rooms.containsKey(title)) {
            out.println(JsonMessageUtil.getStatusMessage("!exist"));
            out.flush();
            return;
        }
        if (isUserJoined(clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("already"));
            out.flush();
            return;
        }
        Room room = rooms.get(title);
        if (room.isPlaying()) {
            out.println(JsonMessageUtil.getStatusMessage("playing"));
            out.flush();
            return;
        }
        if (room.getPassword() != null && !room.getPassword().equals(password)) {
            out.println(JsonMessageUtil.getStatusMessage("!password"));
            out.flush();
            return;
        }
        room.putSocketOutput(clientId, out);
        room.addPlayer(clientId);
        registerUser(room);
        registerRoom(room);

        System.out.println(clientId+"님이 "+title+" 방에 참가함");
        sendToRoomPlayers(room, getRoomInfoMessage(room));
    }

    public void ready() {
        if (!isUserJoined(clientId)){
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }

        Room room = getUserRoom(clientId);
        if (isOwner(room, clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("already"));
            out.flush();
            return;
        }
        if (room.getCustomLevelName().isEmpty() || room.getCustomLevelUrl().isEmpty()) {
            out.println(JsonMessageUtil.getStatusMessage("!level"));
            out.flush();
            return;
        }
        if (isPlayerReady(room, clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("already"));
            out.flush();
            return;
        }
        if (room.isPlaying()) return;
        room.addReadyPlayer(clientId);
        registerRoom(room);
        sendToRoomPlayers(room, getRoomInfoMessage(room));
    }

    public void unReady() {
        if (!isUserJoined(clientId)){
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        Room room = getUserRoom(clientId);
        if (!isPlayerReady(room, clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("already"));
            out.flush();
            return;
        }
        if (room.isPlaying()) return;
        room.removeReadyPlayer(clientId);
        registerRoom(room);
        sendToRoomPlayers(room, getRoomInfoMessage(room));
    }

    public void setLevel(String name, String url) {
        if (!isUserJoined(clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        Room room = getUserRoom(clientId);
        if (!room.getOwnerId().equals(clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("!owner"));
            out.flush();
            return;
        }
        room.clearReadyPlayer();
        room.setCustomLevelName(name);
        room.setCustomLevelUrl(url);
        registerRoom(room);
        sendToRoomPlayers(room, getRoomInfoMessage(room));
    }

    public void start() {
        if (!isUserJoined(clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        Room room = getUserRoom(clientId);
        if (room.getPlayers().isEmpty()) {
            out.println(JsonMessageUtil.getStatusMessage("!player"));
            out.flush();
            return;
        }
        if (!isOwner(room, clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("!owner"));
            out.flush();
            return;
        }
        if (room.getCustomLevelName().isEmpty() || room.getCustomLevelUrl().isEmpty()) {
            out.println(JsonMessageUtil.getStatusMessage("!level"));
            out.flush();
            return;
        }

        if (room.getPlayers().size() != room.getReadyPlayers().size()) {
            out.println(JsonMessageUtil.getStatusMessage("!ready"));
            out.flush();
        } else {
            room.setPlaying(true);
            room.clearReadyPlayer();
            room.putAccuracy(clientId, "0");
            for (String client : room.getPlayers()) {
                room.putAccuracy(client, "0");
            }
            registerRoom(room);
            sendToRoomPlayers(room, JsonMessageUtil.getStatusMessage("start"));
        }
    }

    public void changeOwner(String clientId) {
        if (!isUserJoined(this.clientId) || !isUserJoined(clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        Room room = getUserRoom(this.clientId);
        if (!isOwner(room, this.clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        room.addPlayer(room.getOwnerId());
        room = setOwner(room, clientId);

        if (room == null) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
        }
        else {
            registerRoom(room);
            sendToRoomPlayers(room, getRoomInfoMessage(room));
        }
    }

    public void leftFromRoom() {
        if (!isUserJoined(clientId)) {
            return;
        }
        Room room = getUserRoom(clientId);

        if (isOwner(room, clientId)){
            if (room.getPlayers().isEmpty()) {
                room.setPlaying(false);
                unRegisterRoom(room);
                unRegisterUser(clientId);
                System.out.println(clientId + "님이 "+room.getTitle()+"에서 퇴장함");
                return;
            }
            room = removeOwner(room, clientId);
        } else {
            room = removePlayer(room, clientId);
        }
        unRegisterUser(clientId);
        registerRoom(Objects.requireNonNull(room));
        System.out.println(clientId + "님이 "+ Objects.requireNonNull(room).getTitle()+"에서 퇴장함");
        sendToRoomPlayers(room, getRoomInfoMessage(room));
        PlayUtil.left(room);
    }

    public void kick(String clientId) {
        Room room = getUserRoom(this.clientId);
        if (!isOwner(room, this.clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        if (this.clientId.equals(clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        if (!isUserJoined(room,clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("already"));
            out.flush();
            return;
        }
        sendToRoomPlayer(room, clientId, JsonMessageUtil.getStatusMessage("kick"));
        room = removePlayer(room, clientId);
        registerRoom(room);
        unRegisterUser(clientId);
        sendToRoomPlayers(Objects.requireNonNull(room), getRoomInfoMessage(room));
    }

    //helpful Methods

    public boolean isOwner(Room room, String clientId) {
        return room.getOwnerId().equals(clientId);
    }

    public static boolean isUserJoined(Room room, String clientId) {
        return room.getPlayers().contains(clientId) || room.getOwnerId().equals(clientId);
    }

    public static boolean isUserJoined(String clientId) {
        return joinedRoomTitles.containsKey(clientId);
    }

    public static boolean isPlayerReady(Room room, String clientId) {
        return room.getReadyPlayers().contains(clientId);
    }

    public static Room getUserRoom(String clientId) {
        return rooms.get(joinedRoomTitles.get(clientId));
    }

    private Room removePlayer(Room room, String clientId) {
        if (!isUserJoined(room, clientId)) return null;

        room.removeSocketOutput(clientId);
        room.removePlayer(clientId);
        if (isPlayerReady(room, clientId)) {
            room.removeReadyPlayer(clientId);
        }
        if (room.isPlaying()) {
            room.removeAccuracy(clientId);
        }
        return room;
    }

    private Room removeOwner(Room room, String clientId) {
        if (!isOwner(room, clientId)) return null;

        room.removeSocketOutput(clientId);
        room = setOwner(room, room.getPlayers().get(0));
        if (room.isPlaying()) {
            room.removeAccuracy(clientId);
        }
        return room;
    }

    public Room setOwner(Room room, String clientId) {
        if (!isUserJoined(room, clientId)) return null;

        room.setOwnerId(clientId);
        if (isPlayerReady(room, clientId)) {
            room.removeReadyPlayer(clientId);
        }
        room.removePlayer(clientId);
        return room;
    }

    public static void registerRoom(Room room) {
        rooms.put(room.getTitle(), room);
    }

    private void unRegisterRoom(Room room) {
        rooms.remove(room.getTitle());
    }

    private void registerUser(Room room) {
        joinedRoomTitles.put(clientId, room.getTitle());
    }

    private void unRegisterUser(String clientId) {
        joinedRoomTitles.remove(clientId);
    }


    public static void sendToRoomPlayers(Room room, String message) {
        PrintWriter preOut = room.getSocketOutput().get(room.getOwnerId());
        preOut.println(message);
        preOut.flush();
        for (String player : room.getPlayers()) {
            PrintWriter out = room.getSocketOutput().get(player);
            out.println(message);
            out.flush();
        }
    }

    public static void sendToRoomPlayer(Room room, String clientId, String message) {
        PrintWriter out = room.getSocketOutput().get(clientId);
        out.println(message);
        out.flush();
    }
}
