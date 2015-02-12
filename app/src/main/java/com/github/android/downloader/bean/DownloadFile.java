package com.github.android.downloader.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;


public class DownloadFile implements Parcelable {
    public String downUrl;
    public String savePath;
    public String fileName;
    public long fileSize;
    public long currentSize;
    public int status;
    public RequestParams requestParams;

    public DownloadFile() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.downUrl);
        dest.writeString(this.savePath);
        dest.writeString(this.fileName);
        dest.writeLong(this.fileSize);
        dest.writeLong(this.currentSize);
        dest.writeInt(this.status);
        dest.writeParcelable(this.requestParams, 0);
    }

    private DownloadFile(Parcel in) {
        readFromParcel(in);
    }

    public static final Creator<DownloadFile> CREATOR = new Creator<DownloadFile>() {
        public DownloadFile createFromParcel(Parcel source) {
            return new DownloadFile(source);
        }

        public DownloadFile[] newArray(int size) {
            return new DownloadFile[size];
        }
    };
    
    
    public void readFromParcel(Parcel in){
        this.downUrl = in.readString();
        this.savePath = in.readString();
        this.fileName = in.readString();
        this.fileSize = in.readLong();
        this.currentSize = in.readLong();
        this.status = in.readInt();
        this.requestParams = in.readParcelable(RequestParams.class.getClassLoader());
    }
}
