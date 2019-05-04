package net.wang.nio.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * NIO 客户端
 */
@SuppressWarnings("Duplicates")
public class NioClient {

    public static void start() throws IOException {
        //链接服务端

        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));

        //接受服务端响应数据
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        new Thread(new NioClientHandler(selector)).start();

        //向服务端发送数据
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            socketChannel.write(Charset.forName("UTF-8").encode(line));
        }

    }

    public static void main(String[] args) throws Exception {
        NioClient.start();
    }


}
