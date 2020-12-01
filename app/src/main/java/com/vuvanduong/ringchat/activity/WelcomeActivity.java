package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.database.DatabaseHelper;
import com.vuvanduong.ringchat.database.UserDB;
import com.vuvanduong.ringchat.database.UserLoginDB;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.service.LinphoneService;
import com.vuvanduong.ringchat.service.NetworkChangeService;
import com.vuvanduong.ringchat.util.MD5;
import com.vuvanduong.ringchat.util.NetworkUtil;
import com.vuvanduong.ringchat.util.SharedPrefs;
import android.os.Handler;

import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class WelcomeActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbReference = database.getReference();
    DatabaseReference users = dbReference.child("users");
    private Handler mHandler;
    User user;
    private CoreListenerStub mCoreListener;
    UserDB userDB;
    UserLoginDB userLoginDB;
    private Map<String, String> config = new HashMap<String, String>();

    private void configCloudinary() {
        config.put("cloud_name", "vuduongtp");
        config.put("api_key", "987439358416729");
        config.put("api_secret", "Uj9Jes5zUjtAnYLXd81uR5qnGts");
        try {
            MediaManager.init(Objects.requireNonNull(WelcomeActivity.this), config);
        }catch (IllegalStateException ex){
            Log.e("exist","cloudinary");
        }
    }

    private void openDatabase() throws java.sql.SQLException {
        DatabaseHelper myDbHelper = new DatabaseHelper(WelcomeActivity.this);
        try {
            myDbHelper.createDataBase();

        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        Log.e("database","create database success");
    }

    private void setLanguage(String local, String country)  {
        Resources res = getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(local.toLowerCase(),country)); // API 17+ only.
        //Toast.makeText(this, local+"-"+country, Toast.LENGTH_SHORT).show();
        // Use conf.locale = new Locale(...) if targeting lower versions
        res.updateConfiguration(conf, dm);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        try {
            openDatabase();
        } catch (java.sql.SQLException throwables) {
            throwables.printStackTrace();
        }
        userDB = new UserDB(WelcomeActivity.this);
        userLoginDB = new UserLoginDB(WelcomeActivity.this);

        Locale current = getResources().getConfiguration().locale;
        String language = SharedPrefs.getInstance().get(Constant.LANGUAGE_CODE, String.class);
        String country = "";
        if (!language.equalsIgnoreCase(current.toString().substring(0,2)) && !language.equalsIgnoreCase("")) {
            if (language.equalsIgnoreCase("vi")) {
                country = "VN";
                setLanguage(language, country);
                Intent refresh = new Intent(this, WelcomeActivity.class);
                if (user!=null) {
                    refresh.putExtra("user_login", (Serializable) user);
                    refresh.putExtra(Constant.IS_FROM_LOGIN, true);
                }
                finish();
                startActivity(refresh);
            } else if (language.equalsIgnoreCase("en")) {
                country = "EN";
                setLanguage(language, country);
                Intent refresh = new Intent(this, WelcomeActivity.class);
                if (user!=null) {
                    refresh.putExtra("user_login", (Serializable) user);
                    refresh.putExtra(Constant.IS_FROM_LOGIN, true);
                }
                finish();
                startActivity(refresh);
            } else return;
        }

        mHandler = new Handler();

        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core core, ProxyConfig cfg, RegistrationState state, String message) {
                if (state == RegistrationState.Ok) {
                    System.out.println("OK");
                } else if (state == RegistrationState.Failed) {
                    Toast.makeText(WelcomeActivity.this, "Failure: " + message, Toast.LENGTH_LONG).show();
                    Intent login = new Intent(WelcomeActivity.this, LoginActivity.class);
                    startActivity(login);
                    finish();
                }
            }
        };

    }

    private void setEvent() {
        Intent intent = getIntent();
        boolean isFromLogin = intent.getBooleanExtra(Constant.IS_FROM_LOGIN,false);
        if (isFromLogin) {
            user = (User) intent.getSerializableExtra("user_login");
            userLoginDB.deleteAll();
            long rs = userLoginDB.login(user);
            Log.e(Constant.TAG_SQLITE,"set login "+rs);
            Intent home = new Intent(WelcomeActivity.this, HomeActivity.class);
            home.putExtra("user_login", (Serializable) user);
            startActivity(home);
            finish();
            return;
        }
        if (SharedPrefs.getInstance().get(Constant.IS_LOGIN, Boolean.class)){
            final String email = SharedPrefs.getInstance().get(Constant.EMAIL_USER_LOGIN, String.class);
            final String pass = SharedPrefs.getInstance().get(Constant.PASS_USER_LOGIN, String.class);
            if (NetworkUtil.getConnectivityStatusString(WelcomeActivity.this)==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                User userLogin = userLoginDB.getUserLoginByEmail(email);
                if (userLogin!=null) {
                    user = userLogin;

                    Intent netWorkService = new Intent(WelcomeActivity.this, NetworkChangeService.class);
                    startService(netWorkService);
                    configCloudinary();

                    SipRegister();
                    Intent home = new Intent(WelcomeActivity.this, HomeActivity.class);
                    home.putExtra("user_login", (Serializable) user);
                    startActivity(home);
                }else {
                    Intent login = new Intent(WelcomeActivity.this, LoginActivity.class);
                    startActivity(login);
                    Toast.makeText(WelcomeActivity.this, R.string.required_login, Toast.LENGTH_SHORT).show();
                }
                finish();
                return;
            }
            ValueEventListener getUser = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        User userCheck = item.getValue(User.class);
                        assert userCheck != null;
                        userCheck.setId(item.getKey());
                        long rs = userDB.insert(userCheck);
                        Log.e(Constant.TAG_SQLITE,"add user "+rs);
                        if (userCheck.getEmail().equalsIgnoreCase(email)&&userCheck.getPassword().equalsIgnoreCase(MD5.getMd5(pass))) {
                            SharedPrefs.getInstance().put(Constant.IS_LOGIN, true);
                            SharedPrefs.getInstance().put(Constant.MY_AVATAR, userCheck.getImage());
                            SharedPrefs.getInstance().put(Constant.EMAIL_USER_LOGIN, userCheck.getEmail());
                            SharedPrefs.getInstance().put(Constant.ID_USER_LOGIN, userCheck.getId());
                            SharedPrefs.getInstance().put(Constant.LASTNAME_USER_LOGIN, userCheck.getLastname());
                            SharedPrefs.getInstance().put(Constant.FIRSTNAME_USER_LOGIN, userCheck.getFirstname());
                            SharedPrefs.getInstance().put(Constant.PASS_USER_LOGIN, pass);
                            SharedPrefs.getInstance().put(Constant.BIRTHDAY_USER_LOGIN, userCheck.getBirthday());
                            user = userCheck;

                            Intent netWorkService = new Intent(WelcomeActivity.this, NetworkChangeService.class);
                            startService(netWorkService);
                            configCloudinary();

                            SipRegister();
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

    private void SipRegister() {
        LinphoneService.getCore().addListener(mCoreListener);
        // Manually update the LED registration state, in case it has been registered before
        // we add a chance to register the above listener
        ProxyConfig proxyConfig = LinphoneService.getCore().getDefaultProxyConfig();
        if (proxyConfig != null) {
            Intent home = new Intent(WelcomeActivity.this, HomeActivity.class);
            home.putExtra("user_login", (Serializable) user);
            startActivity(home);
        } else {
            if (NetworkUtil.getConnectivityStatusString(WelcomeActivity.this)!=NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                Intent login = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(login);
                Toast.makeText(WelcomeActivity.this, R.string.required_login, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        // Check whether the Service is already running
        if (LinphoneService.isReady()) {
            onServiceReady();
        } else {
            // If it's not, let's start it
            startService(
                    new Intent().setClass(this, LinphoneService.class));
            // And wait for it to be ready, so we can safely use it afterwards
            new ServiceWaitThread().start();
        }
    }

    private void onServiceReady() {
        // Once the service is ready, we can move on in the application
        // We'll forward the intent action, type and extras so it can be handled
        // by the next activity if needed, it's not the launcher job to do that
        setEvent();
    }

    private class ServiceWaitThread extends Thread {
        public void run() {
            while (!LinphoneService.isReady()) {
                try {
                    sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            // As we're in a thread, we can't do UI stuff in it, must post a runnable in UI thread
            mHandler.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            onServiceReady();
                        }
                    });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Like I said above, remove unused Core listeners in onPause
        LinphoneService.getCore().removeListener(mCoreListener);
    }

}
