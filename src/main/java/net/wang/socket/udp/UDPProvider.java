package net.wang.socket.udp;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * UDPProvider 提供者，用于提供服务
 */
@Slf4j
public class UDPProvider {

    public static void main(String[] args) throws IOException {

        log.info("UDPProvider Started !");
        //作为接收者，指定一个端口用于接收数据
        DatagramSocket datagramSocket = new DatagramSocket(20000);

        //构建接收实体
        final byte[] bytes = new byte[1024];
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

        //接收
        datagramSocket.receive(packet);

        //输出收到信息的发送者信息
        int length = packet.getLength();
        log.info("sender info ===>> IP :: {} | PORT :: {} | DATA_LEN :: {} | DATA :: {} ",
                packet.getAddress().getHostAddress(),
                packet.getPort(),
                length,
                new String(packet.getData(), 0, length));

        //构建回送数据
        byte[] response = ("length = " + length).getBytes();
        //根据发送者的信息发送构建的回送数据
        DatagramPacket responsePacket = new DatagramPacket(response,
                response.length,
                packet.getAddress(),
                packet.getPort());

        //发送
        datagramSocket.send(responsePacket);

        log.info("UDPProvider Finished !");
        //关闭
        datagramSocket.close();
    }

}
