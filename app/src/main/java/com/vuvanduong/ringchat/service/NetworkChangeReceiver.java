package com.vuvanduong.ringchat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vuvanduong.ringchat.activity.ConversationActivity;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.database.ConversationMessageDB;
import com.vuvanduong.ringchat.database.GroupMessageDB;
import com.vuvanduong.ringchat.model.Message;
import com.vuvanduong.ringchat.util.DBUtil;
import com.vuvanduong.ringchat.util.NetworkUtil;
import com.vuvanduong.ringchat.util.SharedPrefs;

import java.util.ArrayList;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference conversationLastMessage, conversationMessages;
    private DatabaseReference groupLastMessages, groupMessages, users;
    private ConversationMessageDB conversationMessageDB;
    private GroupMessageDB groupMessageDB;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.e("network", "Lang nghe");
        int status = NetworkUtil.getConnectivityStatusString(context);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                Log.e("network", "mat ket noi");
            } else {
                Log.e("network", "ket noi");
                try {
                    String userId = SharedPrefs.getInstance().get(Constant.ID_USER_LOGIN, String.class);
                    users = dbReference.child("users/" + userId);
                    users.child("status").setValue("Online");

                } catch (Exception ex) {
                    System.err.println(ex.toString());
                }
                try {
                    new Thread(new Runnable() {
                        public void run() {
                            conversationMessageDB = new ConversationMessageDB(context);
                            conversationLastMessage = dbReference.child("conversationLastMessage/");
                            conversationMessages = dbReference.child("conversationMessages/");
                            ArrayList<Message> pendingMessages = conversationMessageDB.getAllMessagePending();
                            conversationMessageDB.deletePending();
                            if (pendingMessages.size() == 0) return;
                            for (Message message : pendingMessages) {
                                String messageId = message.getMessageId();
                                String roomId = message.getIdRoom();
                                System.out.println(message.toString());
                                message.setIdRoom(null);
                                message.setMessageId(null);
                                message.setType("messageP");
                                conversationMessages.child(roomId).child(messageId).setValue(message);
                                message.setDatetime(DBUtil.convertDatetimeMessage(message.getDatetime()));
                                conversationLastMessage.child(roomId).setValue(message);
                                Log.e("network", "add message");
                            }
                        }
                    }).start();

                    new Thread(new Runnable() {
                        public void run() {
                            groupMessageDB = new GroupMessageDB(context);
                            groupMessages = dbReference.child("groupMessages/");
                            groupLastMessages = dbReference.child("groupLastMessages/");
                            ArrayList<Message> pendingMessages = groupMessageDB.getAllMessagePending();
                            groupMessageDB.deletePending();
                            if (pendingMessages.size() == 0) return;
                            for (Message message : pendingMessages) {
                                String messageId = message.getMessageId();
                                String roomId = message.getIdRoom();
                                System.out.println(message.toString());
                                message.setIdRoom(null);
                                message.setMessageId(null);
                                message.setType("messageP");
                                groupMessages.child(roomId).child(messageId).setValue(message);
                                message.setDatetime(DBUtil.convertDatetimeMessage(message.getDatetime()));
                                groupLastMessages.child(roomId).child("context").setValue(message.getContext());
                                groupLastMessages.child(roomId).child("datetime").setValue(message.getDatetime());
                                groupLastMessages.child(roomId).child("type").setValue(message.getType());
                                groupLastMessages.child(roomId).child("userID").setValue(message.getUserID());
                                Log.e("network", "add message");
                            }
                        }
                    }).start();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}