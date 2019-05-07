package net.wang.socket.udp.search.server;

import lombok.extern.slf4j.Slf4j;
import net.wang.socket.udp.search.constants.TCPConstants;

@Slf4j
public class Server {
    public static void main(String[] args) {

        //启动UDPServer，监听客户端，针对合法的连接将  TCPConstants.PORT_SERVER 响应给客户端
        ServerProvider.start(TCPConstants.PORT_SERVER);

        //读取键盘输入流，关闭UDPServer
        try {
            System.in.read();
        } catch (Exception e) {
            log.error("", e);
        }
        ServerProvider.stop();
    }
}
