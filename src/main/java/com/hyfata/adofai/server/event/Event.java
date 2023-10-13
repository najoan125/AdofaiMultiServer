package com.hyfata.adofai.server.event;

import com.hyfata.adofai.server.util.JsonMessageUtil;
import com.hyfata.adofai.server.util.PlayUtil;
import com.hyfata.adofai.server.util.RoomUtil;
import com.hyfata.adofai.server.util.mysql.UserDB;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.sql.SQLException;

public class Event {
    RoomUtil roomUtil;
    PlayUtil playUtil;
    PrintWriter out;
    String clientId;
    String nickName;
    public boolean shouldDisconnect = false;
    public void registerPrintWriter(PrintWriter out) {
        this.out = out;
    }

    public void onConnect(String clientId) {
        this.clientId = clientId;
        System.out.println(this.clientId + " 연결됨");

        // load nickName
        try {
            nickName = UserDB.getUserNickName(clientId);
            if (nickName == null) {
                out.println(JsonMessageUtil.getStatusMessage("!nickname"));
                out.flush();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }

        // apply
        roomUtil = new RoomUtil(nickName, out);
        playUtil = new PlayUtil(nickName, out);
        out.println(JsonMessageUtil.getJsonMessage("nickname",nickName));
        out.flush();
    }

    public void onReceive(String inputMsg) {
        JSONObject received;
        try {
            received = new JSONObject(inputMsg);
        } catch (JSONException e) {
            if (nickName != null) {
                onCommand(inputMsg);
            }
            else {
                out.println(JsonMessageUtil.getStatusMessage("!nickname"));
                out.flush();
            }
            return;
        }

        //{"setNick":"changed"}
        if (received.has("setNick")) {
            if (nickName != null && RoomUtil.isUserJoined(nickName)) {
                out.println(JsonMessageUtil.getStatusMessage("cantSetNick"));
                out.flush();
                return;
            }
            try {
                if (UserDB.isNickNameExist(received.getString("setNick"))){
                    out.println(JsonMessageUtil.getStatusMessage("exist"));
                    out.flush();
                    return;
                }
            } catch (SQLException e) {
                out.println(JsonMessageUtil.getStatusMessage("error"));
                out.flush();
                return;
            }
            setNick(received);
        }

        else if (nickName == null) {
            out.println(JsonMessageUtil.getStatusMessage("!nickname"));
            out.flush();
        }

        //{"createRoom":{"title":"testTitle","password":"testPassword"}}
        //{"createRoom":{"title":"testTitle"}}
        else if (received.has("createRoom")) {
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

    private void setNick(JSONObject received) {
        String changedNickName = received.getString("setNick");
        if (nickName == null) {
            if (UserDB.insertNickName(clientId, changedNickName) == 1) {
                nickName = changedNickName;
                roomUtil = new RoomUtil(changedNickName, out);
                playUtil = new PlayUtil(changedNickName, out);
                out.println(JsonMessageUtil.getJsonMessage("nickname", changedNickName));
                out.flush();
                return;
            }
        }
        else {
            if (UserDB.updateNickName(clientId, changedNickName) == 1) {
                nickName = changedNickName;
                roomUtil = new RoomUtil(changedNickName, out);
                playUtil = new PlayUtil(changedNickName, out);
                out.println(JsonMessageUtil.getJsonMessage("nickname", changedNickName));
                out.flush();
                return;
            }
        }
        out.println(JsonMessageUtil.getStatusMessage("error"));
        out.flush();
    }

    private void onCommand(String inputMsg) {
        switch (inputMsg) {
            case "quit": {
                shouldDisconnect = true;
                break;
            }
            case "left": {
                roomUtil.leftFromRoom();
                out.println(JsonMessageUtil.getStatusMessage("success"));
                out.flush();
                break;
                // status: success
            }
            case "rooms": {
                out.println(roomUtil.getAllRoomsInfoMessage());
                out.flush();
                break;
                // status: !exist
                // rooms:{title:{password, players, playing}}
            }
            case "ready": {
                roomUtil.ready();
                break;
                // status: already, error, !level
                // roomInfo
            }
            case "unready": {
                roomUtil.unReady();
                break;
                // status: already, error
                // roomInfo
            }
            case "start": {
                roomUtil.start();
                break;
                // status: error, !player, !level, !ready, start
            }
            case "clientReady": {
                playUtil.ready();
                break;
                // status: error, rstart
            }
            case "complete": {
                playUtil.complete();
                break;
                // status: error, complete
            }
            case "deleteUser": {
                if (RoomUtil.isUserJoined(nickName)) {
                    out.println(JsonMessageUtil.getStatusMessage("cantDelete"));
                }
                else if (UserDB.deleteUser(clientId) == 1) {
                    roomUtil = null;
                    playUtil = null;
                    nickName = null;
                    out.println(JsonMessageUtil.getStatusMessage("success"));
                }
                else {
                    out.println(JsonMessageUtil.getStatusMessage("error"));
                }
                out.flush();
                break;
            }
            default: {
                out.println(JsonMessageUtil.getStatusMessage("error"));
                out.flush();
            }
        }
    }


    public void onDisconnect() {
        System.out.println(clientId + " 연결 종료");
        roomUtil.leftFromRoom();
    }
}
