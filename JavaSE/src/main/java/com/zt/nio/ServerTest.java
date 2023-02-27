package com.zt.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @author ZT
 * @version 1.0
 * @description: NIO
 * @date 2023/1/22 16:12
 */
@Slf4j
public class ServerTest {

    public static void main(String[] args) throws Exception {
        log.info("服务端启动");
        // 打开服务端通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 设置非阻塞
        serverSocketChannel.configureBlocking(false);
        // 绑定端口
        serverSocketChannel.bind(new InetSocketAddress(9999));
        // 将通道注册到selector，并指定接收事件
        Selector selector = Selector.open();
        // 读 : SelectionKey.OP_READ （1）
        // 写 : SelectionKey.OP_WRITE （4）
        // 连接 : SelectionKey.OP_CONNECT （8）
        // 接收 : SelectionKey.OP_ACCEPT （16）
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // 使用selector选择器轮询接收，并处理事件，没有事件时会阻塞
        while(selector.select() > 0) {
            log.info("接收到新事件");
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            it.forEachRemaining(selectionKey -> {
                if (selectionKey.isAcceptable()) {
                    // 相当于新客户端连接
                    log.info("新事件为isAcceptable");
                    try {
                        SocketChannel accept = serverSocketChannel.accept();
                        // 必须设置非阻塞，否则 IllegalBlockingModeException
                        accept.configureBlocking(false);
                        // 对于服务端来说  读操作
                        accept.register(selector, SelectionKey.OP_READ);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (selectionKey.isReadable()) {
                    // 相当于客户端发送消息，服务端读取
                    log.info("新事件isReadable");
                    // 服务端接收字节数限制，如果超出时，多次buffer可能乱码
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    try {
                        int len;
                        // 一次读取1024字节，并输出
                        while ((len = channel.read(buffer)) > 0) {
                            buffer.flip();
                            String str = new String(buffer.array(), 0, len);
                            log.info("str = {}", str);
                            buffer.clear();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                it.remove();
            });
        }
    }
}
