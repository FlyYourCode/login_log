package com.job.scheduler;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.job.scheduler.util.EncryptUtil;
import com.job.scheduler.util.NetWordUtil;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class MainActivity extends AppCompatActivity {

    DataReceiver receiver;
    TextView tv_ip;
    boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_ip = findViewById(R.id.tv_ip);
        initListener();
        initReceiver();
    }






    private void initReceiver() {
        receiver = new DataReceiver();
        registerReceiver(receiver, new IntentFilter("sendData"));
    }

    private void initListener() {
        final TextView tv_button = findViewById(R.id.tv_button);
        final EditText et_user_name = findViewById(R.id.et_user_name);
        final EditText et_user_pass = findViewById(R.id.et_user_pass);
        tv_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(et_user_name.getText().toString())) {
                    Toast.makeText(MainActivity.this, "请输入用户名", Toast.LENGTH_LONG).show();
                    return;
                }
                MyApp.userName = et_user_name.getText().toString();
                if (TextUtils.isEmpty(et_user_pass.getText().toString())) {
                    Toast.makeText(MainActivity.this, "请输入密码", Toast.LENGTH_LONG).show();
                    return;
                }
                MyApp.password = et_user_pass.getText().toString();
                if (!running) {
                    startService();
                    tv_button.setText("运行中...");
                    tv_button.setBackgroundColor(Color.parseColor("#1ABC9C"));
                    running = true;
                }
            }
        });
    }


    private void startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, MyNetService.class));
        } else {
            startService(new Intent(this, MyNetService.class));
        }
    }


    class DataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String ip = intent.getStringExtra("currentIp");
            if (!TextUtils.isEmpty(ip) && (tv_ip != null)) {
                tv_ip.setText(ip);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }
}