package com.vuvanduong.ringchat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.MD5;
import com.vuvanduong.ringchat.util.NetworkUtil;

import java.io.Serializable;

public class ResetPasswordActivity extends AppCompatActivity {
    User user;
    ImageView btnBackToResetPass;
    TextView txtErrorResetPass;
    EditText txtPasswordNewResetPass, txtConfirmPasswordNewResetPass;
    Button btnResetPass;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbReference = database.getReference();
    DatabaseReference users = dbReference.child("users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user_reset");

        setControl();
        setEvent();
    }

    private void setEvent() {
        btnBackToResetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnResetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtPasswordNewResetPass.getText().toString().trim().equalsIgnoreCase("")){
                    txtErrorResetPass.setText(getString(R.string.please_type_new_pass));
                }else if (txtConfirmPasswordNewResetPass.getText().toString().trim().equalsIgnoreCase("")){
                    txtErrorResetPass.setText(getString(R.string.please_type_confirm_new_pass));
                }else if (!txtPasswordNewResetPass.getText().toString().trim().equalsIgnoreCase(txtConfirmPasswordNewResetPass.getText().toString().trim())){
                    txtErrorResetPass.setText(getString(R.string.new_pass_not_match));
                }else {
                    if (NetworkUtil.getConnectivityStatusString(ResetPasswordActivity.this) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                        Toast.makeText(ResetPasswordActivity.this, getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String id = user.getId();
                    user.setId(null);
                    user.setPassword(MD5.getMd5(txtPasswordNewResetPass.getText().toString().trim()));
                    users.child(id).setValue(user);
                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.change_pass_success), Toast.LENGTH_SHORT).show();
                    Intent login = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    startActivity(login);
                    finishAffinity();
                }
            }
        });
    }

    private void setControl() {
        btnBackToResetPass = findViewById(R.id.btnBackToResetPass);
        txtErrorResetPass = findViewById(R.id.txtErrorResetPass);
        txtPasswordNewResetPass = findViewById(R.id.txtPasswordNewResetPass);
        txtConfirmPasswordNewResetPass = findViewById(R.id.txtConfirmPasswordNewResetPass);
        btnResetPass = findViewById(R.id.btnResetPass);
    }
}
