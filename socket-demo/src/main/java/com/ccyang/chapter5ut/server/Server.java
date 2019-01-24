package com.ccyang.chapter5ut.server;

import java.io.IOException;

import com.ccyang.chapter5ut.constants.TCPConstants;

/**
 * @author: yangjinpeng
 * @date: 2019-01-20
 * @description: 服务端
 */
public class Server {

    public static void main(String[] args) {

        ServerProvider.start(TCPConstants.PORT_SERVER);

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ServerProvider.stop();
    }

}
