package com.vuvanduong.ringchat.model;

import java.io.Serializable;

public class Message implements Serializable {
    private String UserID;
    private String Context;
    private String Datetime;
    private String Type;

    public Message() {
    }

    public Message(String userID, String context, String datetime, String type) {
        UserID = userID;
        Context = context;
        Datetime = datetime;
        Type = type;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getContext() {
        return Context;
    }

    public void setContext(String context) {
        Context = context;
    }

    public String getDatetime() {
        return Datetime;
    }

    public void setDatetime(String datetime) {
        Datetime = datetime;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }
}
