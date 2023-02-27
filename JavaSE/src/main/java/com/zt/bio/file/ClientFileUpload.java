package com.zt.bio.file;

import lombok.extern.slf4j.Slf4j;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

@Slf4j
@SuppressWarnings("Duplicates")
public class ClientFileUpload {

    public static void main(String[] args) throws IOException {
        log.info("客户端启动...");
        Socket socket = new Socket("127.0.0.1", 6002);
        log.info("开始发送数据...");
        InputStream is = new FileInputStream("I:\\笔记资料\\大厂面试之IO模式详解资料\\文件\\壁纸.jpg");
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeUTF(".jpg");
        byte[] buffer = new byte[1024];
        int len;
        while((len = is.read(buffer)) != -1) {
            dos.write(buffer, 0, len);
        }
        dos.flush();
        socket.close();
        log.info("客户端发送完成！");
    }
}
