package com.memrecap.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("MemRequest")
public class MemRequest extends ParseObject {

    public static final String KEY_FROM_USER = "fromUser";
    public static final String KEY_TO_USER = "toUser";
    public static final String KEY_STATUS = "status";
    public static final String KEY_INDEX = "index";

    public ParseUser getFromUser() {
        return getParseUser(KEY_FROM_USER);
    }

    public void setFromUser(ParseUser user) {
        put(KEY_FROM_USER, user);
    }

    public ParseUser getToUser() {
        return getParseUser(KEY_TO_USER);
    }

    public void setToUser(ParseUser user) {
        put(KEY_TO_USER, user);
    }

    public String getStatus() {
        return getString(KEY_STATUS);
    }

    public void setStatus(String description) {
        put(KEY_STATUS, description);
    }

    public String getIndex() {
        return getString(KEY_INDEX);
    }

    public void setIndex(String index) {
        put(KEY_INDEX, index);
    }
}
