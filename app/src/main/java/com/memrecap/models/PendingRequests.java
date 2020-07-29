package com.memrecap.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;

@ParseClassName("PendingRequests")
public class PendingRequests extends ParseObject {

    public static final String KEY_USER = "user";
    public static final String KEY_PENDING_REQUESTS = "pendingRequests";

    public ParseUser getPendingRequestsUser() {
        return getParseUser(KEY_USER);
    }

    public void setPendingRequestsUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public JSONArray getPendingRequests() {
        return getJSONArray(KEY_PENDING_REQUESTS);
    }

    public void setPendingRequests(MemRequest request) {
        put(KEY_PENDING_REQUESTS, request);
    }
}
