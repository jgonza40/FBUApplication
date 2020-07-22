package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.memrecap.R;

public class ComposeActivity extends AppCompatActivity {

    public static final String TAG = "ComposeActivity";
    private Button btnImage;
    private Button btnQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        btnImage = findViewById(R.id.btnImage);
        btnQuote = findViewById(R.id.btnQuote);

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ComposeActivity.this, ComposeImageMemory.class);
                startActivity(i);
            }
        });

        btnQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ComposeActivity.this, ComposeQuoteMemoryActivity.class);
                startActivity(i);
            }
        });
    }
}