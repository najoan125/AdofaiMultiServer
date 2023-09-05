package com.hyfata.adofai.server.util;

import com.hyfata.adofai.server.Room;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.util.HashMap;

public class RoomUtil {
    private static final HashMap<String,Room> rooms = new HashMap<>(); //title, Room
    private static final HashMap<String,String> joinedRoomTitles = new HashMap<>(); //clientId, roomTitle
    private final PrintWriter out;
    private final String clientId;

    public RoomUtil(String clientId, PrintWriter out) {
        this.clientId = clientId;
        this.out = out;
    }

    public String getRoomInfoMessage(Room room) {
        JSONObject result = new JSONObject();

        JSONObject object = new JSONObject();
        object.put("title", room.getTitle());
        object.put("players", room.getPlayers());
        object.put("owner", room.getOwnerId());
        object.put("customName", room.getCustomLevelName());
        object.put("customUrl", room.getCustomLevelUrl());

        result.put("roomInfo", object);
        return result.toString();
    }

    public void createRoom(JSONObject received) {
        JSONObject room = received.getJSONObject("createRoom");

        String title = room.getString("title").trim();
        if (rooms.containsKey(title)) {
            out.println(JsonMessageUtil.getStatusMessage("exist"));
            out.flush();
            return;
        }
        String password = null;
        if (room.has("password"))
            password = room.getString("password");

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
        if (!rooms.get(title).getPassword().equals(password)) {
            out.println(JsonMessageUtil.getStatusMessage("!password"));
            out.flush();
            return;
        }

        Room room = rooms.get(title);
        room.addPlayer(clientId);
        room.putSocketOutput(clientId, out);

        rooms.put(title, room);
        joinedRoomTitles.put(clientId,title);

        System.out.println(clientId+"님이 "+title+" 방에 참가함");
        sendToRoomPlayers(room, getRoomInfoMessage(room));
    }

    public void leftFromRoom() {
        if (!joinedRoomTitles.containsKey(clientId)) {
            return;
        }
        String roomTitle = joinedRoomTitles.get(clientId);
        Room room = rooms.get(roomTitle);
        room.removeSocketOutput(clientId);

        if (room.getOwnerId().equals(clientId)){
            if (room.getPlayers().isEmpty()) {
                rooms.remove(roomTitle);
                joinedRoomTitles.remove(clientId);
                System.out.println(clientId + "님이 "+roomTitle+"에서 퇴장함");
                return;
            }
            room.setOwnerId(room.getPlayers().get(0));
            room.removePlayer(0);
            rooms.put(roomTitle, room);
        } else {
            room.removePlayer(clientId);
        }

        System.out.println(clientId + "님이 "+roomTitle+"에서 퇴장함");
        sendToRoomPlayers(room, getRoomInfoMessage(room));
        joinedRoomTitles.remove(clientId);
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
}
