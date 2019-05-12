package net.wang.socket.channel.server;

import net.wang.socket.channel.server.handle.ClientHandler;

public interface ClientHandlerCallback {

   void onNewMessage(ClientHandler clientHandler,String message);

}
