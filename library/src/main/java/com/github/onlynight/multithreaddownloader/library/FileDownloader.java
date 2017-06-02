package com.github.onlynight.multithreaddownloader.library;

import android.content.Context;
import android.util.SparseIntArray;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lion on 2017/2/7.
 */

public class FileDownloader {

    public static final String TAG = "FileDownloader";

    /**
     * http connection timeout
     */
    private static int CONNECTION_TIME_OUT = 10 * 1000;

    private DownloadProgressManager downloadProgressManager;

    private DownloadRunnable[] downloadThreads;

    private String tagName = "";

    private String downloadUrl;
    private String fileSavePath;
    private String filename;
    private int threadNum = 1;
    private int fileSize = 0;
    private int currentDownloadSize = 0;

    private SparseIntArray currentDownloads;

    public DownloadRunnable[] getDownloadThreads() {
        return downloadThreads;
    }

    public DownloadProgressManager getDownloadProgressManager() {
        return downloadProgressManager;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getFileSavePath() {
        return fileSavePath;
    }

    public String getFilename() {
        return filename;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public int getCurrentDownloadSize() {
        return currentDownloadSize;
    }

    public int getThreadNum() {
        return threadNum;
    }

    synchronized int appendDownloadSize(int size) {
        currentDownloadSize += size;
        return currentDownloadSize;
    }

    public FileDownloader(Context context) {
        this.currentDownloads = new SparseIntArray();
        this.downloadProgressManager = new DownloadProgressManager(context);
    }

    private void requestFileInfo(String downloadUrl) throws RuntimeException {
        try {
            HttpURLConnection connection = (HttpURLConnection)
                    new URL(downloadUrl).openConnection();
            connection.setConnectTimeout(CONNECTION_TIME_OUT);
            connection.setRequestMethod("GET");

            //set accept file meta-data type
            connection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg," +
                    " image/pjpeg, application/x-shockwave-flash, application/xaml+xml, " +
                    "application/vnd.ms-xpsdocument, application/x-ms-xbap, " +
                    "application/x-ms-application, application/vnd.ms-excel, " +
                    "application/vnd.ms-powerpoint, application/msword, */*");

            connection.setRequestProperty("Accept-Language", "zh-CN");
            connection.setRequestProperty("Referer", downloadUrl);
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; " +
                    "Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; " +
                    ".NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
//            connection.setRequestProperty("Connection", "Keep-Alive");

            connection.connect();

            if (connection.getResponseCode() == 200) {
                fileSize = connection.getContentLength();
                if (fileSize <= 0) {
                    throw new RuntimeException(TAG + " Unknown file size");
                }

                filename = getFilename(connection);
            } else {
                throw new RuntimeException(TAG + " Server Response Code is "
                        + connection.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFilename(HttpURLConnection connection) {
        String filename = downloadUrl != null ?
                downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1) : null;
        if (filename == null || "".equals(filename.trim())) {//如果获取不到文件名称
            for (int i = 0; ; i++) {
                String mine = connection.getHeaderField(i);
                if (mine == null) break;
                if ("content-disposition".equals(connection.getHeaderFieldKey(i).toLowerCase())) {
                    Matcher m = Pattern.compile(".*filename=(.*)").
                            matcher(mine.toLowerCase());
                    if (m.find()) return m.group(1);
                }
            }
            filename = UUID.randomUUID() + ".tmp";//默认取一个文件名
        }
        return filename;
    }

    public void prepare(String downloadUrl, String fileSavePath, int threadNum) {
        this.downloadUrl = downloadUrl;
        this.fileSavePath = fileSavePath;
        requestFileInfo(downloadUrl);
        SparseIntArray progresses = downloadProgressManager.getProgress(downloadUrl);

        if (threadNum <= 0) {
            threadNum = this.threadNum;
        } else {
            this.threadNum = threadNum;
        }

        if (progresses != null && progresses.size() > 0) {
            threadNum = progresses.size();
            for (int i = 0; i < progresses.size(); i++) {
                currentDownloadSize += progresses.get(i);
            }
        }

        int block = fileSize % threadNum == 0 ?
                fileSize / threadNum : fileSize / threadNum + 1;

        downloadThreads = new DownloadRunnable[threadNum];

        for (int i = 0; i < threadNum; i++) {
            downloadThreads[i] = new DownloadRunnable(this, i, block,
                    progresses != null && progresses.size() == threadNum ?
                            progresses.valueAt(progresses.keyAt(i)) == -1 ? 0 :
                                    progresses.valueAt(progresses.keyAt(i)) : 0);
        }
    }

    public void start(OnDownloadListener listener) {
        boolean isFinish = false;
        int lastDownloadSize = 0;
        int speed = 0;
        Date current = new Date();
        while (!isFinish) {
            if (listener != null) {
                int percent = (int) (currentDownloadSize / (float) fileSize * 100);
                long time = new Date().getTime() - current.getTime();
                speed = (int) ((currentDownloadSize - lastDownloadSize) / 1024f / time * 1000f);
                listener.onUpdate(fileSize, currentDownloadSize, speed, percent);
                if (percent == 100) {
                    downloadProgressManager.finishDownload(downloadUrl);
                    break;
                }
            }
            current = new Date();
            lastDownloadSize = currentDownloadSize;
            updateProgress();
            isFinish = checkFinish();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (listener != null) {
            listener.onUpdate(fileSize, fileSize, 0, 100);
            listener.onFinish(downloadUrl, filename);
        }
    }

    private boolean checkFinish() {
        if (downloadThreads != null && downloadThreads.length > 0) {
            for (DownloadRunnable downloadThread : downloadThreads) {
                if (!downloadThread.isFinish()) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    public boolean isFinish() {
        return checkFinish();
    }

    void updateProgress() {
        for (DownloadRunnable downloadThread : downloadThreads) {
            updateProgress(downloadThread.getThreadId(), downloadThread.getDownloadLength());
        }
    }

    synchronized void updateProgress(int threadId, int downloaded) {
        currentDownloads.put(threadId, downloaded);
        downloadProgressManager.saveProgress(downloadUrl, currentDownloads);
//        SparseIntArray progress = downloadProgressManager.getProgress(downloadUrl);
//        for (int i = 0; i < progress.size(); i++) {
//            System.out.println("prepare progress = " + progress.valueAt(progress.keyAt(i)));
//        }
    }

    public boolean isStart() {
        for (DownloadRunnable runnable : downloadThreads) {
            if (runnable.isStart()) {
                return true;
            }
        }

        return false;
    }

    static int getConnectionTimeOut() {
        return CONNECTION_TIME_OUT;
    }

    static void setConnectionTimeOut(int timeOut) {
        CONNECTION_TIME_OUT = timeOut;
    }

    public interface OnDownloadListener {
        void onUpdate(int totalSize, int currentSize, int speed, int percent);

        void onFinish(String downloadUrl, String filepath);
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
