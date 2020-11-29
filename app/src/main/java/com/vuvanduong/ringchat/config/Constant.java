package com.vuvanduong.ringchat.config;

import android.content.Context;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constant {
    public static final String SIP_SERVER = "103.116.105.136";
    private static Locale local= Locale.getDefault();
    public static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", local);
    public static final SimpleDateFormat dateMessage = new SimpleDateFormat("dd/MM/yyyy HH:mm", local);
    public static final SimpleDateFormat dateChatRoom = new SimpleDateFormat("yyyy//MM/dd HH:mm", local);
    public static final String EMAIL = "email";
    public static final String MY_AVATAR = "my_avatar";
    public static final String PASSWORD = "password";
    public static final String IS_SAVE_PASS = "is_save_pass";
    public static final String IS_LOGIN = "is_login";
    public static final String ID_USER_LOGIN = "id_user_login";
    public static final String EMAIL_USER_LOGIN = "email_user_login";
    public static final String LASTNAME_USER_LOGIN = "lastname_user_login";
    public static final String FIRSTNAME_USER_LOGIN = "firstname_user_login";
    public static final String IS_FROM_LOGIN = "is_from_login";
    public static final String PASS_USER_LOGIN = "pass_user_login";
    public static final String BIRTHDAY_USER_LOGIN = "birthday_user_login";
    public static final int GET_MEMBER_REMAIN = 1000;
    public static final int GET_NEW_MEMBER = 1001;
    public static final int GET_NEW_USER_INFO = 1002;
    public static final int GET_NEW_USER_PASS = 1003;
    public static final int LISTEN_REMOVE_MEMBER = 1004;
    public static final String LANGUAGE_CODE = "language";
    public static final String EMAIL_SENDER = "mailsenderptithcm@gmail.com";
    public static final String PASS_EMAIL_SENDER = "ptithcm123";
    public static final int IMAGE_SIZE_DISPLAY = 500;
    public static final int GET_GET_AVATAR = 1006;
    public static final String TAG_SQLITE = "SQLITE";

}
