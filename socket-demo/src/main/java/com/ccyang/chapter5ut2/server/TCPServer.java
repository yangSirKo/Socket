package com.ccyang.chapter5ut2.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author: yangjinpeng
 * @date: 2019-01-22
 * @description:
 */
public class TCPServer {

    private final int port;
    private ClientListener mListener;

    public TCPServer(int port) {
        this.port = port;
    }

    public boolean start() {
        try {
            ClientListener listener = new ClientListener(port);
            mListener = listener;
            listener.start();
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop(){
        if (mListener != null){
            mListener.exit();
            mListener = null;
        }
    }

    /**
     * 服务器监听客户端连接
     */
    private static class ClientListener extends Thread {
        private ServerSocket server;
        private boolean done = false;

        public ClientListener(int listenPort) throws IOException {
            server = new ServerSocket(listenPort);
            System.out.println("服务器信息：ip:" + server.getInetAddress() + ", port:" + server.getLocalPort());
        }

        @Override
        public void run() {

            System.out.println("服务端启动");
            // 等待客户端连接
            do {
                Socket client;
                try {
                    // 建立连接 client
                    client = server.accept();
                } catch (IOException e) {
                    continue;
                }
                // 客户端构建异步线程
                ClientHandler clientHandler = new ClientHandler(client);
                // 启动线程
                clientHandler.start();

            } while (!done);
            System.out.println("服务器关闭");
        }

        void exit(){
            done = true;
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 客户端消息处理
     */
    private static class ClientHandler extends Thread {
        private Socket socket;
        private boolean flag = true;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("建立客户端连接：" + socket.getInetAddress() + ", P:" + socket.getPort());

            try {
                // 得到打印流，用于数据输出；服务端回送数据使用
                PrintStream socketOutput = new PrintStream(socket.getOutputStream());

                // 得到输出流，用于接收数据
                BufferedReader socketInput =
                        new BufferedReader(new InputStreamReader(socket.getInputStream()));

                do {
                    // 客户端拿到一条数据
                    String str = socketInput.readLine();
                    if ("bye".equalsIgnoreCase(str)) {
                        flag = false;
                        socketOutput.println(str);
                    } else {
                        // 打印并回送数据长度
                        System.out.println(str);
                        socketOutput.println("回送：" + str.length());
                    }

                } while (flag);

                socketInput.close();
                socketOutput.close();

            } catch (IOException e) {

                System.out.println("连接异常断开");
            } finally {
                // 连接关闭
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("客户端退出 " + socket.getInetAddress() + ", P:" + socket.getPort() );
        }
    }


}


















