package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.ui.AccountFragment;
import com.vuvanduong.ringchat.ui.ContactFragment;
import com.vuvanduong.ringchat.ui.GroupFragment;
import com.vuvanduong.ringchat.ui.HomeFragment;
import com.vuvanduong.ringchat.util.SharedPrefs;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements AccountFragment.OnDataPass {
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user_login");

        setControl();
    }

    private void setControl() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        HomeFragment home = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("user_login", user);
        home.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                home).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment selectedFragment = null;
            switch (menuItem.getItemId()) {
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_group:
                    selectedFragment = new GroupFragment();
                    break;
                case R.id.nav_contact:
                    selectedFragment = new ContactFragment();
                    break;
                case R.id.nav_account:
                    selectedFragment = new AccountFragment();
                    break;

            }
            assert selectedFragment != null;
            Bundle bundle = new Bundle();
            bundle.putSerializable("user_login", user);
            selectedFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
            return true;
        }
    };

    @Override
    public void onDataPass(User data) {
        if (data != null) {
            user = data;
        }
    }
}
