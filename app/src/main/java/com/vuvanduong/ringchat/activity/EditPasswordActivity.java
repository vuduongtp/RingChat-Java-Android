package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.vuvanduong.ringchat.util.DBUtil;
import com.vuvanduong.ringchat.util.MD5;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class EditPasswordActivity extends AppCompatActivity {
    private User user;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbReference = database.getReference();
    DatabaseReference users = dbReference.child("users");
    ImageView imageAvatarChangePass,btnBackToChangePass;
    EditText txtPasswordRecentEditPass,txtPasswordNewEditPass,txtConfirmPasswordNewEditPass;
    TextView txtNameChangePass,txtErrorChangePass;
    Button btnEditPass;
    String id="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user_login");
        setControl();
        setEvent();
    }

    private void setEvent() {
        btnBackToChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnEditPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtPasswordRecentEditPass.getText().toString().trim().equalsIgnoreCase("")){
                    txtErrorChangePass.setText(R.string.please_type_recent_pass);
                }
                else if(txtPasswordNewEditPass.getText().toString().trim().equalsIgnoreCase("")){
                    txtErrorChangePass.setText(R.string.please_type_new_pass);
                }
                else if(txtConfirmPasswordNewEditPass.getText().toString().trim().equalsIgnoreCase("")){
                    txtErrorChangePass.setText(R.string.please_type_confirm_new_pass);
                }
                else if(!MD5.getMd5(txtPasswordRecentEditPass.getText().toString().trim()).equalsIgnoreCase(user.getPassword()) ){
                    txtErrorChangePass.setText(R.string.incorrect_recent_pass);
                }
                else if(!txtConfirmPasswordNewEditPass.getText().toString().trim().equalsIgnoreCase(txtConfirmPasswordNewEditPass.getText().toString().trim())){
                    txtErrorChangePass.setText(R.string.new_pass_not_match);
                }
                else {
                    user.setPassword(MD5.getMd5(txtPasswordNewEditPass.getText().toString().trim()));
                    user.setId(null);
                    users.child(id).setValue(user);
                    Toast.makeText(EditPasswordActivity.this,R.string.change_pass_success, Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent();
                    intent.putExtra("userPass",  user.getPassword());
                    setResult(Constant.GET_NEW_USER_PASS,intent);
                    finish();
                 }
            }
        });

    }

    private void setControl() {
        imageAvatarChangePass = findViewById(R.id.imageAvatarChangePass);
        txtPasswordRecentEditPass = findViewById(R.id.txtPasswordRecentEditPass);
        txtPasswordNewEditPass = findViewById(R.id.txtPasswordNewEditPass);
        txtConfirmPasswordNewEditPass = findViewById(R.id.txtConfirmPasswordNewEditPass);
        txtNameChangePass = findViewById(R.id.txtNameChangePass);
        txtErrorChangePass = findViewById(R.id.txtErrorChangePass);
        btnBackToChangePass = findViewById(R.id.btnBackToChangePass);
        btnEditPass = findViewById(R.id.btnEditPass);
        id=user.getId();
    }


}
