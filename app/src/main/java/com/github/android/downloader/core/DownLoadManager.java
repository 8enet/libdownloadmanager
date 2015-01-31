package com.github.android.downloader.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.android.downloader.bean.DownloadFile;
import com.github.android.downloader.bean.DownloadInfo;
import com.github.android.downloader.net.HttpDownAnalysis;
import com.github.android.downloader.net.HttpDownloadTask;
import com.github.android.downloader.net.HttpTaskListener;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by zl on 2015/1/31.
 */
public class DownLoadManager {
    private static final String TAG="DownLoadManager";
    
    private volatile static DownLoadManager downLoadControllerAsync;

    public static DownLoadManager getInstance(){
        if(downLoadControllerAsync == null){
            synchronized (DownLoadControllerAsync.class){
                downLoadControllerAsync=new DownLoadManager();
            }
        }
        return downLoadControllerAsync;
    }

    private ThreadPoolExecutor threadPoolExecutor;
    private DownLoadManager(){
        threadPoolExecutor=new ThreadPoolExecutor(5,10,5, TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>());
    }


    public void addDownLoadTask(final DownloadFile dFile,final DownloadListener listener){
        
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
                    HttpTaskListener[] listeners=new HttpTaskListener[sz];
                    for(int i=0;i<sz;i++){
                        listeners[i]=async.creatHttpTaskListener();
                        threadPoolExecutor.submit(new HttpDownloadTask(infos[i],listeners[i]));
                    }


                }
            }
        }));
    }

   
    
}
