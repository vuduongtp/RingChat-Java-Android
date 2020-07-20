package com.vuvanduong.ringchat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.vuvanduong.ringchat.R;

public class ForgetPasswordActivity extends AppCompatActivity {
    ImageView backToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        addControl();
        addEvent();
    }

    private void addControl() {
        backToLogin = findViewById(R.id.btnBackToLogin2);
    }

    private void addEvent() {
        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
