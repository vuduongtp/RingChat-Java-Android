package com.vuvanduong.ringchat.util;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DBUtil {
    private static FirebaseDatabase database = FirebaseDatabase.getInstance();
    private static DatabaseReference dbReference = database.getReference();
    private static DatabaseReference users = dbReference.child("users");
    public static DatabaseReference contacts = dbReference.child("contacts");

    // datetime string for message
    public static String getStringDateTime() {
        String result = "";
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat formatterNew = new SimpleDateFormat("dd-MM-yyyy  HH:mm", Locale.getDefault());
        result = formatterNew.format(currentTime);
        return result;
    }

    // datetime string for chatroom
    public static String getStringDateTimeChatRoom() {
        String result = "";
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat formatterNew = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.getDefault());
        result = formatterNew.format(currentTime);
        return result;
    }

    public static String getChatRoomByTwoUserId(String userId1, String userId2) {
        String[] myArray = {userId1, userId2};
        StringBuilder result = new StringBuilder();
        int size = myArray.length;
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < myArray.length; j++) {
                if (myArray[i].compareTo(myArray[j]) > 0) {
                    String temp = myArray[i];
                    myArray[i] = myArray[j];
                    myArray[j] = temp;
                }
            }
        }
        for (int i = 0; i < size; i++) {
            if (i == 0) {
                result.append(myArray[i]);
            } else {
                result.append("&").append(myArray[i]);
            }
        }
        return result.toString();
    }

    public static String convertDatetimeMessage(String date){
        Date dateTime = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy  HH:mm", Locale.getDefault());
        SimpleDateFormat formatNew = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.getDefault());
        try {
            dateTime = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert dateTime != null;
        return formatNew.format(dateTime);
    }

    public static String convertDatetimeToString(Date date){
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return format.format(date);
    }

    public static String revertDatetimeMessage(String date){
        Date dateTime = new Date();
        SimpleDateFormat formatNew = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            dateTime = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert dateTime != null;
        return formatNew.format(dateTime);
    }

}
