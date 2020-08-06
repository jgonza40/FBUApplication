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
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ImageMemoryDetailsActivity extends AppCompatActivity {

    public static final String TAG = "ImageMemoryDetailsActivity";
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

    private ImageView ivDetImgPostImage;
    private ImageView ivDetImgLocation;
    private ImageView ivDetImgProfilePic;
    private TextView tvDetImgUsername;
    private TextView tvDetImgCreatedAt;
    private TextView tvDetImgCaption;
    private TextView tvDetImgTripTitle;
    private TextView tvDetImgCategory;

    private Memory memory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_memory_details);

        ivDetImgPostImage = findViewById(R.id.ivDetImgPostImage);
        ivDetImgProfilePic = findViewById(R.id.ivDetImgProfilePic);
        ivDetImgLocation = findViewById(R.id.ivDetImgLocation);
        tvDetImgUsername = findViewById(R.id.tvDetImgUsername);
        tvDetImgCreatedAt = findViewById(R.id.tvDetImgCreatedAt);
        tvDetImgCaption = findViewById(R.id.tvDetImgCaption);
        tvDetImgTripTitle = findViewById(R.id.tvDetImgTripTitle);
        tvDetImgCategory = findViewById(R.id.tvDetImgCategory);

        // Getting image memory that was clicked from the Memory Adapter
        memory = (Memory) Parcels.unwrap(getIntent().getParcelableExtra(Memory.class.getSimpleName()));

        tvDetImgUsername.setText(memory.getUser().getUsername());
        tvDetImgCreatedAt.setText(getRelativeTimeAgo(memory.getCreatedAt().toString()));
        tvDetImgCaption.setText(memory.getDescription());
        String sourceString = "<b>" + "Category:" + "</b> " + getCategoryTitle(memory.getCategory());
        tvDetImgCategory.setText(Html.fromHtml(sourceString));
        tvDetImgTripTitle.setText(memory.getMemoryTitle());
        ParseFile image = memory.getImage();
        if (image != null) {
            Glide.with(getApplicationContext())
                    .load(image.getUrl())
                    .into(ivDetImgPostImage);
        }
        Glide.with(getApplicationContext())
                .load(memory.getUser().getParseFile(USER_PROFILE_PIC).getUrl())
                .into(ivDetImgProfilePic);
        Glide.with(getApplicationContext())
                .load(getApplicationContext().getResources().getDrawable(R.drawable.ic_location))
                .placeholder(R.mipmap.ic_location)
                .into(ivDetImgLocation);
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

    public void exitBackHome(View view) {
        finish();
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
}