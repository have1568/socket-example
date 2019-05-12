package net.wang.socket.channel.client;

import lombok.extern.slf4j.Slf4j;
import net.wang.clink.bean.ServerInfo;
import net.wang.clink.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class TCPClient {

    private static final ExecutorService pool = Executors.newSingleThreadExecutor();

    static boolean readFlag = true;

    private final PrintStream output;

    private final Socket socket;

    public TCPClient(Socket socket) throws IOException {
        this.socket = socket;
        this.output = new PrintStream(socket.getOutputStream());
    }


    public static TCPClient link(ServerInfo info) throws IOException {

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
                                break;
                            }
                            log.info("TCPClient Read Data :: {}", line);
                        } catch (SocketTimeoutException socketTimeoutException) {
                            log.warn("Time Out !");
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

        } catch (Exception e) {
            log.info("TCP Client Error", e);
        }

        return new TCPClient(socket);
    }


    public void exit() {
        readFlag = false;
        pool.shutdownNow();
        CloseUtils.close(output, socket);
    }

    public void sendData(String message) throws IOException {
        output.println(message);
    }


}
