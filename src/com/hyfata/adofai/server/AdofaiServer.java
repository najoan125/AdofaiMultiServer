package com.hyfata.adofai.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AdofaiServer {
    public static void main(String[] args) {
        AdofaiServer adofaiServer = new AdofaiServer();
        adofaiServer.start(8000);
    }

    public void start(int port) {
        ServerSocket serverSocket = null;
        Socket socket;
        try {
            serverSocket = new ServerSocket(port);
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
