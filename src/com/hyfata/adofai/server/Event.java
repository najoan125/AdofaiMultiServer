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
        if ("quit".equals(inputMsg)) {
            shouldDisconnect = true;
            return;
        }
        if ("left".equals(inputMsg)) {
            roomUtil.leftFromRoom();
            out.println(JsonMessageUtil.getStatusMessage("success"));
            out.flush();
            return;
        }
        JSONObject received = new JSONObject(inputMsg);

        //{"createRoom":{"title":"testTitle","password":"testPassword"}}
        if (received.has("createRoom")) {
            roomUtil.createRoom(received);
        }

        //{"joinRoom":{"title":"testTitle","password":"testPassword"}}
        else if (received.has("joinRoom")) {
            roomUtil.joinRoom(received);
        }
    }


    public void onDisconnect() {
        System.out.println("[" + clientId + " 연결 종료]");
        roomUtil.leftFromRoom();
    }
}
