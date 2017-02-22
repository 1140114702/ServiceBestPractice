package com.example.zyb.servicebestpractice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class DownloadService extends Service {

    private DownloadTask downloadTask;
    private String downloadUrl;

    private int mProgress;

    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("下载中...", progress));
            mProgress = progress;
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            //下载成功是将前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载完成", -1));
            openFile();
            Toast.makeText(DownloadService.this, "下载完成", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            //下载失败时将前台服务通知关闭，并创建一个下载失败的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("下载失败", -1));
            Toast.makeText(DownloadService.this, "下载失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            Toast.makeText(DownloadService.this, "暂停下载", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "取消下载", Toast.LENGTH_SHORT).show();
        }
    };
    private File file;

    //自动打开下载的文件
    private void openFile() {
        if (downloadUrl != null) {
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory + fileName);
        }
        try {
            Log.e("OpenFile", file.getName());
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class DownloadBinder extends Binder {

        public void startDownload(String url) {
            if (downloadTask == null) {
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                startForeground(1, getNotification("下载中...", 0));
                Toast.makeText(DownloadService.this, "正在下载", Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
            if (mProgress > 0 && mProgress < 100) {
                getNotificationManager().notify(1, getNotification("暂停下载", mProgress));
            }
        }

        public void cancelDownload() {
            if (downloadTask != null) {
                downloadTask.cancelDownload();
            } else {
                if (downloadUrl != null) {
                    //取消下载时需要将文件删除，并将通知关闭
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory
                            (Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this, "取消下载", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress > 0) {
            //当progress大于或等于0时才需要显示下载进度条
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
}
