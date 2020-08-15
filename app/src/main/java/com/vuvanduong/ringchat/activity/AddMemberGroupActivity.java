package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.adapter.SelectFriendAdapter;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.GroupChat;
import com.vuvanduong.ringchat.model.Message;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.DBUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class AddMemberGroupActivity extends AppCompatActivity {
    User userLogin;
    private GroupChat groupChat;
    private ArrayList<User> listFriend;
    private ArrayList<User> usersInRoom;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference users = dbReference.child("users");
    private DatabaseReference userContacts = dbReference.child("contacts");
    private DatabaseReference groupMembers = dbReference.child("groupMembers");
    private DatabaseReference groupMessages = dbReference.child("groupMessages");
    private DatabaseReference groupLastMessages = dbReference.child("groupLastMessages");
    RecyclerView rvFriendsAddMemberGroup;
    EditText txtMemberAddGroupName;
    SelectFriendAdapter selectFriendAdapter;
    ImageView btnBackToGroupAddMember;
    Button btnAddMemberGroup;
    ProgressBar loadingAddMemberGroup;
    ArrayList<User> chosenContact;
    ArrayList<String> idFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member_group);

        Intent intent = getIntent();
        userLogin = (User) intent.getSerializableExtra("user_login");
        groupChat = (GroupChat) intent.getSerializableExtra("group");
        usersInRoom = intent.getParcelableArrayListExtra("usersInRoom");
        loadingAddMemberGroup = findViewById(R.id.loadingAddMemberGroup);
        loadingAddMemberGroup.setVisibility(View.VISIBLE);

        initFriend();
    }

    private void setEvent() {
        btnBackToGroupAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.putExtra("chosenContact",chosenContact);
                intent.putExtra("groupChat",groupChat);
                setResult(Constant.GET_NEW_MEMBER,intent);
                finish();
            }
        });

        btnAddMemberGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenContact = new ArrayList<>();
                chosenContact = selectFriendAdapter.getListFriendSelected();
                if (chosenContact.size()==0){
                    Toast.makeText(AddMemberGroupActivity.this, getString(R.string.please_select_friend), Toast.LENGTH_SHORT).show();
                }else if (txtMemberAddGroupName.getText().toString().trim().equalsIgnoreCase("")){
                    Toast.makeText(AddMemberGroupActivity.this, getString(R.string.please_type_group_name), Toast.LENGTH_SHORT).show();
                }else if (txtMemberAddGroupName.getText().toString().trim().length()>50){
                    Toast.makeText(AddMemberGroupActivity.this, getString(R.string.group_name_too_long), Toast.LENGTH_SHORT).show();
                }else {
                    StringBuilder nameMembers = new StringBuilder();
                    String groupName = txtMemberAddGroupName.getText().toString().trim();
                    for (int i = 0; i < chosenContact.size(); i++) {
                        groupMembers.child(groupChat.getIdRoom()).child(chosenContact.get(i).getId()).setValue(chosenContact.get(i).getId());
                        if (i == chosenContact.size() - 1) {
                            nameMembers.append(chosenContact.get(i).getFullname());
                        } else {
                            nameMembers.append(chosenContact.get(i).getFullname()).append(",");
                        }
                    }
                    Message message = new Message();
                    message.setDatetime(DBUtil.getStringDateTimeChatRoom());
                    message.setUserID(userLogin.getId());
                    message.setType("group");
                    String context = userLogin.getFullname() + " " + getString(R.string.added) + " " +
                            nameMembers + " " + getString(R.string.into_group);
                    message.setContext(context);
                    GroupChat group = new GroupChat(message.getUserID(), message.getContext(), message.getDatetime(), message.getType(), groupName);
                    groupLastMessages.child(groupChat.getIdRoom()).setValue(group);
                    message.setDatetime(DBUtil.getStringDateTime());
                    groupMessages.child(groupChat.getIdRoom()).push().setValue(message);
                    Toast.makeText(AddMemberGroupActivity.this, context, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("chosenContact",chosenContact);
                    groupChat.setContext(group.getContext());
                    groupChat.setGroupName(group.getGroupName());
                    groupChat.setDatetime(group.getDatetime());
                    groupChat.setUserID(userLogin.getId());
                    intent.putExtra("groupChat",groupChat);
                    setResult(Constant.GET_NEW_MEMBER, intent);
                    finish();
                }
            }
        });

    }

    private void checkExistMember() {
        for (int i=0;i<listFriend.size();i++){
            for (int j=0;j<usersInRoom.size();j++){
                if (usersInRoom.get(j).getId().equalsIgnoreCase(listFriend.get(i).getId())||listFriend.get(i).getId().equalsIgnoreCase(userLogin.getId())){
                    listFriend.remove(listFriend.get(i));
                }
            }
        }

    }

    private void setControl() {
        rvFriendsAddMemberGroup = findViewById(R.id.rvFriendsAddMemberGroup);
        txtMemberAddGroupName = findViewById(R.id.txtMemberAddGroupName);
        btnBackToGroupAddMember = findViewById(R.id.btnBackToGroupAddMember);
        btnAddMemberGroup = findViewById(R.id.btnAddMemberGroup);
        chosenContact = new ArrayList<>();
        selectFriendAdapter = new SelectFriendAdapter(listFriend, AddMemberGroupActivity.this);
        txtMemberAddGroupName.setText(groupChat.getGroupName());
        initView();
    }

    private void initView(){
        rvFriendsAddMemberGroup.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AddMemberGroupActivity.this,LinearLayoutManager.VERTICAL,false);
        rvFriendsAddMemberGroup.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvFriendsAddMemberGroup.getContext(),
                linearLayoutManager.getOrientation());
        rvFriendsAddMemberGroup.addItemDecoration(dividerItemDecoration);
        rvFriendsAddMemberGroup.setAdapter(selectFriendAdapter);
    }

    private void initFriend() {
        String id = userLogin.getId();
        userContacts = dbReference.child("contacts/"+id);
        idFriends = new ArrayList<>();
        ValueEventListener getAllFriend = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()==0){
                    setControl();
                    setEvent();
                }
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    String userId = item.getKey();
                    idFriends.add(userId);
                    if (dataSnapshot.getChildrenCount()==idFriends.size()){
                        getListFriend();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("db_err: "+databaseError);
            }
        };
        userContacts.addListenerForSingleValueEvent(getAllFriend);
        userContacts.removeEventListener(getAllFriend);
    }

    private void getListFriend() {
        ValueEventListener getUser = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                listFriend = new ArrayList<>();
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    count++;
                    User user = item.getValue(User.class);
                    assert user != null;
                    user.setId(item.getKey());
                    for (String id : idFriends){
                        if (id.equalsIgnoreCase(user.getId())){
                            listFriend.add(user);
                        }
                    }
                    if (dataSnapshot.getChildrenCount() == count){
                        checkExistMember();
                        setControl();
                        setEvent();
                        loadingAddMemberGroup.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("db_err: "+databaseError);
            }
        };
        users.addListenerForSingleValueEvent(getUser);
        users.removeEventListener(getUser);
    }
}
