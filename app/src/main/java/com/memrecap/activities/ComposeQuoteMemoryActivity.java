package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.memrecap.models.Memory;
import com.memrecap.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ComposeQuoteMemoryActivity extends AppCompatActivity {

    public static final String TAG = "ComposeQuoteMemory";

    public static final String SELF_CARE = "selfCare";
    public static final String FOOD = "food";
    public static final String FAMILY = "family";
    public static final String STEPPING_STONE = "steppingStone";
    public static final String ACTIVE = "active";
    public static final String TRAVEL = "travel";

    private Button btnImageFood;
    private Button btnImageSelfCare;
    private Button btnImageFamily;
    private Button btnImageTravel;
    private Button btnImageSteppingStone;
    private Button btnImageActive;
    private String setCategory;
    private EditText etQuote;
    private Button btnPost;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_quote_memory);
        btnPost = findViewById(R.id.btnQuotePost);
        btnImageFood = findViewById(R.id.btnQuoteFood);
        btnImageSelfCare = findViewById(R.id.btnQuoteSelfCare);
        btnImageFamily = findViewById(R.id.btnQuoteFamily);
        btnImageTravel = findViewById(R.id.btnQuoteTravel);
        btnImageSteppingStone = findViewById(R.id.btnQuoteSteppingStone);
        btnImageActive = findViewById(R.id.btnQuoteActive);
        pb = findViewById(R.id.pbQuoteLoad);
        etQuote = findViewById(R.id.etQuote);

        getCategory();

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quote = etQuote.getText().toString();
                ParseUser currentUser = ParseUser.getCurrentUser();
                pb.setVisibility(ProgressBar.VISIBLE);
                savePost(quote, currentUser, setCategory);
            }
        });
    }

    private void savePost(String quote, ParseUser currentUser, String category) {
        Memory memory = new Memory();
        memory.setQuote(quote);
        memory.setUser(currentUser);
        memory.setCategory(category);
        memory.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getApplicationContext(), "error while saving!", Toast.LENGTH_LONG).show();
                    return;
                }
                etQuote.setText("");
                // Setting pb to invisible once post is submitted
                pb.setVisibility(View.INVISIBLE);
                Intent i = new Intent(ComposeQuoteMemoryActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private String getCategory(){
        setCategory = "";
        btnImageFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCategory = FOOD;
            }
        });
        btnImageSelfCare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCategory = SELF_CARE;
            }
        });
        btnImageFamily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCategory = FAMILY;
            }
        });
        btnImageTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCategory = TRAVEL;
            }
        });
        btnImageSteppingStone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCategory = STEPPING_STONE;
            }
        });
        btnImageActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setCategory = ACTIVE;
            }
        });
        return "";
    }
}