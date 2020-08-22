package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.ui.AccountFragment;
import com.vuvanduong.ringchat.ui.ContactFragment;
import com.vuvanduong.ringchat.ui.GroupFragment;
import com.vuvanduong.ringchat.ui.HomeFragment;

import org.linphone.core.tools.Log;

import java.util.ArrayList;


public class HomeActivity extends AppCompatActivity implements AccountFragment.OnDataPass {
    private User user;
    private int count=0;

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
            User newUser;
            newUser = data;
            if (!newUser.getFirstname().equalsIgnoreCase("")) {
                user = data;
            }
        }
    }

    @Override
    public void onBackPressed() {
        count+=1;
        if (count==1){
            Toast.makeText(this, getString(R.string.press_back), Toast.LENGTH_SHORT).show();
        }if (count==2){
            count=0;
            finishAffinity();
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRequestCallPermissions();
    }

    private void checkAndRequestCallPermissions() {
        ArrayList<String> permissionsList = new ArrayList<>();
        // Some required permissions needs to be validated manually by the user
        // Here we ask for record audio and camera to be able to make video calls with sound
        // Once granted we don't have to ask them again, but if denied we can
        int recordAudio =
                getPackageManager()
                        .checkPermission(Manifest.permission.RECORD_AUDIO, getPackageName());
        org.linphone.core.tools.Log.i(
                "[Permission] Record audio permission is "
                        + (recordAudio == PackageManager.PERMISSION_GRANTED
                        ? "granted"
                        : "denied"));
        int camera =
                getPackageManager().checkPermission(Manifest.permission.CAMERA, getPackageName());
        org.linphone.core.tools.Log.i(
                "[Permission] Camera permission is "
                        + (camera == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

        if (recordAudio != PackageManager.PERMISSION_GRANTED) {
            org.linphone.core.tools.Log.i("[Permission] Asking for record audio");
            permissionsList.add(Manifest.permission.RECORD_AUDIO);
        }

        if (camera != PackageManager.PERMISSION_GRANTED) {
            Log.i("[Permission] Asking for camera");
            permissionsList.add(Manifest.permission.CAMERA);
        }

        if (permissionsList.size() > 0) {
            String[] permissions = new String[permissionsList.size()];
            permissions = permissionsList.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }
}
