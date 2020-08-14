package com.vuvanduong.ringchat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.AddFriendActivity;
import com.vuvanduong.ringchat.adapter.MessageAdapter;
import com.vuvanduong.ringchat.model.Message;
import com.vuvanduong.ringchat.model.User;

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
    private ProgressBar loading;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference conversationLastMessage = dbReference.child("conversationLastMessage");
    private DatabaseReference users = dbReference.child("users");
    private ArrayList<Message> messages;
    private ArrayList<User> friends;
    private MessageAdapter messageAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        assert getArguments() != null;
        user = (User) getArguments().getSerializable("user_login");
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

    }

    private void setControl(View view) {
        txtUserHome = view.findViewById(R.id.txtUserHome);
        txtEmailHome = view.findViewById(R.id.txtEmailHome);
        btnSearchHome = view.findViewById(R.id.btnSearchHome);
        rvConversation = view.findViewById(R.id.rvConversation);
        txtUserHome.setText(user.getFullname());
        txtEmailHome.setText(user.getEmail());
        loading = view.findViewById(R.id.loadingHomeFragment);
        loading.setVisibility(View.VISIBLE);
        messages = new ArrayList<>();
        friends = new ArrayList<>();

        final Query listChatRoom = conversationLastMessage.getRef()
                .orderByKey()
                .startAt("&"+user.getId()+"&")
                .endAt("\uf8ff");
        listChatRoom.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //get data when after update meessage
                if (dataSnapshot.exists()) {// get data whem first open
                    if (!messages.isEmpty()) messages.clear();
                    if (!friends.isEmpty()) friends.clear();
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        String[] usersId = Objects.requireNonNull(item.getKey()).split("&");
                        if (usersId[0].equalsIgnoreCase(user.getId())||usersId[1].equalsIgnoreCase(user.getId())) {
                            String idfriend = "";
                            if (usersId[0].equalsIgnoreCase(user.getId())) {
                                idfriend = usersId[1];
                            } else {
                                idfriend = usersId[0];
                            }
                            Message message;
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
                                        user.setId(item.getKey());
                                        friends.add(user);
                                        if (messages.size() == friends.size()){
                                            chatBoxView(200);
                                            loading.setVisibility(View.GONE);
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
        messageAdapter = new MessageAdapter(messages, getActivity(), user,friends,false);
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
}
