package net.wang.socket.udp.tcp.client;

import lombok.extern.slf4j.Slf4j;
import net.wang.socket.udp.tcp.bean.ServerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class TCPClient {

    public static void linkWith(ServerInfo info) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(3000);

        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()), 3000);

        log.info("客户端信息 : IP :: {} \tPORT :: {}", socket.getLocalAddress(), socket.getLocalPort());
        log.info("服务端信息 : IP :: {} \tPORT :: {}", socket.getInetAddress(), socket.getPort());

        try {
            //发送数据
            sandData(socket);
        } catch (Exception e) {
            log.info("TCP Client Error", e);
        }
    }

    private static void sandData(Socket client) throws IOException {

        //获取键盘输入流并构建缓冲器
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        //获得 Socket Client 的输出流，用于向服务端发送数据
        PrintStream output = new PrintStream(client.getOutputStream());

        //得到 Socket Server 向 客户端输入流，并转换为缓冲器
        BufferedReader serverResponse = new BufferedReader(new InputStreamReader(client.getInputStream()));

        boolean flag = true;

        do {
            String line = input.readLine();
            output.println(line);

            //读取服务端响应的数据
            String serverRes = serverResponse.readLine();
            if ("bye".equalsIgnoreCase(serverRes)) {
                flag = false;
            }

        } while (flag);

        output.close();
        serverResponse.close();

    }
}
