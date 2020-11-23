package com.vuvanduong.ringchat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.vuvanduong.ringchat.model.GroupChat;
import com.vuvanduong.ringchat.model.Message;

import java.util.ArrayList;

public class GroupLastMessagesDB {
    SQLiteDatabase database;
    DatabaseHelper dbHelper;
    public final static String GROUPLASTMESSAGES = "groupLastMessages";
    public final static String GROUPID = "groupid";
    public final static String CONTEXT = "context";
    public final static String DATETIME = "datetime";
    public final static String TYPE = "type";
    public final static String USERID = "userid";
    public final static String GROUPNAME = "groupname";
    public final static String URL = "url";

    public GroupLastMessagesDB(Context context) {
        dbHelper = new DatabaseHelper(context);
        try {
            database = dbHelper.getWritableDatabase();
        } catch (SQLException ex) {
            database = dbHelper.getReadableDatabase();
        }
    }

    public long insert(GroupChat groupChat) {
        ContentValues values = new ContentValues();
        values.put(GROUPID, groupChat.getIdRoom());
        values.put(CONTEXT, groupChat.getContext());
        values.put(DATETIME, groupChat.getDatetime());
        values.put(TYPE, groupChat.getType());
        values.put(USERID, groupChat.getUserID());
        values.put(GROUPNAME, groupChat.getGroupName());
        values.put(URL, "");
        return database.insert(GROUPLASTMESSAGES, null, values);
    }

    public long delete(String groupid) {
        return database.delete(GROUPLASTMESSAGES,GROUPID +"='"+groupid+"'", null);
    }

    public GroupChat getLastMessageOfGroup(String groupid) {
        String selectQuery = "SELECT * FROM " + GROUPLASTMESSAGES+ " WHERE "+ GROUPID+" ='"+groupid+"'";
        Log.e("select",selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        Log.e("count",""+cursor.getCount());
        GroupChat groupChat = new GroupChat();
        if (cursor != null && cursor.getCount()!=0) {
            cursor.moveToFirst();
            groupChat.setGroupName(cursor.getString(cursor.getColumnIndex(GROUPNAME)));
            groupChat.setIdRoom(cursor.getString(cursor.getColumnIndex(GROUPID)));
            groupChat.setContext(cursor.getString(cursor.getColumnIndex(CONTEXT)));
            groupChat.setDatetime(cursor.getString(cursor.getColumnIndex(DATETIME)));
            groupChat.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
            groupChat.setUserID(cursor.getString(cursor.getColumnIndex(USERID)));
        }
        cursor.close();
        return groupChat;
    }

}
