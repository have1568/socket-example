package net.wang.socket.channel.server;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class TCPServer implements SocketTCPServer {

    private final int port;
    private final AtomicBoolean isRun = new AtomicBoolean(true);

    private List<SocketChannel> socketChannels = new CopyOnWriteArrayList<>();
    private ExecutorService listenerHandlerPool = ThreadPoolBuilder.buildFixPool("Listener-Handler-Pool", 1);
    private ExecutorService readPool = ThreadPoolBuilder.build("Read-Handler-Pool", 5, 10);
    private ExecutorService writePool = ThreadPoolBuilder.build("Write-Handler-Pool", 2, 5);

    private Selector selector;

    public TCPServer(int port) {
        this.port = port;
    }

    /**
     * 启动无异常则启动成功
     * 异步接收连接事件
     *
     * @return
     */
    @Override
    public boolean start() {
        try {
            //异步接收连接事件
            selector = Selector.open();
            //打开Selector(选择器)
            //创建一个非阻塞serverSocket
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            //配置 serverSocketChannel 为非阻塞IO
            serverSocketChannel.configureBlocking(false);
            //绑定 server IP 和 端口
            serverSocketChannel.bind(new InetSocketAddress(port));
            //注册监听连接事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            log.info("服务器信息：{}", serverSocketChannel.getLocalAddress().toString());

            listenerHandlerPool.execute(() -> listenHandler(selector, serverSocketChannel));

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void listenHandler(Selector selector, ServerSocketChannel serverSocketChannel) {
        do {
            try {
                if (selector.select() == 0) {
                    if (!isRun.get()) {
                        break;
                    }
                    continue;
                }
                //获取当前就绪的事件
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    //8. 获取准备“就绪”的是事件
                    SelectionKey key = iterator.next();

                    //9. 判断具体是什么事件准备就绪
                    if (key.isAcceptable()) {
                        //10. 若“接收就绪”，获取客户端连接
                        SocketChannel socketChannel = serverSocketChannel.accept();

                        //11. 切换非阻塞模式
                        socketChannel.configureBlocking(false);

                        //12. 将该通道注册到选择器上
                        socketChannel.register(selector, SelectionKey.OP_READ);

                        //将准备就绪的通道加入集合
                        socketChannels.add(socketChannel);
                    }

                    if (key.isReadable()) {

                        //13. 获取当前选择器上“读就绪”状态的通道
                        SocketChannel socketChannel = (SocketChannel) key.channel();

                        //处理接收到的消息
                        readHandler(socketChannel);
                    }
                    //15. 取消选择键 SelectionKey
                    iterator.remove();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        while (isRun.get());
    }

    @Override
    public void readHandler(SocketChannel socketChannel) {
        if (!isRun.get()) {
            return;
        }
        readPool.execute(() -> {
            synchronized (Object.class) {
                try {
                    //14. 读取数据
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    int read = 0;
                    while ((read = socketChannel.read(byteBuffer)) > 0) {
                        byteBuffer.flip();
                        String str = new String(byteBuffer.array(), 0, read - 1).replaceAll("\n|\r", "");
                        log.info("Read Data :: {} length :: {}", str, str.length());
                        byteBuffer.clear();
                        onNewMessage(socketChannel, str);
                    }
                } catch (Exception e) {

                }
            }
        });

    }

    @Override
    public void writeHandler(SocketChannel socketChannel, String msg) {
        if (!isRun.get()) {
            return;
        }

        writePool.execute(() -> {
            final ByteBuffer byteBuffer = ByteBuffer.allocate(256);
            byteBuffer.clear();
            byteBuffer.put((msg + "\n").getBytes());
            // 读取模式
            byteBuffer.flip();
            //log.info("Write Data :: {}", msg);
            while (byteBuffer.hasRemaining()) {
                try {
                    int len = socketChannel.write(byteBuffer);
                    // len = 0 合法
                    if (len < 0) {
                        System.out.println("客户端已无法发送数据！");
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });


    }


    //关闭由 Server 控制
    @Override
    public void stop() {
        synchronized (TCPServer.class) {
            isRun.compareAndSet(true, false);
            //唤醒 selector
            selector.wakeup();
            try {
                selector.close();
                for (SocketChannel socketChannel : socketChannels) {
                    socketChannel.close();
                }
                socketChannels.clear();
                listenerHandlerPool.shutdownNow();
                readPool.shutdownNow();
                writePool.shutdownNow();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void onNewMessage(SocketChannel client, String message) throws IOException {
        log.info("size = {} ,data = {}", socketChannels.size(), message);
        for (SocketChannel socketChannel : socketChannels) {
            if (!client.equals(socketChannel)) {
                writeHandler(socketChannel, message);
            }
        }
    }

    /**
     * 循环向已连接的客户端发送消息，实现类似广播的功能
     *
     * @param message
     */
    @Override
    public void broadcast(String message) {
        log.info("size = {} ,data = {}", socketChannels.size(), message);
        for (SocketChannel socketChannel : socketChannels) {
            writeHandler(socketChannel, message);
        }
    }
}
