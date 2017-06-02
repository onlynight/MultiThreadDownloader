package com.github.onlynight.multithreaddownloader.library;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseIntArray;

import com.github.onlynight.multithreaddownloader.library.DownloaderDBHelper.DownloadLog;
import com.github.onlynight.multithreaddownloader.library.DownloaderDBHelper.Downloading;

import java.util.ArrayList;

/**
 * Created by lion on 2017/2/7.
 */

public class DownloadProgressManager {

    private DownloaderDBHelper downloaderDBHelper;

    public DownloadProgressManager(Context context) {
        downloaderDBHelper = new DownloaderDBHelper(context);
    }

    public synchronized SparseIntArray getProgress(String downloadPath) {
        SQLiteDatabase db = downloaderDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Downloading.TABLE_NAME + " WHERE download_path=?",
                new String[]{downloadPath});
        if (cursor != null) {
            SparseIntArray array = new SparseIntArray();
            while (cursor.moveToNext()) {
                Downloading log =
                        Downloading.convert(cursor);
                array.put(log.getThreadId(), log.getDownloadLength());
            }
            cursor.close();
            db.close();

            return array;
        }
        return null;
    }

    public synchronized void updateProgress(ArrayList<Downloading> logs) {
        if (logs != null) {
            SQLiteDatabase db = downloaderDBHelper.getWritableDatabase();

            for (Downloading log : logs) {
                db.update(Downloading.TABLE_NAME, Downloading.toContentValues(log),
                        Downloading.FIELD_ID + "=?", new String[]{Integer.toString(log.getId())});
            }

            db.close();
        }
    }

    public synchronized void saveProgress(String downloadPath, SparseIntArray logs) {
        ArrayList<Downloading> array =
                sparseArray2ArrayList(downloadPath, logs);
        saveProgress(downloadPath, array);
    }

    private ArrayList<Downloading> sparseArray2ArrayList(
            String downloadUrl, SparseIntArray logs) {
        if (logs != null) {
            ArrayList<Downloading> downloadings = new ArrayList<>();
            for (int i = 0; i < logs.size(); i++) {
                Downloading downloading = new Downloading();
                downloading.setThreadId(logs.keyAt(i));
                downloading.setDownloadPath(downloadUrl);
                downloading.setDownloadLength(logs.valueAt(i));
                downloadings.add(downloading);
            }

            return downloadings;
        }
        return null;
    }

    public synchronized void saveProgress(String downloadPath, ArrayList<Downloading> logs) {
        if (logs == null) {
            return;
        }

        boolean exist = existDownload(downloadPath);

        if (!exist) {
            startDownload(downloadPath);
        }

        SQLiteDatabase db = downloaderDBHelper.getWritableDatabase();
        String sql = "";
        String[] args;
        for (Downloading log : logs) {
            sql = "SELECT * FROM " + Downloading.TABLE_NAME + " WHERE " +
                    Downloading.FIELD_DOWNLOAD_PATH + "=? AND " +
                    Downloading.FIELD_THREAD_ID + "=?";
            args = new String[]{downloadPath, Integer.toString(log.getThreadId())};
            Cursor cursor = db.rawQuery(sql, args);
            if (cursor != null && cursor.moveToNext()) {
                Downloading downloading = Downloading.convert(cursor);
                cursor.close();
                if (downloading != null) {
                    sql = "UPDATE " + Downloading.TABLE_NAME + " SET " +
                            Downloading.FIELD_DOWNLOAD_LENGTH + "=? WHERE " +
                            Downloading.FIELD_DOWNLOAD_PATH + "=?" + " AND " +
                            Downloading.FIELD_THREAD_ID + "=?";
                    args = new String[]{Integer.toString(log.getDownloadLength()),
                            downloadPath, Integer.toString(log.getThreadId())};
                    db.execSQL(sql, args);
                }
            } else {
                ArrayList<String> temp = log.toStringArgs();
                if (temp.size() >= 1) {
                    db.execSQL(temp.get(0), temp.subList(1, temp.size()).toArray(new String[]{}));
                }
            }
        }
        db.close();
    }

    public synchronized boolean existDownload(String downloadPath) {
        SQLiteDatabase db = downloaderDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DownloadLog.TABLE_NAME +
                        " WHERE " + Downloading.FIELD_DOWNLOAD_PATH + "=?",
                new String[]{downloadPath});
        if (cursor != null) {
            DownloadLog log = null;
            if (cursor.moveToNext()) {
                log = DownloadLog.convert(cursor);
            }
            cursor.close();
            db.close();

            return log != null;
        }
        db.close();
        return false;
    }

    public synchronized void startDownload(String downloadPath) {
        SQLiteDatabase db = downloaderDBHelper.getWritableDatabase();
        DownloadLog log = new DownloadLog();
        log.setDownloadPath(downloadPath);
        ArrayList<String> args = log.toStringArgs();
        if (args != null && args.size() >= 1) {
            String[] temp = args.subList(1, args.size()).toArray(new String[]{});
            db.execSQL(args.get(0), temp);
        }
        db.close();
    }

    public synchronized void finishDownload(String downloadPath) {
        SQLiteDatabase db = downloaderDBHelper.getWritableDatabase();
        String sql = "DELETE FROM " + Downloading.TABLE_NAME + " WHERE " +
                Downloading.FIELD_DOWNLOAD_PATH + "=?";
        String[] args = {downloadPath};
        db.execSQL(sql, args);

        sql = "SELECT * FROM " + DownloadLog.TABLE_NAME + " WHERE " +
                DownloadLog.FIELD_DOWNLOAD_PATH + "=?";
        args = new String[]{downloadPath};
        Cursor cursor = db.rawQuery(sql, args);
        if (cursor != null) {
            DownloadLog log = null;
            if (cursor.moveToNext()) {
                log = DownloadLog.convert(cursor);
            }
            cursor.close();
            if (log != null) {
                log.setIsFinish(DownloadLog.DOWNLOAD_FINISH);
                ContentValues values = DownloadLog.toContentValues(log);
                db.update(DownloadLog.TABLE_NAME, values, "id=?",
                        new String[]{Integer.toString(log.getId())});
            }
        }

        db.close();
    }

    public synchronized boolean downloadFinished(String downloadPath) {
        SQLiteDatabase db = downloaderDBHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DownloadLog.TABLE_NAME +
                        " WHERE " + DownloadLog.FIELD_DOWNLOAD_PATH + "=?",
                new String[]{downloadPath});
        if (cursor != null) {
            DownloadLog log = null;
            if (cursor.moveToNext()) {
                log = DownloadLog.convert(cursor);
            }
            cursor.close();
            db.close();

            return log != null && log.getIsFinish() == DownloadLog.DOWNLOAD_FINISH;
        }
        db.close();
        return false;
    }
}
