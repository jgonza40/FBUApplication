package com.memrecap.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONObject;

@ParseClassName("Friends")
public class Friends extends ParseObject {

    public static final String KEY_USER = "user";
    public static final String KEY_FRIENDS = "friends";
    public static final String KEY_FRIENDS_MAP = "friendsMap";

    public ParseUser getFriendsUser() {
        return getParseUser(KEY_USER);
    }

    public void setFriendsUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public JSONArray getFriends() {
        return getJSONArray(KEY_FRIENDS);
    }

    public void setFriends(ParseUser friend) {
        put(KEY_FRIENDS, friend);
    }

    public JSONObject getFriendsMap() {
        return getJSONObject(KEY_FRIENDS_MAP);
    }

    public void setFriendsMap(ParseUser friend) {
        put(KEY_FRIENDS_MAP, friend);
    }
}
