package net.wang.socket.udp.search.client;

import lombok.extern.slf4j.Slf4j;
import net.wang.socket.udp.search.bean.ServerInfo;

@Slf4j
public class Client {
    public static void main(String[] args) {
        ServerInfo serverInfo = ClientSearcher.searchServer(10000);

        log.info("serverInfo : {}", serverInfo);
    }
}
