package com.vuvanduong.ringchat.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.DBUtil;

import java.util.ArrayList;

public class AddFriendActivity extends AppCompatActivity {
    private User user;
    ImageView btnBackToContact;
    EditText txtSearchNewFriend;
    ImageButton btnSearchNewFriend;
    RecyclerView rvContactAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user_login");

        setControl();
        setEvent();
    }

    private void setEvent() {
        btnSearchNewFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<User> users =  DBUtil.getAllUser();
                Toast.makeText(AddFriendActivity.this, "Size "+users.size(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setControl() {
        btnBackToContact = findViewById(R.id.btnBackToContact);
        txtSearchNewFriend = findViewById(R.id.txtSearchNewFriend);
        btnSearchNewFriend = findViewById(R.id.btnSearchNewFriend);
        rvContactAdd = findViewById(R.id.rvContactAdd);
    }
}
