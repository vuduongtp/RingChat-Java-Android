
package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.adapter.ContactAdapter;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.ui.DialogLanguage;
import com.vuvanduong.ringchat.util.DBUtil;
import com.vuvanduong.ringchat.util.NetworkUtil;
import com.vuvanduong.ringchat.util.SharedPrefs;
import com.vuvanduong.ringchat.util.UserUtil;

import java.io.Serializable;
import java.util.ArrayList;

public class AddFriendActivity extends AppCompatActivity {
    private User userLogin;
    ImageView btnBackToContact;
    EditText txtSearchNewFriend;
    ImageButton btnSearchNewFriend,btnQRScan;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference users = dbReference.child("users");
    private ArrayList<User> userFind;
    private ArrayList<String> idFriends;
    RecyclerView recyclerView;
    ContactAdapter contactAdapter;
    private DatabaseReference userContacts;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        Intent intent = getIntent();
        userLogin = (User) intent.getSerializableExtra("user_login");

        setControl();
        setEvent();
    }

    private void setEvent() {
        btnBackToContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSearchNewFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.getConnectivityStatusString(AddFriendActivity.this) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                    Toast.makeText(AddFriendActivity.this, getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                    return;
                }
                loading.setVisibility(View.VISIBLE);
                final String findUser = txtSearchNewFriend.getText().toString().trim();
                userFind.clear();
                ValueEventListener getAllUser = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int count = 0;
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            User user = item.getValue(User.class);
                            count++;
                            assert user != null;
                            user.setId(item.getKey());
                            boolean isFriend=false;
                            for (String id : idFriends){
                                if (id.equalsIgnoreCase(user.getId())) {
                                    isFriend = true;
                                    break;
                                }
                            }
                            if (!user.getId().equalsIgnoreCase(userLogin.getId())&&!isFriend) {
                                if (UserUtil.matchString(user.getLastname(), findUser)) {
                                    userFind.add(user);
                                } else if (UserUtil.matchString(user.getFirstname(), findUser)) {
                                    userFind.add(user);
                                } else if (UserUtil.matchString(user.getFirstname() + " " + user.getLastname(), findUser)) {
                                    userFind.add(user);
                                } else if (UserUtil.matchString(user.getEmail(), findUser)) {
                                    userFind.add(user);
                                } else if (UserUtil.matchString(user.getId(), findUser)) {
                                    userFind.add(user);
                                }
                            }
                            if (count == dataSnapshot.getChildrenCount()){
                                initView();
                                loading.setVisibility(View.GONE);
//                                contactAdapter.notifyDataSetChanged();
                                if (userFind.isEmpty()){
                                    Toast.makeText(AddFriendActivity.this, R.string.user_not_found, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.err.println("db_err: "+databaseError);
                    }
                };
                users.addListenerForSingleValueEvent(getAllUser);
                users.removeEventListener(getAllUser);
            }
        });

        btnQRScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(AddFriendActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.e("Scan*******", "Cancelled scan");

            } else {
                if (NetworkUtil.getConnectivityStatusString(AddFriendActivity.this) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                    Toast.makeText(AddFriendActivity.this, getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e("Scan", "Scanned");
                String resultString = result.getContents();
                if (resultString.length() != 20 || resultString.equalsIgnoreCase(userLogin.getId())){
                    Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_LONG).show();
                    return;
                }
                Query getUser = users.orderByKey().equalTo(resultString);

                ValueEventListener getAllUser = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            if (item != null){
                                User userScan = item.getValue(User.class);
                                assert userScan != null;
                                userScan.setId(item.getKey());

                                boolean isScanFriend =false;
                                for (String id : idFriends){
                                    if (id.equalsIgnoreCase(userScan.getId())) {
                                        isScanFriend = true;
                                        break;
                                    }
                                }

                                Intent user_profile = new Intent(AddFriendActivity.this, UserProfileActivity.class);
                                user_profile.putExtra("user_login", (Serializable) userLogin);
                                user_profile.putExtra("user_scan", (Serializable) userScan);
                                user_profile.putExtra("isScanFriend", isScanFriend);
                                startActivity(user_profile);
                            }else {
                                Toast.makeText(AddFriendActivity.this, R.string.user_not_found, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.err.println("db_err: "+databaseError);
                    }
                };
                getUser.addListenerForSingleValueEvent(getAllUser);
                getUser.removeEventListener(getAllUser);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void initView(){
        recyclerView = findViewById(R.id.rvContactAdd);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        contactAdapter = new ContactAdapter(userFind,getApplicationContext(),true,userLogin);
        recyclerView.setAdapter(contactAdapter);

    }

    private void setControl() {
        btnBackToContact = findViewById(R.id.btnBackToContact);
        txtSearchNewFriend = findViewById(R.id.txtSearchNewFriend);
        btnSearchNewFriend = findViewById(R.id.btnSearchNewFriend);
        btnQRScan = findViewById(R.id.btnQRScan);
        userFind = new ArrayList<>();
        loading = findViewById(R.id.loadingAddContact);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        String id = SharedPrefs.getInstance().get(Constant.ID_USER_LOGIN, String.class);
        userContacts = dbReference.child("contacts/"+id);
        idFriends = new ArrayList<>();
        ValueEventListener getAllFriend = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    String userId = item.getKey();
                    idFriends.add(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.err.println("db_err: "+databaseError);
            }
        };
        userContacts.addListenerForSingleValueEvent(getAllFriend);
        userContacts.removeEventListener(getAllFriend);
    }
}
