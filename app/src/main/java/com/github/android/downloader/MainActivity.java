package com.github.android.downloader;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.android.downloader.bean.DownloadFile;
import com.github.android.downloader.bean.DownloadInfo;
import com.github.android.downloader.bean.RequestParams;
import com.github.android.downloader.core.DownLoadServices;
import com.github.android.downloader.core.IDownLoadServices;
import com.github.android.downloader.core.SimpleDownloadListener;
import com.github.android.downloader.io.FileCoalition;

import java.io.File;
import java.util.List;

import static android.view.View.OnClickListener;


public class MainActivity extends Activity implements OnClickListener{

    private static final String TAG="MainActivity";
    
    private ProgressBar progressBar;
    private TextView tvSpeed;
    
    private IDownLoadServices mService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_stop).setOnClickListener(this);
        findViewById(R.id.btn_resume).setOnClickListener(this);
        Intent intent=new Intent(this, DownLoadServices.class);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
        
        progressBar= (ProgressBar) findViewById(R.id.progressBar);
        tvSpeed= (TextView) findViewById(R.id.tv_down_speed);
        progressBar.setMax(100);
        progressBar.setProgress(0);

    }
    
    

    
    
    private ServiceConnection connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService= IDownLoadServices.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService=null;
        }
    };

    private String downUrl="http://gdown.baidu.com/data/wisegame/6fa8b3e535a4f361/baidushoujiweishi_1712.apk";

    long st;
    private void download(){

        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            DownloadFile dFile=new DownloadFile();
            dFile.downUrl=downUrl;
            dFile.fileName=dFile.downUrl.substring(dFile.downUrl.lastIndexOf('/')+1);
            dFile.savePath=Environment.getExternalStorageDirectory()+ File.separator+"down/"+dFile.fileName;
            
            dFile.requestParams= RequestParams.buildTestParams();
            Log.d(TAG," dFile.savePath -->  "+ dFile.savePath);

            try {
                mService.addDownLoadTask(dFile,new SimpleDownloadListener(){

                    @Override
                    public void onStart() throws RemoteException {
                        super.onStart();
                        st=System.currentTimeMillis();
                        Log.d(TAG,"  onStart  -->>> ");
                        showToast("onStart");
                    }


                    @Override
                    public void onDownloading(long total, long curr, float speed, float perc) throws RemoteException {
                        super.onDownloading(total, curr, speed, perc);
                        int p= (int) (perc*100f);
                        if(p> 100){
                            progressBar.setProgress(100);
                        }else {
                            progressBar.setProgress(p);
                        }
                        tvSpeed.setText(((int)speed)+"kb/s");
                    }

                    @Override
                    public void onPause(List<DownloadInfo> dInfos) throws RemoteException {
                        super.onPause(dInfos);
                        showToast("onPause   --->>> ");
                        Log.d(TAG,"onPause  -->    ");
                        if(dInfos != null){
                            for(DownloadInfo d:dInfos){
                                //Log.d(TAG,"onPause DownloadInfo -->    "+d);
                            }
                        }
                    }

                    @Override
                    public void onResume() throws RemoteException {
                        super.onResume();
                    }

                    @Override
                    public void onFinsh(DownloadInfo dInfo) throws RemoteException {
                        super.onFinsh(dInfo);

                        long time=System.currentTimeMillis()-st;
                        Log.d(TAG,"onFinsh  -->    "+time);
                        showToast("  onFinsh  -->   "+time);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG,Log.getStackTraceString(e));
            }
        }
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start:
                download();
                break;
            case R.id.btn_stop:
                stopD();
                break;
            case R.id.btn_resume:
                resumeD();
                break;
                
        }
    }
    
    
    
    private void stopD(){
        try {
            mService.stopDownLoadTask(downUrl);
            Log.d(TAG,"stopD  -->    ");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    
    private void resumeD(){
        try {
            mService.resumeDownLoadTask(downUrl);
            Log.d(TAG,"resumeD  -->    ");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    
    
    private void copyTest(){

        try {
            String s=Environment.getExternalStorageDirectory()+ File.separator+"down/";
            File file=new File(s+"aaa");
            File f1=new File(s+"baidushoujiweishi_1712_0.apk");
            File f2=new File(s+"baidushoujiweishi_1712_1.apk");
            File f3=new File(s+"baidushoujiweishi_1712_2.apk");
            File f4=new File(s+"baidushoujiweishi_1712_3.apk");
            long st=System.currentTimeMillis();
            FileCoalition fc=new FileCoalition(file,f1,f2,f3,f4);
            fc.merge();
            long e=System.currentTimeMillis()-st;
            Log.d(TAG,"   time 1"+e);

            st=System.currentTimeMillis();
            fc.merge2();
            e=System.currentTimeMillis()-st;
            Log.d(TAG,"   time 2"+e);
            
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    
    private void showToast(String s){
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
        
    }

   

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
