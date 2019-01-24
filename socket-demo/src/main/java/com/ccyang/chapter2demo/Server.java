package com.ccyang.chapter2demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 第二章，服务端实现。
 * @author: yangjinpeng
 * @date: 2019-01-09
 * @description: 先启动服务器，在启动客户端
 *
 */
public class Server {

    private static final String BYE = "bye";

    public static void main(String[] args) throws IOException {

        ServerSocket server = new ServerSocket(2000);

        System.out.println("服务器端准备就绪...");
        System.out.println("服务器信息 " + server.getInetAddress() + " P: " + server.getLocalPort());

        // 等待客户端连接
        for(;;) {
            // 接收客户端连接
            Socket client = server.accept();
            // 为处理客户端构建异步线程
            ClientHandler clientHandler = new ClientHandler(client);
            // 启动线程
            clientHandler.start();
        }
    }

    /**
     * 客户端消息处理
     */
    private static class ClientHandler extends Thread {
        private Socket socket;
        private boolean flag = true;

        public ClientHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            System.out.println("新客户端连接 " + socket.getInetAddress() + "P: " + socket.getPort());

            try{
                // 得到打印流，用户数据输出；服务器回送数据给客户端
                PrintStream socketOutput = new PrintStream(socket.getOutputStream());
                // 得到输入流，用于接收数据
                BufferedReader socketBufferReader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                do{
                    // 拿到客户端一条数据
                    String str = socketBufferReader.readLine();
                    if (BYE.equalsIgnoreCase(str)){
                        flag = false;
                        // 回送客户端一条数据
                        socketOutput.println("bye");
                    }else {
                        // 打印到屏幕，并回送数据长度
                        System.out.println(str);
                        socketOutput.println("回送：" + str.length());
                    }

                }while (flag);
                // 关闭资源
                socketBufferReader.close();
                socketOutput.close();

            }catch (Exception e) {
                System.out.println("连接异常断开");
            }finally {
                // 关闭连接
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("客户端已退出：" + socket.getInetAddress() +
                        "P: " + socket.getPort());
            }
        }
    }
}
