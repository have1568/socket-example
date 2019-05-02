package net.wang.socket.example;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class SocketClient {
    public static void main(String[] args) throws Exception {

        //创建Socket对象
        Socket socket = new Socket();
        //设置超时时间 3000ms
        socket.setSoTimeout(3000);
        //连接本地，端口2000，超时时间 3000ms
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), 2000), 3000);

        log.info("已发送服务器连接，并进入后续流程~");
        log.info("客户端信息 ：IP {} PORT {}", socket.getLocalAddress(), socket.getPort());
        log.info("服务端信息 ：IP {} PORT {}", socket.getLocalAddress(), socket.getPort());

        //发送数据

        //构建键盘输入流
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        //得到socket输出流，并转换为打印流
        OutputStream outputStream = socket.getOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        //得到socket输入流，并转换为BufferedReader
        InputStream inputStream = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        boolean flag = true;

        do {
            //从键盘读取一行
            String line = input.readLine();

            //发送数据
            printStream.println(line);


            String readLine = reader.readLine();
            if ("BYE".equalsIgnoreCase(readLine)) {
                flag = false;
                log.info("close flag ===<<  {}", readLine);
            } else {
                log.info("===<<{}", readLine);
            }

        } while (flag);

        //关闭流
        printStream.close();
        reader.close();

        //释放资源
        socket.close();

        log.info("客户端已退出~");

    }
}
