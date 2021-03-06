package com.github.android.downloader.core;

import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;


import com.github.android.downloader.bean.DownloadFile;
import com.github.android.downloader.bean.DownloadInfo;
import com.github.android.downloader.net.HttpTaskListener;
import com.github.android.downloader.utils.TrafficSpeed;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by zl on 2015/1/31.
 */
 class DownLoadControllerAsync implements Cloneable{

    private static final String TAG="DownLoadControllerAsync";

    public DownLoadControllerAsync(DownloadFile dFile, IDownloadListener listener) {
        this.dFile = dFile;
        this.listener = listener;
    }

    public static Map<String,List<DownloadInfo>> downInfosCache=null;
    
    private CountDownLatch countDownLatch;
    private DownloadFile dFile;
    private IDownloadListener listener;

    private AtomicLong current = new AtomicLong();
    private AtomicBoolean statisStart = new AtomicBoolean(true);
    private CountDownLatch statisSuccess;
    private CountDownLatch clStop;
    private List<DownloadInfo> mPauseInfos=new CopyOnWriteArrayList<DownloadInfo>();
    
    private SoftReference<MyHandler> handler;
    private volatile boolean running=true;
    
    private volatile TrafficSpeed  trafficSpeed;

    private volatile boolean measureDownSpeed=false;

    private  int sizeTask;
    
    public void setCount(int c) {
        sizeTask=c;
        this.countDownLatch = new CountDownLatch(sizeTask);
        this.statisSuccess = new CountDownLatch(sizeTask);
    }
    
    public void measureSpeed(boolean m){
        this.measureDownSpeed=m;
        if(measureDownSpeed){
            trafficSpeed=new TrafficSpeed();
        }
    }
    
    public void attachListener(IDownloadListener listener){
        this.listener=listener;
    }
    
    public void stopDownload(){
        running=false;
        clStop=new CountDownLatch(sizeTask);
        Log.d(TAG,"stopDownload --->> ");
        for(DownloadInfo dInfo:mPauseInfos){
            dInfo.stop();
        }
        
        if(mPauseInfos != null){
            mPauseInfos.clear();
        }
    }

    public void restart(){
        running=true;
        this.countDownLatch = new CountDownLatch(sizeTask);
        this.statisSuccess = new CountDownLatch(sizeTask);
        clStop=null;
        Log.d(TAG,"restart  running "+running+"   sizeTask "+sizeTask);
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
                mPauseInfos.add(downloadInfo);
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
                if(isNormal && clStop != null){
                    clStop.countDown();
                    mPauseInfos.add(downloadInfo);
                    if(clStop.getCount() == 0 && listener != null){
                        
                        if(downInfosCache == null){
                            downInfosCache=new ConcurrentHashMap<String, List<DownloadInfo>>(5);
                        }

                        getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    downInfosCache.put(dFile.downUrl,mPauseInfos);
                                    listener.onPause(mPauseInfos);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
                
            }

            @Override
            public void onDownloading(final DownloadInfo downloadInfo) {
                if (downloadInfo != null) {

                    if(!running && downloadInfo.isRunning()){
                        downloadInfo.stop();
                    }

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
                Log.d(TAG,"  onSuccess    --->> "+downloadInfo);
                
            }

            @Override
            public void onFail(DownloadInfo downloadInfo, int error) {
                Log.d(TAG,"  onFail    --->> "+downloadInfo);
            }

            @Override
            public void onFinish(final DownloadInfo downloadInfo) {
                countDownLatch.countDown();
                if (countDownLatch.getCount() == 0 && listener != null) {
                    final boolean suc=statisSuccess.getCount() == 0;
                    
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (suc) {
                                try {
                                    listener.onSuccess(dFile);
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
                                listener.onFinsh(dFile);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        };

    }

    
    public static List<DownloadInfo> getDownInfos(String url){
        if(downInfosCache == null){
            return null;
        }
        return downInfosCache.get(url);
        
    }
    
    public static void removeDownInfos(String url){
        if(downInfosCache == null){
            downInfosCache.remove(url);
        }
    }
}
