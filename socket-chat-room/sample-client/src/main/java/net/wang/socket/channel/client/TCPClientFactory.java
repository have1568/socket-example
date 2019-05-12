package net.wang.socket.channel.client;

import net.wang.clink.bean.ServerInfo;

import java.io.IOException;

public class TCPClientFactory {

    public static TCPClient build(ServerInfo serverInfo) {
        try {
            TCPClient client = TCPClient.link(serverInfo);
            return client;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
