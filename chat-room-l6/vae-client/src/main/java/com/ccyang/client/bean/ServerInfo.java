package com.ccyang.client.bean;

/**
 * @author: yangjinpeng
 * @date: 2019-01-20
 * @description:
 */
public class ServerInfo {

    private String sn;
    private int port;
    private String address;

    public ServerInfo(String sn, String ip, int port) {
        this.sn = sn;
        this.port = port;
        this.address = ip;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "sn='" + sn + '\'' +
                ", port=" + port +
                ", address='" + address + '\'' +
                '}';
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
