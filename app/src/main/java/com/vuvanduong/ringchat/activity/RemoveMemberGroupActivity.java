package com.vuvanduong.ringchat.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.adapter.SelectFriendAdapter;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.GroupChat;
import com.vuvanduong.ringchat.model.Message;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.DBUtil;
import com.vuvanduong.ringchat.util.NetworkUtil;
import com.vuvanduong.ringchat.util.UserUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class RemoveMemberGroupActivity extends AppCompatActivity {
    User userLogin;
    private GroupChat groupChat;
    private ArrayList<User> usersInRoom;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference groupMembers = dbReference.child("groupMembers");
    private DatabaseReference groupMessages = dbReference.child("groupMessages");
    private DatabaseReference groupLastMessages = dbReference.child("groupLastMessages");
    RecyclerView rvFriendsRemoveGroup;
    EditText txtGroupNameRemove;
    SelectFriendAdapter selectFriendAdapter;
    ImageView btnBackToRemoveMember;
    Button btnRemoveMemberGroup;
    ArrayList<User> chosenContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_member_group);

        Intent intent = getIntent();
        userLogin = (User) intent.getSerializableExtra("user_login");
        groupChat = (GroupChat) intent.getSerializableExtra("group");
        usersInRoom = intent.getParcelableArrayListExtra("usersInRoom");

        setControl();
        setEvent();
    }

    private void setEvent() {
        btnBackToRemoveMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.putExtra("usersInRoom",usersInRoom);
                intent.putExtra("groupChat",groupChat);
                setResult(Constant.GET_MEMBER_REMAIN,intent);
                finish();
            }
        });

        btnRemoveMemberGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenContact = new ArrayList<>();
                chosenContact = selectFriendAdapter.getListFriendSelected();
                if (chosenContact.size()==0){
                    Toast.makeText(RemoveMemberGroupActivity.this, getString(R.string.please_select_friend), Toast.LENGTH_SHORT).show();
                }else if (txtGroupNameRemove.getText().toString().trim().equalsIgnoreCase("")){
                    Toast.makeText(RemoveMemberGroupActivity.this, getString(R.string.please_type_group_name), Toast.LENGTH_SHORT).show();
                }else if (txtGroupNameRemove.getText().toString().trim().length()>50){
                    Toast.makeText(RemoveMemberGroupActivity.this, getString(R.string.group_name_too_long), Toast.LENGTH_SHORT).show();
                }else {
                    if (NetworkUtil.getConnectivityStatusString(RemoveMemberGroupActivity.this) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                        Toast.makeText(RemoveMemberGroupActivity.this, getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    StringBuilder nameMembers = new StringBuilder();
                    String groupName = txtGroupNameRemove.getText().toString().trim();
                    for (int i=0; i<chosenContact.size();i++) {
                        groupMembers.child(groupChat.getIdRoom()).child(chosenContact.get(i).getId()).removeValue();
                        usersInRoom.remove(chosenContact.get(i));
                        if (i == chosenContact.size() - 1) {
                            nameMembers.append(UserUtil.getFullName(chosenContact.get(i)));
                        } else {
                            nameMembers.append(UserUtil.getFullName(chosenContact.get(i))).append(",");
                        }
                    }
                    Message message = new Message();
                    message.setDatetime(DBUtil.getStringDateTimeChatRoom());
                    message.setUserID(userLogin.getId());
                    message.setType("group");
                    String context = UserUtil.getFullName(userLogin)+" "+getString(R.string.removed)+" "+
                            nameMembers+" "+getString(R.string.from_group);
                    message.setContext(context);
                    GroupChat group = new GroupChat(message.getUserID(),message.getContext(),message.getDatetime(),message.getType(),groupName);
                    groupLastMessages.child(groupChat.getIdRoom()).setValue(group);
                    message.setDatetime(DBUtil.getStringDateTime());
                    groupMessages.child(groupChat.getIdRoom()).push().setValue(message);
                    Toast.makeText(RemoveMemberGroupActivity.this, context, Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent();
                    intent.putExtra("usersInRoom",usersInRoom);
                    intent.putExtra("groupChat",groupChat);
                    setResult(Constant.GET_MEMBER_REMAIN,intent);
                    finish();
                }
            }
        });
    }

    private void setControl() {
        rvFriendsRemoveGroup = findViewById(R.id.rvFriendsRemoveGroup);
        txtGroupNameRemove = findViewById(R.id.txtGroupNameRemove);
        btnBackToRemoveMember = findViewById(R.id.btnBackToRemoveMember);
        btnRemoveMemberGroup = findViewById(R.id.btnRemoveMemberGroup);
        chosenContact = new ArrayList<>();
        selectFriendAdapter = new SelectFriendAdapter(usersInRoom, RemoveMemberGroupActivity.this);
        txtGroupNameRemove.setText(groupChat.getGroupName());
        initView();
    }

    private void initView(){
        rvFriendsRemoveGroup.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(RemoveMemberGroupActivity.this,LinearLayoutManager.VERTICAL,false);
        rvFriendsRemoveGroup.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvFriendsRemoveGroup.getContext(),
                linearLayoutManager.getOrientation());
        rvFriendsRemoveGroup.addItemDecoration(dividerItemDecoration);
        rvFriendsRemoveGroup.setAdapter(selectFriendAdapter);
    }
}
