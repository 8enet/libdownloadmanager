package com.github.android.downloader.core;

import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;


import com.github.android.downloader.bean.DownloadFile;
import com.github.android.downloader.bean.DownloadInfo;
import com.github.android.downloader.net.HttpTaskListener;
import com.github.android.downloader.utils.TrafficSpeed;

import java.lang.ref.SoftReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zl on 2015/1/31.
 */
public class DownLoadControllerAsync {

    private static final String TAG="DownLoadControllerAsync";

    public DownLoadControllerAsync(DownloadFile dFile, IDownloadListener listener) {
        this.dFile = dFile;
        this.listener = listener;
    }

    private CountDownLatch countDownLatch;
    private DownloadFile dFile;
    private IDownloadListener listener;


    private AtomicLong current = new AtomicLong();
    private AtomicBoolean statisStart = new AtomicBoolean(true);
    private CountDownLatch statisSuccess;
    private SoftReference<MyHandler> handler;
    
    private volatile TrafficSpeed  trafficSpeed;

    private volatile boolean measureDownSpeed=false;

    public void setCount(int c) {
        this.countDownLatch = new CountDownLatch(c);
        this.statisSuccess = new CountDownLatch(c);
    }
    
    public void measureSpeed(boolean m){
        this.measureDownSpeed=m;
        if(measureDownSpeed){
            trafficSpeed=new TrafficSpeed();
        }
        
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
                                try {
                                    listener.onStart();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
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

                    double tsp=0;
                    if(measureDownSpeed && trafficSpeed!= null){
                        tsp=trafficSpeed.getSpeed(current.addAndGet(downloadInfo.addByte));
                    }else {
                        current.addAndGet(downloadInfo.addByte);
                    }
                    final double sp=tsp;
                    if (listener != null && downloadInfo != null)
                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                long c=current.get();
                                try {
                                    listener.onDownloading(dFile.fileSize, c, (float)sp, ((float)c)/((float) dFile.fileSize));
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
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
                                try {
                                    listener.onSuccess(downloadInfo);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                try {
                                    listener.onFail();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                listener.onFinsh(downloadInfo);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        };

    }

}
