package com.memrecap;

import android.app.Application;

import com.memrecap.models.MarkerPoint;
import com.memrecap.models.Memory;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Register your parse models
        ParseObject.registerSubclass(Memory.class);
        ParseObject.registerSubclass(MarkerPoint.class);

        // Referring to Heroku App Setting Strings
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("mem-recap")
                .clientKey("myMemRecapSecretString111122")
                .server("https://mem-recap.herokuapp.com/parse/").build());
    }
}
