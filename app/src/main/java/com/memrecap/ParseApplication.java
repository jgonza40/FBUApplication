package com.memrecap;

import android.app.Application;

import com.memrecap.models.Memory;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Register your parse models
        ParseObject.registerSubclass(Memory.class);

        // set applicationId, and server server based on the values in the Heroku settings.
        // clientKey is not needed unless explicitly configured
        // any network interceptors must be added with the Configuration Builder given this syntax
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("mem-recap") // should correspond to APP_ID env variable
                .clientKey("myMemRecapSecretString111122")  // set explicitly unless clientKey is explicitly configured on Parse server
                .server("https://mem-recap.herokuapp.com/parse/").build());
    }
}
