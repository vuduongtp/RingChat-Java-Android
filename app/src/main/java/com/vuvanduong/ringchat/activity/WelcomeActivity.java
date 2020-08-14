package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

public class WelcomeActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbReference = database.getReference();
    DatabaseReference users = dbReference.child("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Intent intent = getIntent();
        User user;
        boolean isFromLogin = intent.getBooleanExtra(Constant.IS_FROM_LOGIN,false);
        if (isFromLogin) {
            user = (User) intent.getSerializableExtra("user_login");
            Intent home = new Intent(WelcomeActivity.this, HomeActivity.class);
            home.putExtra("user_login", (Serializable) user);
            startActivity(home);
            finish();
            return;
        }
        if (SharedPrefs.getInstance().get(Constant.IS_LOGIN, Boolean.class)){
            final String email = SharedPrefs.getInstance().get(Constant.EMAIL_USER_LOGIN, String.class);
            final String pass = SharedPrefs.getInstance().get(Constant.PASS_USER_LOGIN, String.class);
            ValueEventListener getUser = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        User user = item.getValue(User.class);
                        assert user != null;
                        user.setId(item.getKey());
                        if (user.getEmail().equalsIgnoreCase(email)&&user.getPassword().equalsIgnoreCase(MD5.getMd5(pass))) {
                            SharedPrefs.getInstance().put(Constant.IS_LOGIN, true);
                            SharedPrefs.getInstance().put(Constant.MY_AVATAR, user.getImage());
                            SharedPrefs.getInstance().put(Constant.EMAIL_USER_LOGIN, user.getEmail());
                            SharedPrefs.getInstance().put(Constant.ID_USER_LOGIN, user.getId());
                            SharedPrefs.getInstance().put(Constant.LASTNAME_USER_LOGIN, user.getLastname());
                            SharedPrefs.getInstance().put(Constant.FIRSTNAME_USER_LOGIN, user.getFirstname());
                            SharedPrefs.getInstance().put(Constant.PASS_USER_LOGIN, pass);

                            Intent home = new Intent(WelcomeActivity.this, HomeActivity.class);
                            home.putExtra("user_login", (Serializable) user);
                            startActivity(home);
                            finish();
                            return;
                        }
                    }
                    Intent login = new Intent(WelcomeActivity.this, LoginActivity.class);
                    startActivity(login);
                    Toast.makeText(WelcomeActivity.this, R.string.required_login, Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    System.err.println("db_err: "+databaseError);
                }
            };
            users.addListenerForSingleValueEvent(getUser);
            users.removeEventListener(getUser);
        }else {
            Intent login = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(login);
            finish();
        }
    }
}
