package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.adapter.MessageAdapter;
import com.vuvanduong.ringchat.model.GroupChat;
import com.vuvanduong.ringchat.model.Message;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.DBUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class GroupConversationActivity extends AppCompatActivity {
    User userLogin;
    RecyclerView rvChatGroupConversation;
    TextView txtNameGroupConversation;
    ImageButton btnRemoveMemberGroup,btnAddMemberGroup;
    Button btnSendMessageGroupConversation;
    EditText txtContextGroupConversation;
    private GroupChat groupChat;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference groupLastMessage, groupMessages, groupMembers;
    private DatabaseReference users = dbReference.child("users");
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;
    private ArrayList<User> usersInRoom;
    private ChildEventListener messageReceive;
    ProgressBar loadingConversation;
    ImageView btnBackFromGroupConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_conversation);

        Intent intent = getIntent();
        userLogin = (User) intent.getSerializableExtra("userLogin");
        groupChat = (GroupChat) intent.getSerializableExtra("group");

        assert groupChat != null;
        groupLastMessage = dbReference.child("groupLastMessage/"+groupChat.getIdRoom());
        groupMessages = dbReference.child("groupMessages/"+groupChat.getIdRoom());
        groupMembers = dbReference.child("groupMembers/"+groupChat.getIdRoom());
        usersInRoom = new ArrayList<>();
        loadingConversation = findViewById(R.id.loadingGroupConversation);
        loadingConversation.setVisibility(View.VISIBLE);
        setControl();
        setEvent();
        getData();
    }

    private void setEvent() {
        btnBackFromGroupConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSendMessageGroupConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtContextGroupConversation.getText().toString().trim().equals("")){
                    Message newMessage = new Message();
                    newMessage.setType("message");
                    newMessage.setContext(txtContextGroupConversation.getText().toString().trim());
                    newMessage.setDatetime(DBUtil.getStringDateTime());
                    newMessage.setUserID(userLogin.getId());

                    //them vao firebase
                    groupMessages.push().setValue(newMessage);
                    newMessage.setDatetime(DBUtil.getStringDateTimeChatRoom());
                    groupLastMessage.setValue(newMessage);
                }
                txtContextGroupConversation.setText("");
            }
        });

        btnAddMemberGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnRemoveMemberGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent remove = new Intent(GroupConversationActivity.this, RemoveMemberGroupActivity.class);
                remove.putExtra("user_login", (Serializable) userLogin);
                remove.putParcelableArrayListExtra("usersInRoom", usersInRoom);
                remove.putExtra("group",groupChat);
                startActivity(remove);
            }
        });
    }

    private void setControl() {
        rvChatGroupConversation = findViewById(R.id.rvChatGroupConversation);
        txtNameGroupConversation = findViewById(R.id.txtNameGroupConversation);
        btnRemoveMemberGroup = findViewById(R.id.btnRemoveMemberGroup);
        btnAddMemberGroup = findViewById(R.id.btnAddMemberGroup);
        btnSendMessageGroupConversation = findViewById(R.id.btnSendMessageGroupConversation);
        txtContextGroupConversation = findViewById(R.id.txtContextGroupConversation);
        btnBackFromGroupConversation = findViewById(R.id.btnBackFromGroupConversation);

        txtNameGroupConversation.setText(groupChat.getGroupName());
    }

    public void chatBoxView(int delayTime) {
        messages = new ArrayList<>();
        rvChatGroupConversation.setHasFixedSize(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        rvChatGroupConversation.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messages, getApplicationContext(), userLogin,usersInRoom,true);
        rvChatGroupConversation.setAdapter(messageAdapter);

        rvChatGroupConversation.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Objects.requireNonNull(rvChatGroupConversation.getAdapter()).getItemCount() > 0) {
                    rvChatGroupConversation.smoothScrollToPosition(0);
                }
            }
        }, delayTime);

        rvChatGroupConversation.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    rvChatGroupConversation.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (Objects.requireNonNull(rvChatGroupConversation.getAdapter()).getItemCount() > 0) {
                                rvChatGroupConversation.smoothScrollToPosition(0);
                            }
                        }
                    }, 200);
                }
            }
        });
    }

    private void getData(){
        messageReceive = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    if (loadingConversation.getVisibility()==View.VISIBLE){
                        loadingConversation.setVisibility(View.GONE);
                    }
                    //get from firebase
                    Message message;
                    message = dataSnapshot.getValue(Message.class);
                    //add to GUI
                    messageAdapter.addItem(message);
                    rvChatGroupConversation.smoothScrollToPosition(0);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        groupMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final int count = (int) dataSnapshot.getChildrenCount();
                for (DataSnapshot item : dataSnapshot.getChildren()){
                    String userId = item.getKey();
                    Query getUser = users.orderByKey().equalTo(userId);
                    getUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                User user = item.getValue(User.class);
                                assert user != null;
                                user.setId(item.getKey());
                                usersInRoom.add(user);
                                if (count == usersInRoom.size()){
                                    chatBoxView(200);
                                    groupMessages.addChildEventListener(messageReceive);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                groupMembers.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        groupMessages.removeEventListener(messageReceive);
    }
}
