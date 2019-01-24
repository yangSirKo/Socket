package com.ccyang.chapter5utchannel.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author: yangjinpeng
 * @date: 2019-01-22
 * @description:
 */
public class CloseUtils {

    public static void close(Closeable... closeables){
        if (closeables == null) {
            return;
        }

        for (Closeable closeable : closeables){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
