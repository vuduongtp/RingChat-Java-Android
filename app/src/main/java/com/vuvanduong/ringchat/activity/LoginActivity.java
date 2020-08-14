package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.MD5;
import com.vuvanduong.ringchat.util.SharedPrefs;

import java.io.Serializable;

public class LoginActivity extends AppCompatActivity {
    TextView forgetPass, register, txtErrorLogin;
    EditText txtUsernameLogin, txtPasswordLogin;
    Button btnLogin;
    CheckBox chkRememberPass;
    boolean isSavePass = false;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbReference = database.getReference();
    DatabaseReference users = dbReference.child("users");
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        addControl();
        addEvent();
    }

    private void addEvent() {
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(register);
            }
        });

        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent forgetPass = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                startActivity(forgetPass);
            }
        });

        chkRememberPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSavePass = isChecked;
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(LoginActivity.this, "",
                        getString(R.string.loading), true);
                if (txtUsernameLogin.getText().toString().trim().equalsIgnoreCase("")) {
                    txtErrorLogin.setText(R.string.err_emty_email);
                } else if (!RegisterActivity.checkEmailAddress(txtUsernameLogin.getText().toString().trim())) {
                    txtErrorLogin.setText(R.string.err_email_invalid);
                } else if (txtPasswordLogin.getText().toString().trim().equalsIgnoreCase("")) {
                    txtErrorLogin.setText(R.string.err_emty_pass);
                } else {
                    if (isSavePass) {
                        SharedPrefs.getInstance().put(Constant.EMAIL, txtUsernameLogin.getText().toString().trim());
                        SharedPrefs.getInstance().put(Constant.PASSWORD, txtPasswordLogin.getText().toString().trim());
                    }else {
                        SharedPrefs.getInstance().put(Constant.EMAIL, "");
                        SharedPrefs.getInstance().put(Constant.PASSWORD, "");
                    }
                    SharedPrefs.getInstance().put(Constant.IS_SAVE_PASS, isSavePass);
                    final String email=txtUsernameLogin.getText().toString().trim();
                    final String pass=txtPasswordLogin.getText().toString().trim();
                    ValueEventListener getUser = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                User user = item.getValue(User.class);
                                assert user != null;
                                user.setId(item.getKey());
                                if (user.getEmail().equalsIgnoreCase(email)&&user.getPassword().equalsIgnoreCase(MD5.getMd5(pass))) {
                                    SharedPrefs.getInstance().put(Constant.IS_LOGIN, true);
                                    SharedPrefs.getInstance().put(Constant.EMAIL_USER_LOGIN, user.getEmail());
                                    SharedPrefs.getInstance().put(Constant.MY_AVATAR, user.getImage());
                                    SharedPrefs.getInstance().put(Constant.ID_USER_LOGIN, user.getId());
                                    SharedPrefs.getInstance().put(Constant.LASTNAME_USER_LOGIN, user.getLastname());
                                    SharedPrefs.getInstance().put(Constant.FIRSTNAME_USER_LOGIN, user.getFirstname());
                                    SharedPrefs.getInstance().put(Constant.PASS_USER_LOGIN, txtPasswordLogin.getText().toString().trim());

                                    Intent welcome = new Intent(LoginActivity.this, WelcomeActivity.class);
                                    welcome.putExtra("user_login", (Serializable) user);
                                    welcome.putExtra(Constant.IS_FROM_LOGIN,true);
                                    startActivity(welcome);
                                    finish();
                                    return;
                                }
                            }
                            txtErrorLogin.setText(R.string.err_login);
                            dialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            System.err.println("db_err: "+databaseError);
                        }
                    };
                    users.addListenerForSingleValueEvent(getUser);
                    users.removeEventListener(getUser);
                }
            }
        });
    }

    private void addControl() {
        forgetPass = findViewById(R.id.btnForgetPass);
        register = findViewById(R.id.btnGoToRegister);
        txtUsernameLogin = findViewById(R.id.txtUsernameLogin);
        txtPasswordLogin = findViewById(R.id.txtPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        chkRememberPass = findViewById(R.id.chkRememberPass);
        txtErrorLogin = findViewById(R.id.txtErrorLogin);

        boolean isSavePass = SharedPrefs.getInstance().get(Constant.IS_SAVE_PASS, Boolean.class);
        if (isSavePass) {
            String email = SharedPrefs.getInstance().get(Constant.EMAIL, String.class);
            String password = SharedPrefs.getInstance().get(Constant.PASSWORD, String.class);
            txtUsernameLogin.setText(email);
            txtPasswordLogin.setText(password);
            chkRememberPass.setChecked(false);
        }
    }
}
