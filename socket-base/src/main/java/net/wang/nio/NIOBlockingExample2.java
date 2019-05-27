package net.wang.nio;


import org.junit.Test;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


@SuppressWarnings("Duplicates")
public class NIOBlockingExample2 {

    @Test
    public void client() throws IOException {
        //获取通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(Inet4Address.getLocalHost(), 9999));
        FileChannel fileChannel = FileChannel.open(Paths.get("e:/1.txt"), StandardOpenOption.READ);

        //创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //读取本地文件并发送到服务端
        while (fileChannel.read(buffer) != -1) {
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }
        //发送完成，关闭通道，否则一致会阻塞
        socketChannel.shutdownOutput();

        //接收反馈
        int len = 0;
        while ((len = socketChannel.read(buffer)) != -1) {
            buffer.flip();
            System.out.println(new String(buffer.array(), 0, len, Charset.forName("UTF-8")));
        }


        socketChannel.close();
        fileChannel.close();
    }

    @Test
    public void server() throws IOException {
        //获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        FileChannel fileChannel = FileChannel.open(Paths.get("e:/1-rc.txt"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);

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
        buffer.put("接收完成".getBytes());
        buffer.flip();
        socketChannel.write(buffer);

        socketChannel.close();
        fileChannel.close();
        serverSocketChannel.close();

    }

}
