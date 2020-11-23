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

public class ConversationLastMessageDB {
    SQLiteDatabase database;
    DatabaseHelper dbHelper;
    public final static String CONVERSATIONLASTMESSAGE = "conversationLastMessage";
    public final static String CONVERSATIONID = "conversationid";
    public final static String CONTEXT = "context";
    public final static String DATETIME = "datetime";
    public final static String TYPE = "type";
    public final static String USERID = "userid";
    public final static String URL = "url";

    public ConversationLastMessageDB(Context context) {
        dbHelper = new DatabaseHelper(context);
        try {
            database = dbHelper.getWritableDatabase();
        } catch (SQLException ex) {
            database = dbHelper.getReadableDatabase();
        }
    }

    public long insert(GroupChat groupChat) {
        ContentValues values = new ContentValues();
        values.put(CONVERSATIONID, groupChat.getIdRoom());
        values.put(CONTEXT, groupChat.getContext());
        values.put(DATETIME, groupChat.getDatetime());
        values.put(TYPE, groupChat.getType());
        values.put(USERID, groupChat.getUserID());
        values.put(URL, "");
        return database.insert(CONVERSATIONLASTMESSAGE, null, values);
    }

    public long delete(String conversationid) {
        return database.delete(CONVERSATIONLASTMESSAGE,CONVERSATIONID +"='"+conversationid+"'", null);
    }

    public Message getLastMessageOfGroup(String conversationid) {
        String selectQuery = "SELECT * FROM " + CONVERSATIONLASTMESSAGE+ " WHERE "+ CONVERSATIONID+" ='"+conversationid+"'";
        Log.e("select",selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        Log.e("count",""+cursor.getCount());
        Message message = new Message();
        if (cursor != null && cursor.getCount()!=0) {
            cursor.moveToFirst();
            message.setIdRoom(cursor.getString(cursor.getColumnIndex(CONVERSATIONID)));
            message.setContext(cursor.getString(cursor.getColumnIndex(CONTEXT)));
            message.setDatetime(cursor.getString(cursor.getColumnIndex(DATETIME)));
            message.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
            message.setUserID(cursor.getString(cursor.getColumnIndex(USERID)));
        }
        cursor.close();
        return message;
    }

    public ArrayList<Message> getAllLastMessageOfUser(String userid) {
        String selectQuery = "SELECT * FROM " + CONVERSATIONLASTMESSAGE+ " WHERE "+ CONVERSATIONID+" LIKE'%"+userid+"%'";
        Log.e("select",selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        Log.e("count",""+cursor.getCount());
        ArrayList<Message> messages = new ArrayList<>();
        if (cursor != null && cursor.getCount()!=0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                Message message = new Message();
                message.setIdRoom(cursor.getString(cursor.getColumnIndex(CONVERSATIONID)));
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
