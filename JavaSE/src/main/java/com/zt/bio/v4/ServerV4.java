package com.zt.bio.v4;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 使用线程池改进  伪异步IO
 */
@Slf4j
@SuppressWarnings("Duplicates")
public class ServerV4 {

    public static void main(String[] args) throws IOException {
        log.info("服务端启动...");
        ServerSocket serverSocket = new ServerSocket(6002);
        log.info("开始接收客户端连接...");
        // 初始化2核心线程池
        ExecutorService executor = ThreadUtil.newFixedExecutor(2, 10, "server-", new ThreadPoolExecutor.AbortPolicy());
        while (true) {
            // 每个客户端连接时，开启新的线程去处理
            Socket accept = serverSocket.accept();
            log.info("新客户端连接成功，开始接收数据...");
            executor.execute(() -> {
                try {
                    InputStream inputStream = accept.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    // 阻塞式等待 reader.readLine()
                    while((line = reader.readLine()) != null) {
                        log.info("接收到数据 = " + line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
