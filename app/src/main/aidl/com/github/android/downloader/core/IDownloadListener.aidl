
package com.github.android.downloader.core;

import com.github.android.downloader.bean.DownloadInfo;
import  com.github.android.downloader.bean.DownloadFile;

interface IDownloadListener {
        void onStart();

        void onDownloading(long total,long curr,float speed,float perc);

        void onFail();
        
        void onPause(out List<DownloadInfo> dInfos);
        
        void onResume();

        void onSuccess(out DownloadFile dInfo);

        void onCancel(out DownloadInfo dInfo);

        void onFinsh(out DownloadFile dInfo);
}
