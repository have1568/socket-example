package net.wang.socket.channel.server.handle;

import lombok.extern.slf4j.Slf4j;
import net.wang.clink.utils.CloseUtils;
import net.wang.socket.channel.server.ClientHandlerCallback;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class ClientHandler {

    private Socket socket;  //Socket TCP 客户端
    private ExecutorService pool = Executors.newCachedThreadPool();
    private ClientHandlerCallback handlerCallback;
    boolean readFlag = true;

    public ClientHandler(Socket socket, ClientHandlerCallback handlerCallback) {
        this.socket = socket;
        this.handlerCallback = handlerCallback;

    }


    /**
     * 退出客户端处理器
     * 必须同时关闭 socket、线程池、循环读取标记
     */
    public void exit() {
        readFlag = false;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        pool.shutdownNow();
    }

    public void send(String line) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            log.info("Server Send Data :: {}", line);
            printStream.println(line);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readAndPrint() {
        pool.execute(() -> {
            InputStream inputStream = null;
            try {
                inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                do {
                    String readLine = reader.readLine();
                    if (readLine == null) {
                        log.info("客户端已无法读取数据！");
                        readFlag = false;
                        CloseUtils.close(inputStream);
                        pool.shutdownNow();
                    } else {
                        log.info("Read Data :: {}", readLine);
                        handlerCallback.onNewMessage(ClientHandler.this, readLine);
                    }
                } while (readFlag);

            } catch (Exception e) {
                CloseUtils.close(inputStream, socket);
                exit();
                e.printStackTrace();
            }

        });

    }
}
