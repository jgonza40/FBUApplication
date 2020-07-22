package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.memrecap.R;
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
        tvDetImgCategory.setText("category: " + memory.getCategory());

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
}