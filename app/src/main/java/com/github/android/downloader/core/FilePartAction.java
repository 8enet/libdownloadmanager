package com.github.android.downloader.core;

import com.github.android.downloader.bean.DownloadFile;
import com.github.android.downloader.bean.DownloadInfo;

/**
 * Created by zl on 2015/1/31.
 */
public interface FilePartAction {
    DownloadInfo[] partTask(DownloadFile dFile, int length);
}
