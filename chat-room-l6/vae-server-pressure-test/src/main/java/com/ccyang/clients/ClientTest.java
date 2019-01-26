package com.ccyang.clients;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.ccyang.clients.bean.ServerInfo;

/**
 * @author: yangjinpeng
 * @date: 2019-01-26
 * @description: 测试类
 */
public class ClientTest {

    private final static int clientNumbers = 200;
    private static boolean done;

    public static void main(String[] args) throws IOException {

        // 搜索服务器端，超时时间为10s
        ServerInfo info = UDPSearcher.searchServer(10000);
        System.out.println("serverInfo: " + info);

        if (info == null) {
            return;
        }

        int size = 0;
        List<TCPClient> tcpClients = new ArrayList<>(clientNumbers);
        for (int i = 0; i < clientNumbers; i++) {
            TCPClient tcpClient = TCPClient.startWith(info);
            if (tcpClient == null) {
                System.out.println("连接异常");
                continue;
            }
            tcpClients.add(tcpClient);
            System.out.println("连接成功：" + (++size));

            try {
                // 服务端有阈值，连接对列不超过50个
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // 接收键盘输入，进行发送数据
        System.in.read();
        Thread thread = new Thread(() -> {
            while (true) {
                for (TCPClient client : tcpClients) {
                    client.send("hello~");
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        // 接收键盘输入，退出程序
        System.in.read();
        done = true;
        // 等待线程执行完成
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (TCPClient client : tcpClients) {
            client.exit();
        }
    }
}
