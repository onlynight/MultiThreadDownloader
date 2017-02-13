package com.github.onlynight.multithreaddownloader.library;

import android.content.Context;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by lion on 2017/2/8.
 */

public class DownloadManager {

    private static int PARALLEL_DOWNLOAD_SIZE = 6;
    private static DownloadManager instance;

    private Context context;
    private Executor downloadExecutor;
    private ArrayList<FileDownloader> fileDownloaders;

    public static DownloadManager getInstance(Context context) {
        if (instance == null) {
            instance = new DownloadManager(context);
        }
        return instance;
    }

    public DownloadManager(Context context) {
        this.context = context;
        downloadExecutor = Executors.newFixedThreadPool(PARALLEL_DOWNLOAD_SIZE);
//        downloadExecutor = Executors.newCachedThreadPool();
        fileDownloaders = new ArrayList<>();
    }

    public void download(String name, final String downloadUrl, final String fileSavePath, final int threadNum,
                         final FileDownloader.OnDownloadListener listener) {
        for (FileDownloader downloader : fileDownloaders) {
            if (downloader.isFinish()) {
                downloader.setTagName(name);
                startDownload(downloader, downloadUrl, fileSavePath, threadNum, listener);
                return;
            }
        }

        FileDownloader currentDownloader = new FileDownloader(context);
        currentDownloader.setTagName(name);
        fileDownloaders.add(currentDownloader);
        startDownload(currentDownloader, downloadUrl, fileSavePath, threadNum, listener);
    }

    public void download(final String downloadUrl, final String fileSavePath, final int threadNum,
                         final FileDownloader.OnDownloadListener listener) {
        for (FileDownloader downloader : fileDownloaders) {
            if (downloader.isFinish()) {
                startDownload(downloader, downloadUrl, fileSavePath, threadNum, listener);
                return;
            }
        }

        FileDownloader currentDownloader = new FileDownloader(context);
        fileDownloaders.add(currentDownloader);
        startDownload(currentDownloader, downloadUrl, fileSavePath, threadNum, listener);
    }

    private synchronized void startDownload(final FileDownloader currentDownloader,
                                            final String downloadUrl, final String fileSavePath,
                                            final int threadNum,
                                            final FileDownloader.OnDownloadListener listener) {
        downloadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                currentDownloader.prepare(downloadUrl, fileSavePath,
                        threadNum);
                if (currentDownloader.getDownloadThreads() != null) {
                    for (DownloadRunnable runnable :
                            currentDownloader.getDownloadThreads()) {
                        downloadExecutor.execute(runnable);
                    }
                }
                currentDownloader.start(listener);
            }
        });
    }

    public static void setConnectionTimeOut(int timeOut) {
        FileDownloader.setConnectionTimeOut(timeOut);
    }

    public static void setParallelDownloadSize(int size) {
        PARALLEL_DOWNLOAD_SIZE = size;
    }
}
