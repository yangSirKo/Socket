package com.ccyang.client;

import java.io.IOException;

import com.ccyang.client.bean.ServerInfo;

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
            try {
                TCPClient.linkWith(info);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
