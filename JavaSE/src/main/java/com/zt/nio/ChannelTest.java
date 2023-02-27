package com.zt.nio;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author ZT
 * @version 1.0
 * @description: nio channel
 * @date 2023/1/20 17:03
 */
@Slf4j
public class ChannelTest {

    /**
     * fileChannel write
     */
    @Test
    public void testFileChannelWrite() throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream("hello.txt");
        FileChannel channel = fileOutputStream.getChannel();
        // buffer 不能超出指定大小1024字节，会报BufferOverflowException
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put("hello channel 怎么用！".getBytes());
        // 开启读取模式
        buffer.flip();
        channel.write(buffer);
        log.info("写入数据完成！");
    }

    /**
     * fileChannel read
     */
    @Test
    public void testFileChannelRead() throws Exception {
        FileInputStream fileInputStream = new FileInputStream("hello.txt");
        FileChannel channel = fileInputStream.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = channel.read(buffer);
        log.info("read = {}", read);
        // 这里不进行flip  似乎没问题
        buffer.flip();
        String s = new String(buffer.array(), 0, buffer.remaining());
        log.info("s = {}", s);
    }

    /**
     * fileChannel copy
     */
    @Test
    public void testFileChannelCopy() throws Exception {
        FileInputStream fis = new FileInputStream("I:\\test\\ha.jpg");
        FileOutputStream fos = new FileOutputStream("I:\\test\\new.jpg");
        FileChannel fisChannel = fis.getChannel();
        FileChannel fosChannel = fos.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (true) {
            // 清空缓存区
            buffer.clear();
            // 将数据从channel读取到buffer中
            int read = fisChannel.read(buffer);
            if (read == -1) {
                break;
            }
            // 开启读取模式
            buffer.flip();
            // 将数据从buffer写入到channel
            fosChannel.write(buffer);
        }
        fisChannel.close();
        fosChannel.close();
        log.info("复制文件完成");
    }


    /**
     * 多个buffer缓冲区
     */
    @Test
    public void testMultiBuffer() throws Exception {
        RandomAccessFile randomAccessFile1 = new RandomAccessFile("hello.txt", "rw");
        FileChannel channel1 = randomAccessFile1.getChannel();
        ByteBuffer buffer1 = ByteBuffer.allocate(4);
        ByteBuffer buffer2 = ByteBuffer.allocate(1024);
        ByteBuffer[] buffers = {buffer1, buffer2};
        // 将hello.txt文件数据读取到buffer数组中
        channel1.read(buffers);
        for (ByteBuffer buffer : buffers) {
            // 注意从buffer中读取数据时要flip
            buffer.flip();
            String str = new String(buffer.array(), 0, buffer.remaining());
            log.info("str = {}", str);
        }
        // 将buffer数组数据写入到hello2.txt文件中
        RandomAccessFile randomAccessFile2 = new RandomAccessFile("hello2.txt", "rw");
        FileChannel channel2 = randomAccessFile2.getChannel();
        channel2.write(buffers);
        channel1.close();
        channel2.close();
        log.info("执行完成");
    }


    /**
     * Channel transferFrom  and  transferTo
     */
    @Test
    public void testChannelTransferCopy() throws Exception {
        RandomAccessFile accessFile1 = new RandomAccessFile("I:\\test\\ha.jpg", "rw");
        RandomAccessFile accessFile2 = new RandomAccessFile("I:\\test\\new02.jpg", "rw");
        FileChannel channel1 = accessFile1.getChannel();
        FileChannel channel2 = accessFile2.getChannel();
        // 方式1: 使用transferFrom方法
//        channel2.transferFrom(channel1, channel1.position(), channel1.size());
        // 方式2: 使用transferTo方法
        channel1.transferTo(channel1.position(), channel1.size(), channel2);
        channel1.close();
        channel2.close();
        log.info("执行完成");
    }


}
