package net.wang.socket.udp.tcp.server;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class TCPServer {

    private final int port;
    private ClientListener clientListener;

    public TCPServer(int port) {
        this.port = port;
    }

    /**
     * 启动无异常则启动成功
     *
     * @return
     */
    public boolean start() {
        ClientListener listener;
        try {
            listener = this.clientListener = new ClientListener(port);
            listener.start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void stop(){
        if (clientListener != null) {
            clientListener.exit();
        }
    }


    /**
     * TCP 服务端监听器
     */
    private static class ClientListener extends Thread {

        private ServerSocket serverSocket;
        private boolean flag = true;


        private ClientListener(int port) throws IOException {
            //构建 TCP 服务端对象
            serverSocket = new ServerSocket(port);
        }

        @Override
        public void run() {
            super.run();
            do {
                try {
                    // 循环接收客户端连接
                    Socket client = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(client);
                    clientHandler.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (flag);

            log.info("TCP 服务端已关闭");
        }

        private void exit()  {
            flag = false;
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 客户端消息接收处理器
     */
    private static class ClientHandler extends Thread {

        private Socket socket;  //Socket TCP 客户端
        private boolean flag = true;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            log.info("新的客户端连接 ：IP :: {} || PORT :: {}", socket.getInetAddress(), socket.getPort());

            try {
                //获取打印流，用于数据输出；服务器回送数据使用
                PrintStream printStream = new PrintStream(socket.getOutputStream());

                //获取输入流，用于接收数据
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                do {

                    String line = bufferedReader.readLine();
                    if ("bye".equalsIgnoreCase(line)) {
                        flag = false;
                        log.info("===> BYE");
                        printStream.println("BYE");
                    } else {
                        log.info("input data :: {}", line);
                        printStream.println("DATA LEN = " + line.length());
                    }

                } while (flag);

                bufferedReader.close();
                printStream.close();
            } catch (Exception e) {

                log.error("", e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            log.info("客户端已推出 ：IP :: {} || PORT :: {}", socket.getInetAddress(), socket.getPort());
        }
    }

}
