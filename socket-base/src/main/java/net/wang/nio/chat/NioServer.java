package net.wang.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

@SuppressWarnings("Duplicates")
public class NioServer {

    /**
     * 启动
     */
    public void start() throws IOException {
        //1、创建  Selector
        Selector selector = Selector.open();

        //2、通过 ServerSocketChannel 从创建 channel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        //3、为 channel 绑定端口
        serverSocketChannel.bind(new InetSocketAddress(8000));

        //4、设置 channel 为非阻塞模式
        serverSocketChannel.configureBlocking(false);

        //5、将 channel 注册到 selector 上，监听连接事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("server start success !");

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
                if (selectionKey.isAcceptable()) {
                    acceptHandler(selector, serverSocketChannel);
                }

                if (selectionKey.isReadable()) {
                    readHandler(selector, selectionKey);
                }
            }
        }
    }


    /**
     * 接入事件处理器
     *
     * @param selector
     * @param serverSocketChannel
     * @throws IOException
     */
    public void acceptHandler(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {
        //如果是接入事件，创建 socketChannel
        SocketChannel socketChannel = serverSocketChannel.accept();

        //将 socketChannel 设置为非阻塞工作模式
        socketChannel.configureBlocking(false);

        //将channel 注册到selector上，监听可读事件
        socketChannel.register(selector, SelectionKey.OP_READ);

        //回复客户端消息
        socketChannel.write(Charset.forName("UTF-8").encode("你与聊天室里的其他人都不是朋友关系，请注意隐私安全！"));
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
        String request = "";
        while (socketChannel.read(byteBuffer) > 0) {
            //切换buffer为读模式
            byteBuffer.flip();

            //读取 buffer 中的内容
            request += Charset.forName("UTF-8").decode(byteBuffer);

        }

        //将channel 再次注册到 selector 上，监听他的可读事件
        socketChannel.register(selector, SelectionKey.OP_READ);

        //将客户端发送的请求消息广播给其他客户端
        if (request.length() > 0) {
            System.out.println("::" + request);
            broadCast(selector, socketChannel, request);
        }
    }

    private void broadCast(Selector selector, SocketChannel sourceChannel, String request) {
        //获取所有已接入的 channel
        Set<SelectionKey> selectionKeys = selector.keys(); //获取所有就绪状态的channel
        selectionKeys.forEach(selectionKey -> {
            Channel targetChannel = selectionKey.channel();
            if (targetChannel instanceof SocketChannel && targetChannel != sourceChannel) {
                try {
                    ((SocketChannel) targetChannel).write(Charset.forName("UTF-8").encode(request));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        //循环发送消息
    }

    public static void main(String[] args) throws IOException {
        NioServer server = new NioServer();
        server.start();
    }
}
