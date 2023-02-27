package com.zt.bio.v3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务端
 * 解决多个客户端连接问题
 */
@SuppressWarnings("Duplicates")
public class ServerV3 {

    public static void main(String[] args) throws IOException {
        System.out.println("服务端启动...");
        ServerSocket serverSocket = new ServerSocket(6002);
        System.out.println("开始接收客户端连接...");
        while (true) {
            // 每个客户端连接时，开启新的线程去处理
            Socket accept = serverSocket.accept();
            System.out.println("新客户端连接成功，开始接收数据...");
            new Thread(new SocketThread(accept)).start();
        }
    }

    @SuppressWarnings("Duplicates")
    static class SocketThread implements Runnable {

        private Socket socket;

        public SocketThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                // 阻塞式等待 reader.readLine()
                while((line = reader.readLine()) != null) {
                    System.out.println("接收到数据 = " + line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
