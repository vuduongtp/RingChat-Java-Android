package com.vuvanduong.ringchat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.DBUtil;
import com.vuvanduong.ringchat.util.UserUtil;

import java.util.ArrayList;

public class UserLoginDB {
    SQLiteDatabase database;
    DatabaseHelper dbHelper;
    public final static String USERLOGIN = "userlogin";
    public final static String USERID = "userid";
    public final static String EMAIL = "email";
    public final static String FIRSTNAME = "firstname";
    public final static String LASTNAME = "lastname";
    public final static String BIRTHDAY = "birthday";
    public final static String IMAGEURL = "imageurl";
    public final static String IMAGE = "image";
    public final static String STATUS = "status";
    public final static String ISLOGIN = "islogin";
    public final static String LOGINAT = "loginat";

    public UserLoginDB(Context context) {
        dbHelper = new DatabaseHelper(context);
        try {
            database = dbHelper.getWritableDatabase();
        } catch (SQLException ex) {
            database = dbHelper.getReadableDatabase();
        }
    }

    public long login(User user) {
        ContentValues values = new ContentValues();
        values.put(USERID, user.getId());
        values.put(FIRSTNAME, user.getFirstname());
        values.put(LASTNAME, user.getLastname());
        values.put(BIRTHDAY, user.getBirthday());
        values.put(STATUS, user.getStatus());
        values.put(EMAIL, user.getEmail());
        values.put(ISLOGIN, true);
        values.put(LOGINAT, DBUtil.getStringDateTime());
        if (user.getImage() != null && user.getImage().length() < 200) {
            values.put(IMAGEURL, user.getImage());
        } else {
            values.put(IMAGE, user.getImage());
        }
        return database.insert(USERLOGIN, null, values);
    }

    public long logout(User user) {
        ContentValues values = new ContentValues();
        values.put(USERID, user.getId());
        values.put(FIRSTNAME, user.getFirstname());
        values.put(LASTNAME, user.getLastname());
        values.put(BIRTHDAY, user.getBirthday());
        values.put(STATUS, user.getStatus());
        values.put(EMAIL, user.getEmail());
        values.put(ISLOGIN, false);
        values.put(LOGINAT, DBUtil.getStringDateTime());
        if (user.getImage() != null && user.getImage().length() < 200) {
            values.put(IMAGEURL, user.getImage());
        } else {
            values.put(IMAGE, user.getImage());
        }
        return database.insert(USERLOGIN, null, values);
    }

    public long delete(User user) {
        return database.delete(USERLOGIN,
                USERID + " = '" + user.getId()+"'", null);
    }

    public long deleteAll() {
        return database.delete(USERLOGIN,null, null);
    }

    public User getUserLoginById(String id) {
        String selectQuery = "SELECT * FROM " + USERLOGIN + " WHERE " + USERID + " = '" + id + "' AND "+ISLOGIN+"=1";
        Cursor cursor = database.rawQuery(selectQuery, null);
        User user = new User();
        if (cursor != null && cursor.getCount()!=0) {
            cursor.moveToFirst();
            user.setId(cursor.getString(cursor.getColumnIndex(USERID)));
            user.setEmail(cursor.getString(cursor.getColumnIndex(EMAIL)));
            user.setFirstname(cursor.getString(cursor.getColumnIndex(FIRSTNAME)));
            user.setLastname(cursor.getString(cursor.getColumnIndex(LASTNAME)));
            user.setBirthday(cursor.getString(cursor.getColumnIndex(BIRTHDAY)));
            if (user.getImage() != null && user.getImage().length() < 200) {
                user.setImage(cursor.getString(cursor.getColumnIndex(IMAGEURL)));
            } else {
                user.setImage(cursor.getString(cursor.getColumnIndex(IMAGE)));
            }
            user.setStatus(cursor.getString(cursor.getColumnIndex(STATUS)));
        }else {
            return null;
        }
        cursor.close();
        return user;
    }

}
