package com.example.zyb.servicebestpractice;

/**
 * 监听下载过程中的各种状态
 * Created by zyb on 2017/2/21.
 */

public interface DownloadListener {

    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();

}
