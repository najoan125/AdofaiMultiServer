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

    // roomInfo:{owner, players, title, readyPlayers}

    // status: !exist{rooms, joinRoom}, success{left}, already{ready, unready, kick}, error{ready, unready, kick}, kick{none}, connected{none}
    // rooms{rooms}
    // roomInfo{ready, unready, createRoom, joinRoom, kick, none}
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
                // status: success
            }
            case "rooms": {
                out.println(roomUtil.getAllRoomsInfoMessage());
                out.flush();
                return;
                // status: !exist
                // rooms:{title:{password, players, playing}}
            }
            case "ready": {
                roomUtil.ready();
                return;
                // status: already, error
                // roomInfo
            }
            case "unready": {
                roomUtil.unReady();
                return;
                // status: already, error
                // roomInfo
            }
        }
        JSONObject received = new JSONObject(inputMsg);

        //{"createRoom":{"title":"testTitle","password":"testPassword"}}
        //{"createRoom":{"title":"testTitle"}}
        if (received.has("createRoom")) {
            roomUtil.createRoom(received);
            // status: exist
            // roomInfo
        }

        //{"joinRoom":{"title":"testTitle","password":"testPassword"}}
        //{"joinRoom":{"title":"testTitle"}}
        else if (received.has("joinRoom")) {
            roomUtil.joinRoom(received);
            // status: !exist, !password
            // roomInfo
        }

        //{"kick":"test2"}
        else if (received.has("kick")) {
            roomUtil.kick(received.getString("kick"));
            // status: error, already, kick(kicked player)
            // roomInfo
        }
    }


    public void onDisconnect() {
        System.out.println("[" + clientId + " 연결 종료]");
        roomUtil.leftFromRoom();
    }
}
