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
        if (!room.isPlaying() || RoomUtil.isPlayerReady(room, clientId) || room.isStart()) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        room.addReadyPlayer(clientId);
        if (room.getReadyPlayers().size() == room.getPlayers().size()+1) {
            room.clearReadyPlayer();
            room.setStart(true);
            RoomUtil.sendToRoomPlayers(room, JsonMessageUtil.getStatusMessage("rstart"));
            room.sendAccuracyEverySecond();
        }
        RoomUtil.registerRoom(room);
    }

    public void setAccuracy(String accuracy) {
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
        room.putAccuracy(clientId, accuracy);
        RoomUtil.registerRoom(room);
    }

    public void complete() {
        if (!RoomUtil.isUserJoined(clientId)) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }

        Room room = RoomUtil.getUserRoom(clientId);
        if (room.getCompleteUsers().contains(clientId) || !room.isStart()) {
            out.println(JsonMessageUtil.getStatusMessage("error"));
            out.flush();
            return;
        }
        room.addCompleteUser(clientId);
        if (room.getCompleteUsers().size() == room.getPlayers().size()+1) {
            room.clearCompleteUser();
            room.setStart(false);
            room.setPlaying(false);
            RoomUtil.sendToRoomPlayers(room, JsonMessageUtil.getStatusMessage("complete"));
        }
        RoomUtil.registerRoom(room);
    }

    public static void left(Room room) {
        if (room.isPlaying() && !room.isStart() && room.getReadyPlayers().size() == room.getPlayers().size()+1) {
            room.clearReadyPlayer();
            room.setStart(true);
            RoomUtil.sendToRoomPlayers(room, JsonMessageUtil.getStatusMessage("rstart"));
            room.sendAccuracyEverySecond();
            RoomUtil.registerRoom(room);
        }
    }
}
