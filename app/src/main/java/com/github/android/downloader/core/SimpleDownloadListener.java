package com.github.android.downloader.core;

import android.os.IBinder;
import android.os.RemoteException;

import com.github.android.downloader.bean.DownloadInfo;

/**
 * Created by zl on 15/2/2.
 */
public class SimpleDownloadListener implements IDownloadListener {
    @Override
    public void onStart() throws RemoteException {

    }

    @Override
    public void onDownloading(long total, long curr, float speed, float perc) throws RemoteException {

    }

    @Override
    public void onFail() throws RemoteException {

    }

    @Override
    public void onSuccess(DownloadInfo dInfo) throws RemoteException {

    }

    @Override
    public void onCancel(DownloadInfo dInfo) throws RemoteException {

    }

    @Override
    public void onFinsh(DownloadInfo dInfo) throws RemoteException {

    }

    @Override
    public IBinder asBinder() {
        return null;
    }
}
