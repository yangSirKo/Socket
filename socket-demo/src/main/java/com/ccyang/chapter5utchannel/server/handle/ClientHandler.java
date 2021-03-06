package com.ccyang.chapter5utchannel.server.handle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ccyang.chapter5utchannel.util.CloseUtils;

/**
 * 客户端消息处理
 * @author: yangjinpeng
 * @date: 2019-01-22
 * @description:
 */
public class ClientHandler {

    private final Socket socket;
    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final CloseNotify closeNotify;

    public ClientHandler(Socket socket, CloseNotify closeNotify) throws IOException {
        this.socket = socket;
        this.readHandler = new ClientReadHandler(socket.getInputStream());
        this.writeHandler = new ClientWriteHandler(socket.getOutputStream());
        this.closeNotify = closeNotify;
        System.out.println("建立客户端连接：" + socket.getInetAddress() + ", P:" + socket.getPort());
    }

    /**
     * 客户端退出
     */
    public void exit() {
        readHandler.exit();
        writeHandler.exit();
        CloseUtils.close(socket);
        System.out.println("客户端退出 " + socket.getInetAddress() + ", P:" + socket.getPort() );
    }

    /**
     * 发送消息
     */
    public void send(String str) {
        writeHandler.send(str);
    }

    /**
     * 读取数据并打印
     */
    public void readToPrint() {
        readHandler.start();
    }

    /**
     * 当前客户端自己退出
     */
    private void exitBySelf() {
        exit();
        // 告诉外部的closeNotify。自己将自己关闭了
        closeNotify.onSelfClosed(this);
    }

    /**
     * 回调接口。 当客户端自己退出时，需要告诉外部自己退出了，然后从ClientHandlerList中移除自己
     */
    public interface CloseNotify {
        void onSelfClosed(ClientHandler handler);
    }

    /**
     * 读取消息处理
     */
    class ClientReadHandler extends Thread {

        private boolean done = false;
        private final InputStream inputStream;

        public ClientReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {

            try {
                // 得到输出流，用于接收数据
                BufferedReader socketInput =
                        new BufferedReader(new InputStreamReader(inputStream));

                do {
                    // 客户端拿到一条数据
                    String str = socketInput.readLine();
                    if (str == null) {
                        // 服务器端接收客户端连接，产生的客户端
                        System.out.println("客户端已无法读取数据");
                        // 退出当前客户端
                        ClientHandler.this.exitBySelf();
                        break;
                    }
                    // 打印到屏幕
                    System.out.println(str);

                } while (!done);
            } catch (IOException e) {
                // 不是手动退出
                if (!done){
                    System.out.println("连接异常断开");
                    ClientHandler.this.exitBySelf();
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

    /**
     * 发送消息
     */
    class ClientWriteHandler {
        private boolean done = false;
        private final PrintStream printStream;
        private final ExecutorService executorService;

        public ClientWriteHandler(OutputStream outputStream) {
            this.printStream = new PrintStream(outputStream);
            executorService = Executors.newSingleThreadExecutor();
        }

        void exit() {
            done = true;
            CloseUtils.close(printStream);
            executorService.shutdownNow();
        }

        public void send(String str) {
            executorService.execute(new WriteRunnable(str));

        }

        class WriteRunnable implements Runnable {
            private final String msg;

            public WriteRunnable(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                if (ClientWriteHandler.this.done){
                    return;
                }
                try {
                    ClientWriteHandler.this.printStream.println(msg);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
