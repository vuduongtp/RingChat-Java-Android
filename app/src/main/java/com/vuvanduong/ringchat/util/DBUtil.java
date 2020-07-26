package com.vuvanduong.ringchat.util;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuvanduong.ringchat.model.User;

import java.util.ArrayList;

public class DBUtil {
    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static DatabaseReference dbReference = database.getReference();
    private static DatabaseReference users = dbReference.child("users");
    public static DatabaseReference contacts = dbReference.child("contacts");
    private static final ArrayList<User> allUser = new ArrayList<>();

    private void selectAllUser(){
        synchronized (this) {
            allUser.clear();
            ValueEventListener getAllUser = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        User user = item.getValue(User.class);
                        assert user != null;
                        user.setId(item.getKey());
                        allUser.add(user);
                        System.out.println("user "+user.toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            users.addListenerForSingleValueEvent(getAllUser);
            users.removeEventListener(getAllUser);
        }
    }

    public static ArrayList<User> getAllUser(){
        synchronized (allUser) {
            selectAllUser();
        }
        return allUser;
    }

}
