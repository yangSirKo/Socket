package com.ccyang.chapter5ut.util;

/**
 * @author: yangjinpeng
 * @date: 2019-01-20
 * @description:
 */
public class ByteUtils {

    /**
     * 验证 res1 从第一个字节开始，是否和res2 匹配
     * eg：'a','b','v','d' & 'a','b','v'  =》 true
     * eg：'a','b','v','d' & 'a','b','c'  =》 false
     * @return
     */
    public static boolean startsWith(byte[] res1, byte[] res2) {

        if (!isBytesNotNull(res2)){
            return true;
        }

        if (!isBytesNotNull(res1)) {
            return false;
        }

        if(res1.length < res2.length) {
            return false;
        }

        for (int i = 0; i < res2.length; i++) {
            if (res1[i] != res2[i]){
                return false;
            }
        }

        return true;
    }

    /**
     * byte[] 转 short
     * @Param index: 第几位开始取
     */
    public static short byteArrayToShort(byte[] bytes, int index){
        return (short) ((bytes[index++] << 8) | (bytes[index++] & 0xff));
    }

    private static boolean isBytesNotNull(byte[] bytes){
        if (bytes == null) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {

        byte[] a = new byte[]{'a','b','v','d'};
        byte[] b = new byte[]{'a','b','v'};

        System.out.println(startsWith(a, b));

    }
}
