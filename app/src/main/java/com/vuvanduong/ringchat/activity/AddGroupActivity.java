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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.adapter.ContactAdapter;
import com.vuvanduong.ringchat.adapter.SelectFriendAdapter;
import com.vuvanduong.ringchat.model.GroupChat;
import com.vuvanduong.ringchat.model.Message;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.DBUtil;
import com.vuvanduong.ringchat.util.UserUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class AddGroupActivity extends AppCompatActivity {
    private User user;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference users = dbReference.child("users");
    private DatabaseReference groupMembers = dbReference.child("groupMembers");
    private DatabaseReference groupMessages = dbReference.child("groupMessages");
    private DatabaseReference groupLastMessages = dbReference.child("groupLastMessages");
    private ArrayList<User> listFriend;
    private ArrayList<String> idFriends;
    private DatabaseReference userContacts;
    RecyclerView rvFriendsAddGroup;
    EditText txtGroupName;
    private ProgressBar loading;
    SelectFriendAdapter selectFriendAdapter;
    ImageView btnBackToContact;
    Button btnCreateAddGroup;
    ArrayList<User> chosenContact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user_login");
        loading = findViewById(R.id.loadingAddGroup);
        loading.setVisibility(View.VISIBLE);

        initFriend();
    }

    private void setEvent() {
        loading.setVisibility(View.GONE);
        btnBackToContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnCreateAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenContact = new ArrayList<>();
                chosenContact = selectFriendAdapter.getListFriendSelected();
                if (chosenContact.size()==0){
                    Toast.makeText(AddGroupActivity.this, getString(R.string.please_select_friend), Toast.LENGTH_SHORT).show();
                }else if (txtGroupName.getText().toString().trim().equalsIgnoreCase("")){
                    Toast.makeText(AddGroupActivity.this, getString(R.string.please_type_group_name), Toast.LENGTH_SHORT).show();
                }else if (txtGroupName.getText().toString().trim().length()>50){
                    Toast.makeText(AddGroupActivity.this, getString(R.string.group_name_too_long), Toast.LENGTH_SHORT).show();
                }else {
                    String newKey = groupMembers.push().getKey();
                    String groupName = txtGroupName.getText().toString().trim();
                    StringBuilder nameMembers = new StringBuilder();
                    chosenContact.add(user);
                    for (int i=0; i<chosenContact.size();i++) {
                        assert newKey != null;
                        User newUser = chosenContact.get(i);
                        groupMembers.child(newKey).child(newUser.getId()).setValue(newUser.getId());
                        if (!newUser.getId().equalsIgnoreCase(user.getId())) {
                            if (i == chosenContact.size() - 1) {
                                nameMembers.append(UserUtil.getFullName(newUser));
                            } else {
                                nameMembers.append(UserUtil.getFullName(newUser)).append(",");
                            }
                        }
                    }
                    Message message = new Message();
                    message.setDatetime(DBUtil.getStringDateTimeChatRoom());
                    message.setUserID(user.getId());
                    message.setType("group");
                    String context = UserUtil.getFullName(user)+" "+
                            getString(R.string.created_group)+" "+
                            groupName+" "+getString(R.string.and_add)+" "+
                            nameMembers+" "+getString(R.string.into_group);
                    message.setContext(context);
                    assert newKey != null;
                    GroupChat groupChat = new GroupChat(message.getUserID(),message.getContext(),message.getDatetime(),message.getType(),groupName);
                    groupLastMessages.child(newKey).setValue(groupChat);
                    message.setDatetime(DBUtil.getStringDateTime());
                    groupMessages.child(newKey).push().setValue(message);
                    Toast.makeText(AddGroupActivity.this, getString(R.string.create_group_success), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void setControl() {
        rvFriendsAddGroup = findViewById(R.id.rvFriendsAddGroup);
        txtGroupName = findViewById(R.id.txtGroupName);
        btnBackToContact = findViewById(R.id.btnBackToContact);
        btnCreateAddGroup = findViewById(R.id.btnCreateAddGroup);
        initView();
    }

    private void initView(){
        rvFriendsAddGroup.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AddGroupActivity.this,LinearLayoutManager.VERTICAL,false);
        rvFriendsAddGroup.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvFriendsAddGroup.getContext(),
                linearLayoutManager.getOrientation());
        rvFriendsAddGroup.addItemDecoration(dividerItemDecoration);
        selectFriendAdapter = new SelectFriendAdapter(listFriend,AddGroupActivity.this);
        rvFriendsAddGroup.setAdapter(selectFriendAdapter);
    }

    private void initFriend() {
        String id = user.getId();
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
                        setControl();
                        setEvent();
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
