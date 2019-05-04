package net.wang.socket.tcp;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

@Slf4j
public class Client {

    public static final int PORT = 20000;//远程端口
    public static final int LOCAL_PORT = 20001;//本地端口

    public static void main(String[] args) throws Exception {
        Socket socket = createSocket();

        //连接到远程端口，超时时间3s
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 3000);

        log.info("已发起服务器连接，并进入后续流程～");
        log.info("客户端信息：" + socket.getLocalAddress() + " P:" + socket.getLocalPort());
        log.info("服务器信息：" + socket.getInetAddress() + " P:" + socket.getPort());

        try {
            doTransferData(socket);
        } catch (Exception e) {
            log.warn("异常关闭");
        }

        socket.close();
        log.info("客户端退出");
    }

    private static Socket createSocket() throws Exception {
        /*

        // 无代理模式，等于空参构造
        Socket socket = new Socket(Proxy.NO_PROXY);

        //新建一个HTTP代理
        Proxy proxy = new Proxy(Proxy.Type.HTTP,
                new InetSocketAddress(Inet4Address.getByName("www.baidu.com"), 8800)
        );
        Socket socket = new Socket(proxy);

        //新建一个套接字，并直接连接到本地的20000渡口
        Socket socket = new Socket("localhost", 20000);

        //新建一个套接字，并且连接到本地20000端口，并且绑定到本地20001端口上
        Socket socket = new Socket("localhost", PORT, InetAddress.getLocalHost(), LOCAL_PORT);
        */


        Socket socket = new Socket();
        //绑定到本地20001端口
        socket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), LOCAL_PORT));
        return socket;
    }

    private static void initSocket(Socket socket) throws Exception {
        //设置超时时间
        socket.setSoTimeout(20000);

        //是否复用完全关闭的Socket地址，对于指定bind操作后的套接字有效
        socket.setReuseAddress(true);

        //是否开启Nagle算法,避免大量小数据发送
        socket.setTcpNoDelay(true);

        //是否需要在长时间无数据响应发送确认数据（类似心跳包），时间大约 为2小时
        socket.setKeepAlive(true);

        // 对于close关闭操作行为进行怎样的处理；默认为false，0
        // false、0：默认情况，关闭时立即返回，底层系统接管输出流，将缓冲区内的数据发送完成
        // true、0：关闭时立即返回，缓冲区数据抛弃，直接发送RST结束命令到对方，并无需经过2MSL等待
        // true、200：关闭时最长阻塞200毫秒，随后按第二情况处理
        socket.setSoLinger(true, 20);

        //是否让紧急数据内敛，默认为false，紧急数据通过 socket.sendUrgentData(1)发送。会造成紧急数据和行为数据混在一起
        socket.setOOBInline(true);

        //设置接收、发送缓冲器大小 64K
        socket.setReceiveBufferSize(64 * 1024 * 1024);
        socket.setSendBufferSize(64 * 1024 * 1024);

        //设置性能参数：短链接、延迟、带宽的相对重要性
        socket.setPerformancePreferences(1, 1, 0);
    }

    public static void doTransferData(Socket socket) throws IOException {
        //得到 Socket 的输出流
        OutputStream outputStream = socket.getOutputStream();

        //的发哦 Socket 的输入流
        InputStream inputStream = socket.getInputStream();

        //构建缓冲器
        byte[] buffer = new byte[256];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        /**
         * 底层通过位运算符将基本类型转换为byte数组，读取时 byteBuffer.position() 值会有相应变化
         */

        // byte
        byteBuffer.put((byte) 126);

        // char
        char c = 'a';
        byteBuffer.putChar(c);

        // int
        int i = 2323123;
        byteBuffer.putInt(i);

        // bool
        boolean b = true;
        byteBuffer.put(b ? (byte) 1 : (byte) 0);

        // Long
        long l = 298789739;
        byteBuffer.putLong(l);


        // float
        float f = 12.345f;
        byteBuffer.putFloat(f);


        // double
        double d = 13.31241248782973;
        byteBuffer.putDouble(d);

        // String
        String str = "Hello你好！";
        byteBuffer.put(str.getBytes());

        //通过输出流将信息发送到服务端
        outputStream.write(buffer, 0, byteBuffer.position() + 1);

        //接收服务器返回的信息
        int read = inputStream.read(buffer);
        log.info("收到数量 ： {} ", read);

        //关闭流
        outputStream.close();
        inputStream.close();
    }
}
