package com.hyfata.adofai.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class AdofaiServer {
    public static HashMap<String,Room> rooms = new HashMap<>(); //title, Room
    public static HashMap<String,String> players = new HashMap<>(); //clientId, roomTitle

    public static void main(String[] args) {
        AdofaiServer adofaiServer = new AdofaiServer();
        adofaiServer.start();
    }

    public void start() {
        ServerSocket serverSocket = null;
        Socket socket;
        try {
            serverSocket = new ServerSocket(8000);
            while (true) {
                System.out.println("[클라이언트 연결 대기 중]");
                socket = serverSocket.accept();

                ReceiveThread receiveThread = new ReceiveThread(socket);
                receiveThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket!=null) {
                try {
                    serverSocket.close();
                    System.out.println("[서버종료]");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("[서버소켓통신에러]");
                }
            }
        }
    }
}
