package com.hyfata.adofai.server.event;

import com.hyfata.adofai.server.util.JsonMessageUtil;
import com.hyfata.adofai.server.util.PlayUtil;
import com.hyfata.adofai.server.util.RoomUtil;
import org.json.JSONObject;

import java.io.PrintWriter;

public class Event {
    RoomUtil roomUtil;
    PlayUtil playUtil;
    PrintWriter out;
    String clientId;
    public boolean shouldDisconnect = false;
    public void registerPrintWriter(PrintWriter out) {
        this.out = out;
    }

    public void onConnect(String clientId) {
        this.clientId = clientId;
        roomUtil = new RoomUtil(clientId, out);
        playUtil = new PlayUtil(clientId, out);

        System.out.println(this.clientId + " 연결됨");
        out.println(JsonMessageUtil.getStatusMessage("connected"));
        out.flush();
    }

    // roomInfo:{title, players, readyPlayers, owner, customName, customUrl}

    // status: !exist{rooms, joinRoom}, success{left}, already{ready, unready, kick}, error{ready, unready, kick, setOwner}, kick{none}, connected{none}
    // rooms{rooms}
    // roomInfo{ready, unready, createRoom, joinRoom, kick, setOwner, none}
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
                // status: already, error, !level
                // roomInfo
            }
            case "unready": {
                roomUtil.unReady();
                return;
                // status: already, error
                // roomInfo
            }
            case "start": {
                roomUtil.start();
                return;
                // status: error, !player, !level, !ready, start
            }
            case "clientReady": {
                playUtil.ready();
                return;
                // status: error, rstart
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
            // status: !exist, !password, already, playing
            // roomInfo
        }

        //{"kick":"test2"}
        else if (received.has("kick")) {
            roomUtil.kick(received.getString("kick"));
            // status: error, already, kick(kicked player)
            // roomInfo
        }

        //{"setOwner":"test2"}
        else if (received.has("setOwner")) {
            String clientId = received.getString("setOwner");
            roomUtil.changeOwner(clientId);
            // status: error
            // roomInfo
        }

        //{"setLevel":{"name":"test","url":"testurl"}}
        else if (received.has("setLevel")) {
            JSONObject object = received.getJSONObject("setLevel");
            String name = object.getString("name");
            String url = object.getString("url");
            roomUtil.setLevel(name, url);
            // status: error, !owner
            // roomInfo
        }
    }


    public void onDisconnect() {
        System.out.println(clientId + " 연결 종료");
        roomUtil.leftFromRoom();
    }
}