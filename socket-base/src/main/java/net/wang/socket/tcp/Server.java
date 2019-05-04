package net.wang.socket.tcp;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

@Slf4j
public class Server {

    public static final int PORT = 20000;

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = createServerSocket();

        initServerSocket(serverSocket);

        serverSocket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 50);

        log.info("服务端准备就绪 ： IP :: {} | PORT :: {} ", serverSocket.getInetAddress(), serverSocket.getLocalPort());

        //等待客户端连接
        while (true) {
            Socket client = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(client);
            clientHandler.start();
        }
    }

    private static ServerSocket createServerSocket() throws Exception {

        // 创建基础的ServerSocket
        ServerSocket serverSocket = new ServerSocket();

        // 绑定到本地端口20000上，并且设置当前可允许等待链接的队列为50个
        //serverSocket = new ServerSocket(PORT);

        // 等效于上面的方案，队列设置为50个
        //serverSocket = new ServerSocket(PORT, 50);

        // 与上面等同
        // serverSocket = new ServerSocket(PORT, 50, Inet4Address.getLocalHost());

        return serverSocket;
    }

    private static void initServerSocket(ServerSocket serverSocket) throws Exception {

        //是否复用未完全关闭的地址端口
        serverSocket.setReuseAddress(true);

        //设置接收缓冲区大小
        serverSocket.setReceiveBufferSize(64 * 1024 * 1024);

        //设置等待连接超时时间  一般不设置。
        //serverSocket.setSoTimeout(2000);

        //设置性能参数：短链接、延迟、带宽的相对重要性
        serverSocket.setPerformancePreferences(1, 1, 1);
    }

    private static class ClientHandler extends Thread {

        private Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();

            log.info("客户端连接 ： IP :: {}  | PORT :: {}", socket.getInetAddress(), socket.getPort());

            try {
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[256];

                int readCount = inputStream.read(buffer);
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, readCount);

                /**
                 * 读取时按照 Client 中 存入的顺序
                 */
                // byte
                byte be = byteBuffer.get();

                // char
                char c = byteBuffer.getChar();

                // int
                int i = byteBuffer.getInt();

                // bool
                boolean b = byteBuffer.get() == 1;

                // Long
                long l = byteBuffer.getLong();

                // float
                float f = byteBuffer.getFloat();

                // double
                double d = byteBuffer.getDouble();

                // String
                int pos = byteBuffer.position();
                String str = new String(buffer, pos, readCount - pos - 1);

                log.info("收到的数量 ：{} | 数据 : " +
                        "be :: {} |  " +
                        "c :: {} | " +
                        "i :: {} | " +
                        "b :: {} | " +
                        "l :: {} | " +
                        "f :: {} | " +
                        "d :: {} | " +
                        "str :: {} ", readCount, be, c, i, b, l, f, d, str);

                outputStream.write(buffer, 0, readCount);
                outputStream.close();
                inputStream.close();
            } catch (Exception e) {
                log.warn("连接异常断开");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            log.info("客户端退出 ： IP :: {}  | PORT :: {}", socket.getInetAddress(), socket.getPort());

        }
    }
}
