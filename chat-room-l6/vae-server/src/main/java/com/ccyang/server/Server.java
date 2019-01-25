package com.ccyang.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ccyang.constants.TCPConstants;

/**
 * 收发并行的服务端
 * @author: yangjinpeng
 * @date: 2019-01-20
 * @description: 服务端
 */
public class Server {

    public static void main(String[] args) throws IOException {

        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Server tcp started fail");
            return;
        }

        UDPProvider.start(TCPConstants.PORT_SERVER);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str;
        do {
            str = bufferedReader.readLine();
            // 发送消息
            tcpServer.broadcast(str);
        } while (!"00bye00".equalsIgnoreCase(str));

        UDPProvider.stop();
        tcpServer.stop();
    }

}
