package com.ccyang.chapter3udp;

/**
 * @author: yangjinpeng
 * @date: 2019-01-10
 * @description: 消息创建者
 */
public class MessageCreator {

    private static final String SN_HEADER = "收到口令，我是(SN):";
    private static final String PORT_HEADER = "这是口令，请回电端口(port):";

    public static String buildWithPort(int port){
        return PORT_HEADER + port;
    }

    public static int parsePort(String data) {
        if(data.startsWith(PORT_HEADER)){
            return Integer.parseInt(data.substring(PORT_HEADER.length()));
        }
        return -1;
    }

    public static String buildWithSn(String sn){
        return SN_HEADER + sn;
    }

    public static String parseSn(String data){
        if(data.startsWith(SN_HEADER)){
            return data.substring(SN_HEADER.length());
        }
        return null;
    }

    public static void main(String[] args) {
        String ss = "这是口令，请回电端口(port):30000";
        System.out.println(parsePort(ss));
    }

}
