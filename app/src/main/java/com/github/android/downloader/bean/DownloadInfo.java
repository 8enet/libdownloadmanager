package com.github.android.downloader.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zl on 2015/1/31.
 */
public class DownloadInfo implements Parcelable {

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

    public String tempFile;

    public int status;


    
    private boolean running=true;
    private Parcelable tag;
    
    

    public int retry=3;
    
    public Object getTag() {
        return tag;
    }

    public void setTag(Parcelable tag) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.downloadFile, 0);
        dest.writeLong(this.startByte);
        dest.writeLong(this.endByte);
        dest.writeLong(this.realByte);
        dest.writeInt(this.addByte);
        dest.writeInt(this.status);
        dest.writeByte(running ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.tag, flags);
        dest.writeInt(this.retry);
    }

    private DownloadInfo(Parcel in) {
        readFromParcel(in);
    }

    public static final Parcelable.Creator<DownloadInfo> CREATOR = new Parcelable.Creator<DownloadInfo>() {
        public DownloadInfo createFromParcel(Parcel source) {
            return new DownloadInfo(source);
        }

        public DownloadInfo[] newArray(int size) {
            return new DownloadInfo[size];
        }
    };
    
    public void readFromParcel(Parcel in){

        this.downloadFile = in.readParcelable(DownloadFile.class.getClassLoader());
        this.startByte = in.readLong();
        this.endByte = in.readLong();
        this.realByte = in.readLong();
        this.addByte = in.readInt();
        this.status = in.readInt();
        this.running = in.readByte() != 0;
        this.tag = in.readParcelable(Object.class.getClassLoader());
        this.retry = in.readInt();
    }
}
