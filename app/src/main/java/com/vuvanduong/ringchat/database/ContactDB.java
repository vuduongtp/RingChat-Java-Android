package com.vuvanduong.ringchat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class ContactDB {
    SQLiteDatabase database;
    DatabaseHelper dbHelper;
    public final static String CONTACTS = "contacts";
    public final static String USERID = "userid";
    public final static String CONTACTID = "contactid";

    public ContactDB(Context context) {
        dbHelper = new DatabaseHelper(context);
        try {
            database = dbHelper.getWritableDatabase();
        } catch (SQLException ex) {
            database = dbHelper.getReadableDatabase();
        }
    }

    public long insert(String userid, String contactid) {
        ContentValues values = new ContentValues();
        values.put(USERID, userid);
        values.put(CONTACTID, contactid);
        return database.insert(CONTACTS, null, values);
    }

    public long deleteAll(String userid) {
        return database.delete(CONTACTS,USERID +"='"+userid+"'", null);
    }

    public ArrayList<String> getAllFriendOfUser(String userid) {
        String selectQuery = "SELECT * FROM " + CONTACTS+ " WHERE "+ USERID+" = '"+userid+"'";
        Log.e("select",selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        Log.e("count",""+cursor.getCount());
        ArrayList<String> friends = new ArrayList<>();
        if (cursor != null && cursor.getCount()!=0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String friend = cursor.getString(cursor.getColumnIndex(CONTACTID));

                friends.add(friend);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return friends;
    }
}
