package com.ccyang.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ccyang.server.handle.ClientHandler;

/**
 * @author: yangjinpeng
 * @date: 2019-01-22
 * @description:
 */
public class TCPServer implements ClientHandler.ClientHandlerCallback {

    // 监听的端口号
    private final int port;
    private ClientListener mListener;
    /**
     * 保存连接成功的客户端
     */
    private List<ClientHandler> clientHandlerList = new ArrayList<>();
    private ExecutorService forwardingThreadPoolExecutor = null;

    public TCPServer(int port) {
        this.port = port;
        forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor();
    }

    /**
     * 启动服务端
     */
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

    /**
     * 关闭服务端
     */
    public void stop(){
        if (mListener != null){
            mListener.exit();
        }
        synchronized(TCPServer.this) {
            for (ClientHandler clientHandler : clientHandlerList) {
                clientHandler.exit();
            }
            clientHandlerList.clear();
        }

        // 停止线程池
        forwardingThreadPoolExecutor.shutdownNow();
    }

    /**
     * 发送消息
     */
    public synchronized void broadcast(String msg) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(msg);
        }
    }

    /**
     * 客户端退出后，需要在连接的集合中删除
     * @param handler
     */
    @Override
    public synchronized void onSelfClosed(ClientHandler handler) {
        clientHandlerList.remove(handler);
    }

    /**
     * 服务端收到消息，进行转发
     */
    @Override
    public void onNewMessageArrived(ClientHandler handler, String msg) {
        System.out.println("收到了 - " + handler.getClientInfo() +" : " + msg);
        String resendMsg = handler.getClientInfo() +": " + msg;
                forwardingThreadPoolExecutor.execute( () -> {
            synchronized(TCPServer.this) {
                for (ClientHandler clientHandler : clientHandlerList) {
                    if (clientHandler.equals(handler)){
                        // 跳过自己
                        continue;
                    }
                    // 对其它客户端发送消息
                    clientHandler.send(resendMsg);
                }
            }
        });
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
                    ClientHandler clientHandler = new ClientHandler(client, TCPServer.this);
                    // 读取数据并打印
                    clientHandler.readToPrint();
                    // 添加同步
                    synchronized(TCPServer.this) {
                        clientHandlerList.add(clientHandler);
                    }
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


















