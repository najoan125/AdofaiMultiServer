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

        String title = room.getString("title");
        if (rooms.containsKey(title)) {
            out.println(JsonMessageUtil.getStatusMessage("exist"));
            out.flush();
            return;
        }
        String password = null;
        if (room.has("password"))
            password = room.getString("password");

        Room createdRoom = new Room(title, password, clientId);
        createdRoom.addSocketOutput(out);

        rooms.put(title, createdRoom);
        joinedRoomTitles.put(clientId,title);

        System.out.println(clientId+"님이 "+title+" 방을 만듦");
        out.println(getRoomInfoMessage(createdRoom));
        out.flush();
    }

    public void leftFromRoom() {
        if (!joinedRoomTitles.containsKey(clientId)) {
            return;
        }
        String roomTitle = joinedRoomTitles.get(clientId);
        Room room = rooms.get(roomTitle);
        room.removeSocketOutput(out);

        if (room.getOwnerId().equals(clientId)){
            if (room.getPlayers().isEmpty()) {
                rooms.remove(roomTitle);
            } else {
                room.setOwnerId(room.getPlayers().get(0));
                room.removePlayer(0);
                rooms.put(roomTitle, room);
            }
        } else {
            room.removePlayer(clientId);
        }

        System.out.println(clientId + "님이 "+roomTitle+"에서 퇴장함");
        sendToRoomPlayers(room, getRoomInfoMessage(room));
        joinedRoomTitles.remove(clientId);
    }

    public void sendToRoomPlayers(Room room, String message) {
        for (PrintWriter out : room.getSocketOutput()) {
            out.println(message);
            out.flush();
        }
    }
}