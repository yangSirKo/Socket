package com.ccyang.chapter5ut.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.ccyang.chapter3udp.MessageCreator;
import com.ccyang.chapter5ut.client.bean.ServerInfo;
import com.ccyang.chapter5ut.constants.UDPConstants;
import com.ccyang.chapter5ut.util.ByteUtils;

/**
 * @author: yangjinpeng
 * @date: 2019-01-20
 * @description: 客户端提供者
 */
public class ClientProvider {

    private final static int LINSEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE;

    public static ServerInfo searchServer(long timeout){
        System.out.println("UDPSearcher started");

        // 成功收到消息时回送的栅栏
        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listener = null;
        try {
            // 打开监听
            listener = listen(receiveLatch);
            // 发送广播
            sendBroadcast();
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch(Exception e) {
            e.printStackTrace();
        }
        // 完成
        System.out.println("UDPSearcher finished");
        if (listener == null) {
            return null;
        }
        // 获取服务端信息并关闭监听
        List<ServerInfo> devices = listener.getServerAndClose();
        if (devices.size() > 0) {
            return devices.get(0);
        }
        return null;
    }

    /**
     * 启动客户端监听
     */
    private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {
        System.out.println("UDPSearcher start listen");
        // 让主线程等待Listener线程启动
        CountDownLatch startDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LINSEN_PORT, startDownLatch, receiveLatch);
        listener.start();
        startDownLatch.await();
        return listener;
    }

    /**
     * 发送广播
     */
    private static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher sendBroadCast started.");

        // 作为搜索者，让系统自动分配端口
        DatagramSocket ds = new DatagramSocket();

        // 构建一个发送数据
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        // 头部
        byteBuffer.put(UDPConstants.HEADER);
        // cmd命名
        byteBuffer.putShort((short) 1);
        // 回送端口信息
        byteBuffer.putInt(LINSEN_PORT);
        // 构建packet
        DatagramPacket requestPacket = new DatagramPacket(byteBuffer.array(), byteBuffer.position()+1);
        // 30201地址, 广播地址
        requestPacket.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPacket.setPort(UDPConstants.PORT_SERVER_UDP);

        // 发送
        ds.send(requestPacket);

        ds.close();
        System.out.println("UDPSearcher sendBroadCast finished.");
    }

    public static class Listener extends Thread {

        private CountDownLatch startDownLatch;
        private CountDownLatch receiveDownLatch;
        private final int listenPort;
        private final List<ServerInfo> serverInfoList = new ArrayList<>();
        private final byte[] buffer = new byte[128];
        private final int minLen = UDPConstants.HEADER.length + 2 + 4;
        private boolean done = false;
        private DatagramSocket ds = null;

        public Listener(int port, CountDownLatch startDownLatch, CountDownLatch receiveLatch) {
            this.listenPort = port;
            this.startDownLatch = startDownLatch;
            this.receiveDownLatch = receiveLatch;
        }

        @Override
        public void run() {
            // 通知已启动
            startDownLatch.countDown();
            try {
                // 监听回送消息
                ds = new DatagramSocket(listenPort);
                // 构建接收实体
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                while (!done) {
                    // 接收
                    ds.receive(receivePacket);

                    // 打印接收到的信息与发送者的信息
                    // 发送者的 IP地址
                    String ip = receivePacket.getAddress().getHostAddress();
                    int port = receivePacket.getPort();
                    int dataLength = receivePacket.getLength();
                    byte[] data = receivePacket.getData();
                    // 验证是否是我们和客户端约定好的消息
                    // 2是指short类型的一个头部标识，short类型长度为2个字节；4是指int型的端口号，占4个byte
                    boolean isValid = data.length >= minLen
                            && ByteUtils.startsWith(data, UDPConstants.HEADER);

                    System.out.println("ServerSearcher receive from ip: " + ip +
                            " Port: " + port + " isValid: " + isValid);

                    if (!isValid) {
                        // 不是我们要约定好的消息，无效继续
                        continue;
                    }

                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, UDPConstants.HEADER.length, dataLength);
                    // 解析命令与回送端口，用以后续建立tcp连接
                    final short cmd = (short) byteBuffer.getShort();
                    final int serverPort = byteBuffer.getInt();
                    if (cmd != 2 && serverPort <= 0) {
                        System.out.println("ServerSearcher receive cmd nonsupport; cmd: " + cmd + "port: " + serverPort);
                        continue;
                    }

                    String sn = new String(buffer, minLen, dataLength - minLen);
                    ServerInfo serverInfo = new ServerInfo(sn, ip, serverPort);
                    serverInfoList.add(serverInfo);
                    // 成功接收到一份
                    receiveDownLatch.countDown();
                }
            } catch (Exception e) {

            } finally {
                close();
            }
            System.out.println("UDPSearcher finish listen");
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        /**
         * 关闭并返回服务端信息
         * @return
         */
        public List<ServerInfo> getServerAndClose() {
            done = true;
            close();
            return serverInfoList;
        }
    }
}
