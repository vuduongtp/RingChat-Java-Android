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
import androidx.recyclerview.widget.RecyclerView;

import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.AddFriendActivity;
import com.vuvanduong.ringchat.activity.AddGroupActivity;
import com.vuvanduong.ringchat.model.User;

public class GroupFragment extends Fragment {
    private User user;
    private View view;
    private TextView txtUserGroup, txtEmailGroup;
    private ImageButton btnSearchGroup;
    private RecyclerView rvGroupConversation;
    private ProgressBar loading;
    private ImageButton btnAddGroup;

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
                addGroup.putExtra("user_login", user);
                startActivity(addGroup);
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
    }
}
