package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.memrecap.R;

public class ComposeActivity extends AppCompatActivity {

    public static final String TAG = "ComposeActivity";

    public static final String PASS_LAT = "markerClickedLat";
    public static final String PASS_LONG = "markerClickedLong";

    private Button btnImage;
    private Button btnQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        btnImage = findViewById(R.id.btnImage);
        btnQuote = findViewById(R.id.btnQuote);

        // Gets the previously created intent to get 2 marker values
        Intent myIntent = getIntent();
        final String markerLat = myIntent.getStringExtra(PASS_LAT);
        final String markerLong= myIntent.getStringExtra(PASS_LONG);

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ComposeActivity.this, ComposeImageMemoryActivity.class);
                i.putExtra(PASS_LAT, markerLat);
                i.putExtra(PASS_LONG, markerLong);
                startActivity(i);
            }
        });

        btnQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ComposeActivity.this, ComposeQuoteMemoryActivity.class);
                i.putExtra(PASS_LAT, markerLat);
                i.putExtra(PASS_LONG, markerLong);
                startActivity(i);
            }
        });
    }
}