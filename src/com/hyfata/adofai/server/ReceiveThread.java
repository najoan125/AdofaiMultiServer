package com.hyfata.adofai.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ReceiveThread extends Thread {

    static List<PrintWriter> list = Collections.synchronizedList(new ArrayList<>());

    Socket socket;
    BufferedReader in = null;
    PrintWriter out = null;

    public ReceiveThread (Socket socket) {
        this.socket = socket;
        try {
            out = new PrintWriter(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            list.add(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        Event event = new Event(out);
        String clientId;
        try {
            clientId = in.readLine();
            event.onConnect(clientId);

            while (in != null && !event.shouldDisconnect) {
                event.onReceive(in.readLine());
            }
        } catch (IOException ignored) {
        } finally {
            event.onDisconnect();
            list.remove(out);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
