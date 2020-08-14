package com.vuvanduong.ringchat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.AddFriendActivity;
import com.vuvanduong.ringchat.activity.AddGroupActivity;
import com.vuvanduong.ringchat.adapter.GroupAdapter;
import com.vuvanduong.ringchat.adapter.SelectFriendAdapter;
import com.vuvanduong.ringchat.model.GroupChat;
import com.vuvanduong.ringchat.model.User;

import java.io.Serializable;
import java.util.ArrayList;

public class GroupFragment extends Fragment {
    private User user;
    private View view;
    private TextView txtUserGroup, txtEmailGroup;
    private ImageButton btnSearchGroup;
    private RecyclerView rvGroupConversation;
    private ProgressBar loading;
    private ImageButton btnAddGroup;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference groupMembers = dbReference.child("groupMembers");
    private DatabaseReference groupLastMessages = dbReference.child("groupLastMessages");
    private GroupAdapter groupAdapter;
    private ArrayList<GroupChat> groupChats;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_group, container, false);

        assert getArguments() != null;
        user = (User) getArguments().getSerializable("user_login");
        setControl(view);
        setEvent();
        return view;
    }

    private void setEvent() {
        btnAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addGroup = new Intent(getActivity(), AddGroupActivity.class);
                addGroup.putExtra("user_login", (Serializable) user);
                startActivity(addGroup);
            }
        });

        btnSearchGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addFriend = new Intent(getActivity(), AddFriendActivity.class);
                addFriend.putExtra("user_login", (Serializable) user);
                startActivity(addFriend);
            }
        });

        initView();

        final Query getGroups = groupMembers.orderByKey();
        getGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot item : dataSnapshot.getChildren()){
                    for (DataSnapshot members : item.getChildren()){
                        String memberid = members.getKey();
                        assert memberid != null;
                        if (memberid.equalsIgnoreCase(user.getId())){
                            String groupId=item.getKey();
                            Query getGroup = groupLastMessages.orderByKey().equalTo(groupId);
                            getGroup.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                                        GroupChat groupChat = item.getValue(GroupChat.class);
                                        assert groupChat != null;
                                        groupChat.setIdRoom(item.getKey());
                                        groupChats.add(groupChat);
                                        groupAdapter.notifyDataSetChanged();
                                        if (loading.getVisibility()==View.VISIBLE) {
                                            loading.setVisibility(View.GONE);
                                        }
                                    }
                                    groupLastMessages.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }
                groupMembers.removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setControl(View view) {
        txtUserGroup = view.findViewById(R.id.txtUserGroup);
        txtEmailGroup = view.findViewById(R.id.txtEmailGroup);
        btnSearchGroup = view.findViewById(R.id.btnSearchGroup);
        rvGroupConversation = view.findViewById(R.id.rvGroupConversation);
        btnAddGroup = view.findViewById(R.id.img_but_add_group);
        txtUserGroup.setText(user.getFullname());
        txtEmailGroup.setText(user.getEmail());
        loading = view.findViewById(R.id.loadingGroupFragment);
        loading.setVisibility(View.VISIBLE);
        groupChats = new ArrayList<>();
    }

    private void initView(){
        rvGroupConversation.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        rvGroupConversation.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvGroupConversation.getContext(),
                linearLayoutManager.getOrientation());
        rvGroupConversation.addItemDecoration(dividerItemDecoration);
        groupAdapter = new GroupAdapter(groupChats,getActivity(),user);
        rvGroupConversation.setAdapter(groupAdapter);
    }

}
