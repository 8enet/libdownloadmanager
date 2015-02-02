package com.github.android.downloader.utils;


import java.util.concurrent.atomic.AtomicLong;

/**
 * download file network speed
 */
public final class TrafficSpeed {
    private static final String TAG="TrafficSpeed";
    
    private  AtomicLong lastByte=new AtomicLong();
    private  AtomicLong lastTime=new AtomicLong(0);

    private  AtomicDouble speed=new AtomicDouble();
    

    public  double getSpeed(final long lastByte){
        final long c=System.currentTimeMillis();
        final long s=lastTime.get();
        if(s == 0 || (c-s)>1000){
            this.lastTime.set(c);
            speed.set((lastByte-this.lastByte.get())/1024d);
            this.lastByte.set(lastByte);
        }
        return speed.get();
    }
    

    public double getSpeed(){
        return speed.get();
        
    }

    
}
