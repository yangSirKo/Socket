package com.ccyang.chapter3udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * @author: yangjinpeng
 * @date: 2019-01-10
 * @description: UDP提供者，用于提供服务
 */
public class UDPProvider {

    public static void main(String[] args) throws IOException {

        // 生成一份唯一标识
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();

        // 读取到任意字符就退出
        System.in.read();
        provider.exit();
    }

    private static class Provider extends Thread{

        private final String sn;
        private boolean done = false;
        private DatagramSocket ds;

        public Provider(String sn){
            super();
            this.sn = sn;
        }

        @Override
        public void run() {
            System.out.println("UDPProvider started...");
            try {
                // 作为接收者，指定一个端口用于数据接收
                ds = new DatagramSocket(20000);
                while (!done){

                    // 构建接收实体
                    final byte[] buf = new byte[512];
                    DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

                    // 接收
                    ds.receive(receivePacket);

                    // 打印接收到的信息与发送者信息
                    // 发送者的ip
                    String ip = receivePacket.getAddress().getHostAddress();
                    int port = receivePacket.getPort();
                    int dataLen = receivePacket.getLength();
                    String data = new String(receivePacket.getData(), 0, dataLen);
                    System.out.println("UDPProvider receive from ip: " + ip +
                            "  port: " + port + " data:" + data);

                    // 解析端口号
                    int responsePort =  MessageCreator.parsePort(data);
                    if (responsePort != -1) {
                        // 构建一个回送数据
                        String responseData = MessageCreator.buildWithSn(sn);
                        byte[] responseDataBytes = responseData.getBytes();
                        // 直接更加发送者构建一份回送信息
                        DatagramPacket responsePacket =
                                new DatagramPacket(responseDataBytes, 0, responseDataBytes.length,
                                        receivePacket.getAddress(), responsePort);
                        // 发送
                        ds.send(responsePacket);
                    }
                }
            }catch (Exception ignore){
            }finally {
                close();
            }
            System.out.println("Provider finished.");
        }

        private void close(){
            if (ds != null){
                ds.close();
                ds = null;
            }
        }

        private void exit(){
            done = true;
            close();
        }
    }

}
