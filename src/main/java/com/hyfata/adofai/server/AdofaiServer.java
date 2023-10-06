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
            System.out.println("서버 실행됨");
            while (true) {
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
                    System.out.println("서버 종료");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("서버 소켓 통신 오류");
                }
            }
        }
    }
}
