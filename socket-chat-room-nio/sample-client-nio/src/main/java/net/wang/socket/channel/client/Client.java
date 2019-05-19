package net.wang.socket.channel.client;


import lombok.extern.slf4j.Slf4j;
import net.wang.clink.bean.ServerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class Client {
    public static void main(String[] args) throws IOException, InterruptedException {

        TCPClient client = null;

        //搜素服务端
        ServerInfo serverInfo = UDPSearcher.searchServer(10000);
        log.info("serverInfo : {}", serverInfo);
        if (serverInfo != null) {
            client = TCPClientFactory.build(serverInfo);

        }

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        do {
            String line = input.readLine();
            client.sendData(line);

            if ("1".equalsIgnoreCase(line)) {
                break;
            }

        } while (true);
        client.exit();

    }

}
