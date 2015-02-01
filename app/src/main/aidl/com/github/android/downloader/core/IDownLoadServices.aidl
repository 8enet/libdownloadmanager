// IDownLoadServices.aidl
package com.github.android.downloader.core;

import  com.github.android.downloader.bean.DownloadFile;


interface IDownLoadServices {
   void addDownLoadTask(inout DownloadFile dFile);
}
