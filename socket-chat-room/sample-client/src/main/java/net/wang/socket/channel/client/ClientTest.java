package net.wang.socket.channel.client;


import lombok.extern.slf4j.Slf4j;
import net.wang.clink.bean.ServerInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ClientTest {
    private static ExecutorService pool = new ThreadPoolExecutor(100, 500, 5, TimeUnit.SECONDS, new ArrayBlockingQueue(2000));

    public static void main(String[] args) throws IOException {

        List<TCPClient> clients = new ArrayList<>();

        //搜素服务端
        ServerInfo serverInfo = UDPSearcher.searchServer(10000);
        log.info("serverInfo : {}", serverInfo);
        if (serverInfo != null) {
            //连接TCP Server
            for (int i = 0; i < 1000; i++) {
                pool.execute(() -> {
                    TCPClient client = TCPClientFactory.build(serverInfo);
                    if (client != null) {
                        clients.add(client);
                    }
                });
            }
        }
        log.info("SIZE = {}", clients.size());

        int read = System.in.read();
        log.info("Read Data : {} ", read);

        ((Runnable) () -> {
            for (int i = 0; i < clients.size(); i++) {
                try {
                    clients.get(i).sendData("hello" + i);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).run();

    }

}
