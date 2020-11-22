package com.vuvanduong.ringchat.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.ImageUtils;
import com.vuvanduong.ringchat.util.UserUtil;

public class UserProfileActivity extends AppCompatActivity {
    ImageView imgProfileAvatar,imgProfileBack,btnProfileChat,btnProfileUnfriend,btnProfileAddFriend;
    TextView txtNameUserProfile,txtUserProfileInfo;
    User userLogin, userScan;
    boolean isFriend,isUserLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        setControl();
        setEvent();
    }

    private void setEvent() {
        imgProfileBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setControl() {
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        imgProfileBack = findViewById(R.id.imgProfileBack);
        btnProfileChat = findViewById(R.id.btnProfileChat);
        btnProfileUnfriend = findViewById(R.id.btnProfileUnfriend);
        btnProfileAddFriend = findViewById(R.id.btnProfileAddFriend);
        txtNameUserProfile = findViewById(R.id.txtNameUserProfile);
        txtUserProfileInfo = findViewById(R.id.txtUserProfileInfo);

        Intent intent = getIntent();
        userLogin = (User) intent.getSerializableExtra("user_login");
        userScan = (User) intent.getSerializableExtra("user_scan");
        isFriend = intent.getBooleanExtra("isScanFriend",false);
        isUserLogin = intent.getBooleanExtra("isUserLogin",false);

        if (isUserLogin){
            btnProfileChat.setVisibility(View.GONE);
            btnProfileAddFriend.setVisibility(View.GONE);
            btnProfileUnfriend.setVisibility(View.GONE);
        }

        if (isFriend){
            btnProfileAddFriend.setVisibility(View.GONE);
        }else {
            btnProfileChat.setVisibility(View.GONE);
            btnProfileUnfriend.setVisibility(View.GONE);
        }

        txtNameUserProfile.setText(UserUtil.getFullName(userScan));
        String info = getResources().getString(R.string.firstname) + " : "+ userScan.getFirstname() + "\n"
                +getResources().getString(R.string.lastname) + " : "+ userScan.getLastname() + "\n"
                +getResources().getString(R.string.birthday) + " : "+ userScan.getBirthday() + "\n";
        txtUserProfileInfo.setText(info);
        if (!userScan.getImage().equalsIgnoreCase("")) {
            Picasso.with(this)
                    .load(userScan.getImage())
                    .placeholder(R.drawable.user)
                    .into(imgProfileAvatar);
        }
    }

}