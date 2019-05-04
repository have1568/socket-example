package net.wang.socket.example;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class SocketServer {

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(2000);
        log.info("服务端准备就绪，并进入后续流程~");
        log.info("服务端信息 ：IP {} PORT {}", serverSocket.getInetAddress(), serverSocket.getLocalPort());

        //等待客户端连接
        for (; ; ) {
            Socket accept = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(accept);
            clientHandler.start();
        }

    }

    private static class ClientHandler extends Thread {

        private Socket socket;
        private boolean flag;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            log.info("客户端信息 ：IP {} PORT {}", socket.getLocalAddress(), socket.getPort());
            try {
                PrintStream socketOut = new PrintStream(socket.getOutputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                flag = true;

                do {

                    String line = reader.readLine();
                    if ("BYE".equalsIgnoreCase(line)) {
                        flag = false;
                        log.info("close flag ===<<  {}", line);
                        socketOut.println(line); //回传BYE字符串
                    } else {
                        log.info("服务端接收的数据信息  ====>> ：s = {} ,length = {}", line, line.length());
                        socketOut.println("length = " + line.length());
                    }

                } while (flag);
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            log.info("客户端信息 closed ：IP {} PORT {}", socket.getLocalAddress(), socket.getPort());
        }
    }
}
