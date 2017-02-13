package com.github.onlynight.multithreaddownloader.library;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lion on 2017/2/10.
 */

public class DownloadRunnable implements Runnable {
    private int threadId = -1;
    private FileDownloader fileDownloader;
    private int downloadedSize = 0;

    private int startPos = -1;
    private int endPos = -1;
    private int downloadLength = 0;

    private boolean isFinish;
    private boolean isStart;

    public DownloadRunnable(FileDownloader fileDownloader, int threadId, int blockSize,
                            int downloadedSize) {
        this.fileDownloader = fileDownloader;
        this.threadId = threadId;
        int fileSize = fileDownloader.getFileSize();
        this.startPos = blockSize * threadId + downloadedSize;
        this.endPos = blockSize * (threadId + 1) < fileSize ?
                blockSize * (threadId + 1) : fileSize;
        this.downloadedSize = downloadedSize;
    }

    @Override
    public void run() {
        if (startPos >= endPos) {
            isFinish = true;
        } else {
            try {
                isStart = true;
                isFinish = false;
                HttpURLConnection conn = (HttpURLConnection)
                        new URL(fileDownloader.getDownloadUrl()).openConnection();
                conn.setConnectTimeout(FileDownloader.getConnectionTimeOut());
                conn.setRequestMethod("GET");

                //set accept file meta-data type
                conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg," +
                        " image/pjpeg, application/x-shockwave-flash, application/xaml+xml, " +
                        "application/vnd.ms-xpsdocument, application/x-ms-xbap, " +
                        "application/x-ms-application, application/vnd.ms-excel, " +
                        "application/vnd.ms-powerpoint, application/msword, */*");

                conn.setRequestProperty("Accept-Language", "zh-CN");
                conn.setRequestProperty("Referer", fileDownloader.getDownloadUrl());
                conn.setRequestProperty("Charset", "UTF-8");
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; " +
                        "Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; " +
                        ".NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
                conn.setRequestProperty("Connection", "Keep-Alive");

                conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
                conn.connect();

                RandomAccessFile threadFile = new RandomAccessFile(
                        fileDownloader.getFileSavePath() + File.separator +
                                fileDownloader.getFilename(), "rwd");
                threadFile.seek(startPos);
                InputStream inputStream = conn.getInputStream();
                byte[] buffer = new byte[10240];
                int offset;
                downloadLength = downloadedSize;
                while ((offset = inputStream.read(buffer, 0, 10240)) != -1) {
                    threadFile.write(buffer, 0, offset);
                    downloadLength += offset;
                    fileDownloader.appendDownloadSize(offset);
                }
                threadFile.close();
                inputStream.close();

                isFinish = true;
                isStart = false;
            } catch (IOException e) {
                e.printStackTrace();
                downloadLength = -1;
            }
        }
    }

    public int getDownloadLength() {
        return downloadLength;
    }

    public int getThreadId() {
        return threadId;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public boolean isStart() {
        return isStart;
    }
}
