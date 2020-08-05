package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.memrecap.models.MarkerPoint;
import com.memrecap.models.Memory;
import com.memrecap.R;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

public class ComposeQuoteMemoryActivity extends AppCompatActivity {

    public static final String TAG = "ComposeQuoteMemory";

    public static final String PASS_LAT = "markerClickedLat";
    public static final String PASS_LONG = "markerClickedLong";
    public static final String MARKERS_ARRAY = "markers";
    public static final String OBJECT_ID = "objectId";

    public static final String SELF_CARE = "selfCare";
    public static final String FOOD = "food";
    public static final String FAMILY = "family";
    public static final String STEPPING_STONE = "steppingStone";
    public static final String ACTIVE = "active";
    public static final String TRAVEL = "travel";

    private Button btnQuoteFood;
    private Button btnQuoteSelfCare;
    private Button btnQuoteFamily;
    private Button btnQuoteTravel;
    private Button btnQuoteSteppingStone;
    private Button btnQuoteActive;
    private String setCategory;
    private EditText etQuote;
    private Button btnPost;
    private MarkerPoint marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_quote_memory);
        btnPost = findViewById(R.id.btnQuotePost);
        btnQuoteFood = findViewById(R.id.btnQuoteFood);
        btnQuoteSelfCare = findViewById(R.id.btnQuoteSelfCare);
        btnQuoteFamily = findViewById(R.id.btnQuoteFamily);
        btnQuoteTravel = findViewById(R.id.btnQuoteTravel);
        btnQuoteSteppingStone = findViewById(R.id.btnQuoteSteppingStone);
        btnQuoteActive = findViewById(R.id.btnQuoteActive);
        etQuote = findViewById(R.id.etQuote);

        // Gets the previously created intent to get 2 marker values
        Intent composeIntent = getIntent();
        final String markerLat = composeIntent.getStringExtra(PASS_LAT);
        final String markerLong = composeIntent.getStringExtra(PASS_LONG);

        try {
            marker = getMarkerForPost(markerLat, markerLong);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        getCategory();
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quote = etQuote.getText().toString();
                ParseUser currentUser = ParseUser.getCurrentUser();
                savePost(quote, currentUser, setCategory, marker);
            }
        });
    }

    private MarkerPoint getMarkerForPost(String markerLat, String markerLong) throws JSONException, ParseException {
        JSONArray userMarkers = ParseUser.getCurrentUser().getJSONArray(MARKERS_ARRAY);
        MarkerPoint correspondingMarker = null;
        for (int i = 0; i < userMarkers.length(); i++) {
            String marker = userMarkers.getJSONObject(i).getString(OBJECT_ID);
            ParseQuery<MarkerPoint> query = ParseQuery.getQuery(MarkerPoint.class);
            MarkerPoint currMarker = query.get(marker);
            if (currMarker.getMarkerLat().equals(markerLat) && currMarker.getMarkerLong().equals(markerLong)) {
                correspondingMarker = currMarker;
            }
        }
        return correspondingMarker;
    }

    private void savePost(String quote, ParseUser currentUser, String category, MarkerPoint markerPoint) {
        Memory memory = new Memory();
        memory.setQuote(quote);
        memory.setUser(currentUser);
        memory.setCategory(category);
        memory.setMemoryTitle(markerPoint.getMarkerTitle());
        memory.setMarker(markerPoint);
        memory.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving", e);
                    Toast.makeText(getApplicationContext(), "error while saving!", Toast.LENGTH_LONG).show();
                    return;
                }
                etQuote.setText("");
                Intent i = new Intent(ComposeQuoteMemoryActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private String getCategory() {
        setCategory = "";
        btnQuoteFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonColor();
                btnQuoteFood.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                setCategory = FOOD;
            }
        });
        btnQuoteSelfCare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonColor();
                btnQuoteSelfCare.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                setCategory = SELF_CARE;
            }
        });
        btnQuoteFamily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonColor();
                btnQuoteFamily.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                setCategory = FAMILY;
            }
        });
        btnQuoteTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonColor();
                btnQuoteTravel.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                setCategory = TRAVEL;
            }
        });
        btnQuoteSteppingStone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonColor();
                btnQuoteSteppingStone.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                setCategory = STEPPING_STONE;
            }
        });
        btnQuoteActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonColor();
                btnQuoteActive.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                setCategory = ACTIVE;
            }
        });
        return "";
    }

    private void resetButtonColor() {
        btnQuoteFood.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        btnQuoteSelfCare.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        btnQuoteFamily.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        btnQuoteTravel.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        btnQuoteSteppingStone.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        btnQuoteActive.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
    }

    public void exitBackToCompose(View view) {
        finish();
    }
}