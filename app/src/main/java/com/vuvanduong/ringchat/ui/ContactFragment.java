package com.vuvanduong.ringchat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

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
import com.vuvanduong.ringchat.adapter.ContactAdapter;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.database.ContactDB;
import com.vuvanduong.ringchat.database.UserDB;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.NetworkUtil;

import java.io.Serializable;
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
    private ProgressBar loading, reloadListContact;
    private int count = 0;
    private EditText txtSearchFriend;
    private boolean isFirstLoad = true;
    private ContactDB contactDB;
    private UserDB userDB;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contact, container, false);

        assert getArguments() != null;
        user = (User) getArguments().getSerializable("user_login");

        loading = view.findViewById(R.id.loadingContacts);
        loading.setVisibility(View.VISIBLE);
        contactDB = new ContactDB(getActivity());
        userDB = new UserDB(getActivity());

        initFriend();
        return view;
    }

    private void initFriend() {
        idFriends = new ArrayList<>();
        listFriend = new ArrayList<>();
        if (NetworkUtil.getConnectivityStatusString(getActivity())==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
            try {
                if (!idFriends.isEmpty())idFriends.clear();
                if (!listFriend.isEmpty())listFriend.clear();
                idFriends = contactDB.getAllFriendOfUser(user.getId());
                Log.e(Constant.TAG_SQLITE, "size " + idFriends.size());
                for (String idFriend : idFriends) {
                    User friend = userDB.getUserById(idFriend);
                    if (friend==null)continue;
                    listFriend.add(friend);
                    if (idFriends.size() == listFriend.size()) {
                        setControl(view);
                        setEvent();
                    }
                }
            }catch (NullPointerException e){
                //Log.e("error",e.toString());
                e.printStackTrace();
            }
            return;
        }
        String id = user.getId();
        userContacts = dbReference.child("contacts/" + id);
        contactDB.deleteAll(user.getId());
        if (!idFriends.isEmpty())idFriends.clear();
        if (!listFriend.isEmpty())listFriend.clear();
        ValueEventListener getAllFriend = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    setControl(view);
                    setEvent();
                    return;
                }
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    String userId = item.getKey();
                    idFriends.add(userId);
                    long rs = contactDB.insert(user.getId(),userId);
                    Log.e(Constant.TAG_SQLITE, "insert contact "+rs);
                    if (dataSnapshot.getChildrenCount() == idFriends.size()) {
                        getListFriend();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("db_err: " + databaseError);
            }
        };
        userContacts.addListenerForSingleValueEvent(getAllFriend);
        userContacts.removeEventListener(getAllFriend);
    }

    private void getListFriend() {
        ValueEventListener getUser = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count1 = 0;
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    count1++;
                    User user = item.getValue(User.class);
                    assert user != null;
                    user.setId(item.getKey());
                    for (String id : idFriends) {
                        if (id.equalsIgnoreCase(user.getId())) {
                            listFriend.add(user);
                            long rs = userDB.insert(user);
                            Log.e(Constant.TAG_SQLITE, "insert user "+rs);
                        }
                    }
                    if (dataSnapshot.getChildrenCount() == count1) {
                        setControl(view);
                        setEvent();
                        count = 0;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("db_err: " + databaseError);
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
                addFriend.putExtra("user_login", (Serializable) user);
                startActivity(addFriend);
            }
        });

        rvContact.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(-1) && newState==0 && NetworkUtil.getConnectivityStatusString(getActivity())!=NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) { //check for scroll down
                    if (listFriend.size() != 0 && count % listFriend.size() == 0 && reloadListContact.getVisibility() == View.GONE) {
                        listFriend.clear();
                        reloadListContact.setVisibility(View.VISIBLE);
                        initFriend();
                    }
                    if (listFriend.size() == 0 && reloadListContact.getVisibility() == View.GONE) {
                        listFriend.clear();
                        reloadListContact.setVisibility(View.VISIBLE);
                        initFriend();
                    }
                    count++;
                }
            }


            //            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (IsRecyclerViewAtTop()) {
//                    if (listFriend.size() != 0 && count % listFriend.size() == 0 && reloadListContact.getVisibility() == View.GONE) {
//                        listFriend.clear();
//                        reloadListContact.setVisibility(View.VISIBLE);
//                        initFriend();
//                    }
//                    if (listFriend.size() == 0 && reloadListContact.getVisibility() == View.GONE) {
//                        listFriend.clear();
//                        reloadListContact.setVisibility(View.VISIBLE);
//                        initFriend();
//                    }
//                    count++;
//                }
//            }
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//            }
        });

        txtSearchFriend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

    }

    private void filter(String text) {
        ArrayList<User> temp = new ArrayList<>();
        for (User d : listFriend) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            String filter = d.getEmail() + " " + d.getLastname() + " " + d.getFirstname();
            if (filter.toLowerCase().contains(text)) {
                temp.add(d);
            }
        }
        //update recyclerview
        contactAdapter.updateList(temp);
    }

    private void initView() {
        rvContact.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvContact.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvContact.getContext(),
                linearLayoutManager.getOrientation());
        rvContact.addItemDecoration(dividerItemDecoration);
        contactAdapter = new ContactAdapter(listFriend, getActivity(), false, user);
        rvContact.setAdapter(contactAdapter);
    }

    private void setControl(View view) {
        btnAddContact = view.findViewById(R.id.img_but_add_user);
        rvContact = view.findViewById(R.id.rvContact);
        reloadListContact = view.findViewById(R.id.reloadListContact);
        txtSearchFriend = view.findViewById(R.id.txtSearchFriend);
        initView();
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
        if (reloadListContact.getVisibility() == View.VISIBLE) {
            reloadListContact.setVisibility(View.GONE);
        }

    }

}
