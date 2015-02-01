// IDownLoadServices.aidl
package com.github.android.downloader.core;

import  com.github.android.downloader.bean.DownloadFile;
import  com.github.android.downloader.core.IDownloadListener;

interface IDownLoadServices {
   void addDownLoadTask(inout DownloadFile dFile,IDownloadListener listener);
   

}
