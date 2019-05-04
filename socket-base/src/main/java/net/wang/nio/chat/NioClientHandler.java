package net.wang.nio.chat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

@SuppressWarnings("Duplicates")
public class NioClientHandler implements Runnable {

    private Selector selector;

    public NioClientHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            //6、循环等待新连接
            boolean flag = true;
            while (flag) {
                int readyChannels = selector.select(); //返回可用channel 数量
                if (readyChannels < 0) {
                    continue;
                }
                flag = true;

                //获取可用channel集合
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    //获取 SelectionKey 实例
                    SelectionKey selectionKey = iterator.next();
                    //移除当前实例
                    iterator.remove();
                    //7、根据就绪状态，调用对应的方法处理业务逻辑

                    if (selectionKey.isReadable()) {
                        readHandler(selector, selectionKey);
                    }
                }
            }
        } catch (Exception e) {

        }


    }

    /**
     * 可读事件处理器
     *
     * @param
     * @throws IOException
     */
    public void readHandler(Selector selector, SelectionKey selectionKey) throws IOException {
        //要从 selectionKeys中获取已经就绪的channel
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        //创建 buffer
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        //循环读取客户端请求消息
        String response = "";
        while (socketChannel.read(byteBuffer) > 0) {
            //切换buffer为读模式
            byteBuffer.flip();

            //读取 buffer 中的内容
            response += Charset.forName("UTF-8").decode(byteBuffer);

        }

        //将channel 再次注册到 selector 上，监听他的可读事件
        socketChannel.register(selector, SelectionKey.OP_READ);

        //将客户端发送的请求消息广播给其他客户端
        if (response.length() > 0) {
            System.out.println("::" + response);
        }
    }
}
