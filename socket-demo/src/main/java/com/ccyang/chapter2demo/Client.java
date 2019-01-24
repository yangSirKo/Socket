package com.ccyang.chapter2demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author: yangjinpeng
 * @date: 2019-01-09
 * @description: socket第二章，客户端实现
 */
public class Client {

    private static final String BYE = "bye";

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        // 读取流超时时间
        socket.setSoTimeout(3000);

        // 连接本地，端口为2000；超时时间为3000ms
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), 2000), 3000);

        System.out.println("发起服务器连接，继续后续流程...");
        System.out.println("客户端信息 " + socket.getLocalAddress() + "P: " + socket.getLocalPort());
        System.out.println("服务端信息 " + socket.getInetAddress() + " P: " + socket.getPort());

        try {
            // 发送接收数据
            sendReceiveData(socket);
        }catch (Exception e) {
            System.out.println("异常关闭");
        }
    }

    /**
     * 发送接收数据
     */
    private static void sendReceiveData(Socket client) throws IOException {

        // 获取键盘输入
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        // 得到socket输出流，并转换为打印流
        OutputStream output = client.getOutputStream();
        PrintStream socketPrintStream = new PrintStream(output);

        // 得到socket输入流，并转换为BufferedReader
        InputStream inputStream = client.getInputStream();
        BufferedReader socketBufferReader = new BufferedReader(new InputStreamReader(inputStream));

        boolean flag = true;
        do {
            // 读取一行键盘信息
            String str = input.readLine();
            // 发送到服务器
            socketPrintStream.println(str);

            // 读取一行服务器信息
            String echo = socketBufferReader.readLine();

            if (BYE.equals(echo)) {
                flag = false;
            }else {
                System.out.println(echo);
            }
        }while (flag);

        // 关闭资源
        socketBufferReader.close();
        socketPrintStream.close();
    }

}
