package com.zt.bio.chat;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author ZT
 * @version 1.0
 * @description: 服务端chat，实现多个client发送群聊消息
 * @date 2023/1/18 16:44
 */
@Slf4j
@SuppressWarnings("Duplicates")
public class ServerChat {

    /**
     * 保存所有socket，目前有线程安全的问题
     */
    public static List<Socket> onLineSocket = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        log.info("服务端启动...");
        ServerSocket serverSocket = new ServerSocket(6002);
        log.info("开始接收客户端连接...");
        // 初始化4核心线程池，最多四人群聊
        ExecutorService executor = ThreadUtil.newFixedExecutor(4, 10, "server-", new ThreadPoolExecutor.AbortPolicy());
        while (true) {
            // 每个客户端连接时，开启新的线程去处理
            Socket accept = serverSocket.accept();
            log.info("新客户端连接成功，开始添加到在线队列中...");
            onLineSocket.add(accept);
            executor.execute(() -> {
                try {
                    // 接受client发送的消息
                    BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
                    String line;
                    while((line = reader.readLine()) != null) {
                        log.info("服务端读取到消息:{}", line);
                        // 消息转发到每个客户端
                        for (Socket socket : onLineSocket) {
                            PrintStream printStream = new PrintStream(socket.getOutputStream());
                            printStream.println(line);
                            printStream.flush();
                        }
                        log.info("消息:{},服务端处理完成", line);
                    }
                } catch (Exception e) {
                    // 下线socket
                    log.error("" + e);
                    onLineSocket.remove(accept);
                }
            });
        }
    }

}
