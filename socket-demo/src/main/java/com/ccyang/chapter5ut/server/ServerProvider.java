package com.ccyang.chapter5ut.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

import com.ccyang.chapter5ut.constants.UDPConstants;
import com.ccyang.chapter5ut.util.ByteUtils;

/**
 * @author: yangjinpeng
 * @date: 2019-01-20
 * @description: 服务提供者
 */
public class ServerProvider {

    private static Provider PROVIDER_INSTANCE ;

    /**
     * 启动服务端进行监听
     */
    public static void start(int serverPort){
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, serverPort);
        provider.start();
        PROVIDER_INSTANCE = provider;
    }

    /**
     * 停止服务端监听
     */
    public static void stop() {
        if (PROVIDER_INSTANCE != null){
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    /**
     * 服务端监听类，提供服务
     */
    public static class Provider extends Thread {

        private final byte[] sn;
        private final int port;
        private static boolean done = false;
        private static DatagramSocket ds = null;
        // 存储消息的buf
        private final byte[] buffer = new byte[128];

        public Provider(String sn, int port) {
            this.sn = sn.getBytes();
            this.port = port;
        }

        @Override
        public void run() {
            System.out.println("UDPProvider started.");

            try {
                // 监听30201端口, 用来监听客户端的UDP消息
                ds = new DatagramSocket(UDPConstants.PORT_SERVER_UDP);
                // 接收消息的 Packet
                DatagramPacket receivePacket = new DatagramPacket(buffer, 0, buffer.length);

                while(!done) {
                    // 接收
                    ds.receive(receivePacket);

                    // 打印接收到的信息与发送者的信息
                    // 发送者的 IP地址
                    String clientIp = receivePacket.getAddress().getHostAddress();
                    int clientPort = receivePacket.getPort();
                    int clientDataLength = receivePacket.getLength();
                    byte[] clientData = receivePacket.getData();
                    // 验证是否是我们和客户端约定好的消息
                    // 2是指short类型的一个头部标识，short类型长度为2个字节；4是指int型的端口号，占4个byte
                    boolean isValid = clientData.length >= (UDPConstants.HEADER.length + 2 + 4)
                            && ByteUtils.startsWith(clientData, UDPConstants.HEADER);

                    System.out.println("ServerProvider receive from ip: " + clientIp +
                            " Port: " + clientPort + " isValid: " + isValid);

                    if (!isValid) {
                        // 不是我们要约定好的消息，无效继续
                        continue;
                    }

                    // 解析命令与回送端口，用以后续建立tcp连接
                    int index = UDPConstants.HEADER.length;
                    short cmd = ByteUtils.byteArrayToShort(clientData, index);
                    index += 2;
                    int responsePort = (((clientData[index++] << 24) |
                                                 ((clientData[index++] & 0xff) << 16) |
                                                 ((clientData[index++] & 0xff) << 8) |
                                                 (clientData[index++] & 0xff)));
                    // 判断合法行，cmd是约定好的
                    if (cmd == 1 && responsePort > 0) {
                        // 构建一份回送数据
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short) 2);  // 约定好的2
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);
                        int len = byteBuffer.position();

                        // 直接根据发送者构建一份回送信息
                        DatagramPacket packet = new DatagramPacket(buffer, len,
                                receivePacket.getAddress(), UDPConstants.PORT_CLIENT_RESPONSE);
                        ds.send(packet);

                        System.out.println("ServerProvider response to: " + clientIp + " port: " + UDPConstants.PORT_CLIENT_RESPONSE +
                                " dataLen: " + len);

                    } else {
                        System.out.println("ServerProvider receive cmd nonsupport; cmd: " + cmd + " port: " + clientPort);
                    }
                }

            } catch (Exception e) {
            } finally {
                ds.close();
            }
        }

        private void close() {
            if (ds != null){
                ds.close();
                ds = null;
            }
        }

        /**
         * 提供接收退出
         */
        private void exit() {
            done = true;
            ds.close();
        }
    }
}
