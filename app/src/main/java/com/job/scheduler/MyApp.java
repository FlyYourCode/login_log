package com.job.scheduler;

import android.app.Application;
import android.util.Log;

import com.job.scheduler.util.AppUtil;

public class MyApp extends Application {

    public static MyApp instance;
    public static String imsi;
    public static String userName;
    public static String password;
    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
        imsi= AppUtil.getIMSI(this);
        Log.i("imsi",imsi);

    }
}
