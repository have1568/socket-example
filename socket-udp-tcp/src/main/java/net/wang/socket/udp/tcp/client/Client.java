package net.wang.socket.udp.tcp.client;

import lombok.extern.slf4j.Slf4j;
import net.wang.socket.udp.tcp.bean.ServerInfo;

import java.io.IOException;

@Slf4j
public class Client {
    public static void main(String[] args) throws IOException {
        ServerInfo serverInfo = ClientSearcher.searchServer(10000);

        log.info("serverInfo : {}", serverInfo);
        if (serverInfo != null) {
            TCPClient.linkWith(serverInfo);
        }
    }
}
