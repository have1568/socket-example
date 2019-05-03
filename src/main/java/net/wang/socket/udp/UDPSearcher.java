package net.wang.socket.udp;

import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * UDPProvider 搜索者
 */
@Slf4j
public class UDPSearcher {

    public static final int LISTEN_PORT = 30000;

    public static void main(String[] args) throws Exception {

        log.info("UDPSearcher Started !");

        Listener listener = listen();
        sendBroadcast();
        System.in.read();
        listener.getDevices().forEach(device -> {
            log.info(":: {}", device);
        });
        log.info("UDPSearcher Finished !");
    }

    //监听
    public static Listener listen() throws InterruptedException {
        log.info("listener started");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, countDownLatch);
        listener.start();
        countDownLatch.await();
        return listener;

    }

    //发送广播
    public static void sendBroadcast() throws Exception {


        log.info("UDPSearcher senBroadcast Started !");
        //构建socket
        DatagramSocket datagramSocket = new DatagramSocket();

        //构建发送数据
        byte[] request = MessageBuilder.buildWithPort(LISTEN_PORT).getBytes();

        //构建发送包 将发送的数据封装到 DatagramPacket 对象中
        DatagramPacket requestPacket = new DatagramPacket(request,
                request.length);

        //获取广播地址 端口
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPacket.setPort(20000);

        //发送
        datagramSocket.send(requestPacket);

        //关闭
        datagramSocket.close();
        log.info("UDPSearcher sendBroadcast End !");

    }

    private static class Device {
        final int port;
        final String ip;
        final String sn;

        private Device(int port, String ip, String sn) {
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "port=" + port +
                    ", ip='" + ip + '\'' +
                    ", sn='" + sn + '\'' +
                    '}';
        }
    }

    private static class Listener extends Thread {
        private final int listenPort;

        private final CountDownLatch countDownLatch;
        private final List<Device> devices = new ArrayList<>();
        private boolean done = false;
        private DatagramSocket datagramSocket;

        public List<Device> getDevices() {
            return devices;
        }

        private Listener(int listenPort, CountDownLatch countDownLatch) {
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            countDownLatch.countDown();
            try {

                datagramSocket = new DatagramSocket(listenPort);

                while (!done) {
                    //构建接收实体
                    final byte[] bytes = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

                    //接收
                    datagramSocket.receive(packet);
                    //输出收到信息的发送者信息（Provider 响应的数据）
                    int length = packet.getLength();
                    String hostAddress = packet.getAddress().getHostAddress();
                    int port = packet.getPort();
                    String s = new String(packet.getData(), 0, length);
                    log.info("response info ===>> IP :: {} | PORT :: {} | DATA_LEN :: {} | DATA :: {} ",
                            hostAddress,
                            port,
                            length,
                            s);

                    log.info("UDPSearcher Finished !");
                    //关闭
                    datagramSocket.close();

                    String sn = MessageBuilder.parseSN(s);
                    if (sn != null) {
                        devices.add(new Device(port, hostAddress, sn));
                    }
                }
            } catch (Exception e) {

            } finally {
                close();
            }
        }

        private void close() {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }

    }
}
