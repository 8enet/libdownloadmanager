package com.github.android.downloader.io;

import android.util.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by zl on 15/2/2.
 */
public class FileCoalition {

    private static final String TAG="FileCoalition";

    private static final int BUFF_MAP_SIZE = 1024 * 256; //256k

    private File[] srcs;
    private File dest;

    public FileCoalition(File dest, File... src) {
        this.dest = dest;
        this.srcs = src;
    }


    private void check() throws Exception {
        for (File f : srcs) {
            if (f == null || !f.exists()) {
                throw new FileNotFoundException(" src file " + f + " not found");
            }
        }
    }


    /**
     * Multiple file merge
     * @throws Exception
     */
    public void merge() throws Exception {
        check();
        FileOutputStream fos = null;
        FileChannel channel = null;
        try {
            if (dest.exists()) {
                dest.delete();
            }
            dest.createNewFile();
            fos = new FileOutputStream(dest);
            channel = fos.getChannel();
            ByteBuffer buf = ByteBuffer.allocateDirect(BUFF_MAP_SIZE);
            for (File file : srcs) {
                FileInputStream fis=null;
                BufferedInputStream bufferedInputStream=null;
                try {
                    fis=new FileInputStream(file);
                    bufferedInputStream=new BufferedInputStream(fis);
                    int len = -1;
                    byte[] buff = new byte[BUFF_MAP_SIZE];
                    while ((len = bufferedInputStream.read(buff, 0, BUFF_MAP_SIZE)) != -1) {
                        buf.put(buff, 0, len);
                        buf.flip();
                        channel.write(buf);
                        buf.compact();
                    }
                } catch (Exception e) {
                    Log.e(TAG,Log.getStackTraceString(e));
                } finally {
                    FileUtils.closeQuietly(bufferedInputStream, fis);
                }
            }
            buf.flip();
            channel.write(buf);
        } catch (Exception e) {
            Log.e(TAG,Log.getStackTraceString(e));
        } finally {
            FileUtils.closeQuietly(channel, fos);
        }

    }

    /**
     * Multiple file merge {@Deprecated} see merge method,merge method is more then fast !
     * @throws Exception
     */
    @Deprecated
    public void merge2() throws Exception {
        check();
        FileOutputStream fos = null;
        try {
            if (dest.exists()) {
                dest.delete();
            }
            dest.createNewFile();
            fos = new FileOutputStream(dest);
            for (File file:srcs){
                FileInputStream fis=null;
                try {
                    fis=new FileInputStream(file);
                    int len = -1;
                    byte[] buff = new byte[BUFF_MAP_SIZE];
                    while ((len = fis.read(buff,0,BUFF_MAP_SIZE)) != -1){
                        fos.write(buff,0,len);
                    }
                    fos.flush();
                }catch (Exception e){

                }finally {
                    FileUtils.closeQuietly(fis);
                }
            }
        }catch (Exception e){
            Log.e(TAG,Log.getStackTraceString(e));
        }finally {
            FileUtils.closeQuietly(fos);
        }
    }

}