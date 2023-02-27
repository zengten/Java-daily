package com.zt.bio.v4;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

@SuppressWarnings("Duplicates")
public class ClientV4 {

    public static void main(String[] args) throws IOException {
        System.out.println("客户端启动...");
        Socket socket = new Socket("127.0.0.1", 6002);
        System.out.println("开始发送数据...");
        OutputStream outputStream = socket.getOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("say...");
            // 阻塞式等待 sc.nextLine()
            printStream.println(sc.nextLine());
            printStream.flush();
        }
    }
}
