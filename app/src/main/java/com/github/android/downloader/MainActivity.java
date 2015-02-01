package com.github.android.downloader;


import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.android.downloader.bean.DownloadFile;
import com.github.android.downloader.bean.DownloadInfo;
import com.github.android.downloader.core.DownLoadManager;
import com.github.android.downloader.core.DownloadListener;
import com.github.android.downloader.bean.RequestParams;

import java.io.File;

import static android.view.View.OnClickListener;


public class MainActivity extends Activity implements OnClickListener{

    private static final String TAG="MainActivity";
    
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_stop).setOnClickListener(this);
        progressBar= (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        
    }


    long st;
    private void download(){

        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            DownloadFile dFile=new DownloadFile();
            dFile.downUrl="http://gdown.baidu.com/data/wisegame/6fa8b3e535a4f361/baidushoujiweishi_1712.apk";
            dFile.fileName=dFile.downUrl.substring(dFile.downUrl.lastIndexOf('/')+1);
            dFile.savePath=Environment.getExternalStorageDirectory()+ File.separator+"down/"+dFile.fileName;
            
            dFile.requestParams= RequestParams.buildTestParams();
            Log.d(TAG," dFile.savePath -->  "+ dFile.savePath);
                  
            
            DownLoadManager.getInstance().addDownLoadTask(dFile,new DownloadListener() {
                @Override
                public void onStart() {
                    st=System.currentTimeMillis();
                    Log.d(TAG,"  onStart  -->>> ");
                    showToast("onStart");
                    
                }

                @Override
                public void onDownloading(long total, long curr, float speed, float perc) {
                    //Log.d(TAG,"  onDownloading  -->>> total "+total+"   curr "+curr+"    perc "+perc);
                    int p= (int) (perc*100f);
                    if(p> 100){
                        progressBar.setProgress(100);
                    }else {
                        progressBar.setProgress(p);
                    }

                }

                @Override
                public void onSuccess(DownloadInfo dInfo) {

                }

                @Override
                public void onFail() {

                }

                @Override
                public void onCancel(DownloadInfo dInfo) {

                }

                @Override
                public void onFinsh(DownloadInfo dInfo) {

                    long time=System.currentTimeMillis()-st;
                    Log.d(TAG,"onFinsh  -->    "+time);
                    showToast("  onFinsh  -->   "+time);
                }
            });  
        }
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start:
                download();
                break;
            case R.id.btn_stop:
                break;
        }
    }

    
    private void showToast(String s){
        Toast.makeText(this,s,Toast.LENGTH_LONG).show();
        
    }
   
}
