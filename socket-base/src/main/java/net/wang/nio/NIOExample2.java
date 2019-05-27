package net.wang.nio;


import org.junit.Test;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.LocalDateTime;
import java.util.Iterator;


@SuppressWarnings("Duplicates")
public class NIOExample2 {


    @Test
    public void send() throws IOException {
        //获取通道
        DatagramChannel datagramChannel = DatagramChannel.open();

        datagramChannel.configureBlocking(false);

        //创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        //Scanner 有BUG

        //发送数据
        buffer.put((LocalDateTime.now().toString() + "\n").getBytes());
        buffer.flip();
        datagramChannel.send(buffer, new InetSocketAddress(Inet4Address.getLocalHost(), 9999));
        buffer.clear();

        datagramChannel.close();

    }

    @Test
    public void receiver() throws IOException {
        //获取通道
        DatagramChannel datagramChannel = DatagramChannel.open();

        //设置非阻塞模式
        datagramChannel.configureBlocking(false);

        //绑定端口
        datagramChannel.bind(new InetSocketAddress(9999));

        //获取选择器
        Selector selector = Selector.open();

        //将通道注册到选择器上，并指定“监听接收事件”
        datagramChannel.register(selector, SelectionKey.OP_READ);

        //循环获取选择器上已经“准备就绪”的事件
        while (selector.select() > 0) {
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                //获取准备就绪的事件
                SelectionKey key = iterator.next();

                //如果是可读事件
                if (key.isReadable()) {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                    datagramChannel.receive(byteBuffer);

                    byteBuffer.flip();
                    System.out.println(new String(byteBuffer.array(), 0, byteBuffer.limit()));
                    byteBuffer.clear();
                }

                //移除选择键 SelectionKey
                iterator.remove();
            }
        }

        //TODO 循环关闭客户端连接
        datagramChannel.close();

    }

}
