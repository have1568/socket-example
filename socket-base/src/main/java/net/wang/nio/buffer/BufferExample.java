package net.wang.nio.buffer;


import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * <p>一、缓冲区（Buffer）：在Java NIO 中负责数据的存取。用于存储不同数据类型的数据
 *
 * <p>根据不同的数据类型（boolean 除外），提供了相应类型的缓冲区
 *
 * ByteBuffer
 * CharBuffer
 * ShortBuffer
 * IntBuffer
 * LongBuffer
 * FloatBuffer
 * DoubleBuffer
 *
 * <p>上述缓冲区的管理方式几乎一致，通过allocate()获取缓冲区
 *
 * <p>二、缓冲区存取数据的两个核心方法：
 * put()
 * get()
 *
 * <p>三、缓冲区中的四个核心属性：
 *
 * capacity ：容量。表示缓冲区中最大存取数据的容量。一旦声明不能 改变。
 * limit ：界限，表示缓冲区可以操作的数据大小。（limit后的数据不能进行操作）
 * position ：位置，表示正在操作数据的位置。
 * mark：标记，表示记录当前 position 的位置。可以通过reset()恢复到mark的位置
 *
 * 0<= mark <=position<=limit<=capacity
 *
 * <p>四、直接缓冲区与非直接缓冲区：
 * 非直接缓冲区：通过 allocate()方法获取。建立在JVM内存中
 * 直接缓冲区：通过 allocateDirect()方法获取，将缓冲区放在物理内存中，可以提高效率
 *
 */

public class BufferExample {


    @Test
    public void test1(){

        String str = "Hello World";

        //1、分配一个指定大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        System.out.println("==============allocate==============");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        //2、存入数据
        buffer.put(str.getBytes());

        System.out.println("==============put==============");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());


        //3、切换为读模式
        buffer.flip();

        System.out.println("==============flip==============");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        //4、读取数据
        byte[] dst = new byte[buffer.limit()];
        buffer.get(dst);
        System.out.println(new String(dst,0,dst.length));


        System.out.println("==============get==============");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        //5、可重复读
        buffer.rewind();
        System.out.println("==============rewind==============");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        //6、清空缓冲区，数据依然存在
        buffer.clear();
        System.out.println("==============clear==============");
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        System.out.println((char)buffer.get());


    }




}
