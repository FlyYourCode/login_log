package com.job.scheduler;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.job.scheduler.util.NetWordUtil;

import java.util.ArrayList;

public class MyNetService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    BroadcastReceiver netReceiver;


    @Override
    public void onCreate() {
        super.onCreate();
        //Log.i("MyNetService","onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.i("MyNetService", "---MyNetService 启动---");
        //注册监听网络变化广播
        initNetChangeReceiver();
        startNotificationForeground();
        return START_STICKY;
    }

    private void startNotificationForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "NFCService";
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel Channel = new NotificationChannel(CHANNEL_ID, "主服务", NotificationManager.IMPORTANCE_HIGH);
            Channel.enableLights(true);//设置提示灯
            Channel.setLightColor(Color.RED);//设置提示灯颜色
            Channel.setShowBadge(true);//显示logo
            Channel.setDescription("bottombar notification");//设置描述
            Channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); //设置锁屏可见 VISIBILITY_PUBLIC=可见
            if (manager != null) {
                manager.createNotificationChannel(Channel);
            }

            Notification notification = new Notification.Builder(this)
                    .setChannelId(CHANNEL_ID)
                    .setAutoCancel(false)
                    .setContentTitle("主服务")//标题
                    .setContentText("运行中...")//内容
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)//小图标一定需要设置,否则会报错(如果不设置它启动服务前台化不会报错,但是你会发现这个通知不会启动),如果是普通通知,不设置必然报错
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .build();
            startForeground(1, notification);//服务前台化只能使用startForeground()方法,不能使用 notificationManager.notify(1,notification); 这个只是启动通知使用的,使用这个方法你只需要等待几秒就会发现报错了
        }
    }

    private void initNetChangeReceiver() {
        IntentFilter timeFilter = new IntentFilter();
        timeFilter.addAction("android.net.ethernet.ETHERNET_STATE_CHANGED");
        timeFilter.addAction("android.net.ethernet.STATE_CHANGE");
        timeFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        timeFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        timeFilter.addAction("android.net.wifi.STATE_CHANGE");
        timeFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        netReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(
                            Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isAvailable()) {
                        int type2 = networkInfo.getType();
                        String typeName = networkInfo.getTypeName();

                        switch (type2) {
                            case 0://移动 网络    2G 3G 4G 都是一样的
                                if (NetWordUtil.isMobileNetworkAvailable()) {
                                    NetWordUtil.getLocalSavedIpToCompare(MyApp.instance);
                                }
                                getIpDelay();
                                break;
                            case 1: //wifi网络
                                break;
                            default:
                        }
                    } else {// 无网络
                    }
                }
            }

        };
        registerReceiver(netReceiver, timeFilter);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (netReceiver != null) {
            unregisterReceiver(netReceiver);
            netReceiver = null;
        }
    }

    /**
     * 如果服务运行中，间隔2秒调用一次，如果当前网络是移动网络，获取ip和本地存储ip作比较，不一样就上传
     */
    private void getIpDelay() {
        if (isServiceRunning(this, "com.job.scheduler.MyNetService")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (NetWordUtil.isMobileNetworkAvailable()) {
                        Log.i("ip---", "service_delay");
                        NetWordUtil.getLocalSavedIpToCompare(MyNetService.this);
                        getIpDelay();
                    }
                }
            }, 2000);
        }
    }


    /**
     * 判断服务是否开启
     *
     * @return
     */
    public static boolean isServiceRunning(Context context, String ServiceName) {
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(1000);
        for (int i = 0; i < runningService.size(); i++) {
            //Log.i("服务运行1：",""+runningService.get(i).service.getClassName().toString());
            if (runningService.get(i).service.getClassName().toString().equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

}
