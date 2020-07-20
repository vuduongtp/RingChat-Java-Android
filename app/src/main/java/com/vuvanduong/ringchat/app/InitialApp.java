package com.vuvanduong.ringchat.app;

import android.app.Application;

public class InitialApp extends Application {
    private static InitialApp sInstance;

    public static InitialApp self() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance=this;

    }


}
