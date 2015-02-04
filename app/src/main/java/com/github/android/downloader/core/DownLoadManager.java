package com.github.android.downloader.core;

import android.content.Context;
import android.util.Log;

import com.github.android.downloader.bean.DownloadFile;
import com.github.android.downloader.bean.DownloadInfo;
import com.github.android.downloader.net.HttpDownAnalysis;
import com.github.android.downloader.net.HttpDownloadTask;
import com.github.android.downloader.net.HttpTaskListener;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by zl on 2015/1/31.
 */
public class DownLoadManager {
    private static final String TAG="DownLoadManager";

    private ThreadPoolExecutor threadPoolExecutor;
    private Context mContext;
    
    DownLoadManager(Context mContext){
        this.mContext=mContext;
        threadPoolExecutor=new ThreadPoolExecutor(10,10,5, TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>());

    }

    private Map<String,DownLoadControllerAsync> downloadMap=new ConcurrentHashMap<String, DownLoadControllerAsync>();
    
    public void stopDownLoadTask(String downUrl){
        DownLoadControllerAsync async=downloadMap.get(downUrl);
        if(async != null){
            async.stopDownload();
        }
        
    }
    
    public void resumeDownLoadTask(String downUrl){

        
        DownLoadControllerAsync async=downloadMap.get(downUrl);
        List<DownloadInfo> dInfos=DownLoadControllerAsync.getDownInfos(downUrl);
        if(async != null && dInfos != null){
            async.restart();
            submitDownloadTask(downUrl,dInfos.toArray(new DownloadInfo[dInfos.size()]),async);
        }
    }

    public void addDownLoadTask(final DownloadFile dFile,final IDownloadListener listener){
        threadPoolExecutor.submit(new HttpDownAnalysis(dFile,new HttpDownAnalysis.OnDownloadAnalysis() {
            @Override
            public void onComplete(DownloadInfo[] infos) {
                if(infos != null){
                    int sz=infos.length;
                    try{
                        File file=new File(dFile.savePath);
                        file.delete();
                        if(!file.exists()){
                            if(file.createNewFile()){
                                Log.d(TAG," createNewFile  success "+dFile.savePath);
                            }else {
                                Log.e(TAG," createNewFile  fail !!! "+dFile.savePath);
                            }
                        }
                    }catch (Exception e){
                        Log.e(TAG,Log.getStackTraceString(e));
                    }

                    DownLoadControllerAsync async=new DownLoadControllerAsync(dFile,listener);
                    async.setCount(sz);
                    async.measureSpeed(true);
                    HttpTaskListener[] listeners=new HttpTaskListener[sz];
                    for(int i=0;i<sz;i++){
                        listeners[i]=async.creatHttpTaskListener();
                        threadPoolExecutor.submit(new HttpDownloadTask(infos[i],listeners[i]));
                    }
                    downloadMap.put(dFile.downUrl,async);
                }
            }
        }));
    }

    
    private void submitDownloadTask(String url, DownloadInfo[] infos,DownLoadControllerAsync async){
        int sz=infos.length;
        HttpTaskListener[] listeners=new HttpTaskListener[sz];
        DownloadInfo info=null;
        for(int i=0;i<sz;i++){
            info=infos[i];
            listeners[i]=async.creatHttpTaskListener();
            changeRange(info);
            threadPoolExecutor.submit(new HttpDownloadTask(info,listeners[i]));
        }
        downloadMap.put(url,async);
    }
    
    private void changeRange(DownloadInfo info){
        info.startByte+=info.realByte;
        info.realByte=0;
        info.retry=3;
        info.start();
    }
    
    
    

}

