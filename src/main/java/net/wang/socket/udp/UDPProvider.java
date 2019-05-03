package net.wang.socket.udp;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * UDPProvider 提供者，用于提供服务
 *
 * <p> Java 实现 Socket UDP 接收与发送的流程，IP和端口 可以在 DatagramSocket 实例和 DatagramPacket 实例其中一个指定
 *
 * <p> 接收流程：
 * <pre>
 *     //1、创建一个DatagramSocket实例
 *     {@link DatagramSocket#DatagramSocket()} 创建一个DatagramSocket实例，并将该对象绑定到本机默认IP地址、本机所有可用端口中随机选择的某个端口。
 *     {@link DatagramSocket#DatagramSocket(int port)} 创建一个DatagramSocket实例，并将该对象绑定到本机默认IP地址、指定端口。
 *     {@link DatagramSocket#DatagramSocket(int port, java.net.InetAddress laddr)}  创建一个DatagramSocket实例，并将该对象绑定到指定IP地址、指定端口。
 *
 *     DatagramSocket datagramSocket = new DatagramSocket(20000);
 *
 *     //2、构建接收实体（DatagramSocket实例已包含接收监听的地址和端口，DatagramPacket实例不需要指定IP和端口）
 *     final byte[] bytes = new byte[1024];
 *     DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
 *
 *     //3、接收 {@link DatagramSocket#receive(java.net.DatagramPacket p)} 底层无限循环保持线程不停止，最终调用的是Native方法  AbstractPlainDatagramSocketImpl#receive(java.net.DatagramPacket)
 *     datagramSocket.receive(packet)
 *
 *     //4、根据一定条件关闭 连接，也是调用 Native 关闭连接
 *     datagramSocket.close();
 * </pre>
 *
 * <p> 发送流程：
 * <pre>
 *     //1、创建一个DatagramSocket实例
 *     DatagramSocket datagramSocket = new DatagramSocket(20000);
 *
 *     //2、构建DatagramPacket实例
 *     final byte[] bytes = new byte[1024];
 *     DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
 *
 *     //3、发送（发送完成后不需要关闭）
 *     datagramSocket.send(packet);
 * </pre>
 */
@Slf4j
public class UDPProvider {

    public static void main(String[] args) throws IOException {

        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();

        //读取任意字符退出
        System.in.read();
        provider.exit();
    }

    private static class Provider extends Thread {

        private final String sn;
        private boolean done = false;
        private DatagramSocket datagramSocket = null;


        private Provider(String sn) {
            this.sn = sn;
        }


        @Override
        public void run() {

            log.info("UDPProvider Started !");
            try {
                //作为接收者，指定一个端口用于接收数据
                datagramSocket = new DatagramSocket(20000);

                while (!done) {

                    //构建接收实体
                    final byte[] bytes = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

                    //接收
                    datagramSocket.receive(packet);

                    //输出收到信息的发送者信息
                    int length = packet.getLength();
                    String data = new String(packet.getData(), 0, length);
                    log.info("sender info ===>> IP :: {} | PORT :: {} | DATA_LEN :: {} | DATA :: {} ",
                            packet.getAddress().getHostAddress(),
                            packet.getPort(),
                            length,
                            data);

                    //解析端口号
                    int port = MessageBuilder.parsePort(data);

                    if (port != -1) {
                        //构建回送数据
                        byte[] response = (MessageBuilder.buildWithSN(sn)).getBytes();
                        //根据发送者的信息发送构建的回送数据
                        DatagramPacket responsePacket = new DatagramPacket(response,
                                response.length,
                                packet.getAddress(),
                                port);

                        //发送
                        datagramSocket.send(responsePacket);

                        log.info("UDPProvider Finished !");
                        //关闭
                        datagramSocket.close();
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
