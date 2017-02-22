package com.example.zyb.servicebestpractice;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private DownloadService.DownloadBinder downloadBinder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent); //启动服务
        bindService(intent, connection, BIND_AUTO_CREATE); //绑定服务
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
    }

    public void start_download(View view) {
        if (downloadBinder == null) {
            return;
        }
        String url = "http://119.147.33.16/imtt.dd.qq.com/16891/11839B451A8AA86EFD4A5D93CCB5C13F.apk?mkey=58ad1c0ad68032fb&f=8252&c=0&fsname=com.snda.wifilocating_4.1.82_3102.apk&csr=4d5s&p=.apk";
        downloadBinder.startDownload(url);
    }

    public void pause_download(View view) {
        if (downloadBinder == null) {
            return;
        }
        downloadBinder.pauseDownload();
    }

    public void cancel_download(View view) {
        if (downloadBinder == null) {
            return;
        }
        downloadBinder.cancelDownload();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "请授予相关权限，否则无法正常使用程序", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }
}
