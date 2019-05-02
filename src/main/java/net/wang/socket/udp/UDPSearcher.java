package net.wang.socket.udp;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * UDPProvider 提供者，用于提供服务
 */
@Slf4j
public class UDPSearcher {

    public static void main(String[] args) throws IOException {

        log.info("UDPSearcher Started !");
        //作为搜索方，无需指定端口
        DatagramSocket datagramSocket = new DatagramSocket();

        //构建发送数据
        byte[] request = "Hello World".getBytes();
        //将发送的数据封装到 DatagramPacket 对象中
        DatagramPacket requestPacket = new DatagramPacket(request,
                request.length);
        //设置IP和端口
        requestPacket.setAddress(InetAddress.getLocalHost());
        requestPacket.setPort(20000);

        //发送
        datagramSocket.send(requestPacket);

        //构建接收实体
        final byte[] bytes = new byte[1024];
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

        //接收
        datagramSocket.receive(packet);

        //输出收到信息的发送者信息（Provider 响应的数据）
        int length = packet.getLength();
        log.info("response info ===>> IP :: {} | PORT :: {} | DATA_LEN :: {} | DATA :: {} ",
                packet.getAddress().getHostAddress(),
                packet.getPort(),
                length,
                new String(packet.getData(), 0, length));

        log.info("UDPSearcher Finished !");
        //关闭
        datagramSocket.close();
    }

}
