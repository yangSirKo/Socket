package com.ccyang.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.ccyang.client.bean.ServerInfo;
import com.ccyang.util.CloseUtils;

/**
 * @author: yangjinpeng
 * @date: 2019-01-22
 * @description:
 */
public class TCPClient {

    public static void linkWith(ServerInfo serverInfo) throws IOException {
        Socket socket = new Socket();
        // 超时时间
        socket.setSoTimeout(3000);

        // 连接本地 30401端口，超时时间3000ms
        socket.connect(new InetSocketAddress(Inet4Address.getByName(serverInfo.getAddress()), serverInfo.getPort()),
                3000);

        System.out.println("发起服务器连接，继续后续流程...");
        System.out.println("客户端信息 " + socket.getLocalAddress() + "P: " + socket.getLocalPort());
        System.out.println("服务端信息 " + socket.getInetAddress() + " P: " + socket.getPort());

        try {
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            readHandler.start();

            // 发送数据
            write(socket);

            // 退出操作
            readHandler.exit();
        }catch (Exception e) {
            System.out.println("异常关闭");
        }

        // 释放资源
        socket.close();
        System.out.println("客户端退出");
    }

    /**
     * 发送请求
     */
    private static void write(Socket client) throws IOException {

        // 获取键盘输入
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        // 得到socket输出流，并转换为打印流
        OutputStream output = client.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(output);

        do {
            // 读取一行键盘信息
            String str = input.readLine();
            // 发送到服务器
            socketPrintStream.println(str);

            if ("00bye00".equals(str)) {
                break;
            }
        }while (true);

        // 关闭资源
        socketPrintStream.close();
    }

    /**
     * 读取消息处理
     */
    static class ReadHandler extends Thread {

        private boolean done = false;
        private final InputStream inputStream;

        public ReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {

            try {
                // 得到输入流，用于接收数据
                BufferedReader socketInput =
                        new BufferedReader(new InputStreamReader(inputStream));

                do {
                    // 客户端拿到一条数据
                    String str;
                    try {
                        str = socketInput.readLine();
                    }catch (SocketTimeoutException e) {
                        continue;
                    }

                    if (str == null) {
                        System.out.println("连接已关闭，无法读取数据");
                        break;
                    }
                    // 打印到屏幕
                    System.out.println(str);

                } while (!done);
            } catch (IOException e) {
                // 不是手动退出
                if (!done){
                    System.out.println("连接异常断开 " + e.getMessage() );
                }
            } finally {
                // 连接关闭
                CloseUtils.close(inputStream);
            }
        }

        void exit(){
            done = true;
            CloseUtils.close(inputStream);
        }
    }
}
