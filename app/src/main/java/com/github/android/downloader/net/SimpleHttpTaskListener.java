package com.github.android.downloader.net;

import com.github.android.downloader.bean.DownloadInfo;

/**
 * Created by zl on 2015/1/31.
 */
public class SimpleHttpTaskListener implements HttpTaskListener {
    
    
    
    @Override
    public void onStart(DownloadInfo downloadInfo) {

    }

    @Override
    public void onInterruption(DownloadInfo downloadInfo, boolean isNormal) throws Exception {

    }

    @Override
    public void onDownloading(DownloadInfo downloadInfo) {

    }

    @Override
    public void onRetry(int retry, long realSize) {

    }

    @Override
    public void onSuccess(DownloadInfo downloadInfo) {

    }

    @Override
    public void onFail(DownloadInfo downloadInfo, int error) {

    }

    @Override
    public void onFinish(DownloadInfo downloadInfo) {

    }
}
