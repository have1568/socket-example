package net.wang.nio;


import org.junit.Test;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Iterator;


@SuppressWarnings("Duplicates")
public class NIOExample {


    @Test
    public void client() throws IOException {
        //获取通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(Inet4Address.getLocalHost(), 9999));

        socketChannel.configureBlocking(false);

        //创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //Scanner 有BUG

        //发送数据
        buffer.put((LocalDateTime.now().toString() + "\n").getBytes());
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();


        socketChannel.close();

    }

    @Test
    public void server() throws IOException {
        //获取通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //设置非阻塞模式
        serverSocketChannel.configureBlocking(false);

        //绑定端口
        serverSocketChannel.bind(new InetSocketAddress(9999));

        //获取选择器
        Selector selector = Selector.open();

        //将通道注册到选择器上，并指定“监听接收事件”
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //循环获取选择器上已经“准备就绪”的事件
        while (selector.select() > 0) {
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                //获取准备就绪的事件
                SelectionKey key = iterator.next();

                //判断具体是什么事件准备就绪
                if (key.isAcceptable()) {

                    SocketChannel socketChannel = serverSocketChannel.accept();

                    //设置为非阻塞模式
                    socketChannel.configureBlocking(false);

                    //注册可读事件到选择器上（可以新建一个专用的选择器）
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }

                //如果是可读事件
                if (key.isReadable()) {
                    //获取客户端连接
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    int len;
                    while ((len = socketChannel.read(readBuffer)) != -1) {
                        readBuffer.flip();
                        System.out.println(new String(readBuffer.array(), 0, len, Charset.forName("UTF-8")));
                        readBuffer.clear();
                    }
                }

                //移除选择键 SelectionKey
                iterator.remove();
            }
        }

        //TODO 循环关闭客户端连接
        serverSocketChannel.close();

    }

}
