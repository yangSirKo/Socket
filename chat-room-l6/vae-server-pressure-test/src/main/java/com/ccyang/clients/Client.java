package com.ccyang.clients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

import com.ccyang.clients.bean.ServerInfo;

/**
 * @author: yangjinpeng
 * @date: 2019-01-20
 * @description:
 */
public class Client {

    public static void main(String[] args) {

        // 搜索服务器端，超时时间为10s
        ServerInfo info = UDPSearcher.searchServer(10000);
        System.out.println("serverInfo: " + info);

        if (info != null) {
            TCPClient tcpClient = null;
            try {
                tcpClient = TCPClient.startWith(info);
                if (tcpClient == null) {
                    return;
                }
                write(tcpClient);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
              if (tcpClient != null) {
                  tcpClient.exit();
              }
            }
        }
    }

    /**
     * 发送请求
     */
    private static void write(TCPClient tcpClient) throws IOException {
        // 获取键盘输入
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        do {
            // 读取一行键盘信息
            String str = input.readLine();
            // 发送到服务器
            tcpClient.send(str);

            if ("00bye00".equals(str)) {
                break;
            }
        }while (true);
    }




}
