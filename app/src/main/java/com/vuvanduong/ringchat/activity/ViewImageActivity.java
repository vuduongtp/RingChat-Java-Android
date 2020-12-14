package com.vuvanduong.ringchat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.User;

public class ViewImageActivity extends AppCompatActivity {
    String url;
    ImageView imageViewMessage;
    Button btnCloseImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        Intent intent = getIntent();
        url = intent.getStringExtra("url");

        imageViewMessage = findViewById(R.id.imageViewMessage);
        btnCloseImageView = findViewById(R.id.btnCloseImageView);

        Picasso.with(this)
                .load(url)
                .placeholder(R.drawable.emptyimage)
                .into(imageViewMessage);

        btnCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}