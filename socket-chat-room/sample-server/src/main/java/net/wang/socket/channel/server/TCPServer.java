package net.wang.socket.channel.server;

import lombok.extern.slf4j.Slf4j;
import net.wang.socket.channel.server.handle.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class TCPServer implements ClientHandlerCallback {

    private final int port;
    private List<ClientHandler> clientHandlerList = new CopyOnWriteArrayList<>();
    private ExecutorService pool = new ThreadPoolExecutor(10, 30, 5, TimeUnit.SECONDS, new ArrayBlockingQueue(2000));

    private ServerSocket serverSocket;

    private boolean flag = true;

    public TCPServer(int port) {
        this.port = port;
    }

    /**
     * 启动无异常则启动成功
     *
     * @return
     */
    public boolean start() {
        try {
            serverSocket = new ServerSocket(port);
            pool.execute(() -> {
                do {
                    try {
                        // 循环接收客户端连接
                        Socket client = serverSocket.accept();
                        ClientHandler clientHandler = new ClientHandler(client, this);
                        //读取并打印
                        clientHandlerList.add(clientHandler);
                        pool.execute(() -> clientHandler.readAndPrint());
                    } catch (IOException e) {
                        log.info("客户端连接异常");
                        //e.printStackTrace();
                    }
                } while (flag);
                log.info("TCP 服务端已关闭");
            });
        } catch (IOException e) {
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    //关闭由 Server 控制
    public void stop() {
        synchronized (ClientHandler.class) {
            flag = false;
            try {
                serverSocket.close();
                for (ClientHandler clientHandler : clientHandlerList) {
                    clientHandler.exit();
                }
                pool.shutdownNow();
                clientHandlerList.clear();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onNewMessage(ClientHandler clientHandler, String message) {
        pool.execute(() -> {
            for (ClientHandler handler : clientHandlerList) {
                if (!clientHandler.equals(handler)) {
                    handler.send(message);
                }
            }
        });
    }

    /**
     * 循环向已连接的客户端发送消息，实现类似广播的功能
     *
     * @param line
     */
    public void broadcast(String line) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(line);
        }
    }
}
