package com.zt.bio.chat;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author ZT
 * @version 1.0
 * @description: 客户端chat
 * @date 2023/1/18 16:44
 */
@Slf4j
@SuppressWarnings("Duplicates")
public class ClientChat {

    public static void main(String[] args) throws IOException {
        log.info("客户端启动...");
        Socket socket = new Socket("127.0.0.1", 6002);
        // 客户端唯一标识
        ISocket iSocket = new ISocket(IdUtil.fastSimpleUUID(), socket);
        // 启动接收其他客户端消息监听
        startAcceptMsg(iSocket);
        log.info("开始发送数据...");
        OutputStream outputStream = socket.getOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        Scanner sc = new Scanner(System.in);
        while (true) {
            log.info("continue say...");
            // 阻塞式等待 sc.nextLine()
            String line = sc.nextLine();
            log.info("{}发送消息{}", iSocket.getId(), line);
            printStream.println(line);
            printStream.flush();
        }
    }

    private static void startAcceptMsg(ISocket iSocket) {
        Thread task = new Thread(() -> {
            Socket socket = iSocket.getSocket();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String receiveMsg;
                while ((receiveMsg = reader.readLine()) != null) {
                    log.info("{}接收到消息：{}", iSocket.getId(), receiveMsg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        task.start();
    }

}
