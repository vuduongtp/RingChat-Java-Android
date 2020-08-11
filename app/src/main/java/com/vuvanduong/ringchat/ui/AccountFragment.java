package com.vuvanduong.ringchat.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.AddGroupActivity;
import com.vuvanduong.ringchat.activity.LoginActivity;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.SharedPrefs;

import java.util.Objects;

public class AccountFragment extends Fragment {
    private View view;
    private User user;
    TextView txtNameAccount,txtEmailAccount;
    LinearLayout layoutEditInfo,layoutChangePass,layoutLanguage, layoutHelp, layoutAbout, layoutLogout;
    
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
        txtNameAccount.setText(user.getFullname());
        txtEmailAccount.setText(user.getEmail());

    }
}
