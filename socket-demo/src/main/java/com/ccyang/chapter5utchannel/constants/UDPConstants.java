package com.ccyang.chapter5utchannel.constants;

/**
 * @author: yangjinpeng
 * @date: 2019-01-20
 * @description: udp 常量配置
 */
public class UDPConstants {

    // 公用头部
    public static byte[] HEADER = new byte[]{7, 7, 7, 7, 7, 7, 7, 7};

    // 服务器固定UDP接收端口
    public static int PORT_SERVER_UDP = 30201;

    // 客户端的回送端口
    public static int PORT_CLIENT_RESPONSE = 30202;

}
