package com.github.android.downloader.io;

import java.io.Closeable;

/**
 * Created by zl on 15/2/3.
 */
public class FileUtils {

    public static void closeQuietly(final Closeable... closeable) {
        if (closeable != null) {
            for (Closeable cls : closeable) {
                try {
                    if (cls != null)
                        cls.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
