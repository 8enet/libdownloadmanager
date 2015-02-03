
package com.github.android.downloader.core;

import com.github.android.downloader.bean.DownloadInfo;

interface IDownloadListener {
        void onStart();

        void onDownloading(long total,long curr,float speed,float perc);

        void onFail();
        
        void onPause(out List<DownloadInfo> dInfos);
        
        void onResume();

        void onSuccess(out DownloadInfo dInfo);

        void onCancel(out DownloadInfo dInfo);

        void onFinsh(out DownloadInfo dInfo);
}
