package com.vuvanduong.ringchat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.vuvanduong.ringchat.model.User;
import java.util.ArrayList;

public class UserDB {
    SQLiteDatabase database;
    DatabaseHelper dbHelper;
    public final static String USERS = "users";
    public final static String USERID = "userid";
    public final static String EMAIL = "email";
    public final static String FIRSTNAME = "firstname";
    public final static String LASTNAME = "lastname";
    public final static String BIRTHDAY = "birthday";
    public final static String IMAGEURL = "imageurl";
    public final static String IMAGE = "image";
    public final static String STATUS = "status";

    public UserDB(Context context) {
        dbHelper = new DatabaseHelper(context);
        try {
            database = dbHelper.getWritableDatabase();
        } catch (SQLException ex) {
            database = dbHelper.getReadableDatabase();
        }
    }

    public long insert(User user) {
        ContentValues values = new ContentValues();
        values.put(USERID, user.getId());
        values.put(FIRSTNAME, user.getFirstname());
        values.put(LASTNAME, user.getLastname());
        values.put(BIRTHDAY, user.getBirthday());
        values.put(STATUS, user.getStatus());
        values.put(EMAIL, user.getEmail());
        if (user.getImage() != null && user.getImage().length() < 200) {
            values.put(IMAGEURL, user.getImage());
        } else {
            values.put(IMAGE, user.getImage());
        }
        return database.insert(USERS, null, values);
    }

    public long delete(User user) {
        return database.delete(USERS,
                USERID + " = '" + user.getId()+"'", null);
    }

    public long deleteAll() {
        return database.delete(USERS,null, null);
    }

    public ArrayList<User> getAllUser() {
        String selectQuery = "SELECT * FROM " + USERS;
        Cursor cursor = database.rawQuery(selectQuery, null);
        ArrayList<User> users = new ArrayList<>();
        if (cursor != null && cursor.getCount()!=0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                User user = new User();
                user.setId(cursor.getString(cursor.getColumnIndex(USERID)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(EMAIL)));
                user.setFirstname(cursor.getString(cursor.getColumnIndex(FIRSTNAME)));
                user.setLastname(cursor.getString(cursor.getColumnIndex(LASTNAME)));
                user.setBirthday(cursor.getString(cursor.getColumnIndex(BIRTHDAY)));
                String image = cursor.getString(cursor.getColumnIndex(IMAGE));
                if (image!= null){
                    user.setImage(image);
                }else {
                    user.setImage(cursor.getString(cursor.getColumnIndex(IMAGEURL)));
                }
                user.setStatus(cursor.getString(cursor.getColumnIndex(STATUS)));
                users.add(user);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return users;
    }

    public User getUserById(String id) {
        String selectQuery = "SELECT * FROM " + USERS + " WHERE " + USERID + " = '" + id + "'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        User user = new User();
        if (cursor != null && cursor.getCount()!=0) {
            cursor.moveToFirst();
            user.setId(cursor.getString(cursor.getColumnIndex(USERID)));
            user.setEmail(cursor.getString(cursor.getColumnIndex(EMAIL)));
            user.setFirstname(cursor.getString(cursor.getColumnIndex(FIRSTNAME)));
            user.setLastname(cursor.getString(cursor.getColumnIndex(LASTNAME)));
            user.setBirthday(cursor.getString(cursor.getColumnIndex(BIRTHDAY)));
            user.setImage(cursor.getString(cursor.getColumnIndex(IMAGEURL)));
            user.setStatus(cursor.getString(cursor.getColumnIndex(STATUS)));
        }else {
            return null;
        }
        cursor.close();
        return user;
    }

    public long update(User user) {
        ContentValues values = new ContentValues();
        values.put(USERID, user.getId());
        values.put(FIRSTNAME, user.getFirstname());
        values.put(LASTNAME, user.getLastname());
        values.put(BIRTHDAY, user.getBirthday());
        values.put(STATUS, user.getStatus());
        values.put(EMAIL, user.getEmail());
        if (user.getImage() != null && user.getImage().length() < 200) {
            values.put(IMAGEURL, user.getImage());
            //updateImageBase64(user);
        } else {
            values.put(IMAGE, user.getImage());
        }
        return database.update(USERS, values,
                USERID + " = '" + user.getId()+"'", null);
    }
}
