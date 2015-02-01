package com.github.android.downloader.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.android.downloader.bean.DownloadFile;
import com.github.android.downloader.bean.DownloadInfo;
import com.github.android.downloader.net.HttpTaskListener;

import java.lang.ref.SoftReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zl on 2015/1/31.
 */
public class DownLoadControllerAsync {

    private static final String TAG="DownLoadControllerAsync";

    public DownLoadControllerAsync(DownloadFile dFile, DownloadListener listener) {
        this.dFile = dFile;
        this.listener = listener;
    }

    private CountDownLatch countDownLatch;
    private DownloadFile dFile;
    private DownloadListener listener;


    private AtomicLong current = new AtomicLong();
    private AtomicBoolean statisStart = new AtomicBoolean(true);
    private CountDownLatch statisSuccess;
    private SoftReference<MyHandler> handler;


    public void setCount(int c) {
        this.countDownLatch = new CountDownLatch(c);
        this.statisSuccess = new CountDownLatch(c);
    }


    private Handler getHandler() {
        MyHandler h = null;
        if (handler == null) {
            h = new MyHandler(Looper.getMainLooper());
            handler = new SoftReference<MyHandler>(h);
            return h;
        }
        h = handler.get();
        if (h != null) {
            return h;
        }
        h = new MyHandler(Looper.getMainLooper());

        handler = new SoftReference<MyHandler>(h);
        return h;
    }

    static class MyHandler extends Handler {

        MyHandler() {
        }

        MyHandler(Looper looper) {
            super(looper);
        }


    }


    public HttpTaskListener creatHttpTaskListener() {

        return new HttpTaskListener() {
            @Override
            public void onStart(DownloadInfo downloadInfo) {
                if (statisStart.get()) {
                    statisStart.set(false);
                    if (listener != null)
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onStart();
                            }
                        });
                }
            }

            @Override
            public void onInterruption(DownloadInfo downloadInfo, boolean isNormal) throws Exception {

            }

            @Override
            public void onDownloading(final DownloadInfo downloadInfo) {
                if (downloadInfo != null) {
                    current.addAndGet(downloadInfo.addByte);
                    if (listener != null && downloadInfo != null)
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                long c=current.get();
                                listener.onDownloading(dFile.fileSize, c, 0, ((float)c)/((float) dFile.fileSize));
                            }
                        });
                }
            }

            @Override
            public void onRetry(int retry, long realSize) {

            }

            @Override
            public void onSuccess(DownloadInfo downloadInfo) {
                statisSuccess.countDown();
            }

            @Override
            public void onFail(DownloadInfo downloadInfo, int error) {

            }

            @Override
            public void onFinish(final DownloadInfo downloadInfo) {
                countDownLatch.countDown();
                if (countDownLatch.getCount() == 0 && listener != null) {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (statisSuccess.getCount() == 0) {
                                listener.onSuccess(downloadInfo);
                            }else {
                                listener.onFail();
                            }
                            listener.onFinsh(downloadInfo);
                        }
                    });
                }
            }
        };

    }

}
