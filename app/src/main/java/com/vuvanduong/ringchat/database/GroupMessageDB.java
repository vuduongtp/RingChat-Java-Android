package com.vuvanduong.ringchat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.Message;

import java.util.ArrayList;

public class GroupMessageDB {
    SQLiteDatabase database;
    DatabaseHelper dbHelper;
    public final static String GROUPMESSAGE = "groupMessage";
    public final static String MESSAGEID = "messageid";
    public final static String GROUPID = "groupid";
    public final static String CONTEXT = "context";
    public final static String DATETIME = "datetime";
    public final static String TYPE = "type";
    public final static String USERID = "userid";
    public final static String URL = "url";

    public GroupMessageDB(Context context) {
        dbHelper = new DatabaseHelper(context);
        try {
            database = dbHelper.getWritableDatabase();
        } catch (SQLException ex) {
            database = dbHelper.getReadableDatabase();
        }
    }

    public long insert(Message message, String groupId, String messageId) {
        ContentValues values = new ContentValues();
        values.put(MESSAGEID, messageId);
        values.put(GROUPID, groupId);
        values.put(CONTEXT, message.getContext());
        values.put(DATETIME, message.getDatetime());
        values.put(TYPE, message.getType());
        values.put(USERID, message.getUserID());
        values.put(URL, "");
        return database.insert(GROUPMESSAGE, null, values);
    }

    public long deleteAll(String groupid) {
        return database.delete(GROUPMESSAGE,GROUPID +"='"+groupid+"'", null);
    }

    public long deleteAllMess() {
        return database.delete(GROUPMESSAGE,null, null);
    }

    public ArrayList<Message> getAllMessageOfRoom(String groupid) {
        String selectQuery = "SELECT * FROM " + GROUPMESSAGE+ " WHERE "+ GROUPID+" ='"+groupid+"' ORDER BY "+MESSAGEID+" ASC";
        Log.e("select",selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        Log.e("count",""+cursor.getCount());
        ArrayList<Message> messages = new ArrayList<>();
        if (cursor != null && cursor.getCount()!=0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                Message message = new Message();
                message.setMessageId(cursor.getString(cursor.getColumnIndex(MESSAGEID)));
                message.setIdRoom(cursor.getString(cursor.getColumnIndex(GROUPID)));
                message.setContext(cursor.getString(cursor.getColumnIndex(CONTEXT)));
                message.setDatetime(cursor.getString(cursor.getColumnIndex(DATETIME)));
                message.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                message.setUserID(cursor.getString(cursor.getColumnIndex(USERID)));

                messages.add(message);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return messages;
    }

    public ArrayList<Message> getAllMessagePending(String groupid) {
        String selectQuery = "SELECT * FROM " + GROUPMESSAGE+ " WHERE "+ TYPE+" ='Pending' ORDER BY "+MESSAGEID+" ASC";
        Log.e("select",selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        Log.e("count",""+cursor.getCount());
        ArrayList<Message> messages = new ArrayList<>();
        if (cursor != null && cursor.getCount()!=0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                Message message = new Message();
                message.setMessageId(cursor.getString(cursor.getColumnIndex(MESSAGEID)));
                message.setIdRoom(cursor.getString(cursor.getColumnIndex(GROUPID)));
                message.setContext(cursor.getString(cursor.getColumnIndex(CONTEXT)));
                message.setDatetime(cursor.getString(cursor.getColumnIndex(DATETIME)));
                message.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
                message.setUserID(cursor.getString(cursor.getColumnIndex(USERID)));

                messages.add(message);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return messages;
    }
}
