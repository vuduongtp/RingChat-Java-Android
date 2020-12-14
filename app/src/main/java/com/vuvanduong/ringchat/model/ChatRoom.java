package com.vuvanduong.ringchat.model;

public class ChatRoom {
    private String IdRoom;
    private String UserId1;
    private String UserId2;
    private String Context;
    private String Type;
    private String Datetime;
    private String UserID;

    public ChatRoom() {
    }

    public ChatRoom(String idRoom, String userId1, String userId2, String context, String type, String datetime) {
        IdRoom = idRoom;
        UserId1 = userId1;
        UserId2 = userId2;
        Context = context;
        Type = type;
        Datetime = datetime;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getIdRoom() {
        return IdRoom;
    }

    public void setIdRoom(String idRoom) {
        IdRoom = idRoom;
    }

    public String getUserId1() {
        return UserId1;
    }

    public void setUserId1(String userId1) {
        UserId1 = userId1;
    }

    public String getUserId2() {
        return UserId2;
    }

    public void setUserId2(String userId2) {
        UserId2 = userId2;
    }

    public String getContext() {
        return Context;
    }

    public void setContext(String context) {
        Context = context;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getDatetime() {
        return Datetime;
    }

    public void setDatetime(String datetime) {
        Datetime = datetime;
    }
}
