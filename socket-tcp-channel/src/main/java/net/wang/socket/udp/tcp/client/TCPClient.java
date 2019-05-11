package net.wang.socket.udp.tcp.client;

import lombok.extern.slf4j.Slf4j;
import net.wang.clink.net.qiujuer.clink.utils.CloseUtils;
import net.wang.socket.udp.tcp.bean.ServerInfo;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class TCPClient {

    private static ExecutorService pool = Executors.newFixedThreadPool(3);
    boolean readFlag = true;

    public void linkWith(ServerInfo info) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(3000);

        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()), 3000);

        log.info("客户端信息 : IP :: {} \tPORT :: {}", socket.getLocalAddress(), socket.getLocalPort());
        log.info("服务端信息 : IP :: {} \tPORT :: {}", socket.getInetAddress(), socket.getPort());

        try {
            //读取数据
            pool.execute(() -> {
                InputStream inputStream = null;
                try {
                    inputStream = socket.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    do {
                        try {
                            String line = reader.readLine();
                            if (line == null) {
                                log.warn("连接已关闭，无法读取数据！");
                                exit();
                                break;
                            }
                            log.info("TCPClient Read Data :: {}", line);
                        } catch (SocketTimeoutException socketTimeoutException) {
                            continue;
                        }

                    } while (readFlag);
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("TCPClient 连接异常断开：");
                } finally {
                    CloseUtils.close(inputStream, socket);
                }
            });

            //发送数据
            sandData(socket);
        } catch (Exception e) {
            log.info("TCP Client Error", e);
        }
    }

    private void sandData(Socket client) throws IOException {

        //获取键盘输入流并构建缓冲器
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        //获得 Socket Client 的输出流，用于向服务端发送数据
        PrintStream output = new PrintStream(client.getOutputStream());
        do {
            String line = input.readLine();
            output.println(line);

            if ("1".equalsIgnoreCase(line)) {
                exit();
                CloseUtils.close(output, client);
                break;
            }

        } while (true);
        CloseUtils.close(output);
    }

    private void exit() {
        readFlag = false;
        pool.shutdownNow();
    }
}
