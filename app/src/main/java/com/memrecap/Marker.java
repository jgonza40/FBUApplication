package com.example.memrecap;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Marker")
public class Marker extends ParseObject {
    public static final String KEY_MARKER_USER = "user";
    public static final String KEY_MARKER_TITLE = "title";
    public static final String KEY_MARKER_LAT = "latitude";
    public static final String KEY_MARKER_LONG = "longitude";

    public String getMarkerTitle() {
        return getString(KEY_MARKER_TITLE);
    }

    public void setMarkerTitle(String markerTitle) {
        put(KEY_MARKER_TITLE, markerTitle);
    }

    public ParseUser getMarkerUser() {
        return getParseUser(KEY_MARKER_USER);
    }

    public void setMarkerUser(ParseUser user) {
        put(KEY_MARKER_USER, user);
    }

    public String getMarkerLat() {
        return getString(KEY_MARKER_LAT);
    }

    public void setMarkerLat(String markerLat) {
        put(KEY_MARKER_LAT, markerLat);
    }

    public String getMarkerLong() {
        return getString(KEY_MARKER_LONG);
    }

    public void setMarkerLong(String markerLong) {
        put(KEY_MARKER_LONG, markerLong);
    }
}
