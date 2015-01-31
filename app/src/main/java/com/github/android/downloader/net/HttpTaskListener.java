package com.github.android.downloader.net;



import com.github.android.downloader.bean.DownloadInfo;

/**
 * this listener callbcak invok not in main thread.
 */
public interface HttpTaskListener {
    void onStart(DownloadInfo downloadInfo);
    
    void onInterruption(DownloadInfo downloadInfo,boolean isNormal) throws Exception;
    
    void onDownloading(DownloadInfo downloadInfo);
    
    void onRetry(int retry,long realSize);
    
    void onSuccess(DownloadInfo downloadInfo);
    
    void onFail(DownloadInfo downloadInfo,int error);
    
    void onFinish(DownloadInfo downloadInfo);
    
}
