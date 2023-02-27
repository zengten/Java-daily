package com.zt.nio;

import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * @author ZT
 * @version 1.0
 * @description: nio client
 * @date 2023/1/22 16:48
 */
@Slf4j
public class ClientTest {

    public static void main(String[] args) throws Exception {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9999));
        socketChannel.configureBlocking(false);
        Scanner scanner = new Scanner(System.in);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (true) {
            log.info("continue say");
            String line = scanner.nextLine();
            buffer.put(line.getBytes());
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }
    }

}
