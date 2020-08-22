package com.vuvanduong.ringchat.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.HomeActivity;
import com.vuvanduong.ringchat.activity.WelcomeActivity;
import com.vuvanduong.ringchat.app.InitialApp;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.SharedPrefs;

import java.io.Serializable;
import java.util.Locale;

public class DialogLanguage extends Activity implements
        android.view.View.OnClickListener {

    public RadioButton vn, en;
    Button cancel, save;
    String locale = "", country = "";
    ImageView btnBackFromLanguage;
    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.language_dialog);
        vn = findViewById(R.id.rbtnVN);
        en = findViewById(R.id.rbtnEN);
        cancel = findViewById(R.id.btnDialogCancel);
        save = findViewById(R.id.btnDialogOK);
        btnBackFromLanguage = findViewById(R.id.btnBackFromLanguage);
        vn.setOnClickListener(this);
        en.setOnClickListener(this);
        cancel.setOnClickListener(this);
        save.setOnClickListener(this);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user_login");

        btnBackFromLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rbtnVN:
                //Toast.makeText(c, "VN", Toast.LENGTH_SHORT).show();
                locale = "vi";
                country = "VN";
                break;
            case R.id.rbtnEN:
                //Toast.makeText(c, "EN", Toast.LENGTH_SHORT).show();
                locale = "en";
                country = "EN";
                break;
            case R.id.btnDialogCancel:
                finish();
                break;
            case R.id.btnDialogOK:
                String language = SharedPrefs.getInstance().get(Constant.LANGUAGE_CODE, String.class);
                if (locale.equalsIgnoreCase("")) {
                    Toast.makeText(this, R.string.alert_select_language, Toast.LENGTH_SHORT).show();
                } else if (!language.equalsIgnoreCase(locale)) {
                    setLanguage(locale);
                    SharedPrefs.getInstance().put(Constant.LANGUAGE_CODE, locale);
                    Intent refresh = new Intent(this, WelcomeActivity.class);
                    refresh.putExtra("user_login", (Serializable) user);
                    Toast.makeText(this, R.string.success_language, Toast.LENGTH_SHORT).show();
                    InitialApp.self().changeLanguage(locale);
                    finish();
                    startActivity(refresh);
                }else if(language.equalsIgnoreCase(locale)){
                    Toast.makeText(this, R.string.your_lang, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void setLanguage(String local) {
        Resources res = getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(local.toLowerCase(), country)); // API 17+ only.
        // Use conf.locale = new Locale(...) if targeting lower versions
        res.updateConfiguration(conf, dm);
    }
}
