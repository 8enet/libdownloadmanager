package com.github.android.downloader.net;


import android.os.Environment;
import android.util.Log;

import com.github.android.downloader.bean.DownloadInfo;
import com.github.android.downloader.io.FileCoalition;
import com.github.android.downloader.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * download task
 * Created by zl on 2015/1/31.
 */
public class HttpDownloadTask implements Callable<DownloadInfo> {

    private static final String TAG = "HttpDownloadTask";

    private static final int TIME_OUT = 30000;
    private static final int BUFF_SIZE = 2 << 13; //16kb
    private DownloadInfo dInfo;

    public HttpDownloadTask(DownloadInfo dInfo, HttpTaskListener taskListener) {
        if (dInfo == null || (dInfo.downloadFile == null)) {
            throw new NullPointerException("DownloadInfo is Null !!!");
        }

        this.dInfo = dInfo;
        this.taskListener = taskListener;
    }

    private HttpTaskListener taskListener;


    public void setHttpTaskListener(HttpTaskListener taskListener) {
        this.taskListener = taskListener;
    }

    @Override
    public DownloadInfo call() throws Exception {
        boolean retry = false;
        boolean fail = false;
        do {
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                if (!retry && taskListener != null) {
                    taskListener.onStart(this.dInfo);
                }

                URL url = new URL(dInfo.downloadFile.downUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(TIME_OUT);
                connection.setReadTimeout(TIME_OUT * 2);

                String r = planRange().toString();
                connection.addRequestProperty(HttpHeaders.RANGE, r);
                Log.d(TAG, " request range --> " + r);
                if (dInfo.getHeards() != null) {
                    Set<Map.Entry<String, String>> entries = dInfo.getHeards().entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        connection.addRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
                int code = connection.getResponseCode();
                Log.d(TAG, " http response code " + code);
                if (code == HttpURLConnection.HTTP_PARTIAL) {
                    Log.d(TAG, " dInfo.isRunning() " + dInfo.isRunning());
                    if (dInfo.isRunning()) {
                        inputStream = connection.getInputStream();
                        String fileName = createTempFile();
                        saveFile(inputStream, fileName);
                    } else {
                        if (taskListener != null) {
                            retry = false;
                            taskListener.onInterruption(dInfo, true);
                            break;
                        }
                    }
                } else {
                    if (taskListener != null) {
                        fail = true;
                        taskListener.onFail(this.dInfo, code);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
                retry = true;
                dInfo.retry--;
                if (taskListener != null) {
                    taskListener.onRetry(dInfo.retry, dInfo.realByte);
                }
            } finally {
                FileUtils.closeQuietly(inputStream);
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } while (retry && dInfo.retry != 0);

        if (dInfo.isSuccess()) {
            if (taskListener != null) {
                taskListener.onSuccess(dInfo);
            }
        } else {
            if (!fail && taskListener != null) {
                taskListener.onFail(dInfo, DownloadInfo.DOWNLOAD_FAIL);
            }
        }

        if (taskListener != null) {

            taskListener.onFinish(dInfo);
        }
        return this.dInfo;
    }

    private String createTempFile() {
        int lastIndexOf = dInfo.downloadFile.savePath.lastIndexOf(".");
        String filePath = dInfo.downloadFile.savePath.substring(0, lastIndexOf) + FileUtils.getTempFileFormat();
        return filePath;
    }

    private void saveFile(InputStream inputStream, String fileName) throws Exception {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(fileName, "rw");
            if ("".equals(dInfo.tempFiles)){
                dInfo.tempFiles=fileName;
            }else{
                dInfo.tempFiles+=","+fileName;
            }
            Log.d(TAG, "save file seek -->> " + dInfo.getCurrentByte());
            raf.seek(dInfo.getCurrentByte());
            byte buff[] = new byte[BUFF_SIZE];
            int len = -1;
            boolean r = dInfo.isRunning();

            while (r && (len = inputStream.read(buff, 0, BUFF_SIZE)) != -1) {
                raf.write(buff, 0, len);
                dInfo.realByte += len;
                dInfo.addByte = len;
                r = dInfo.isRunning();
                if (!r) {
                    r = false;
                    break;
                }
                if (taskListener != null) {
                    taskListener.onDownloading(dInfo);
                }
            }
            if (!r) {
                if (taskListener != null) {
                    dInfo.retry = 0;
                    taskListener.onInterruption(dInfo, true);
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            FileUtils.closeQuietly(raf);
        }
    }


    private StringBuilder planRange() {
        StringBuilder rangs = new StringBuilder("bytes=");


        if (dInfo.startByte != DownloadInfo.RANGE_NONE) {
            rangs.append(dInfo.startByte);
        }

        rangs.append('-');
        if (dInfo.endByte != DownloadInfo.RANGE_NONE) {
            rangs.append(dInfo.endByte);
        }
        return rangs;
    }
}
