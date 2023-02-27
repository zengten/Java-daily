package com.zt.nio;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @author ZT
 * @version 1.0
 * @description: nio buffer api
 * @date 2023/1/19 16:22
 */
@Slf4j
public class BufferTest {

    /**
     * static XxxBuffer allocate(int capacity)  创建一个容量为capacity的 XxxBuffer对象
     * Buffer flip() 将缓冲区的界限设置为当前位置，并将当前位置设置为 0
     * int capacity() 返回 Buffer 的 capacity 大小
     * int limit() 返回 Buffer 的界限(limit) 的位置
     * int position() 返回缓冲区的当前位置 position
     * get()  读取单个字节
     * Buffer clear() 清空缓冲区并返回对缓冲区的引用
     */
    @Test
    public void test01() {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        // 初始化：0  10  10
        print(buffer);
        buffer.put("hello".getBytes());
        // 存入hello后状态：5  10  10
        print(buffer);
        buffer.flip();
        // flip后状态：0  5  10
        print(buffer);
        char ch = (char) buffer.get();
        log.info("ch = {}", ch);
        // get后状态：1  5  10
        print(buffer);
        buffer.clear();
        // clear后回到初始：0  10  10
        print(buffer);
        // clear不会覆盖旧数据 ch = h
        log.info("ch = {}", (char) buffer.get());
    }


    /**
     * boolean hasRemaining() 判断缓冲区中是否还有元素
     * int limit() 返回 Buffer 的界限(limit) 的位置
     * Buffer mark() 对缓冲区设置标记
     * int remaining() 返回 position 和 limit 之间的元素个数
     * Buffer reset() 将位置 position 转到以前设置的 mark 所在的位置
     * Buffer rewind() 将位置设为为 0， 取消设置的 mark
     * get(byte[] dst)：批量读取多个字节到 dst中，如果没有数据会抛出异常
     */
    @Test
    public void test02() {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put("helloNIO".getBytes());
        buffer.flip();
        // 0 8 10
        print(buffer);
        byte[] bytes = new byte[2];
        buffer.get(bytes);
        String str = new String(bytes);
        log.info("str = {}", str);// he
        print(buffer);// 2 8 10
        // 标记当前位置
        buffer.mark();
        bytes = new byte[3];
        buffer.get(bytes);
        str = new String(bytes);
        log.info("str = {}", str);// llo
        print(buffer);// 5 8 10
        // reset 回到mark时状态，如果没有mark则抛出异常
        buffer.reset();
        // reset后重新回到mark位置 2 8 10
        print(buffer);
        if (buffer.hasRemaining()) {
            log.info("剩下未读元素：{}", buffer.remaining());// 6
        }
        buffer.rewind();
        // rewind重新回到 0 8 10
        print(buffer);
    }


    @Test
    public void test03() {
        // 基于直接内存(操作系统)的buffer
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(10);
        boolean direct1 = buffer1.isDirect();
        log.info("direct1 = {}", direct1);
        // 基于非直接内存(堆内存)的buffer
        ByteBuffer buffer2 = ByteBuffer.allocate(10);
        boolean direct2 = buffer2.isDirect();
        log.info("direct2 = {}", direct2);
    }


    private void print(ByteBuffer buffer) {
        int position = buffer.position();
        log.info("position = {}", position);
        int limit = buffer.limit();
        log.info("limit = {}", limit);
        int capacity = buffer.capacity();
        log.info("capacity = {}", capacity);
        log.info("-----------------------");
    }

}
