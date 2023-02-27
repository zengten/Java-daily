package com.zt.bio.v2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务端
 * 缺点：不能接收多个客户端连接
 */
@SuppressWarnings("Duplicates")
public class ServerV2 {

    public static void main(String[] args) throws IOException {
        System.out.println("服务端启动...");
        ServerSocket serverSocket = new ServerSocket(6002);
        System.out.println("开始接收客户端连接...");
        // 只会接收一个客户端，启动多个客户端不能接收到消息
        Socket accept = serverSocket.accept();
        System.out.println("客户端连接成功，开始接收数据...");
        InputStream inputStream = accept.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        // 阻塞式等待 reader.readLine()
        while((line = reader.readLine()) != null) {
            System.out.println("接收到数据 = " + line);
        }
    }
}
