package com.vuvanduong.ringchat.ui;

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
import com.vuvanduong.ringchat.model.User;

public class HomeFragment extends Fragment {
    User user;
    private View view;
    TextView txtUserHome,txtEmailHome;
    ImageButton btnSearchHome;
    RecyclerView rvConversation;
    ProgressBar loading;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container,false);

        assert getArguments() != null;
        user = (User) getArguments().getSerializable("user_login");
        setControl(view);
        setEvent();
        return view;
    }

    private void setEvent() {
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
    }
}
