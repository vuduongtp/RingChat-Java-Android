package com.vuvanduong.ringchat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.google.firebase.database.ValueEventListener;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.AddFriendActivity;
import com.vuvanduong.ringchat.activity.RegisterActivity;
import com.vuvanduong.ringchat.adapter.ContactAdapter;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.MD5;
import com.vuvanduong.ringchat.util.SharedPrefs;

import java.util.ArrayList;

public class ContactFragment extends Fragment {
    private ImageButton btnAddContact;
    private User user;
    RecyclerView rvContact;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference users = dbReference.child("users");
    private DatabaseReference userContacts;
    private ArrayList<String> idFriends;
    private ArrayList<User> listFriend;
    private ContactAdapter contactAdapter;
    private View view;
    private ProgressBar loading;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contact, container,false);

        assert getArguments() != null;
        user = (User) getArguments().getSerializable("user_login");

        loading = view.findViewById(R.id.loadingContacts);
        loading.setVisibility(View.VISIBLE);
        initFriend();
        return view;
    }

    private void initFriend() {
        String id = user.getId();
        userContacts = dbReference.child("contacts/"+id);
        idFriends = new ArrayList<>();
        ValueEventListener getAllFriend = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount()==0){
                    setControl(view);
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
                        setControl(view);
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

    private void setEvent() {
        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addFriend = new Intent(getActivity(), AddFriendActivity.class);
                addFriend.putExtra("user_login",user);
                startActivity(addFriend);
            }
        });
    }

    private void initView(){
        rvContact.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        rvContact.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvContact.getContext(),
                linearLayoutManager.getOrientation());
        rvContact.addItemDecoration(dividerItemDecoration);
        contactAdapter = new ContactAdapter(listFriend,getActivity(),false,user);
        rvContact.setAdapter(contactAdapter);
    }

    private void setControl(View view) {
        btnAddContact = view.findViewById(R.id.img_but_add_user);
        rvContact = view.findViewById(R.id.rvContact);
        initView();
        loading.setVisibility(View.GONE);
    }
}
