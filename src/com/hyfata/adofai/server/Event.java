package com.hyfata.adofai.server;

import com.hyfata.adofai.server.util.JsonMessageUtil;
import com.hyfata.adofai.server.util.RoomUtil;
import org.json.JSONObject;

import java.io.PrintWriter;

public class Event {
    RoomUtil roomUtil;
    PrintWriter out;
    String clientId;
    public boolean shouldDisconnect = false;
    public Event(PrintWriter out) {
        this.out = out;
    }

    public void onConnect(String clientId) {
        this.clientId = clientId;
        roomUtil = new RoomUtil(clientId, out);

        System.out.println("[" + this.clientId + " 연결됨]");
        out.println(JsonMessageUtil.getStatusMessage("connected"));
        out.flush();
    }

    public void onReceive(String inputMsg) {
        switch (inputMsg) {
            case "quit": {
                shouldDisconnect = true;
                return;
            }
            case "left": {
                roomUtil.leftFromRoom();
                out.println(JsonMessageUtil.getStatusMessage("success"));
                out.flush();
                return;
            }
            case "rooms": {
                out.println(roomUtil.getAllRoomsInfoMessage());
                out.flush();
                return;
            }
            case "ready": {
                roomUtil.ready();
                return;
            }
            case "unready": {
                roomUtil.unReady();
                return;
            }
        }
        JSONObject received = new JSONObject(inputMsg);

        //{"createRoom":{"title":"testTitle","password":"testPassword"}}
        //{"createRoom":{"title":"testTitle"}}
        if (received.has("createRoom")) {
            roomUtil.createRoom(received);
        }

        //{"joinRoom":{"title":"testTitle","password":"testPassword"}}
        //{"joinRoom":{"title":"testTitle"}}
        else if (received.has("joinRoom")) {
            roomUtil.joinRoom(received);
        }
    }


    public void onDisconnect() {
        System.out.println("[" + clientId + " 연결 종료]");
        roomUtil.leftFromRoom();
    }
}
