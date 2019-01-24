package com.ccyang.chapter5ut2.server;

import java.io.IOException;

import com.ccyang.chapter5ut2.constants.TCPConstants;

/**
 * @author: yangjinpeng
 * @date: 2019-01-20
 * @description: 服务端
 */
public class Server {

    public static void main(String[] args) {

        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Server tcp started fail");
            return;
        }

        UDPProvider.start(TCPConstants.PORT_SERVER);

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UDPProvider.stop();
        tcpServer.stop();

    }

}
