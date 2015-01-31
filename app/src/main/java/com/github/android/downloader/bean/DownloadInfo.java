package com.github.android.downloader.bean;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zl on 2015/1/31.
 */
public class DownloadInfo {

    public static final int RANGE_NONE=-1;
    
    
    public static final int DOWNLOAD_FAIL=-1;

    public DownloadFile downloadFile;
    
    public DownloadInfo(){}
    
    public DownloadInfo(DownloadFile dFile,long startByte,long endByte){
        this.downloadFile=dFile;
        this.startByte=startByte;
        this.endByte=endByte;
        
    }

    public long startByte;

    public long endByte;

    public long realByte;
    
    public int addByte;


    public int status;


    
    private boolean running=true;
    private Object tag;
    
    

    public int retry=3;
    
    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }


    public void stop(){
        running=false;
    }

    public boolean isRunning(){
        return running;
    }




    public Map<String,String> getHeards(){
        return (downloadFile.requestParams != null?downloadFile.requestParams.getHeaders():null );

    }
    
    public boolean isSuccess(){
        return endByte==getCurrentByte();
        
    }
    
    
    public long getCurrentByte(){
        long c=startByte+realByte;
        return c>0?c:0;
    }
}
