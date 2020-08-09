package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.memrecap.R;
import com.memrecap.StaticVariables;
import com.memrecap.models.Memory;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class QuoteMemoryDetailsActivity extends AppCompatActivity {

    public static final String TAG = "QuoteMemoryDetailsActivity";
    public static final String USER_PROFILE_PIC = "profilePicture";

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private static final String SELF_CARE_TITLE = "self care";
    private static final String FOOD_TITLE = "food";
    private static final String FAMILY_TITLE = "family";
    private static final String STEPPING_STONE_TITLE = "milestone";
    private static final String ACTIVE_TITLE = "active";
    private static final String TRAVEL_TITLE = "travel";

    private ImageView ivDetQuoteProfilePic;
    private ImageView ivDetQuoteLocation;
    private TextView tvDetQuoteUsername;
    private TextView tvDetQuoteCreatedAt;
    private TextView tvDetQuoteTripTitle;
    private TextView tvDetQuoteCategory;
    private TextView tvDetQuote;

    private Memory memory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_memory_details);

        ivDetQuoteProfilePic = findViewById(R.id.ivDetQuoteProfilePic);
        ivDetQuoteLocation = findViewById(R.id.ivDetQuoteLocation);
        tvDetQuoteUsername = findViewById(R.id.tvDetQuoteUsername);
        tvDetQuoteCreatedAt = findViewById(R.id.tvDetQuoteCreatedAt);
        tvDetQuoteTripTitle = findViewById(R.id.tvDetQuoteTripTitle);
        tvDetQuoteCategory = findViewById(R.id.tvDetQuoteCategory);
        tvDetQuote = findViewById(R.id.tvDetQuote);

        // Getting quote memory that was clicked from the Memory Adapter
        memory = (Memory) Parcels.unwrap(getIntent().getParcelableExtra(Memory.class.getSimpleName()));

        tvDetQuoteUsername.setText(memory.getUser().getUsername());
        tvDetQuoteCreatedAt.setText(getRelativeTimeAgo(memory.getCreatedAt().toString()));
        String sourceString = "<b>" + "Category:" + "</b> " + getCategoryTitle(memory.getCategory());
        tvDetQuoteCategory.setText(Html.fromHtml(sourceString));
        tvDetQuote.setText(" \" " + memory.getQuote() + " \"");
        tvDetQuoteTripTitle.setText(memory.getMemoryTitle());
        Glide.with(getApplicationContext())
                .load(memory.getUser().getParseFile(USER_PROFILE_PIC).getUrl())
                .into(ivDetQuoteProfilePic);
        Glide.with(getApplicationContext())
                .load(getApplicationContext().getResources().getDrawable(R.drawable.ic_location))
                .placeholder(R.mipmap.ic_location)
                .into(ivDetQuoteLocation);
    }

    // The purpose of this method is to get appropriate time stamps for posts
    public String getRelativeTimeAgo(String rawJsonDate) {
        String MemRecapFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(MemRecapFormat, Locale.ENGLISH);
        sf.setLenient(true);
        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + "m" + " ago";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + "h" + " ago";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + "d" + " ago";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getCategoryTitle(String category) {
        String categoryTitle = "";
        if (category.equals(StaticVariables.FOOD)) {
            categoryTitle = FOOD_TITLE;
        } else if (category.equals(StaticVariables.SELF_CARE)) {
            categoryTitle = SELF_CARE_TITLE;
        } else if (category.equals(StaticVariables.FAMILY)) {
            categoryTitle = FAMILY_TITLE;
        } else if (category.equals(StaticVariables.STEPPING_STONE)) {
            categoryTitle = STEPPING_STONE_TITLE;
        } else if (category.equals(StaticVariables.TRAVEL)) {
            categoryTitle = TRAVEL_TITLE;
        } else {
            categoryTitle = ACTIVE_TITLE;
        }
        return categoryTitle;
    }

    public void exitToHome(View view) {
        finish();
    }
}