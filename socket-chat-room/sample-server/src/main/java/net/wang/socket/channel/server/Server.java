package net.wang.socket.channel.server;

import lombok.extern.slf4j.Slf4j;
import net.wang.clink.constants.TCPConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class Server {
    public static void main(String[] args) throws IOException {

        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSuccess = tcpServer.start();

        if (!isSuccess) {
            log.info("TCP Server Start Fail !");
            return;
        }
        //启动UDPServer，监听客户端，针对合法的连接将  TCPConstants.PORT_SERVER 响应给客户端
        ServerProvider.start(TCPConstants.PORT_SERVER);

        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        do {
            //循环读取键盘输入流
            line = reader.readLine();
            tcpServer.broadcast(line);
        } while (!"1".equalsIgnoreCase(line));

        ServerProvider.stop();
        log.info("ServerProvider Closed");
        tcpServer.stop();
        log.info("TCPServer Closed");
    }
}
