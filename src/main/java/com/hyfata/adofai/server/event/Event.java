package com.hyfata.adofai.server.event;

import com.hyfata.adofai.server.util.JsonMessageUtil;
import com.hyfata.adofai.server.util.PlayUtil;
import com.hyfata.adofai.server.util.RoomUtil;
import com.hyfata.adofai.server.util.mysql.UserDB;
import org.json.JSONException;
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
        String nickName = UserDB.getUserNickName(clientId);
        System.out.println(this.clientId + " 연결됨");
        if (nickName == null) {
            out.println(JsonMessageUtil.getStatusMessage("!nickname"));
            out.flush();
            return;
        }

        roomUtil = new RoomUtil(nickName, out);
        playUtil = new PlayUtil(nickName, out);


        out.println(JsonMessageUtil.getJsonMessage("nickname",nickName));
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
            case "complete": {
                playUtil.complete();
                return;
                // status: error, complete
            }
        }
        JSONObject received;
        try {
            received = new JSONObject(inputMsg);
        } catch (JSONException e) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }

        //{"createRoom":{"title":"testTitle","password":"testPassword"}}
        //{"createRoom":{"title":"testTitle"}}
        if (received.has("createRoom")) {
            roomUtil.createRoom(received);
            // status: exist
            // roomInfo
        }

        //{"setPassword":"cp"}
        else if (received.has("setPassword")) {
            roomUtil.changePassword(received.getString("setPassword"));
            // status: error, !owner, success
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

        //{"Accuracy":"99.56"}
        else if (received.has("Accuracy")) {
            playUtil.setAccuracy(received.getString("Accuracy"));
            // status: error
        }
    }


    public void onDisconnect() {
        System.out.println(clientId + " 연결 종료");
        roomUtil.leftFromRoom();
    }
}
