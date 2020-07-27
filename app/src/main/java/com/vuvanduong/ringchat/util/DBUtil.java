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

}
