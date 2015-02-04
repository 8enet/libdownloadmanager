package com.github.android.downloader.net;

import android.text.TextUtils;
import android.util.Log;

import com.github.android.downloader.bean.DownloadFile;
import com.github.android.downloader.bean.DownloadInfo;
import com.github.android.downloader.io.FilePartAction;
import com.github.android.downloader.io.FileUtils;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;


/**
 * Analysis multithread download broken range
 * Created by zl on 2015/1/31.
 */
public class HttpDownAnalysis implements Runnable {

    private static final String TAG="HttpDownAnalysis";
    
    private DownloadFile dFile;

    private static final int SEGM_REFERENCE = 1024 * 1024 * 3; //3M
    private static final int MAX_SEGM_TASK = 3;

    public HttpDownAnalysis(DownloadFile dFile,OnDownloadAnalysis mAnalysis) {
        if (dFile == null || TextUtils.isEmpty(dFile.downUrl)) {
            throw new IllegalArgumentException("download url  is empty !!!");
        }
        this.dFile = dFile;
        this.mAnalysis=mAnalysis;
    }
    
    private OnDownloadAnalysis mAnalysis;

    private FilePartAction mFilePartAction;

    public void setFilePartAction(FilePartAction action) {
        this.mFilePartAction = action;
    }


    @Override
    public void run()  {
        HttpURLConnection connection=null;
        try {
             connection = (HttpURLConnection) new URL(dFile.downUrl).openConnection();
            if(dFile.requestParams != null && dFile.requestParams.getHeaders() != null){
                Set<Map.Entry<String, String>> entries = dFile.requestParams.getHeaders().entrySet();
                for(Map.Entry<String, String> entry:entries){
                    connection.addRequestProperty(entry.getKey(),entry.getValue());
                }
            }
            int code=connection.getResponseCode();
            Log.d(TAG," http response code  "+code);
            if (code == HttpURLConnection.HTTP_OK) {

                int length = connection.getContentLength();

                Log.d(TAG," http content length  "+length);
                dFile.fileSize=length;
                if(mAnalysis == null){
                    return;
                }
                
                if (mFilePartAction == null) {
                    mFilePartAction = new DefaultFilePartAction();
                }
               
                if(mAnalysis != null){
                    mAnalysis.onComplete(mFilePartAction.partTask(dFile,length));
                }

            }
            
        }catch (Exception e){
            Log.e(TAG,Log.getStackTraceString(e));
        }finally {
            if(connection != null){
                connection.disconnect();
            }
        }
    }

    static class DefaultFilePartAction implements FilePartAction {

        @Override
        public DownloadInfo[] partTask(DownloadFile dFile, int length) {
            int s = length / SEGM_REFERENCE+1;
            if(s == 1){
                return new DownloadInfo[]{new DownloadInfo(dFile,0,DownloadInfo.RANGE_NONE)};
            }
            if(s > MAX_SEGM_TASK){
                s=MAX_SEGM_TASK;
            }
            int p=length/s;
            DownloadInfo[] infos = new DownloadInfo[s];
            infos[0]=new DownloadInfo(dFile,0,p);
            for(int i=1;i<s-1;i++){
                infos[i]=new DownloadInfo(dFile,(p*i+i),(p*(i+1)+i));
            }
            infos[s-1]=new DownloadInfo(dFile,(p*(s-1)+(s-1)),DownloadInfo.RANGE_NONE);
            return infos;
        }
    }
    
    
    public static interface OnDownloadAnalysis{
        
      void onComplete(DownloadInfo[] infos);
    }
}
