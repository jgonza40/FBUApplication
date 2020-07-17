package com.example.fbuapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etBirthday;
    private Button btnSignUp;
    public static final String TAG = "SignUpActivity";
    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";
    public static final String KEY_BIRTHDAY = "birthday";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        // Makes sure that current user remains during app restarts
        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }
        // Declaring all of the components
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etBirthday = findViewById(R.id.etBirthday);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String birthday = etBirthday.getText().toString();
                String firstName = etFirstName.getText().toString();
                String lastName = etLastName.getText().toString();
                signUpUser(username, password, birthday, firstName, lastName);
            }
        });
    }

    private void signUpUser(String newUsername, String newPassword, String newFirstName,
                            String newLastName, String newBirthday) {
        // Create the ParseUser
        ParseUser user = new ParseUser();
        // Set core properties
        user.setUsername(newUsername);
        user.setPassword(newPassword);
        user.put(KEY_FIRST_NAME, newFirstName);
        user.put(KEY_LAST_NAME, newLastName);
        user.put(KEY_BIRTHDAY, newBirthday);
        // Invoke signUpInBackground
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with sign-up", e);
                    Toast.makeText(SignUpActivity.this, "Issue with password", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    goMainActivity();
                }
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void errorCheckBirthday(String birthdayInput) {
        //DEAL W ERROR CHECKING HERE
    }
}