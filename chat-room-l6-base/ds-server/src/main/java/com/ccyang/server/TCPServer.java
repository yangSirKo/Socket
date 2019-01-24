package com.ccyang.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.ccyang.server.handle.ClientHandler;

/**
 * @author: yangjinpeng
 * @date: 2019-01-22
 * @description:
 */
public class TCPServer {

    // 监听的端口号
    private final int port;
    private ClientListener mListener;
    /**
     * 保存连接成功的客户端
     */
    private List<ClientHandler> clientHandlerList = new ArrayList<>();

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
        }

        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.exit();
        }
        clientHandlerList.clear();
    }

    /**
     * 发送消息
     */
    public void broadcast(String str) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(str);
        }
    }

    /**
     * 服务器监听客户端连接
     */
    private class ClientListener extends Thread {
        private ServerSocket server;
        private boolean done = false;

        public ClientListener(int listenPort) throws IOException {
            server = new ServerSocket(listenPort);
            System.out.println("服务器信息：ip:" + server.getInetAddress() + ", port:" + server.getLocalPort());
        }

        @Override
        public void run() {

            System.out.println("服务器启动");
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
                try {
                    ClientHandler clientHandler = new ClientHandler(client,
                            handler -> clientHandlerList.remove(handler));
                    // 读取数据并打印
                    clientHandler.readToPrint();
                    clientHandlerList.add(clientHandler);
                } catch (IOException e) {
                    System.out.println("客户端连接异常 " + e.getMessage());
                }

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

}


















