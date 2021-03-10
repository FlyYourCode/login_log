package com.job.scheduler.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;


public class AppUtil {

    /**
     * 获取手机设备码
     */
    public static String getDeviceId(Context context) {
        String id = "";
        try {
            if (Build.VERSION.SDK_INT < 29) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                //低于android10获取IMSI号
                id = telephonyManager.getDeviceId();
            } else {
                //从Android10开始获取不到imsi及其他设备码，改为获取androidId
                id = Settings.System.getString(context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            }
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
