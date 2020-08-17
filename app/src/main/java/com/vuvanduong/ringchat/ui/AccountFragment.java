package com.vuvanduong.ringchat.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.AddFriendActivity;
import com.vuvanduong.ringchat.activity.AddGroupActivity;
import com.vuvanduong.ringchat.activity.EditInforActivity;
import com.vuvanduong.ringchat.activity.EditPasswordActivity;
import com.vuvanduong.ringchat.activity.LoginActivity;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.GroupChat;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.SharedPrefs;
import com.vuvanduong.ringchat.util.UserUtil;

import java.io.Serializable;
import java.util.Objects;

public class AccountFragment extends Fragment {
    private View view;
    private User user;
    TextView txtNameAccount,txtEmailAccount;
    LinearLayout layoutEditInfo,layoutChangePass,layoutLanguage, layoutHelp, layoutAbout, layoutLogout;
    ImageView btnSearchInAccount;

    private OnDataPass dataPasser;
    public interface OnDataPass {
        public void onDataPass(User data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }

    public void passData(User data) {
        dataPasser.onDataPass(data);
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account, container, false);

        assert getArguments() != null;
        user = (User) getArguments().getSerializable("user_login");
        setControl(view);
        setEvent();
        return view;
    }

    private void setEvent() {
        layoutLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                SharedPrefs.getInstance().put(Constant.IS_LOGIN, false);
                                SharedPrefs.getInstance().put(Constant.IS_SAVE_PASS, true);
                                Intent login = new Intent(getActivity(), LoginActivity.class);
                                startActivity(login);
                                Objects.requireNonNull(getActivity()).finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(Objects.requireNonNull(getActivity()).getString(R.string.confirm_logout))
                        .setPositiveButton(Objects.requireNonNull(getActivity()).getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(Objects.requireNonNull(getActivity()).getString(R.string.no), dialogClickListener).show();
            }
        });

        layoutEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editInfor = new Intent(getActivity(), EditInforActivity.class);
                editInfor.putExtra("user_login", (Serializable) user);
                startActivityForResult(editInfor, Constant.GET_NEW_USER_INFO);
            }
        });

        layoutChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editPass = new Intent(getActivity(), EditPasswordActivity.class);
                editPass.putExtra("user_login", (Serializable) user);
                startActivityForResult(editPass, Constant.GET_NEW_USER_PASS);
            }
        });

        btnSearchInAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent search = new Intent(getActivity(), AddFriendActivity.class);
                search.putExtra("user_login", (Serializable) user);
                startActivity(search);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.GET_NEW_USER_INFO && data != null) {
            User newUser = (User) data.getSerializableExtra("userEdit");
            if(newUser != null){
                user = newUser;
                setControl(view);
                passData(user);
            }
        }
        if (requestCode == Constant.GET_NEW_USER_PASS && data != null) {
            String password = data.getStringExtra("userPass");
            if(password != null){
                user.setPassword(password);
                passData(user);
            }
        }

    }

    private void setControl(View view) {

        txtNameAccount = view.findViewById(R.id.txtNameAccount);
        txtEmailAccount = view.findViewById(R.id.txtEmailAccount);
        layoutEditInfo = view.findViewById(R.id.layoutEditInfo);
        layoutChangePass = view.findViewById(R.id.layoutChangePass);
        layoutLanguage  = view.findViewById(R.id.layoutLanguage);
        layoutHelp = view.findViewById(R.id.layoutHelp);
        layoutAbout = view.findViewById(R.id.layoutAbout);
        layoutLogout = view.findViewById(R.id.layoutLogout);
        btnSearchInAccount = view.findViewById(R.id.btnSearchInAccount);
        txtNameAccount.setText(UserUtil.getFullName(user));
        txtEmailAccount.setText(user.getEmail());

    }
}
