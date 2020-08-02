package com.vuvanduong.ringchat.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.adapter.MessageAdapter;
import com.vuvanduong.ringchat.model.Message;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.DBUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ConversationActivity extends AppCompatActivity {
    User userLogin,friend;
    TextView txtStatusConversation,txtNameFriendConversation;
    EditText txtContextConversation;
    RecyclerView rvChatConversation;
    Button btnSendMessageConversation;
    private String chatRoom = "";
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference conversationLastMessage, conversationMessages, allConversationMessages;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;
    private ArrayList<User> usersInRoom;
    private ChildEventListener messageReceive;
    ProgressBar loadingConversation;
    ImageView btnBackFromConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        Intent intent = getIntent();
        userLogin = (User) intent.getSerializableExtra("userLogin");
        friend = (User) intent.getSerializableExtra("friend");
        usersInRoom = new ArrayList<>();
        usersInRoom.add(friend);
        loadingConversation = findViewById(R.id.loadingConversation);
        loadingConversation.setVisibility(View.VISIBLE);
        setControl();
        setEvent();
    }

    private void setEvent() {
        btnSendMessageConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtContextConversation.getText().toString().trim().equals("")){
                    Message newMessage = new Message();
                    newMessage.setType("message");
                    newMessage.setContext(txtContextConversation.getText().toString().trim());
                    newMessage.setDatetime(DBUtil.getStringDateTime());
                    newMessage.setUserID(userLogin.getId());

                    //them vao firebase
                    conversationLastMessage.setValue(newMessage);
                    conversationMessages.push().setValue(newMessage);
                }
                txtContextConversation.setText("");
            }
        });

        btnBackFromConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void setControl() {
        txtStatusConversation = findViewById(R.id.txtStatusConversation);
        txtNameFriendConversation = findViewById(R.id.txtNameFriendConversation);
        txtContextConversation = findViewById(R.id.txtContextConversation);
        rvChatConversation = findViewById(R.id.rvChatConversation);
        btnSendMessageConversation = findViewById(R.id.btnSendMessageConversation);
        btnBackFromConversation = findViewById(R.id.btnBackFromConversation);

        txtNameFriendConversation.setText(friend.getFullname());
        if (friend.getStatus()==null || friend.getStatus().equalsIgnoreCase("" )
                || friend.getStatus().equalsIgnoreCase("Offline")){
            txtStatusConversation.setText("Offline");
            txtStatusConversation.setTextColor(ContextCompat.getColor(this, R.color.red));
        }else {
            txtStatusConversation.setText(friend.getStatus());
            txtStatusConversation.setTextColor(ContextCompat.getColor(this, R.color.green));
        }

        chatRoom = DBUtil.getChatRoomByTwoUserId(userLogin.getId(),friend.getId());
        allConversationMessages = dbReference.child("conversationLastMessage");
        conversationLastMessage = dbReference.child("conversationLastMessage/"+chatRoom);
        conversationMessages = dbReference.child("conversationMessages/"+chatRoom);

        messages = new ArrayList<>();
        ValueEventListener getListMessage = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //get data when after update message
 //               if (dataSnapshot.exists()) {// get data when first open
                    if (!messages.isEmpty()) messages.clear();
//                    for (DataSnapshot item : dataSnapshot.getChildren()) {
//                        messages.add(item.getValue(Message.class));
//                    }
                    Collections.reverse(messages);
                    chatBoxView(200);
                    conversationMessages.addChildEventListener(messageReceive);
                    loadingConversation.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error load database", Toast.LENGTH_SHORT).show();
            }
        };

        messageReceive = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()) {
                    //get from firebase
                    Message message;
                    message = dataSnapshot.getValue(Message.class);
                    //add to GUI
                    messageAdapter.addItem(message);
                    rvChatConversation.smoothScrollToPosition(0);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        //create messages if it is not exist
        final Query check_ChatRoom = allConversationMessages;
        check_ChatRoom.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(chatRoom)){
                    conversationMessages.setValue(0);
                }else {
                    return;
                }
                check_ChatRoom.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Lỗi load database", Toast.LENGTH_SHORT).show();
            }
        });

        conversationMessages.addListenerForSingleValueEvent(getListMessage);
        conversationMessages.removeEventListener(getListMessage);

    }

    public void chatBoxView(int delayTime) {
        rvChatConversation.setHasFixedSize(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        rvChatConversation.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messages, getApplicationContext(), userLogin,usersInRoom,false);
        rvChatConversation.setAdapter(messageAdapter);

        rvChatConversation.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Objects.requireNonNull(rvChatConversation.getAdapter()).getItemCount() > 0) {
                    rvChatConversation.smoothScrollToPosition(0);
                }
            }
        }, delayTime);

        rvChatConversation.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    rvChatConversation.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (Objects.requireNonNull(rvChatConversation.getAdapter()).getItemCount() > 0) {
                                rvChatConversation.smoothScrollToPosition(0);
                            }
                        }
                    }, 200);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        conversationMessages.removeEventListener(messageReceive);
    }
}
