package com.ccyang.chapter4tcp;

import java.util.Arrays;

/**
 * @author: yangjinpeng
 * @date: 2019-01-17
 * @description: byte数字 和 int型的转换
 */
public class Tools {

    public static int byteArrayToInt(byte[] b){
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a){
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public static void main(String[] args) {
        int i = byteArrayToInt(new byte[] {1, 1, 1, 1});
        System.out.println(i);

        System.out.println(Arrays.toString(intToByteArray(1)));

    }
}
