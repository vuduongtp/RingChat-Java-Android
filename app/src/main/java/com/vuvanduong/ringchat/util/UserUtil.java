package com.vuvanduong.ringchat.util;

public class UserUtil {
    public static boolean matchString(String s, String p) {
        String us = s.toUpperCase();
        int i = 0;
        for (char c : p.toUpperCase().toCharArray()) {
            int next = us.indexOf(c, i);
            if (next < 0) {
                return false;
            }
            i = next+1;
        }
        return true;
    }
}
