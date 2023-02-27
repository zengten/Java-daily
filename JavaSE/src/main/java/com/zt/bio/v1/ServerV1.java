package com.zt.bio.v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务端，实现客户端发消息，服务端接收消息
 */
public class ServerV1 {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(6001);
        Socket accept = serverSocket.accept();
        InputStream inputStream = accept.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = reader.readLine()) != null) {
            System.out.println("accept data = " + line);
        }
    }
}
