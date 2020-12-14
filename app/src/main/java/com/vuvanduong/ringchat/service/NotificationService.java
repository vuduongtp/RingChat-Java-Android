package com.vuvanduong.ringchat.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;

import java.util.ArrayList;

public class NotificationService extends Service {
    private ArrayList<ChildEventListener> conversationEventListeners;
    private ArrayList<ChildEventListener> groupEventListeners;
    private String conversationId="";
    private String groupId="";
    private BroadcastReceiver mMessageReceiver;
    public NotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("NOTIFY");
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get extra data included in the Intent
                String brStatus = intent.getStringExtra("message");

                if(brStatus != null){

                    Log.e("NOTIFY ",brStatus);
                }
            }
        };
        registerReceiver(mMessageReceiver, filter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}