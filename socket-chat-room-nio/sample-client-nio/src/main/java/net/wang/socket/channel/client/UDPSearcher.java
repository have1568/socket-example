package net.wang.socket.channel.client;

import lombok.extern.slf4j.Slf4j;
import net.wang.clink.bean.ServerInfo;
import net.wang.clink.constants.UDPConstants;
import net.wang.clink.utils.ByteUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class UDPSearcher {

    public static final int LISTEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE;

    /**
     * 在局域网里搜索约定的 UDPServer 关键点 <code>sendBroadcast()</code>
     *
     * @param timeout
     * @return
     */
    public static ServerInfo searchServer(int timeout) {

        log.info("UDPSearcher Started !");

        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listener = null;
        try {
            listener = listen(receiveLatch);
            sendBroadcast();
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("UDPSearcher Finished !");
        if (listener == null) {
            return null;
        }
        List<ServerInfo> serverInfos = listener.getServerAndClose();

        if (serverInfos.isEmpty()) {
            return null;
        }
        return serverInfos.get(0);

    }

    /**
     * 在局域网了广播地址发送约定的数据，等待服务端 UDP 回送 TCP 端口数据
     *
     * @throws IOException
     */
    private static void sendBroadcast() throws IOException {
        log.info("UDPSearcher sendBroadcast started.");

        // 作为搜索方，让系统自动分配端口
        DatagramSocket ds = new DatagramSocket();

        // 构建一份请求数据
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        // 头部
        byteBuffer.put(UDPConstants.HEADER);
        // CMD命名
        byteBuffer.putShort((short) 1);
        // 回送端口信息
        byteBuffer.putInt(LISTEN_PORT);

        byteBuffer.put("search token = xxx".getBytes());
        // 直接构建packet
        DatagramPacket requestPacket = new DatagramPacket(byteBuffer.array(),
                byteBuffer.position() + 1);
        // 广播地址
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        // 设置服务器端口
        requestPacket.setPort(UDPConstants.PORT_SERVER);

        // 发送
        ds.send(requestPacket);
        ds.close();

        // 完成
        log.info("UDPSearcher sendBroadcast finished.");
    }

    /**
     * 监听UDPServer 响应 TCP端口数据的端口 {@link UDPConstants#PORT_CLIENT_RESPONSE }
     *
     * @param receiveLatch 接收到数据栅栏
     * @return
     * @throws InterruptedException
     */
    private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {

        log.info("Listen Started !");
        CountDownLatch startLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, startLatch, receiveLatch);
        listener.start();
        startLatch.await();
        return listener;
    }

    private static class Listener extends Thread {
        private final int listenPort;
        private final CountDownLatch startDownLatch;
        private final CountDownLatch receiveDownLatch;
        private final List<ServerInfo> serverInfoList = new ArrayList<>();
        private final byte[] buffer = new byte[128];
        private final int minLen = UDPConstants.HEADER.length + 2 + 4;
        private boolean done = false;
        private DatagramSocket ds = null;

        /**
         * 监听UDPServer 响应 TCP端口数据的端口  UDPConstants.PORT_CLIENT_RESPONSE
         *
         * @param listenPort       约定的响应数据端口号 {@link UDPConstants#PORT_CLIENT_RESPONSE}
         * @param startDownLatch   监听栅栏
         * @param receiveDownLatch 接收到数据栅栏
         */
        private Listener(int listenPort, CountDownLatch startDownLatch, CountDownLatch receiveDownLatch) {
            super();
            this.listenPort = listenPort;
            this.startDownLatch = startDownLatch;
            this.receiveDownLatch = receiveDownLatch;
        }

        @Override
        public void run() {
            super.run();

            // 通知已启动
            startDownLatch.countDown();
            try {
                // 监听回送端口
                ds = new DatagramSocket(listenPort);
                // 构建接收实体
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                while (!done) {
                    // 接收
                    ds.receive(receivePack);

                    // 打印接收到的信息与发送者的信息
                    // 发送者的IP地址
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength();
                    byte[] data = receivePack.getData();
                    boolean isValid = dataLen >= minLen
                            && ByteUtils.startsWith(data, UDPConstants.HEADER);

                    log.info("UDPSearcher receive form ip:" + ip
                            + "\tport:" + port + "\tdataValid:" + isValid);

                    if (!isValid) {
                        // 无效继续
                        continue;
                    }

                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, UDPConstants.HEADER.length, dataLen);
                    final short cmd = byteBuffer.getShort();
                    final int serverPort = byteBuffer.getInt();
                    if (cmd != 2 || serverPort <= 0) {
                        log.info("UDPSearcher receive cmd:" + cmd + "\tserverPort:" + serverPort);
                        continue;
                    }

                    //有效、构建包含TCP连接端口的 ServerInfo
                    String sn = new String(buffer, minLen, dataLen - minLen);
                    ServerInfo info = new ServerInfo(serverPort, ip, sn);
                    serverInfoList.add(info);
                    // 成功接收到数据，放行
                    receiveDownLatch.countDown();
                }
            } catch (Exception ignored) {
            } finally {
                close();
            }
            log.info("UDPSearcher listener finished.");
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        List<ServerInfo> getServerAndClose() {
            done = true;
            close();
            return serverInfoList;
        }
    }
}
