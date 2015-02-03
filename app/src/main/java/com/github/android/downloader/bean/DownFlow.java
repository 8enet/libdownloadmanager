package com.github.android.downloader.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.android.downloader.db.annotation.Id;

/**
 * Created by zhaocheng on 2015/2/3.
 */
public class DownFlow implements Parcelable {
    @Id
    public long _id;
    public String name;
    public long downTime;
    public long byteSize; //kb

    public DownFlow(Parcel parcel) {
        readFromParcel(parcel);
    }

    public void readFromParcel(Parcel in) {
        this._id = in.readLong();
        this.name = in.readString();
        this.downTime = in.readLong();
        this.byteSize = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this._id);
        parcel.writeString(this.name);
        parcel.writeLong(this.downTime);
        parcel.writeLong(this.byteSize);
    }

    public static final Creator<DownFlow> CREATOR = new Creator<DownFlow>() {
        @Override
        public DownFlow createFromParcel(Parcel parcel) {
            return new DownFlow(parcel);
        }

        @Override
        public DownFlow[] newArray(int i) {
            return new DownFlow[i];
        }
    };
}
