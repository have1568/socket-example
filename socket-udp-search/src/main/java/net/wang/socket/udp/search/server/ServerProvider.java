package net.wang.socket.udp.search.server;

import lombok.extern.slf4j.Slf4j;
import net.wang.clink.net.qiujuer.clink.utils.ByteUtils;
import net.wang.socket.udp.search.constants.UDPConstants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

@Slf4j
public class ServerProvider {


    private static Provider PROVIDER_INSTANCE;

    /**
     * @param portServer
     */
    public static void start(int portServer) {
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, portServer);
        provider.start();
        PROVIDER_INSTANCE = provider;
    }

    public static void stop() {

        if (PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    private static class Provider extends Thread {

        private final String sn;
        //TCP的端口号
        private final int port;
        private boolean done = false;
        private DatagramSocket datagramSocket = null;
        // 存储消息的Buffer
        final byte[] buffer = new byte[128];


        private Provider(String sn, int port) {
            this.sn = sn;
            this.port = port;
        }


        @Override
        public void run() {

            log.info("UDPProvider Started !");
            try {
                //服务端监听 UDPConstants.PORT_SERVER 端口30201
                datagramSocket = new DatagramSocket(UDPConstants.PORT_SERVER);

                while (!done) {

                    //构建接收实体
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    //接收
                    datagramSocket.receive(packet);

                    //发送者信息
                    String clientIP = packet.getAddress().getHostAddress();
                    int clientPort = packet.getPort();
                    byte[] clientData = packet.getData();
                    int clientDataLength = packet.getLength();
                    int minLen = UDPConstants.HEADER.length + 2 + 4;
                    //验证是否以满足 UDPConstants.HEADER + 2个字节short类型的cmd口令+4个字节的回送端口port 和 约定的header开头
                    boolean isValid = clientDataLength >= minLen
                            && ByteUtils.startsWith(clientData, UDPConstants.HEADER);

                    String data = new String(clientData, minLen, clientDataLength - minLen);
                    log.info("client info >> \nIP :: {}  \nPORT :: {}  \nDATA_LEN :: {}  \nDATA :: {}  \nisValid :: {}",
                            clientIP,
                            clientPort,
                            clientDataLength,
                            data,
                            isValid);

                    if (!isValid) {
                        log.warn("Header is not valid !");
                        continue;
                    }


                    // 解析命令与回送端口
                    int index = UDPConstants.HEADER.length; //起始地址
                    short cmd = (short) ((clientData[index++] << 8) | (clientData[index++] & 0xff));
                    int responsePort = (((clientData[index++]) << 24) |
                            ((clientData[index++] & 0xff) << 16) |
                            ((clientData[index++] & 0xff) << 8) |
                            ((clientData[index] & 0xff)));


                    // 判断合法性 cmd = 1 搜索命令
                    if (cmd == 1 && responsePort > 0) {
                        // 构建一份回送数据,包含TCP连接的端口号
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short) 2); //回送指令
                        byteBuffer.putInt(port); //TCP 地址端口号
                        byteBuffer.put(sn.getBytes());
                        int len = byteBuffer.position();
                        // 直接根据发送者构建一份回送信息
                        DatagramPacket responsePacket = new DatagramPacket(buffer,
                                len,
                                packet.getAddress(),
                                responsePort);
                        datagramSocket.send(responsePacket);
                        log.info("ServerProvider response to:" + clientIP + "\tport:" + responsePort + "\tdataLen:" + len);
                    } else {
                        log.info("ServerProvider receive cmd nonsupport; cmd:" + cmd + "\tport:" + port);
                    }

                }

            } catch (Exception e) {
                //e.printStackTrace();
            } finally {
                close();
            }
        }

        //关闭连接
        private void close() {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }

        //标记退出
        public void exit() {
            done = true;
            close();
        }
    }
}