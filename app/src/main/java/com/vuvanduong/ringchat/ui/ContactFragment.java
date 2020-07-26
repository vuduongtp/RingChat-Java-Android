package com.vuvanduong.ringchat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.AddFriendActivity;
import com.vuvanduong.ringchat.model.User;

public class ContactFragment extends Fragment {
    private ImageButton btnAddContact;
    private User user;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container,false);

        assert getArguments() != null;
        user = (User) getArguments().getSerializable("user_login");

        setControl(view);
        setEvent();

        return view;
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

    private void setControl(View view) {
        btnAddContact = view.findViewById(R.id.img_but_add_user);
    }
}
