package com.github.android.downloader.core;

import com.github.android.downloader.bean.DownloadInfo;

/**
 * this callback invok in ui thread !!!
 * Created by zl on 2015/1/31.
 */
public interface DownloadListener {
    void onStart();
    
    void onDownloading(long total,long curr,float speed,float perc);
    
    void onFail();
    
    void onSuccess(DownloadInfo dInfo);
    
    void onCancel(DownloadInfo dInfo);
    
    void onFinsh(DownloadInfo dInfo);
    
}
