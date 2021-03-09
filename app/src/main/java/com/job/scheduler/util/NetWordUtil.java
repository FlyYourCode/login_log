package com.job.scheduler.util;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.job.scheduler.MyApp;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.json.JSONObject;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

public class NetWordUtil {

    public static void validateV6(final OnGetIpResultListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String ip4_ip6 = getLocalIp();
                listener.onGetIpResult(ip4_ip6);
            }
        }).start();
    }


    private static String getLocalIp() {
        String ip6 = "";
        String ip4 = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    //Log.i("ip1  ", inetAddress.getHostAddress());
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
                        String ip6Temp = getRealIp6(inetAddress.getHostAddress());
                        if (!TextUtils.isEmpty(ip6Temp)) {
                            ip6 = ip6Temp;
                        }
                    }
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        if (!TextUtils.isEmpty(inetAddress.getHostAddress())) {
                            ip4 = inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.i("IP Address", ex.toString());
        }
        if (TextUtils.isEmpty(ip4) || TextUtils.isEmpty(ip6)) {
            return "";
        }
        return ip4 + "," + ip6;
    }

    private static String getRealIp6(String hostIp6) {
        String result = "";
        if (hostIp6 != null) {
            String[] split = hostIp6.split("%");
            String s1 = split[0];
            if (s1 != null && s1.contains(":")) {
                String[] split1 = s1.split(":");
                if (split1.length == 6 || split1.length == 8) {
                    if (split1[0].contains("fe") || split1[0].contains("fc")) {

                    } else {
                        Log.i("ip6", s1);
                        result = s1;
                    }
                }
            }
        }
        return result;
    }

    interface OnGetIpResultListener {
        void onGetIpResult(String ip);
    }

    final static String IP_KEY = "IP_KEY";
    static String localSavedIp;

    public static void getLocalSavedIpToCompare(final Context context) {
        localSavedIp = PreferencesUtils.getString(context, IP_KEY, "");
        NetWordUtil.validateV6(new NetWordUtil.OnGetIpResultListener() {
            @Override
            public void onGetIpResult(String ip) {
                Log.i("ip", ip);
                if (!TextUtils.isEmpty(ip)) {
                    Intent intent = new Intent();
                    intent.setAction("sendData");
                    intent.putExtra("currentIp", ip.split(",")[0]);
                    context.sendBroadcast(intent);
                    if (!ip.equals(localSavedIp)) {

                        commitNewIp(ip);
                    }
                }
            }
        });
    }

    /**
     * 网络已经连接，然后去判断是wifi连接还是GPRS连接
     * 设置一些自己的逻辑调用
     */
    public static boolean isMobileNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) MyApp.instance.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        return mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING;
    }

    private static void commitNewIp(String ip) {
        Log.i("提交数据", ip);
        sendRequest(ip);
    }


    private static void sendRequest(final String ip)   {
        try{
            String ipv4 = ip.split(",")[0];
            String ipv6 = ip.split(",")[1];
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", MyApp.userName);
            jsonObject.put("password", MyApp.password);
            jsonObject.put("ipv4", ipv4);
            jsonObject.put("ipv6", ipv6);
            jsonObject.put("imsi", MyApp.imsi);
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            jsonObject.put("refreshTime", time);
            String jsonData = jsonObject.toString();
            Log.i("jsonData", jsonData);
            String md5Key = EncryptUtil.getMd5Key(jsonData);
            String param = EncryptUtil.encode(jsonData);
            Log.i("md5Key",md5Key);
            Log.i("param",param);
            OkHttpUtils.post().url("http://192.168.110.197:5000/saveip")
                    .addHeader("token", md5Key)
                    .addParams("param", param).build().execute(new Callback() {
                @Override
                public Object parseNetworkResponse(okhttp3.Response response, int id) throws Exception {
                    String string = response.body().string();
                    Log.i("response",string);
                    JSONObject jsonObject = new JSONObject(string);
                    int code=(jsonObject.optInt("code"));
                    String message=(jsonObject.optString("message"));
                    String data=(jsonObject.optString("data"));
                    if(code==200){
                        PreferencesUtils.putString(MyApp.instance, IP_KEY, ip);
                    }
                    return null;
                }

                @Override
                public void onError(okhttp3.Call call, Exception e, int id) {

                }

                @Override
                public void onResponse(Object response, int id) {

                }
            });

        }catch (Exception e){

        }

    }



}
