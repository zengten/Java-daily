package com.zt.bio.v1;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * 客户端
 * 缺点: 只能发送一次消息
 */
public class ClientV1 {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("127.0.0.1", 6001);
        OutputStream outputStream = socket.getOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        printStream.println("hello");
        printStream.flush();
    }
}
