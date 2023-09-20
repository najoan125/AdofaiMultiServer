package com.hyfata.adofai.server.util;

import com.hyfata.adofai.server.Room;

import java.io.PrintWriter;

public class PlayUtil {
    private final PrintWriter out;
    private final String clientId;

    public PlayUtil(String clientId, PrintWriter out) {
        this.clientId = clientId;
        this.out = out;
    }

    public void ready() {
        if (!RoomUtil.isUserJoined(clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        Room room = RoomUtil.getUserRoom(clientId);
        if (!room.isPlaying()) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        room.addReadyPlayer(clientId);
        if (room.getReadyPlayers().size() == room.getPlayers().size()+1) {
            room.clearReadyPlayer();
            RoomUtil.sendToRoomPlayers(room, JsonMessageUtil.getStatusMessage("rstart"));
        }
        RoomUtil.registerRoom(room);
    }
}
