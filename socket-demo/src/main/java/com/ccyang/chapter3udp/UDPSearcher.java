package com.ccyang.chapter3udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * UDP搜索者，用于搜索服务支持方
 * @author: yangjinpeng
 * @date: 2019-01-10
 * @description:
 */
public class UDPSearcher {

    public static final int LISTEN_PORT = 30000;
    
    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("UDPSearcher sendBroadCast started...");

        Listener listener = listen();
        sendBroadCast();

        // 读到键盘任意信息后退出
        System.in.read();
        List<Device> devices = listener.getDevicesAndClose();
        for (Device device : devices) {
            System.out.println("device: " + device);
        }
        // 完成
        System.out.println("UDPSearcher finished");
    }

    private static Listener listen() throws InterruptedException {
        System.out.println("UDPSearcher listen started...");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, countDownLatch);
        listener.start();

        countDownLatch.await();
        return listener;
    }

    public static void sendBroadCast() throws IOException {
        System.out.println("UDPSearcher sendBroadCast started...");

        // 作为搜索者，让系统自动分配端口
        DatagramSocket ds = new DatagramSocket();

        // 构建一个发送数据
        String requestData = MessageCreator.buildWithPort(LISTEN_PORT);
        byte[] requestDataBytes = requestData.getBytes();
        // 直接更加发送者构建一份回送信息
        DatagramPacket requestPacket = new DatagramPacket(requestDataBytes,0 ,requestDataBytes.length);
        // 20000地址, 广播地址
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPacket.setPort(20000);

        // 发送
        ds.send(requestPacket);

        ds.close();
        System.out.println("UDPSearcher sendBroadCast finished.");
    }

    private static class Device {
        final int port;
        final String ip;
        final String sn;

        public Device(int port, String ip, String sn) {
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "port=" + port +
                    ", ip=" + ip +
                    ", sn=" + sn +
                    '}';
        }
    }

    private static class Listener extends Thread{

        private final int listenPort;
        private final CountDownLatch countDownLatch;
        private final List<Device> devices = new ArrayList<>();
        private boolean done = false;
        private DatagramSocket ds = null;

        public Listener(int listenPort, CountDownLatch countDownLatch) {
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {

            // 通知主线程，监听已启动
            countDownLatch.countDown();
            try {
                ds = new DatagramSocket(listenPort);
                while (!done) {
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
                    System.out.println("UDPSearcher receive from ip: " + ip +
                            "  port: " + port + " data：" + data);

                    // 解析sn
                    String sn = MessageCreator.parseSn(data);
                    if (sn != null) {
                        Device device = new Device(port, ip, sn);
                        devices.add(device);
                    }
                }
            } catch (Exception e) {
            } finally {
                close();
            }
            System.out.println("UDPSearcher listener finished");
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        private List<Device> getDevicesAndClose() {
            done = true;
            close();
            return devices;
        }
    }
}
