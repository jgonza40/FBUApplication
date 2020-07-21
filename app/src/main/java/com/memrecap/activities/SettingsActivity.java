package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;

import com.memrecap.R;
import com.memrecap.activities.LoginActivity;
import com.parse.ParseUser;

public class SettingsActivity extends AppCompatActivity {

    private Button btnLogout;
    private Toolbar toolbar;
    public static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        btnLogout = findViewById(R.id.btnLogout);
        ParseUser currentUser = ParseUser.getCurrentUser();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick logout button");
                logoutUser();
                goLoginActivity();
            }

        });
    }

    private void logoutUser(){
        ParseUser.logOut();
    }

    private void goLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}