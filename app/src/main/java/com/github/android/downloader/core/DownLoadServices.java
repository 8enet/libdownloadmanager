package com.github.android.downloader.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.github.android.downloader.bean.DownloadFile;
import com.github.android.downloader.bean.DownloadInfo;

/**
 * Created by zl on 2015/1/31.
 */
public class DownLoadServices extends Service {

    private DownLoadManager mDownLoadManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        mDownLoadManager=new DownLoadManager(this);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mService.asBinder();
    }
    
    private IDownLoadServices.Stub mService=new IDownLoadServices.Stub() {

        @Override
        public void addDownLoadTask(DownloadFile dFile, IDownloadListener listener) throws RemoteException {
            if(mDownLoadManager != null){
                mDownLoadManager.addDownLoadTask(dFile,listener);
            }
        }


        @Override
        public void stopDownLoadTask(String downUrl) throws RemoteException {
            if(mDownLoadManager != null){
                mDownLoadManager.stopDownLoadTask(downUrl);
            }
        }

        @Override
        public void resumeDownLoadTask(String downUrl) throws RemoteException {
            if(mDownLoadManager != null){
                mDownLoadManager.resumeDownLoadTask(downUrl);
            }
        }
    };
}
