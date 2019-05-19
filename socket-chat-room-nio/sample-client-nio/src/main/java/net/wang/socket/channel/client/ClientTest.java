package net.wang.socket.channel.client;


import lombok.extern.slf4j.Slf4j;
import net.wang.clink.bean.ServerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ClientTest {
    public static void main(String[] args) throws IOException {

        List<TCPClient> clients = new ArrayList<>();

        //搜素服务端
        ServerInfo serverInfo = UDPSearcher.searchServer(10000);
        log.info("serverInfo : {}", serverInfo);
        if (serverInfo != null) {
            //连接TCP Server
            for (int i = 0; i < 10000; i++) {
                TCPClient client = TCPClientFactory.build(serverInfo);
                if (client != null) {
                    clients.add(client);
                }

            }
        }
        log.info("SIZE = {}", clients.size());

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        do {
            String line = input.readLine();
            for (int i = 0; i < clients.size(); i++) {
                try {
                    clients.get(i).sendData("=====> ||" + line + "||" + i);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if ("1".equalsIgnoreCase(line)) {
                for (TCPClient client : clients) {
                    client.exit();
                }
                break;
            }

        } while (true);

    }

}
