package net.wang.nio;


import org.junit.Test;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * <p>一、使用NIO完成网络通信的三个核心
 *
 * <p>1、通道（Channel）:负责连接 {@link java.nio.channels.Channel}
 *
 * <p>2、缓冲器（Buffer）：负责数据存取
 *
 * <p>3、选择器（Selector）：是SelectableChannel的多路复用器，用于监控SelectableChannel的IO状况 {@link java.nio.channels.SelectableChannel}
 */
public class NIOBlockingExample {

    @Test
    public void client() throws IOException {
        //获取通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(Inet4Address.getLocalHost(), 9999));
        FileChannel fileChannel = FileChannel.open(Paths.get("e:/test.mp4"), StandardOpenOption.READ);

        //创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //读取本地文件并发送到服务端
        while (fileChannel.read(buffer) != -1) {
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }

        socketChannel.close();
        fileChannel.close();
    }

    @Test
    public void server() throws IOException {
        //获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        FileChannel fileChannel = FileChannel.open(Paths.get("e:/test-rc.mp4"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        //绑定端口
        serverSocketChannel.bind(new InetSocketAddress(9999));

        //获取连接
        SocketChannel socketChannel = serverSocketChannel.accept();

        //创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //接收客户端数据，并保存到本能
        while (socketChannel.read(buffer) != -1) {
            buffer.flip();
            fileChannel.write(buffer);
            buffer.clear();
        }

        socketChannel.close();
        fileChannel.close();
        serverSocketChannel.close();

    }

}
