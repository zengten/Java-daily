package com.zt.bio.file;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 使用线程池改进  伪异步IO
 */
@Slf4j
@SuppressWarnings("Duplicates")
public class ServerFileUpload {

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
                    DataInputStream dis = new DataInputStream(accept.getInputStream());
                    String suffix = dis.readUTF();
                    String path = "I:\\笔记资料\\大厂面试之IO模式详解资料\\文件\\server\\";
                    OutputStream os = new FileOutputStream(path + IdUtil.fastSimpleUUID() + suffix);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = dis.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                    os.flush();
                    log.info("服务端接收完成");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

}
