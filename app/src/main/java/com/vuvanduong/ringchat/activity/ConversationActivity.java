package com.vuvanduong.ringchat.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.model.User;

public class ConversationActivity extends AppCompatActivity {
    User userLogin,friend;
    TextView txtStatusConversation,txtNameFriendConversation;
    EditText txtContextConversation;
    RecyclerView rvChatConversation;
    Button btnSendMessageConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Intent intent = getIntent();
        userLogin = (User) intent.getSerializableExtra("userLogin");
        friend = (User) intent.getSerializableExtra("friend");
        assert friend != null;
        setControl();
        setEvent();
    }

    private void setEvent() {
    }

    private void setControl() {
        txtStatusConversation = findViewById(R.id.txtStatusConversation);
        txtNameFriendConversation = findViewById(R.id.txtNameFriendConversation);
        txtContextConversation = findViewById(R.id.txtContextConversation);
        rvChatConversation = findViewById(R.id.rvChatConversation);
        btnSendMessageConversation = findViewById(R.id.btnSendMessageConversation);
        txtNameFriendConversation.setText(userLogin.getFullname());
    }
}
