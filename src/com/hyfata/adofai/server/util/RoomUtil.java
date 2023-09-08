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

        rooms.put(title, createdRoom);
        joinedRoomTitles.put(clientId,title);

        System.out.println(clientId+"님이 "+title+" 방을 만듦");
        out.println(getRoomInfoMessage(createdRoom));
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
        Room room = rooms.get(title);
        if (room.getPassword() != null && !room.getPassword().equals(password)) {
            out.println(JsonMessageUtil.getStatusMessage("!password"));
            out.flush();
            return;
        }

        room.addPlayer(clientId);
        room.putSocketOutput(clientId, out);

        rooms.put(title, room);
        joinedRoomTitles.put(clientId,title);

        System.out.println(clientId+"님이 "+title+" 방에 참가함");
        sendToRoomPlayers(room, getRoomInfoMessage(room));
    }

    public void ready() {
        if (!isPlayerJoined(clientId)){
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }

        Room room = getPlayerRoom(clientId);
        if (isOwner(room, clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("already"));
            out.flush();
            return;
        }
        if (isPlayerReady(room, clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("already"));
            out.flush();
            return;
        }
        room.addReadyPlayer(clientId);
        sendToRoomPlayers(room, getRoomInfoMessage(room));
    }

    public void unReady() {
        if (!isPlayerJoined(clientId)){
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        Room room = getPlayerRoom(clientId);
        if (!isPlayerReady(room, clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("already"));
            out.flush();
            return;
        }
        room.removeReadyPlayer(clientId);
        sendToRoomPlayers(room, getRoomInfoMessage(room));
    }

    public void changeOwner(String clientId) {
        if (!isPlayerJoined(this.clientId) || !isPlayerJoined(clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        Room room = getPlayerRoom(this.clientId);
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
            sendToRoomPlayers(room, getRoomInfoMessage(room));
        }
    }

    public void leftFromRoom() {
        if (!isPlayerJoined(clientId)) {
            return;
        }
        Room room = getPlayerRoom(clientId);

        if (isOwner(room, clientId)){
            if (room.getPlayers().isEmpty()) {
                rooms.remove(room.getTitle());
                joinedRoomTitles.remove(clientId);
                System.out.println(clientId + "님이 "+room.getTitle()+"에서 퇴장함");
                return;
            }
            room = removeOwner(room, clientId);
        } else {
            room = removePlayer(room, clientId);
        }

        System.out.println(clientId + "님이 "+ Objects.requireNonNull(room).getTitle()+"에서 퇴장함");
        sendToRoomPlayers(room, getRoomInfoMessage(room));
    }

    public void kick(String clientId) {
        Room room = getPlayerRoom(this.clientId);
        if (!isOwner(room, this.clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        if (!isPlayerJoined(room,clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("already"));
            out.flush();
            return;
        }
        sendToRoomPlayer(room, clientId, JsonMessageUtil.getStatusMessage("kick"));
        room = removePlayer(room, clientId);
        sendToRoomPlayers(Objects.requireNonNull(room), getRoomInfoMessage(room));
    }

    private boolean isOwner(Room room, String clientId) {
        return room.getOwnerId().equals(clientId);
    }

    private boolean isPlayerJoined(Room room, String clientId) {
        return room.getPlayers().contains(clientId);
    }

    private boolean isPlayerJoined(String clientId) {
        return joinedRoomTitles.containsKey(clientId);
    }

    private boolean isPlayerReady(Room room, String clientId) {
        return room.getReadyPlayers().contains(clientId);
    }

    public Room getPlayerRoom(String clientId) {
        return rooms.get(joinedRoomTitles.get(clientId));
    }

    private Room removePlayer(Room room, String clientId) {
        if (!isPlayerJoined(room, clientId)) return null;

        room.removeSocketOutput(clientId);
        room.removePlayer(clientId);
        if (isPlayerReady(room, clientId)) {
            room.removeReadyPlayer(clientId);
        }

        rooms.put(room.getTitle(), room);
        joinedRoomTitles.remove(clientId);

        return room;
    }

    private Room removeOwner(Room room, String clientId) {
        if (!isOwner(room, clientId)) return null;

        room.removeSocketOutput(clientId);
        room = setOwner(room, room.getPlayers().get(0));
        joinedRoomTitles.remove(clientId);

        return room;
    }

    public Room setOwner(Room room, String clientId) {
        if (!isPlayerJoined(room, clientId)) return null;

        room.setOwnerId(clientId);
        if (isPlayerReady(room, clientId)) {
            room.removeReadyPlayer(clientId);
        }
        room.removePlayer(clientId);

        rooms.put(room.getTitle(), room);
        return room;
    }

    public void sendToRoomPlayers(Room room, String message) {
        PrintWriter preOut = room.getSocketOutput().get(room.getOwnerId());
        preOut.println(message);
        preOut.flush();
        for (String player : room.getPlayers()) {
            PrintWriter out = room.getSocketOutput().get(player);
            out.println(message);
            out.flush();
        }
    }

    public void sendToRoomPlayer(Room room, String clientId, String message) {
        PrintWriter out = room.getSocketOutput().get(clientId);
        out.println(message);
        out.flush();
    }
}
