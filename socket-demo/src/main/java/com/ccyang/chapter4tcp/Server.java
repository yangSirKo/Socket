package com.ccyang.chapter4tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * 服务端
 * @author: yangjinpeng
 * @date: 2019-01-17
 * @description:
 */
public class Server {

    private final static int PORT = 20000;

    public static void main(String[] args) throws IOException {
        // 创建serverSocket
        ServerSocket serverSocket = createServerSocket();

        // serverSocket 初始化
        initServerSocket(serverSocket);

        // 绑定到本地端口，允许等待的连接队列为50个
        serverSocket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 50);

        System.out.println("启动服务端");
        System.out.println("客户端信息：" + serverSocket.getInetAddress() +
                "P: " + serverSocket.getLocalPort() );

        // 等待客户端连接
        for (; ; ){
            // 建立连接，得到客户端
            Socket socket = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(socket);
            clientHandler.start();
        }
    }

    /**
     * 创建serverSocket
     */
    private static ServerSocket createServerSocket() throws IOException {
        // 创建基础ServerSocket,建议这样做，然后将绑定放在初始化之后，防止初始化很多设置无效
        ServerSocket serverSocket = new ServerSocket();
        // 绑定到本地端口，允许等待的连接队列为50个
        // serverSocket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 50);

        // 绑定到本地端口，允许等待的连接队列为50个
        // ServerSocket serverSocket = new ServerSocket(PORT);

        // 绑定到本地端口，队列设置为50个
        // ServerSocket serverSocket = new ServerSocket(PORT, 50);

        // 与上面等同
        // ServerSocket serverSocket = new ServerSocket(PORT, 50, Inet4Address.getLocalHost());

        return serverSocket;
    }


    private static void initServerSocket(ServerSocket serverSocket) throws SocketException {
        // 是否复用未完全关闭的socket地址，对于指定bing操作后的套接字有效
        serverSocket.setReuseAddress(true);

        // 设置接收缓冲区大小
        serverSocket.setReceiveBufferSize(64 * 1024 * 1024);

        // 设置读取的超时时间2s
        // serverSocket.setSoTimeout(2000);

        // 设置性能参数：短链接、延迟、宽带的重要性.
        // 延迟参数越大，延迟优先级就越高，延迟时间就越短
        serverSocket.setPerformancePreferences(1, 1, 1);
    }

    public static class ClientHandler extends Thread{
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("新客户端连接：" + socket.getInetAddress() + "P: " + socket.getPort());

            try {
                // 获取socket输入流
                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[256];

                // 获取socket输出流
                OutputStream outputStream = socket.getOutputStream();

                // 接收客户端数据
                int readCount = inputStream.read(buffer);
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

                // boolean
                boolean b = byteBuffer.get() == 1;

                // byte
                byte byteVar = byteBuffer.get();

                // char
                char charVar = byteBuffer.getChar();

                // int
                int intVar = byteBuffer.getInt();

                // float
                float floatVar = byteBuffer.getFloat();

                // double
                double douVar = byteBuffer.getDouble();

                // long
                long aLong = byteBuffer.getLong();

                // String
                int pos = byteBuffer.position();
                String var = new String(buffer, pos, readCount - pos - 1);

                System.out.println("boolean:" + b + ", byte:" + byteVar + ", char:" + charVar + ", int:" + intVar +
                        ", float:" + floatVar + ", double:" + douVar + ", long:" + aLong + ", String:" + var);

                outputStream.write(Tools.intToByteArray(readCount));
//                if (readCount > 0) {
//                    int value =  Tools.byteArrayToInt(buffer);
//                    System.out.println("收到数量：" + readCount + "，数据：" + value);
//                    outputStream.write(buffer, 0, readCount);
//                } else {
//                    System.out.println("没有收到：" + readCount);
//                    outputStream.write(new byte[]{0});
//                }

                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                System.out.println("异常连接断开");
            }finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("客户端退出 " + socket.getInetAddress() + " P: " + socket.getPort());
        }
    }

}
