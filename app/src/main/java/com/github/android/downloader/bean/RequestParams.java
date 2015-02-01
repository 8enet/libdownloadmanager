package com.github.android.downloader.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.android.downloader.net.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zl on 2015/1/31.
 */
public class RequestParams implements Parcelable {
    private Map<String,String> params;

    public RequestParams(Map<String,String> params){
        this.params=params;
    }
    
    public RequestParams(){
        params=new HashMap<String, String>(5);
        
    }
    
    public void setUserAgent(String userAgent){
        params.put(HttpHeaders.USER_AGENT,userAgent);
    }
    
    public void setHost(String host){
        params.put(HttpHeaders.HOST,host);
    }
    
    
    public void setReferer(String referer){
        params.put(HttpHeaders.REFERER,referer);
    }
    
    
    public void setAccept(String accept){
        params.put(HttpHeaders.ACCEPT,accept);
    }
    
    
    public void setCookie(String cookie){
        params.put(HttpHeaders.COOKIE,cookie);
    }
    
    public void setAcceptEncoding(String acceptEncoding){
        params.put(HttpHeaders.ACCEPT_ENCODING,acceptEncoding); 
    }
    
   
    
    public Map<String,String> getHeaders(){
        return params;
        
    }


    public static RequestParams buildTestParams(){
        RequestParams params=new RequestParams();
        params.setHost("gdown.baidu.com");
        params.setAccept("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        params.setUserAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.93 Safari/537.36");
        params.setReferer("http://shouji.baidu.com/soft/item?docid=7462432&from=as&f=all%40softwarerec%401");
        params.setAcceptEncoding("gzip, deflate, sdch");
        return params;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeMap(this.params);
    }

    private RequestParams(Parcel in) {
        this.params = in.readHashMap(HashMap.class.getClassLoader());
    }

    public static final Parcelable.Creator<RequestParams> CREATOR = new Parcelable.Creator<RequestParams>() {
        public RequestParams createFromParcel(Parcel source) {
            return new RequestParams(source);
        }

        public RequestParams[] newArray(int size) {
            return new RequestParams[size];
        }
    };
}
