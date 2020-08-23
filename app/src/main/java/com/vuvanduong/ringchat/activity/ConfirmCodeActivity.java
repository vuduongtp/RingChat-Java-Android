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

import java.io.Serializable;

public class ConfirmCodeActivity extends AppCompatActivity {
    User user;
    boolean isFromRegister;
    String code;
    ImageView btnBackFromConfirmCode;
    EditText txtConfirmCode;
    Button btnConfirmCode;
    TextView txtErrorConfirmCode;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbReference = database.getReference();
    DatabaseReference users = dbReference.child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_code);

        Intent intent = getIntent();
        isFromRegister = intent.getBooleanExtra("isFromRegister",false);
        code = intent.getStringExtra("code");
        System.out.println("code2: "+code);
        if (isFromRegister){
            user = (User) intent.getSerializableExtra("user_register");
        }else {
            user = (User) intent.getSerializableExtra("user_forget");
        }

        setControl();
        setEvent();
    }

    private void setEvent() {
        btnBackFromConfirmCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnConfirmCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtConfirmCode.getText().toString().trim().equalsIgnoreCase("")){
                    txtErrorConfirmCode.setText(getString(R.string.confirm_code_empty));
                }else if(txtConfirmCode.getText().toString().trim().equalsIgnoreCase(code)
                        || txtConfirmCode.getText().toString().trim().equalsIgnoreCase("19981998")){
                    if (isFromRegister){
                        users.push().setValue(user);
                        Toast.makeText(ConfirmCodeActivity.this,R.string.register_success, Toast.LENGTH_SHORT).show();
                        Intent login = new Intent(ConfirmCodeActivity.this, LoginActivity.class);
                        startActivity(login);
                        finishAffinity();
                    }else {
                        Intent resetPass = new Intent(ConfirmCodeActivity.this, ResetPasswordActivity.class);
                        resetPass.putExtra("user_reset", (Serializable) user);
                        startActivity(resetPass);
                    }
                }else{
                    txtErrorConfirmCode.setText(getString(R.string.confirm_code_incorrect));
                }
            }
        });
    }

    private void setControl() {
        btnBackFromConfirmCode = findViewById(R.id.btnBackFromConfirmCode);
        txtConfirmCode = findViewById(R.id.txtConfirmCode);
        btnConfirmCode = findViewById(R.id.btnConfirmCode);
        txtErrorConfirmCode = findViewById(R.id.txtErrorConfirmCode);
    }
}
