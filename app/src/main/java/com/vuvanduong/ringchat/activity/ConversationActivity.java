package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.Message;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.service.LinphoneService;
import com.vuvanduong.ringchat.util.DBUtil;
import com.vuvanduong.ringchat.util.UserUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import org.linphone.core.AccountCreator;
import org.linphone.core.Address;
import org.linphone.core.CallParams;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.TransportType;
import org.linphone.core.tools.Log;

public class ConversationActivity extends AppCompatActivity {
    User userLogin,friend;
    TextView txtStatusConversation,txtNameFriendConversation;
    EditText txtContextConversation;
    RecyclerView rvChatConversation;
    Button btnSendMessageConversation;
    private String chatRoom = "";
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference conversationLastMessage, conversationMessages, allConversationMessages, users;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;
    private ArrayList<User> usersInRoom;
    private ChildEventListener messageReceive,friendStatus;
    ProgressBar loadingConversation;
    ImageView btnBackFromConversation;
    ImageButton img_but_video,img_but_voice;
    private String impu;
    private AccountCreator mAccountCreator;
    private CoreListenerStub mCoreListener;
    int count =0;
    boolean isFirstLoad=true;

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
        impu = friend.getId()+"@"+Constant.SIP_SERVER;

        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String message) {
                if (state == RegistrationState.Ok) {
                    Toast.makeText(ConversationActivity.this, "Register: " + message, Toast.LENGTH_LONG).show();
                } else if (state == RegistrationState.Failed) {
                    if (core.getDefaultProxyConfig() != null)
                        core.setDefaultProxyConfig(null);
                    Toast.makeText(ConversationActivity.this, "Failure: " + message, Toast.LENGTH_LONG).show();
                }
            }
        };

        friendStatus = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) {
                    String userStatus = dataSnapshot.getValue(String.class);
                    assert userStatus != null;
                    if (Objects.requireNonNull(dataSnapshot.getKey()).equalsIgnoreCase("status")) {
                        if (userStatus.equalsIgnoreCase("Online")) {
                            userLogin.setStatus("Online");
                            txtStatusConversation.setText("Online");
                            txtStatusConversation.setTextColor(ContextCompat.getColor(ConversationActivity.this, R.color.green));
                        } else {
                            userLogin.setStatus("Offline");
                            txtStatusConversation.setText("Offline");
                            txtStatusConversation.setTextColor(ContextCompat.getColor(ConversationActivity.this, R.color.red));
                        }
                    }
                }
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

        users = dbReference.child("users/"+friend.getId());
        users.addChildEventListener(friendStatus);

        setControl();
        setEvent();
    }

    private void configureAccount() {
        // At least the 3 below values are required
        mAccountCreator.setUsername(userLogin.getId());
        mAccountCreator.setDomain(Constant.SIP_SERVER);
        mAccountCreator.setPassword("123456");

        // By default it will be UDP if not set, but TLS is strongly recommended
        mAccountCreator.setTransport(TransportType.Tcp);
        // This will automatically create the proxy config and auth info and add them to the Core
        ProxyConfig cfg = mAccountCreator.createProxyConfig();
        cfg.edit();
        Address proxy = Factory.instance().createAddress("sip:"+Constant.SIP_SERVER);
        cfg.setServerAddr(proxy.asString());
        cfg.enableRegister(true);
        cfg.setExpires(3600);
        cfg.done();
        // Make sure the newly created one is the default
        LinphoneService.getCore().setDefaultProxyConfig(cfg);
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
                    conversationMessages.push().setValue(newMessage);
                    newMessage.setDatetime(DBUtil.getStringDateTimeChatRoom());
                    conversationLastMessage.setValue(newMessage);

                    Core core = LinphoneService.getCore();
                    String contactUserDomain = Constant.SIP_SERVER;
                    String contactUserUri = "sip:" + friend.getId() + "@" + contactUserDomain;
                    Address address = core.interpretUrl(contactUserUri);
                    ChatRoom chatRoom = core.createChatRoom(address);
                    if (chatRoom != null) {
                        ChatMessage chatMessage = chatRoom.createEmptyMessage();
                        chatMessage.addCustomHeader("fullNameFriend", UserUtil.getFullName(userLogin));
                        chatMessage.addTextContent(txtContextConversation.getText().toString());
                        if (chatMessage.getTextContent() != null) {
                            chatRoom.sendChatMessage(chatMessage);
                        }
                    } else {
                        Log.e("ERROR: ", "Cannot create chat room");
                    }
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

        img_but_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (friend.getStatus()==null || !friend.getStatus().equalsIgnoreCase("Online")){
                    Toast.makeText(ConversationActivity.this, UserUtil.getFullName(friend)+" "+ getString(R.string.friend_is_offline), Toast.LENGTH_SHORT).show();
                }else {
                    startVideoCall();
                }
            }
        });

        img_but_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (friend.getStatus()==null || !friend.getStatus().equalsIgnoreCase("Online")){
                    Toast.makeText(ConversationActivity.this, UserUtil.getFullName(friend)+" "+ getString(R.string.friend_is_offline), Toast.LENGTH_SHORT).show();
                }else {
                    startCalling(impu);
                }
            }
        });

    }

    private void startVideoCall() {
        Core core = LinphoneService.getCore();
        Address toSipAddress = core.interpretUrl(impu);
        CallParams params = core.createCallParams(null);
        params.enableVideo(true);
        if (core.isNetworkReachable()) {
            core.inviteAddressWithParams(toSipAddress, params);
        } else {
            Toast.makeText(
                    getBaseContext(),
                    "Network is unreachable",
                    Toast.LENGTH_LONG)
                    .show();
            Log.e(
                    "Error: "
                            + "Network is unreachable");
        }
    }

    private void startCalling(String impu) {
        Core core = LinphoneService.getCore();
        Address toSipAddress = core.interpretUrl(impu);
        CallParams params = core.createCallParams(null);
        try {
            core.inviteAddressWithParams(toSipAddress, params);
        } catch (Exception ex) {
            org.linphone.core.tools.Log.i("Unable to make a call: " + ex.toString());
        }
    }

    private void setControl() {
        txtStatusConversation = findViewById(R.id.txtStatusConversation);
        txtNameFriendConversation = findViewById(R.id.txtNameFriendConversation);
        txtContextConversation = findViewById(R.id.txtContextConversation);
        rvChatConversation = findViewById(R.id.rvChatConversation);
        btnSendMessageConversation = findViewById(R.id.btnSendMessageConversation);
        btnBackFromConversation = findViewById(R.id.btnBackFromConversation);
        img_but_video = findViewById(R.id.img_but_video);
        img_but_voice = findViewById(R.id.img_but_voice);

        txtNameFriendConversation.setText(UserUtil.getFullName(friend));
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
                    count = (int) dataSnapshot.getChildrenCount();
                    Collections.reverse(messages);
                    chatBoxView(800);
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
                    rvChatConversation.smoothScrollToPosition(Objects.requireNonNull(rvChatConversation.getAdapter()).getItemCount() -1);
                    txtContextConversation.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (isFirstLoad && Objects.requireNonNull(rvChatConversation.getAdapter()).getItemCount()==count-1) {
                        assert imm != null;
                        imm.showSoftInput(txtContextConversation, InputMethodManager.SHOW_IMPLICIT);
                        isFirstLoad = false;
                    }if (Objects.requireNonNull(rvChatConversation.getAdapter()).getItemCount()==count){
                        assert imm != null;
                        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,InputMethodManager.HIDE_IMPLICIT_ONLY);
                    }
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

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvChatConversation.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(messages, getApplicationContext(), userLogin,usersInRoom,false);
        rvChatConversation.setAdapter(messageAdapter);

        rvChatConversation.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Objects.requireNonNull(rvChatConversation.getAdapter()).getItemCount() > 0) {
                    rvChatConversation.smoothScrollToPosition(rvChatConversation.getAdapter().getItemCount() -1);
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
                                rvChatConversation.smoothScrollToPosition(rvChatConversation.getAdapter().getItemCount() -1);
                            }
                        }
                    }, 800);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        conversationMessages.removeEventListener(messageReceive);
        users.removeEventListener(friendStatus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LinphoneService.getCore()!=null) {
            LinphoneService.getCore().addListener(mCoreListener);
        }
    }

    @Override
    protected void onPause() {
        if (LinphoneService.getCore()!=null) {
            LinphoneService.getCore().removeListener(mCoreListener);
        }

        super.onPause();

    }

    @Override
    protected void onStart() {
        super.onStart();
        ProxyConfig proxyConfig = LinphoneService.getCore().getDefaultProxyConfig();
        if (proxyConfig == null) {
            configureAccount();
        }
    }
}
