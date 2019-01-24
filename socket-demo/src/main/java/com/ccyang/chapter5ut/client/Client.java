package com.ccyang.chapter5ut.client;

import com.ccyang.chapter5ut.client.bean.ServerInfo;

/**
 * @author: yangjinpeng
 * @date: 2019-01-20
 * @description:
 */
public class Client {

    public static void main(String[] args) {
        // 搜索服务器端，超时时间为10s
        ServerInfo info = ClientProvider.searchServer(10000);
        System.out.println("serverInfo: " + info);

    }
}
