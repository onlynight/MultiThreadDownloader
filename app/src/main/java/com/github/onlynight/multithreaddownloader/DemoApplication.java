package com.github.onlynight.multithreaddownloader;

import android.app.Application;
import android.content.Context;

/**
 * Created by lion on 2017/2/9.
 */

public class DemoApplication extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        context = base;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
