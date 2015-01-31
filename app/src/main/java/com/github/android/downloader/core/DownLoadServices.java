package com.github.android.downloader.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by zl on 2015/1/31.
 */
public class DownLoadServices extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
