package com.github.onlynight.multithreaddownloader;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.onlynight.multithreaddownloader.library.DownloadManager;
import com.github.onlynight.multithreaddownloader.library.DownloadProgressManager;
import com.github.onlynight.multithreaddownloader.library.DownloaderDBHelper.Downloading;
import com.github.onlynight.multithreaddownloader.library.FileDownloader;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DownloadProgressManager manager;

    public static final String TEST_DOWNLOAD_URL = "http://www.baidu.com";
    public static final String WECHAT_DOWNLOAD_URL = "http://dldir1.qq.com/weixin/Windows/WeChatSetup.exe";
    public static final String QQ_DOWNLOAD_URL = "http://sw.bos.baidu.com/sw-search-sp/software/4b8362acdfc7e/QQ_8.9.19990.0_setup.exe";
    public static final String ADOBE_DOWNLOAD_URL = "http://sw.bos.baidu.com/sw-search-sp/software/0af215a3a8be0/flashplayer_24_ax_debug_24.0.0.194.exe";

    private TextView textProgress1;
    private ProgressBar progressBar1;
    private TextView textProgress2;
    private ProgressBar progressBar2;
    private TextView textProgress3;
    private ProgressBar progressBar3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textProgress1 = (TextView) findViewById(R.id.textProgress1);
        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
        textProgress2 = (TextView) findViewById(R.id.textProgress2);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        textProgress3 = (TextView) findViewById(R.id.textProgress3);
        progressBar3 = (ProgressBar) findViewById(R.id.progressBar3);

        manager = new DownloadProgressManager(this);
//        startDownload();
        downloadWechat();
    }

    private void downloadWechat() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                FileDownloader downloader = new FileDownloader(DemoApplication.getContext());
//                downloader.prepare(
//                        WECHAT_DOWNLOAD_URL, "/sdcard", 3
//                );
//                for (DownloadRunnable runnable : downloader.getDownloadThreads()) {
//                    new Thread(runnable).start();
//                }
//                downloader.start(new FileDownloader.OnDownloadListener() {
//                    @Override
//                    public void onUpdate(int totalSize, int currentSize, int speed, int percent) {
//                        System.out.println("1 percent = " + percent + "%");
//                        System.out.println("1 speed = " + speed + "kb/s");
//                        updateDownloadProgress(percent);
//                    }
//                });
//            }
//        }).start();
        DownloadManager instance = DownloadManager.
                getInstance(DemoApplication.getContext());
        instance.download("WECHAT", WECHAT_DOWNLOAD_URL, "/sdcard", 3, new FileDownloader.OnDownloadListener() {
            @Override
            public void onUpdate(int totalSize, int currentSize, int speed, int percent) {
                System.out.println("1 percent = " + percent + "%");
                System.out.println("1 speed = " + speed + "kb/s");
                updateDownloadProgress(percent, 1);
            }
        });

        instance.download("QQ", QQ_DOWNLOAD_URL, "/sdcard", 3, new FileDownloader.OnDownloadListener() {
            @Override
            public void onUpdate(int totalSize, int currentSize, int speed, int percent) {
                System.out.println("current/total=" + currentSize + "/" + totalSize);
                System.out.println("2 percent = " + percent + "%");
                System.out.println("2 speed = " + speed + "kb/s");
                updateDownloadProgress(percent, 2);
            }
        });

        instance.download("ADOBE", ADOBE_DOWNLOAD_URL, "/sdcard", 3, new FileDownloader.OnDownloadListener() {
            @Override
            public void onUpdate(int totalSize, int currentSize, int speed, int percent) {
                System.out.println("3 percent = " + percent + "%");
                System.out.println("3 speed = " + speed + "kb/s");
                updateDownloadProgress(percent, 3);
            }
        });
    }

    private void updateDownloadProgress(float downloadProgress, int id) {
        Message msg = new Message();
        msg.what = id;
        msg.obj = downloadProgress;
        handler.sendMessage(msg);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                textProgress1.setText((float) msg.obj + "%");
                float progress = (float) msg.obj;
                progressBar1.setProgress((int) progress);
            } else if (msg.what == 2) {
                textProgress2.setText((float) msg.obj + "%");
                float progress = (float) msg.obj;
                progressBar2.setProgress((int) progress);
            } else if (msg.what == 3) {
                textProgress3.setText((float) msg.obj + "%");
                float progress = (float) msg.obj;
                progressBar3.setProgress((int) progress);
            }
        }
    };

    private void startDownload() {
        ArrayList<Downloading> downloadings = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Downloading downloading = new Downloading();
            downloading.setDownloadPath(TEST_DOWNLOAD_URL);
            downloading.setDownloadLength(1024 + i);
            downloading.setThreadId(i);
            downloadings.add(downloading);
        }
        manager.saveProgress(TEST_DOWNLOAD_URL, downloadings);

        SparseIntArray progress = manager.getProgress(TEST_DOWNLOAD_URL);
        for (int i = 0; i < progress.size(); i++) {
            System.out.println("prepare progress = " + progress.get(progress.keyAt(i)));
        }

        System.out.println("prepare progress finished = " +
                manager.downloadFinished(TEST_DOWNLOAD_URL));
        manager.finishDownload(TEST_DOWNLOAD_URL);
        System.out.println("prepare progress finished = " +
                manager.downloadFinished(TEST_DOWNLOAD_URL));
    }
}
