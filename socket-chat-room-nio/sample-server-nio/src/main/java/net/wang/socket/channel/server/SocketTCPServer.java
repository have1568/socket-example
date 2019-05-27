package net.wang.socket.channel.server;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public interface SocketTCPServer {

    boolean start();

    void listenHandler(Selector selector, ServerSocketChannel serverSocketChannel);

    void readHandler(SocketChannel socketChannel);

    void writeHandler(SocketChannel socketChannel, String msg);

    void broadcast(String line);

    void onNewMessage(SocketChannel client,String message) throws IOException;

    void stop();
}
