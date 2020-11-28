package com.vuvanduong.ringchat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.AddFriendActivity;
import com.vuvanduong.ringchat.activity.UserProfileActivity;
import com.vuvanduong.ringchat.adapter.MessageAdapter;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.Message;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.CircleTransform;
import com.vuvanduong.ringchat.util.UserUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private User user;
    private View view;
    private TextView txtUserHome, txtEmailHome;
    private ImageButton btnSearchHome;
    private RecyclerView rvConversation;
    private ProgressBar loading, reloadListMessage;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference conversationLastMessage = dbReference.child("conversationLastMessage");
    private DatabaseReference users = dbReference.child("users");
    private ArrayList<Message> messages;
    private ArrayList<User> friends;
    private MessageAdapter messageAdapter;
    private int count = 0;
    private boolean isFirstLoad = true;
    private ImageView img_avt_friend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        assert getArguments() != null;
        user = (User) getArguments().getSerializable("user_login");
        loading = view.findViewById(R.id.loadingHomeFragment);
        loading.setVisibility(View.VISIBLE);
        setControl(view);
        setEvent();
        return view;
    }

    private void setEvent() {
        btnSearchHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addFriend = new Intent(getActivity(), AddFriendActivity.class);
                addFriend.putExtra("user_login", (Serializable) user);
                startActivity(addFriend);
            }
        });

        img_avt_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent user_profile = new Intent(getActivity(), UserProfileActivity.class);
                user_profile.putExtra("user_login", (Serializable) user);
                user_profile.putExtra("user_scan", (Serializable) user);
                user_profile.putExtra("isScanFriend", false);
                user_profile.putExtra("isUserLogin", true);
                startActivityForResult(user_profile, Constant.GET_GET_AVATAR);
            }
        });

        rvConversation.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(-1) && newState==0) { //check for scroll down
                    if (messages.size() != 0 && count % messages.size() == 0 && reloadListMessage.getVisibility() == View.GONE) {
                        messages.clear();
                        reloadListMessage.setVisibility(View.VISIBLE);
                        getData();
                    }
                    if (messages.size() == 0 && reloadListMessage.getVisibility() == View.GONE) {
                        messages.clear();
                        reloadListMessage.setVisibility(View.VISIBLE);
                        getData();
                    }
                    count++;
                }
            }
        });
    }

    private void setControl(View view) {
        txtUserHome = view.findViewById(R.id.txtUserHome);
        txtEmailHome = view.findViewById(R.id.txtEmailHome);
        btnSearchHome = view.findViewById(R.id.btnSearchHome);
        rvConversation = view.findViewById(R.id.rvConversation);
        img_avt_friend = view.findViewById(R.id.img_avt_friend);
        txtUserHome.setText(UserUtil.getFullName(user));
        txtEmailHome.setText(user.getEmail());
        reloadListMessage = view.findViewById(R.id.reloadListMessage);
        messages = new ArrayList<>();
        friends = new ArrayList<>();

        Picasso.with(getActivity())
                .load(user.getImage())
                .placeholder(R.drawable.user)
                .transform(new CircleTransform())
                .into(img_avt_friend);

        getData();
    }

    private void getData() {
        final Query listChatRoom = conversationLastMessage.getRef()
                .orderByKey();
//                .startAt("&" + user.getId() + "&")
//                .endAt("\uf8ff");
        listChatRoom.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (loading.getVisibility() == View.VISIBLE) {
                    loading.setVisibility(View.GONE);
                }
                //get data when after update meessage
                if (dataSnapshot.exists()) {// get data whem first open
                    if (!messages.isEmpty()) messages.clear();
                    if (!friends.isEmpty()) friends.clear();
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        String[] usersId = Objects.requireNonNull(item.getKey()).split("&");
                        if (usersId[0].equalsIgnoreCase(user.getId()) || usersId[1].equalsIgnoreCase(user.getId())) {
                            String idfriend = "";
                            if (usersId[0].equalsIgnoreCase(user.getId())) {
                                idfriend = usersId[1];
                            } else {
                                idfriend = usersId[0];
                            }
                            final Message message;
                            message = item.getValue(Message.class);
                            assert message != null;
                            message.setIdRoom(item.getKey());
                            messages.add(message);
                            Query getFriend = users.orderByKey()
                                    .equalTo(idfriend);
                            getFriend.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                                        User user;
                                        user = item.getValue(User.class);
                                        assert user != null;
                                        user.setId(item.getKey());
                                        friends.add(user);
                                        if (messages.size() == friends.size()) {
                                            chatBoxView(200);
                                            if (loading.getVisibility() == View.VISIBLE) {
                                                loading.setVisibility(View.GONE);
                                            }
                                            if (reloadListMessage.getVisibility() == View.VISIBLE) {
                                                reloadListMessage.setVisibility(View.GONE);
                                            }
                                            count = 0;
                                        }
                                    }
                                    users.removeEventListener(this);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                    listChatRoom.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error loading database", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void chatBoxView(int delayTime) {
        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return o2.getDatetime().compareTo(o1.getDatetime());
            }
        });
        rvConversation.setHasFixedSize(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvConversation.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvConversation.getContext(),
                layoutManager.getOrientation());
        rvConversation.addItemDecoration(dividerItemDecoration);
        messageAdapter = new MessageAdapter(messages, getActivity(), user, friends, false);
        rvConversation.setAdapter(messageAdapter);

        rvConversation.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Objects.requireNonNull(rvConversation.getAdapter()).getItemCount() > 0) {
                    rvConversation.smoothScrollToPosition(0);
                }
            }
        }, delayTime);

        rvConversation.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    rvConversation.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (Objects.requireNonNull(rvConversation.getAdapter()).getItemCount() > 0) {
                                rvConversation.smoothScrollToPosition(0);
                            }
                        }
                    }, 200);
                }
            }
        });
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == Constant.GET_GET_AVATAR && data != null) {
//            String newUser = data.getStringExtra("avatar");
//            if (newUser != null) {
//                Picasso.with(getActivity())
//                        .load(user.getImage())
//                        .placeholder(R.drawable.user)
//                        .transform(new CircleTransform())
//                        .into(img_avt_friend);
//            }
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isFirstLoad){
            messages.clear();
            getData();
        }
        isFirstLoad=false;
    }
}
