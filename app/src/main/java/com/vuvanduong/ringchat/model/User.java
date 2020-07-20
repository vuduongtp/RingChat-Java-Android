package com.vuvanduong.ringchat.model;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    private String id;
    private String email;
    private  String password;
    private  String lastname;
    private  String firstname;
    private Date birthday;
    private String image;
    private  String status;

    public User(){
    }

    public User(String email, String password, String lastname, String firstname, Date birthday, String status) {
        this.email = email;
        this.password = password;
        this.lastname = lastname;
        this.firstname = firstname;
        this.birthday = birthday;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
