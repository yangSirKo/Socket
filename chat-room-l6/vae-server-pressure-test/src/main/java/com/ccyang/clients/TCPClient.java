package com.ccyang.clients;

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

import com.ccyang.clients.bean.ServerInfo;
import com.ccyang.util.CloseUtils;

/**
 * @author: yangjinpeng
 * @date: 2019-01-22
 * @description:
 */
public class TCPClient {

    private final Socket socket;
    private final ReadHandler readHandler;
    private final PrintStream printStream;

    public TCPClient(Socket socket, ReadHandler readHandler) throws IOException {
        this.socket = socket;
        this.readHandler = readHandler;
        this.printStream = new PrintStream(socket.getOutputStream());
    }

    /**
     * 退出
     */
    public void exit() {
        readHandler.exit();
        CloseUtils.close(printStream);
        CloseUtils.close(socket);
    }

    /**
     * 发送消息
     */
    public void send(String msg) {
        printStream.println(msg);
    }

    public static TCPClient startWith(ServerInfo serverInfo) throws IOException {
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

            return new TCPClient(socket, readHandler);

        }catch (Exception e) {
            System.out.println("连接异常");
            CloseUtils.close(socket);
        }

        return null;
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
