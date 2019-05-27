package net.wang.nio.channel;

import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

/**
 * <p>一、通道（Channel）：用于源节点和目标节点的连接。在Java NIO中负责数据的传输。channel 本身不存储数据，因此需要配合缓冲区进行传输。
 *
 * <p>二、通道的主要实现类
 * java.nio.channels.Channel 接口：
 * |-- FileChannel
 * |-- SocketChannel
 * |-- SocketServerChannel
 * |-- DatagramChannel
 * 三、获取通道
 * 1、Java针对支持通道的类提供了 getChannel()方法
 * 本地IO
 * FileInputStream/FileOutputStream
 * RandomAccessFile
 * 网络IO
 * Socket
 * ServerSocket
 * DatagramSocket
 * 2、在 JDK1.7中的 NIO2中针对每个通道提供了静态方法 open()
 * 3、在 JDK1.7中的 NIO2 的 File 工具类的newByteChannel()
 *
 * <p>四、通道之间的数据传输
 * transferFrom()
 * transferTo()
 *
 * <p>五、分散（Scatter）与聚集（Gather）
 * 分散读取（Scattering Reads）:将通道中的数据分散到多个缓冲区
 * 聚集写入 （Gathering Writes）: 将缓冲区中的数据采集到通道中
 *
 * <p>六、字符集 Charset
 * 编码：字符串 ——>字节数组
 * 解码：字节数组——> 字符串
 */
public class TestChannel {

    public void close(Closeable... closeable) {
        for (Closeable c : closeable) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException e) {

                }
            }
        }
    }

    /**
     * 利用通道完成文件的复制（非直接缓冲区）
     */
    @Test
    public void test1() {
        long start = System.currentTimeMillis();
        //获取流和通道
        FileChannel fileOutputStreamChannel = null;
        FileChannel fileInputStreamChannel = null;
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream("e:/test.mp4");
            fileOutputStream = new FileOutputStream("e:/test-copy.mp4");
            fileInputStreamChannel = fileInputStream.getChannel();
            fileOutputStreamChannel = fileOutputStream.getChannel();

            //创建缓冲区(缓冲区大小会影响复制文件的速度)
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

            //将通道中的数据写入缓冲区
            while (fileInputStreamChannel.read(byteBuffer) != -1) {
                //切换为读数据模式
                byteBuffer.flip();
                //将缓冲区中的数据写入通道中
                fileOutputStreamChannel.write(byteBuffer);
                //清空缓冲区
                byteBuffer.clear();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(fileOutputStreamChannel, fileInputStreamChannel, fileOutputStream, fileInputStream);
        }
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - start));
    }

    /**
     * 使用直接缓冲区完成文件复制
     */
    @Test
    public void test2() throws IOException {
        long start = System.currentTimeMillis();
        FileChannel inChannel = FileChannel.open(Paths.get("e:/test.mp4"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("e:/test-cp.mp4"), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

        //内存映射文件
        MappedByteBuffer inBuf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
        MappedByteBuffer outBuf = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());

        //直接缓冲区进行数据的读取操作
        byte[] dst = new byte[inBuf.limit()];
        inBuf.get(dst);
        outBuf.put(dst);

        inChannel.close();
        outChannel.close();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - start));

    }

    /**
     * 通道之间的数据传输（直接缓冲区）
     */
    @Test
    public void test3() throws IOException {

        FileChannel inChannel = FileChannel.open(Paths.get("e:/test.mp4"), StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("e:/test-cp-channel.mp4"), StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);

        //inChannel.transferTo(0,inChannel.size(),outChannel);
        outChannel.transferFrom(inChannel, 0, inChannel.size());

        outChannel.close();
        inChannel.close();
    }

    //字符集
    @Test
    public void test6() throws IOException {
        Charset cs1 = Charset.forName("GBK");

        //获取编码器
        CharsetEncoder ce = cs1.newEncoder();

        //获取解码器
        CharsetDecoder cd = cs1.newDecoder();

        CharBuffer cBuf = CharBuffer.allocate(1024);
        cBuf.put("尚硅谷威武！");
        cBuf.flip();

        //编码
        ByteBuffer bBuf = ce.encode(cBuf);

        for (int i = 0; i < 12; i++) {
            System.out.println(bBuf.get());
        }

        //解码
        bBuf.flip();
        CharBuffer cBuf2 = cd.decode(bBuf);
        System.out.println(cBuf2.toString());

        System.out.println("------------------------------------------------------");

        Charset cs2 = Charset.forName("GBK");
        bBuf.flip();
        CharBuffer cBuf3 = cs2.decode(bBuf);
        System.out.println(cBuf3.toString());
    }

    @Test
    public void test5() {
        Map<String, Charset> map = Charset.availableCharsets();

        Set<Map.Entry<String, Charset>> set = map.entrySet();

        for (Map.Entry<String, Charset> entry : set) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    }

    //分散和聚集
    @Test
    @Ignore
    public void test4() throws IOException {
        RandomAccessFile raf1 = new RandomAccessFile("1.txt", "rw");

        //1. 获取通道
        FileChannel channel1 = raf1.getChannel();

        //2. 分配指定大小的缓冲区
        ByteBuffer buf1 = ByteBuffer.allocate(100);
        ByteBuffer buf2 = ByteBuffer.allocate(1024);

        //3. 分散读取
        ByteBuffer[] bufs = {buf1, buf2};
        channel1.read(bufs);

        for (ByteBuffer byteBuffer : bufs) {
            byteBuffer.flip();
        }

        System.out.println(new String(bufs[0].array(), 0, bufs[0].limit()));
        System.out.println("-----------------");
        System.out.println(new String(bufs[1].array(), 0, bufs[1].limit()));

        //4. 聚集写入
        RandomAccessFile raf2 = new RandomAccessFile("2.txt", "rw");
        FileChannel channel2 = raf2.getChannel();

        channel2.write(bufs);
    }
}
