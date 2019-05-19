package net.wang.clink.utils;

import java.io.Closeable;
import java.io.IOException;

public class CloseUtils {

    public static void close(Closeable... closeable) {
        if (closeable == null) {
            return;
        }
        for (Closeable close : closeable) {
            try {
                close.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
