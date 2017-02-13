package com.github.onlynight.multithreaddownloader.library;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by lion on 2017/2/7.
 */

public class DownloaderDBHelper extends SQLiteOpenHelper {

    private static final String NAME = "multi_thread_downloader.db";
    private static final int VERSION = 1;

    private static final String CREATE_TABLE_DOWNLOADING = "CREATE TABLE IF NOT EXISTS " +
            Downloading.TABLE_NAME + "(" + Downloading.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            Downloading.FIELD_DOWNLOAD_PATH + " TEXT, " + Downloading.FIELD_THREAD_ID + " INTEGER," +
            Downloading.FIELD_DOWNLOAD_LENGTH + " INTEGER)";

    private static final String CREATE_TABLE_DOWNLOAD_LOG = "CREATE TABLE IF NOT EXISTS " +
            DownloadLog.TABLE_NAME + "(" + DownloadLog.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            DownloadLog.FIELD_DOWNLOAD_PATH + " TEXT, " + DownloadLog.FIELD_IS_FINISH + " INTEGER," +
            DownloadLog.FIELD_LOCAL_SAVE_PATH + " TEXT)";

    public DownloaderDBHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_DOWNLOADING);
        db.execSQL(CREATE_TABLE_DOWNLOAD_LOG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Downloading.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DownloadLog.TABLE_NAME);
        onCreate(db);
    }

    public static class Downloading {

        public static final String TABLE_NAME = "downloading";

        public static final String FIELD_ID = "id";
        public static final String FIELD_DOWNLOAD_PATH = "download_path";
        public static final String FIELD_THREAD_ID = "thread_id";
        public static final String FIELD_DOWNLOAD_LENGTH = "download_length";

        private int id;
        private String downloadPath;
        private int threadId;
        private int downloadLength;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDownloadPath() {
            return downloadPath;
        }

        public void setDownloadPath(String downloadPath) {
            this.downloadPath = downloadPath;
        }

        public int getThreadId() {
            return threadId;
        }

        public void setThreadId(int threadId) {
            this.threadId = threadId;
        }

        public int getDownloadLength() {
            return downloadLength;
        }

        public void setDownloadLength(int downloadLength) {
            this.downloadLength = downloadLength;
        }

        public static Downloading convert(Cursor cursor) {
            if (cursor != null) {
                Downloading log = new Downloading();
                log.setId(cursor.getInt(cursor.getColumnIndex(FIELD_ID)));
                log.setThreadId(cursor.getInt(cursor.getColumnIndex(FIELD_THREAD_ID)));
                log.setDownloadLength(cursor.getInt(
                        cursor.getColumnIndex(FIELD_DOWNLOAD_LENGTH)));
                log.setDownloadPath(cursor.getString(
                        cursor.getColumnIndex(FIELD_DOWNLOAD_PATH)));

                return log;
            }
            return null;
        }

        public ArrayList<String> toStringArgs() {
            ArrayList<String> args = new ArrayList<>();
            String sql = "INSERT INTO " + TABLE_NAME + "(" +
                    Downloading.FIELD_THREAD_ID + "," +
                    Downloading.FIELD_DOWNLOAD_PATH + "," +
                    Downloading.FIELD_DOWNLOAD_LENGTH
                    + ") VALUES(?,?,?)";
            args.add(sql);
            args.add(Integer.toString(threadId));
            args.add(downloadPath);
            args.add(Integer.toString(downloadLength));
            return args;
        }

        public static ContentValues toContentValues(Downloading log) {
            if (log == null) {
                return null;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(FIELD_ID, log.getId());
            contentValues.put(FIELD_THREAD_ID, log.getId());
            contentValues.put(FIELD_DOWNLOAD_LENGTH, log.getDownloadLength());
            contentValues.put(FIELD_DOWNLOAD_PATH, log.getDownloadPath());
            return contentValues;
        }
    }

    public static class DownloadLog {

        public static final String FIELD_ID = "id";
        public static final String FIELD_DOWNLOAD_PATH = "download_path";
        public static final String FIELD_LOCAL_SAVE_PATH = "save_path";
        public static final String FIELD_IS_FINISH = "is_finish";

        public static final int DOWNLOAD_FINISH = 1;
        public static final String TABLE_NAME = "download_log";

        private int id;
        private String downloadPath;
        private String savePath;
        private int isFinish;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDownloadPath() {
            return downloadPath;
        }

        public void setDownloadPath(String downloadPath) {
            this.downloadPath = downloadPath;
        }

        public int getIsFinish() {
            return isFinish;
        }

        public void setIsFinish(int isFinish) {
            this.isFinish = isFinish;
        }

        public String getSavePath() {
            return savePath;
        }

        public void setSavePath(String savePath) {
            this.savePath = savePath;
        }

        public static DownloadLog convert(Cursor cursor) {
            if (cursor != null) {
                DownloadLog log = new DownloadLog();
                log.setId(cursor.getInt(cursor.getColumnIndex(FIELD_ID)));
                log.setIsFinish(cursor.getInt(cursor.getColumnIndex(FIELD_IS_FINISH)));
                log.setDownloadPath(cursor.getString(
                        cursor.getColumnIndex(FIELD_DOWNLOAD_PATH)));

                return log;
            }
            return null;
        }

        public ArrayList<String> toStringArgs() {
            String sql = "INSERT INTO " + TABLE_NAME + "(" +
                    FIELD_DOWNLOAD_PATH + "," +
                    FIELD_IS_FINISH + "," +
                    FIELD_LOCAL_SAVE_PATH
                    + ")" +
                    " VALUES(?,?,?)";
            ArrayList<String> args = new ArrayList<>();
            args.add(sql);
            args.add(downloadPath);
            args.add(savePath);
            args.add(Integer.toString(isFinish));
            return args;
        }

        public static ContentValues toContentValues(DownloadLog log) {
            ContentValues values = new ContentValues();
            values.put(FIELD_ID, log.getId());
            values.put(FIELD_DOWNLOAD_PATH, log.getDownloadPath());
            values.put(FIELD_IS_FINISH, log.getIsFinish());
            values.put(FIELD_LOCAL_SAVE_PATH, log.getSavePath());
            return values;
        }
    }
}
