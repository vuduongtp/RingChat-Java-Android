package com.vuvanduong.ringchat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

public class GroupMemberDB {
    SQLiteDatabase database;
    DatabaseHelper dbHelper;
    public final static String GROUPMEMBERS = "groupMembers";
    public final static String GROUPID = "groupid";
    public final static String GROUPMEMBERID = "groupmemberid";

    public GroupMemberDB(Context context) {
        dbHelper = new DatabaseHelper(context);
        try {
            database = dbHelper.getWritableDatabase();
        } catch (SQLException ex) {
            database = dbHelper.getReadableDatabase();
        }
    }

    public long insert(String groupid, String groupmemberid) {
        ContentValues values = new ContentValues();
        values.put(GROUPID, groupid);
        values.put(GROUPMEMBERID, groupmemberid);
        return database.insert(GROUPMEMBERS, null, values);
    }

    public long deleteAll(String groupid) {
        return database.delete(GROUPMEMBERS,GROUPID +"='"+groupid+"'", null);
    }

    public long deleteMember(String groupId, String memberId) {
        return database.delete(GROUPMEMBERS,GROUPMEMBERID +"='"+memberId+"' AND "+GROUPID + "='"+groupId+"'", null);
    }

    public ArrayList<String> getAllMemberOfGroup(String groupid) {
        String selectQuery = "SELECT * FROM " + GROUPMEMBERS+ " WHERE "+ GROUPID+" = '"+groupid+"'";
        Log.e("select",selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        Log.e("count",""+cursor.getCount());
        ArrayList<String> members = new ArrayList<>();
        if (cursor != null && cursor.getCount()!=0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                String friend = cursor.getString(cursor.getColumnIndex(GROUPMEMBERID));

                members.add(friend);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return members;
    }
}
