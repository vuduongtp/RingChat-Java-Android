package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.adapter.MessageAdapter;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.database.ConversationLastMessageDB;
import com.vuvanduong.ringchat.database.ConversationMessageDB;
import com.vuvanduong.ringchat.model.Message;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.service.LinphoneService;
import com.vuvanduong.ringchat.util.CircleTransform;
import com.vuvanduong.ringchat.util.DBUtil;
import com.vuvanduong.ringchat.util.ImageUtils;
import com.vuvanduong.ringchat.util.NetworkUtil;
import com.vuvanduong.ringchat.util.UserUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

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
    User userLogin, friend;
    TextView txtStatusConversation, txtNameFriendConversation;
    EditText txtContextConversation;
    RecyclerView rvChatConversation;
    Button btnSendMessageConversation, btnSendImageMessageConversation;
    private String chatRoom = "";
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference conversationLastMessage, conversationMessages, allConversationMessages, users;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;
    private ArrayList<User> usersInRoom;
    private ChildEventListener messageReceive, friendStatus;
    ProgressBar loadingConversation;
    ImageView btnBackFromConversation, imgAvatarFriendConversation;
    ImageButton img_but_video, img_but_voice;
    private String impu;
    private AccountCreator mAccountCreator;
    private CoreListenerStub mCoreListener;
    boolean isFirstLoad = true;

    private static final int PERMISSION_CODE = 2;
    private static final int PICK_IMAGE = 2;
    int rotationInDegrees = 0, rotation = 0;
    ProgressDialog dialog;

    private int pageCount;
    private int increment = 0;
    private int originNumItem = 0;
    private int countChildAdded = 0;
    public int TOTAL_LIST_ITEMS = 0;
    public int NUM_ITEMS_PAGE = 10;
    ArrayList<Message> messageDisplay;
    private Map<String, String> config = new HashMap<String, String>();
    private ConversationMessageDB conversationMessageDB;
    private ConversationLastMessageDB conversationLastMessageDB;

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
        impu = friend.getId() + "@" + Constant.SIP_SERVER;
        imgAvatarFriendConversation = findViewById(R.id.imgAvatarFriendConversation);
        Picasso.with(this)
                .load(friend.getImage())
                .placeholder(R.drawable.user)
                .transform(new CircleTransform())
                .into(imgAvatarFriendConversation);

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

        conversationMessageDB = new ConversationMessageDB(ConversationActivity.this);
        conversationLastMessageDB = new ConversationLastMessageDB(ConversationActivity.this);

        friendStatus = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
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

        users = dbReference.child("users/" + friend.getId());
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
        Address proxy = Factory.instance().createAddress("sip:" + Constant.SIP_SERVER);
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
                if (!txtContextConversation.getText().toString().trim().equals("")) {
                    if (NetworkUtil.getConnectivityStatusString(ConversationActivity.this) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                        Message newMessage = new Message();
                        String id = conversationMessages.push().getKey();
                        newMessage.setType("Pending");
                        newMessage.setContext(txtContextConversation.getText().toString().trim());
                        newMessage.setDatetime(DBUtil.getStringDateTime());
                        newMessage.setUserID(userLogin.getId());

                        conversationMessageDB.insert(newMessage, id,chatRoom);
                        newMessage.setDatetime(DBUtil.getStringDateTimeChatRoom());
                        conversationLastMessageDB.insert(newMessage, chatRoom);
                        messageAdapter.addItem(newMessage);
                        rvChatConversation.smoothScrollToPosition(Objects.requireNonNull(rvChatConversation.getAdapter()).getItemCount() - 1);
                        txtContextConversation.setText("");
                        return;
                    }
                    Message newMessage = new Message();
                    newMessage.setType("message");
                    newMessage.setContext(txtContextConversation.getText().toString().trim());
                    newMessage.setDatetime(DBUtil.getStringDateTime());
                    newMessage.setUserID(userLogin.getId());

                    //them vao firebase
                    String id = conversationMessages.push().getKey();
                    conversationMessages.child(id).setValue(newMessage);
                    conversationMessageDB.insert(newMessage, id, chatRoom);
                    newMessage.setDatetime(DBUtil.getStringDateTimeChatRoom());
                    conversationLastMessage.setValue(newMessage);
                    conversationLastMessageDB.insert(newMessage,chatRoom);

                    //send by sip
//                    Core core = LinphoneService.getCore();
//                    String contactUserDomain = Constant.SIP_SERVER;
//                    String contactUserUri = "sip:" + friend.getId() + "@" + contactUserDomain;
//                    Address address = core.interpretUrl(contactUserUri);
//                    ChatRoom chatRoom = core.createChatRoom(address);
//                    if (chatRoom != null) {
//                        ChatMessage chatMessage = chatRoom.createEmptyMessage();
//                        chatMessage.addCustomHeader("fullNameFriend", UserUtil.getFullName(userLogin));
//                        chatMessage.addTextContent(txtContextConversation.getText().toString());
//                        if (chatMessage.getTextContent() != null) {
//                            chatRoom.sendChatMessage(chatMessage);
//                        }
//                    } else {
//                        Log.e("ERROR: ", "Cannot create chat room");
//                    }
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
                if (NetworkUtil.getConnectivityStatusString(ConversationActivity.this) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                    Toast.makeText(ConversationActivity.this, getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (friend.getStatus() == null || !friend.getStatus().equalsIgnoreCase("Online")) {
                    Toast.makeText(ConversationActivity.this, UserUtil.getFullName(friend) + " " + getString(R.string.friend_is_offline), Toast.LENGTH_SHORT).show();
                } else {
                    startVideoCall();
                }
            }
        });

        img_but_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.getConnectivityStatusString(ConversationActivity.this) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                    Toast.makeText(ConversationActivity.this, getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (friend.getStatus() == null || !friend.getStatus().equalsIgnoreCase("Online")) {
                    Toast.makeText(ConversationActivity.this, UserUtil.getFullName(friend) + " " + getString(R.string.friend_is_offline), Toast.LENGTH_SHORT).show();
                } else {
                    startCalling(impu);
                }
            }
        });

        btnSendImageMessageConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.getConnectivityStatusString(ConversationActivity.this) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                    Toast.makeText(ConversationActivity.this, getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                    return;
                }
                configCloudinary();
                requestPermission();
            }
        });

    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission
                (Objects.requireNonNull(ConversationActivity.this),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
        ) {
            accessTheGallery();
        } else {
            ActivityCompat.requestPermissions(
                    Objects.requireNonNull(ConversationActivity.this),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );
        }
    }

    public void accessTheGallery() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                accessTheGallery();
            } else {
                Toast.makeText(Objects.requireNonNull(ConversationActivity.this), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && data != null) {
            try {
                dialog = ProgressDialog.show(ConversationActivity.this, "",
                        "", true);
                dialog.show();
                ExifInterface exif = new ExifInterface(getRealPathFromUri(data.getData(), Objects.requireNonNull(ConversationActivity.this)));
                rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                rotationInDegrees = ImageUtils.exifToDegrees(rotation);
                InputStream inputStream = Objects.requireNonNull(ConversationActivity.this).getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
                new ImageProcessUpload().execute(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getRealPathFromUri(Uri imageUri, Activity activity) {
        Cursor cursor = activity.getContentResolver().query(imageUri, null, null, null, null);
        if (cursor == null) {
            return imageUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    private class ImageProcessUpload extends AsyncTask<InputStream, Void, Void> {
        protected Void doInBackground(InputStream... inputStream) {
            try {
                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream[0]);
                Bitmap liteImage = ImageUtils.getResizedBitmap(imgBitmap, ImageUtils.PHOTO_MAX_SIZE);
                //liteImage = ImageUtils.cropToSquare(liteImage);
                Matrix matrix = new Matrix();
                if (rotation != 0f) {
                    matrix.preRotate(rotationInDegrees);
                }
                rotation = 0;
                rotationInDegrees = 0;
                liteImage = Bitmap.createBitmap(liteImage, 0, 0, liteImage.getWidth(), liteImage.getHeight(), matrix, true);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                liteImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                uploadByteToCloudinary(byteArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private void uploadByteToCloudinary(final byte[] image) {
        try {
            MediaManager.get().upload(image)
                    .option("tags", "message")
                    .option("folder", "message")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            Message newMessage = new Message();
                            newMessage.setType("image");
                            newMessage.setContext(resultData.get("url").toString());
                            newMessage.setDatetime(DBUtil.getStringDateTime());
                            newMessage.setUserID(userLogin.getId());

                            //them vao firebase
                            String id = conversationMessages.push().getKey();
                            conversationMessages.child(id).setValue(newMessage);
                            conversationMessageDB.insert(newMessage, id, chatRoom);

                            newMessage.setDatetime(DBUtil.getStringDateTimeChatRoom());
                            newMessage.setContext(UserUtil.getFullName(userLogin) + " " + getString(R.string.sent_a_image));
                            conversationLastMessage.setValue(newMessage);
                            conversationLastMessageDB.insert(newMessage, chatRoom);
                            dialog.dismiss();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e("upload_image", error.toString());
                            dialog.dismiss();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                        }
                    }).dispatch();

        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void configCloudinary() {
        config.put("cloud_name", "vuduongtp");
        config.put("api_key", "987439358416729");
        config.put("api_secret", "Uj9Jes5zUjtAnYLXd81uR5qnGts");
        try {
            MediaManager.init(Objects.requireNonNull(ConversationActivity.this), config);
        }catch (IllegalStateException ex){
            android.util.Log.e("IllegalStateException",ex.toString());
        }
    }

    private Boolean CheckListEnable() {
        if (increment == pageCount) {
            return false;
        } else if (increment == 0) {
            return true;
        } else {
            return true;
        }
    }

    private ArrayList<Message> loadMessage(int number) {
        ArrayList<Message> messagesDisplay = new ArrayList<>();

        int start = (number + 1) * NUM_ITEMS_PAGE;
        if (start > TOTAL_LIST_ITEMS) start = TOTAL_LIST_ITEMS;
        for (int i = TOTAL_LIST_ITEMS - start; i < TOTAL_LIST_ITEMS; i++) {
            if (i < messages.size()) {
                messagesDisplay.add(messages.get(i));
            } else {
                break;
            }
        }
        return messagesDisplay;
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
        btnSendImageMessageConversation = findViewById(R.id.btnSendImageMessageConversation);

        txtNameFriendConversation.setText(UserUtil.getFullName(friend));
        if (friend.getStatus() == null || friend.getStatus().equalsIgnoreCase("")
                || friend.getStatus().equalsIgnoreCase("Offline")) {
            txtStatusConversation.setText("Offline");
            txtStatusConversation.setTextColor(ContextCompat.getColor(this, R.color.red));
        } else {
            txtStatusConversation.setText(friend.getStatus());
            txtStatusConversation.setTextColor(ContextCompat.getColor(this, R.color.green));
        }

        chatRoom = DBUtil.getChatRoomByTwoUserId(userLogin.getId(), friend.getId());
        allConversationMessages = dbReference.child("conversationLastMessage");
        conversationLastMessage = dbReference.child("conversationLastMessage/" + chatRoom);
        conversationMessages = dbReference.child("conversationMessages/" + chatRoom);

        messages = new ArrayList<>();
        messageDisplay = new ArrayList<>();

        if (NetworkUtil.getConnectivityStatusString(ConversationActivity.this)==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
            messages = conversationMessageDB.getAllMessageOfRoom(chatRoom);
            TOTAL_LIST_ITEMS = (int) messages.size();
            originNumItem = TOTAL_LIST_ITEMS;
            int val = TOTAL_LIST_ITEMS % NUM_ITEMS_PAGE;
            val = val == 0 ? 0 : 1;
            pageCount = TOTAL_LIST_ITEMS / NUM_ITEMS_PAGE + val;
            chatBoxView(500);
            if (loadingConversation.getVisibility()==View.VISIBLE) {
                loadingConversation.setVisibility(View.GONE);
            }
        }
        else {
            conversationMessageDB.deleteAll(chatRoom);
            ValueEventListener getListMessage = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //get data when after update message
                    if (dataSnapshot.exists()) {// get data when first open
                        if (!messages.isEmpty()) messages.clear();
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            Message message;
                            message = item.getValue(Message.class);
                            assert message != null;
                            messages.add(message);
                            conversationMessageDB.insert(message, item.getKey(), chatRoom);

                        }
                        TOTAL_LIST_ITEMS = (int) dataSnapshot.getChildrenCount();
                        originNumItem = TOTAL_LIST_ITEMS;
                        int val = TOTAL_LIST_ITEMS % NUM_ITEMS_PAGE;
                        val = val == 0 ? 0 : 1;
                        pageCount = TOTAL_LIST_ITEMS / NUM_ITEMS_PAGE + val;
                        //Collections.reverse(messages);
                    }
                    chatBoxView(500);
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
                    if (dataSnapshot.exists()) {
                        //get from firebase
                        countChildAdded++;
                        if (countChildAdded > originNumItem) {
                            Message message;
                            message = dataSnapshot.getValue(Message.class);
                            //add to GUI
                            if (message.getType().equalsIgnoreCase("messageP")){
                                return;
                            }
                            messageAdapter.addItem(message);

                            messages.add(message);
                            conversationMessageDB.insert(message, dataSnapshot.getKey(), chatRoom);
                            conversationLastMessageDB.insert(message, chatRoom);
                            TOTAL_LIST_ITEMS++;
                            int val = TOTAL_LIST_ITEMS % NUM_ITEMS_PAGE;
                            val = val == 0 ? 0 : 1;
                            pageCount = TOTAL_LIST_ITEMS / NUM_ITEMS_PAGE + val;

                            rvChatConversation.smoothScrollToPosition(Objects.requireNonNull(rvChatConversation.getAdapter()).getItemCount() - 1);
                            txtContextConversation.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            if (isFirstLoad && Objects.requireNonNull(rvChatConversation.getAdapter()).getItemCount() == TOTAL_LIST_ITEMS - 1) {
                                assert imm != null;
                                imm.showSoftInput(txtContextConversation, InputMethodManager.SHOW_IMPLICIT);
                                isFirstLoad = false;
                            }
                            if (Objects.requireNonNull(rvChatConversation.getAdapter()).getItemCount() == TOTAL_LIST_ITEMS) {
                                assert imm != null;
                                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
                            }
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
                    if (!dataSnapshot.hasChild(chatRoom)) {
                        conversationMessages.setValue(0);
                    } else {
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
    }

    public void chatBoxView(int delayTime) {
        rvChatConversation.setHasFixedSize(false);
        rvChatConversation.setItemViewCacheSize(30);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvChatConversation.setLayoutManager(layoutManager);
        if (!messageDisplay.isEmpty()) messageDisplay.clear();
        messageDisplay = loadMessage(increment);
        messageAdapter = new MessageAdapter(messageDisplay, getApplicationContext(), userLogin, usersInRoom, false);
        rvChatConversation.setAdapter(messageAdapter);
        increment++;

        rvChatConversation.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(-1) && newState == 0) {
                    if (CheckListEnable() && pageCount > 1 && messageDisplay.size() != TOTAL_LIST_ITEMS && messageDisplay.size()>0) {
                        loadingConversation.setVisibility(View.VISIBLE);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!messageDisplay.isEmpty()) messageDisplay.clear();
                                        messageDisplay = loadMessage(increment);
                                        messageAdapter.addArrayItem(messageDisplay);
                                        loadingConversation.setVisibility(View.GONE);
                                        increment++;
                                        rvChatConversation.smoothScrollToPosition(NUM_ITEMS_PAGE);
                                    }
                                });
                            }
                        }, 800);
                    }
                }
            }
        });

        rvChatConversation.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Objects.requireNonNull(rvChatConversation.getAdapter()).getItemCount() > 0) {
                    rvChatConversation.smoothScrollToPosition(rvChatConversation.getAdapter().getItemCount() - 1);
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
                                rvChatConversation.smoothScrollToPosition(rvChatConversation.getAdapter().getItemCount() - 1);
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
        try {
            conversationMessages.removeEventListener(messageReceive);
            users.removeEventListener(friendStatus);
        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LinphoneService.getCore() != null) {
            LinphoneService.getCore().addListener(mCoreListener);
        }
    }

    @Override
    protected void onPause() {
        if (LinphoneService.getCore() != null) {
            LinphoneService.getCore().removeListener(mCoreListener);
        }

        super.onPause();

    }

    @Override
    protected void onStart() {
        super.onStart();
        ProxyConfig proxyConfig = LinphoneService.getCore().getDefaultProxyConfig();
        if (proxyConfig == null) {
           try {
               configureAccount();
           }catch (RuntimeException ex){
               android.util.Log.e("RuntimeException", ex.toString());
           }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
