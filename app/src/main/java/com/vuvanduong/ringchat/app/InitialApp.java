package com.vuvanduong.ringchat.app;

import android.app.ActivityManager;
import android.app.Application;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.util.SharedPrefs;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

public class InitialApp extends Application {
    private static InitialApp sInstance;

    public static InitialApp self() {
        return sInstance;
    }

    public void changeLanguage(String lang){
        setLanguage(lang);
        SharedPrefs.getInstance().put(Constant.LANGUAGE_CODE, lang);
    }

    private void setLanguage(String local) {
        Resources res = getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(local.toLowerCase(), local.toUpperCase())); // API 17+ only.
        // Use conf.locale = new Locale(...) if targeting lower versions
        res.updateConfiguration(conf, dm);
    }

    public void clearApplicationData() {
        File cacheDirectory = getCacheDir();
        File applicationDirectory = new File(cacheDirectory.getParent());
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) {
                    deleteFile(new File(applicationDirectory, fileName));
                }
            }
        }
        ((ActivityManager) Objects.requireNonNull(sInstance.getSystemService(ACTIVITY_SERVICE)))
                .clearApplicationUserData();
    }

    public static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    deletedAll = deleteFile(new File(file, children[i])) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }

        return deletedAll;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance=this;

    }


}
