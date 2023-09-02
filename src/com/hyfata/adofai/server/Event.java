package com.hyfata.adofai.server;

import org.json.JSONObject;

import java.io.PrintWriter;

public class Event {
    PrintWriter out;
    String clientId;
    public boolean shouldDisconnect = false;
    public Event(PrintWriter out) {
        this.out = out;
    }

    public void onConnect(String clientId) {
        this.clientId = clientId;
        System.out.println("[" + this.clientId + " 연결됨]");
        out.println(getStatusMessage("connected"));
        out.flush();
    }

    public void onReceive(String inputMsg) {
        if ("quit".equals(inputMsg)) {
            shouldDisconnect = true;
            return;
        }
        if ("left".equals(inputMsg)) {
            leftFromRoom();
            out.println(getStatusMessage("success"));
            out.flush();
            return;
        }
        JSONObject received = new JSONObject(inputMsg);
        //{"createRoom":{"title":"testTitle","password":"testPassword"}}
        if (received.has("createRoom")) {
            createRoom(received);
        }
    }


    public void onDisconnect() {
        System.out.println("[" + clientId + " 연결 종료]");
        leftFromRoom();
    }

    //otherMethods
    private String getStatusMessage(String message) {
        JSONObject object = new JSONObject();
        object.put("status",message);
        return object.toString();
    }

    private void createRoom(JSONObject received) {
        JSONObject room = received.getJSONObject("createRoom");

        String title = room.getString("title");
        String password = null;
        if (room.has("password"))
            password = room.getString("password");

        Room createdRoom = new Room(title, password, clientId);

        if (AdofaiServer.rooms.containsKey(title)) {
            out.println(getStatusMessage("exist"));
            out.flush();
            return;
        }
        AdofaiServer.rooms.put(title, createdRoom);
        AdofaiServer.players.put(clientId,title);
        out.println(getStatusMessage("success"));
        out.flush();
    }

    private void leftFromRoom() {
        if (AdofaiServer.players.containsKey(clientId)) {
            String roomTitle = AdofaiServer.players.get(clientId);
            Room room = AdofaiServer.rooms.get(roomTitle);

            if (room.getOwnerId().equals(clientId)){
                if (room.getPlayers().isEmpty()) {
                    AdofaiServer.rooms.remove(roomTitle);
                }
                else {
                    room.setOwnerId(room.getPlayers().get(0));
                    room.removePlayer(0);
                    AdofaiServer.rooms.put(roomTitle, room);
                }
            }
            else {
                room.removePlayer(clientId);
            }
            AdofaiServer.players.remove(clientId);
        }
    }
}
