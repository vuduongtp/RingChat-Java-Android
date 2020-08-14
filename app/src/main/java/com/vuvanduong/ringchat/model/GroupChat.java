package com.vuvanduong.ringchat.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class GroupChat implements Serializable {
    private String IdRoom;
    private String UserID;
    private String Context;
    private String Datetime;
    private String Type;
    private String GroupName;

    public GroupChat(String userID, String context, String datetime, String type, String groupName) {
        UserID = userID;
        Context = context;
        Datetime = datetime;
        Type = type;
        GroupName = groupName;
    }

    public GroupChat() {
    }

    public String getIdRoom() {
        return IdRoom;
    }

    public void setIdRoom(String idRoom) {
        IdRoom = idRoom;
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

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    @NonNull
    @Override
    public String toString() {
        return "GroupChat{" +
                "IdRoom='" + IdRoom + '\'' +
                ", UserID='" + UserID + '\'' +
                ", Context='" + Context + '\'' +
                ", Datetime='" + Datetime + '\'' +
                ", Type='" + Type + '\'' +
                ", GroupName='" + GroupName + '\'' +
                '}';
    }
}
