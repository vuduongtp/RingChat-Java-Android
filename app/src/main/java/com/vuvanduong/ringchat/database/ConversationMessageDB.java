package com.vuvanduong.ringchat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.vuvanduong.ringchat.model.Message;

import java.util.ArrayList;

public class ConversationMessageDB {
    SQLiteDatabase database;
    DatabaseHelper dbHelper;
    public final static String CONVERSATIONMESSAGES = "conversationMessages";
    public final static String MESSAGEID = "messageid";
    public final static String CONVERSATIONID = "conversationid";
    public final static String CONTEXT = "context";
    public final static String DATETIME = "datetime";
    public final static String TYPE = "type";
    public final static String USERID = "userid";
    public final static String URL = "url";

    public ConversationMessageDB(Context context) {
        dbHelper = new DatabaseHelper(context);
        try {
            database = dbHelper.getWritableDatabase();
        } catch (SQLException ex) {
            database = dbHelper.getReadableDatabase();
        }
    }

    public long insert(Message message) {
        ContentValues values = new ContentValues();
        values.put(MESSAGEID, message.getMessageId());
        values.put(CONVERSATIONID, message.getIdRoom());
        values.put(CONTEXT, message.getContext());
        values.put(DATETIME, message.getDatetime());
        values.put(TYPE, message.getType());
        values.put(USERID, message.getUserID());
        values.put(URL, "");
        return database.insert(CONVERSATIONMESSAGES, null, values);
    }

    public long deleteAll() {
        return database.delete(CONVERSATIONMESSAGES,null, null);
    }

    public ArrayList<Message> getAllMessageOfRoom(String room) {
        String selectQuery = "SELECT * FROM " + CONVERSATIONMESSAGES+ " WHERE "+ CONVERSATIONID+" LIKE '%"+room+"%' ORDER BY "+CONVERSATIONID+" ASC LIMIT 10";
        Log.e("select",selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        Log.e("count",""+cursor.getCount());
        ArrayList<Message> messages = new ArrayList<>();
        if (cursor != null && cursor.getCount()!=0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                Message message = new Message();
                message.setMessageId(cursor.getString(cursor.getColumnIndex(MESSAGEID)));
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
