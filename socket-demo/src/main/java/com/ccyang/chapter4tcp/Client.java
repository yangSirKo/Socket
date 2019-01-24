package com.ccyang.chapter4tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * 客户端
 * @author: yangjinpeng
 * @date: 2019-01-17
 * @description:
 */
public class Client {

    // 服务端端口
    private final static int PORT = 20000;
    // 本地监听端口
    private final static int LOCAL_PORT = 20001;

    public static void main(String[] args) throws IOException {

        // 创建socket
        Socket socket = createSocket();
        // 初始化socket
        initSocket(socket);
        // 链接到本地的20000端口，超时时间为3s，超时则抛异常
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 5000);

        System.out.println("发起服务端连接...");
        System.out.println("客户端信息：" + socket.getLocalAddress() + "P: " + socket.getLocalPort() );
        System.out.println("服务端信息：" + socket.getInetAddress() + "P: " + socket.getPort());

        try{
            // 发送数据
            todo(socket);
        } catch (Exception e) {
            System.out.println("异常关闭");
        }

        // 释放资源
        socket.close();
        System.out.println("客户端释放连接。");
    }

    /**
     * 创建一个socket，并绑定到本地的 20001端口
     */
    private static Socket createSocket() throws IOException {

        // 无代理模式，等效于无参构造函数
        // Socket socket = new Socket(Proxy.NO_PROXY);

        //新建一份具有HTTP代理的套接字，传输数据将通过 www.baidu.com:8888 端口转发
        // Proxy proxy = new Proxy(Proxy.Type.HTTP,
        //        new InetSocketAddress(Inet4Address.getByName("www.baidu.com"), 8888));
        // Socket socket = new Socket(proxy);

        // 新建一个套接字，并直接连接到本地服务器的 20000端口
        // Socket socket = new Socket("localhost", PORT);

        // 新建一个套接字，并直接连接到本地服务器的 20000端口
        // Socket socket = new Socket(Inet4Address.getLocalHost(), PORT);

        // 新建一个套接字，并直接连接到本地服务器的 20000端口, 同时绑定到本地的20001端口。这种方式会直接与服务器建立连接
        // Socket socket = new Socket(Inet4Address.getLocalHost(), PORT, Inet4Address.getLocalHost(), LOCAL_PORT);
        // Socket socket = new Socket("localhost", PORT, Inet4Address.getLocalHost(), LOCAL_PORT);

        // 建议下面这种方式，因为后续好控制
        Socket socket = new Socket();
        // 绑定到本地的20001端口
        socket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), LOCAL_PORT));

        return socket;
    }

    /**
     * 初始化socket连接
     */
    private static void initSocket(Socket socket) throws SocketException {
        // 设置读取的超时时间2s
        socket.setSoTimeout(2000);

        // 是否复用未完全关闭的socket地址，对于指定bing操作后的套接字有效
        socket.setReuseAddress(true);

        // 是否开启Nagle算法
        socket.setTcpNoDelay(true);

        // 是否需要在长时间无数据响应时发送确认数据（类似心跳包），大约两小时
        socket.setKeepAlive(true);

        // 对于close关闭操作行为进行怎么样的处理：默认是 false，0
        // 1、false，0：默认情况，关闭时立即返回，底层系统接管输出流，将缓冲区内的数据发送完成
        // 2、ture，0：关闭时立即返回，将缓冲区数据抛弃，直接发送RST结束命令到对方，并无需经过2MSL等待
        // 3、true，200：关闭时最长阻塞200毫秒，然后按第二种方式处理
        socket.setSoLinger(true, 200);

        // 是否让紧急数据内敛，默认false；紧急数据通过 socket.sendUrgentData(1);发送
        socket.setOOBInline(true);

        // 设置接收、发送缓冲区大小
        socket.setReceiveBufferSize(64 * 1024 * 1024);
        socket.setSendBufferSize(64 * 1024 * 1024);

        // 设置性能参数：短链接、延迟、宽带的重要性.
        // 延迟参数越大，延迟优先级就越高，延迟时间就越短
        socket.setPerformancePreferences(1, 1, 1);
    }

    /**
     * 收发数据
     */
    private static void todo(Socket client) throws IOException {

        // 得到socket输出流
        OutputStream outputStream = client.getOutputStream();

        // 得到socket输入流
        InputStream inputStream = client.getInputStream();
        byte[] buffer = new byte[256];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

        // int 转byte[] 测试
        // byte[] ints = Tools.intToByteArray(556677323);
        // outputStream.write(ints);

        // boolean
        boolean b = true;
        byteBuffer.put((byte)(b ? 1 : 0));

        // byte
        byteBuffer.put((byte)123);

        // char
        char c = 'a';
        byteBuffer.putChar(c);

        // int
        int intVar = 45454545;
        byteBuffer.putInt(intVar);

        // float
        float floatVar = 12.34f;
        byteBuffer.putFloat(floatVar);

        // double
        double douVar = 123.231233213;
        byteBuffer.putDouble(douVar);

        // long
        long longVar = 1233231233213L;
        byteBuffer.putLong(longVar);

        // String
        String var = "hello";
        byteBuffer.put(var.getBytes());

        outputStream.write(buffer, 0, byteBuffer.position() + 1);

        int read = inputStream.read(buffer);
        System.out.println("收到数量：" + Tools.byteArrayToInt(buffer));

//        if (read > 0) {
//            int value = Tools.byteArrayToInt(buffer);
//        } else {
//            System.out.println("没有收到：" + read);
//        }

        inputStream.close();
        outputStream.close();
    }

}