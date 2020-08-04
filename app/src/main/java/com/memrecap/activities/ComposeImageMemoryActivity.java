package com.memrecap.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.memrecap.models.MarkerPoint;
import com.memrecap.models.Memory;
import com.memrecap.R;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;

public class ComposeImageMemoryActivity extends AppCompatActivity {

    public static final String TAG = "ComposeImageMemoryActivity";

    public static final String PASS_LAT = "markerClickedLat";
    public static final String PASS_LONG = "markerClickedLong";
    public static final String MARKERS_ARRAY = "markers";
    public static final String OBJECT_ID = "objectId";

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public static final String SELF_CARE = "selfCare";
    public static final String FOOD = "food";
    public static final String FAMILY = "family";
    public static final String STEPPING_STONE = "steppingStone";
    public static final String ACTIVE = "active";
    public static final String TRAVEL = "travel";


    private File photoFile;
    private EditText etDescription;
    private ImageView ivPostImage;

    private Button btnPost;
    private Button btnCaptureImage;
    private Button btnImageFood;
    private Button btnImageSelfCare;
    private Button btnImageFamily;
    private Button btnImageTravel;
    private Button btnImageSteppingStone;
    private Button btnImageActive;
    private MarkerPoint marker;

    private String setCategory;
    public String photoFileName = "photo.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_image_memory);
        etDescription = findViewById(R.id.etDescription);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        ivPostImage = findViewById(R.id.ivPostImage);
        btnPost = findViewById(R.id.btnImagePost);
        btnImageFood = findViewById(R.id.btnImageFood);
        btnImageSelfCare = findViewById(R.id.btnImageSelfCare);
        btnImageFamily = findViewById(R.id.btnImageFamily);
        btnImageTravel = findViewById(R.id.btnImageTravel);
        btnImageSteppingStone = findViewById(R.id.btnImageSteppingStone);
        btnImageActive = findViewById(R.id.btnImageActive);

        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        // Gets the previously created intent to get 2 marker values
        Intent myIntent = getIntent();
        final String markerLat = myIntent.getStringExtra(PASS_LAT);
        final String markerLong = myIntent.getStringExtra(PASS_LONG);

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
                String description = etDescription.getText().toString();
                if (description.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "caption cannot be blank :(", Toast.LENGTH_LONG).show();
                    return;
                }
                if (photoFile == null || ivPostImage.getDrawable() == null) {
                    Toast.makeText(ComposeImageMemoryActivity.this, "there is no image!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (setCategory == "") {
                    Toast.makeText(ComposeImageMemoryActivity.this, "must select a category!", Toast.LENGTH_LONG).show();
                    return;
                }
                ParseUser currentUser = ParseUser.getCurrentUser();

                savePost(description, currentUser, photoFile, setCategory, marker);
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

    private String getCategory() {
        setCategory = "";
        btnImageFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonColor();
                btnImageFood.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                setCategory = FOOD;
            }
        });
        btnImageSelfCare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonColor();
                btnImageSelfCare.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                setCategory = SELF_CARE;
            }
        });
        btnImageFamily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonColor();
                btnImageFamily.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                setCategory = FAMILY;
            }
        });
        btnImageTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonColor();
                btnImageTravel.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                setCategory = TRAVEL;
            }
        });
        btnImageSteppingStone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonColor();
                btnImageSteppingStone.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                setCategory = STEPPING_STONE;
            }
        });
        btnImageActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtonColor();
                btnImageActive.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                setCategory = ACTIVE;
            }
        });
        return "";
    }

    private void resetButtonColor(){
        btnImageFood.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        btnImageSelfCare.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        btnImageFamily.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        btnImageTravel.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        btnImageSteppingStone.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        btnImageActive.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(ComposeImageMemoryActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // Load the taken image into a preview
                ivPostImage.setImageBitmap(takenImage);
            } else {
                Toast.makeText(getApplicationContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos to avoid requesting external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }
        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private void savePost(String description, ParseUser currentUser, File photoFile, String category, MarkerPoint markerPoint) {
        Memory memory = new Memory();
        memory.setDescription(description);
        memory.setImage(new ParseFile(photoFile));
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
                etDescription.setText("");
                ivPostImage.setImageResource(0);

                Intent i = new Intent(ComposeImageMemoryActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}