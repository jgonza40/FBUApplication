package com.memrecap.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

import com.memrecap.models.Memory;
import com.memrecap.R;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;

public class ComposeImageMemoryActivity extends AppCompatActivity {

    public static final String TAG = "ComposeImageMemoryActivity";

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public static final String SELF_CARE = "selfCare";
    public static final String FOOD = "food";
    public static final String FAMILY = "family";
    public static final String STEPPING_STONE = "steppingStone";
    public static final String ACTIVE = "active";
    public static final String TRAVEL = "travel";


    private File photoFile;
    private ProgressBar pb;
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

    private String setCategory;
    public String photoFileName = "photo.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_image_memory);
        pb = findViewById(R.id.pbImgLoad);
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
                ParseUser currentUser = ParseUser.getCurrentUser();
                if (setCategory == "") {
                    Toast.makeText(ComposeImageMemoryActivity.this, "must select a category!", Toast.LENGTH_LONG).show();
                    return;
                }
                pb.setVisibility(ProgressBar.VISIBLE);
                savePost(description, currentUser, photoFile, setCategory);
            }
        });
    }

    private String getCategory() {
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

    private void savePost(String description, ParseUser currentUser, File photoFile, String category) {
        Memory memory = new Memory();
        memory.setDescription(description);
        memory.setImage(new ParseFile(photoFile));
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
                etDescription.setText("");
                ivPostImage.setImageResource(0);
                // Setting pb to invisible once post is submitted
                pb.setVisibility(View.INVISIBLE);
                Intent i = new Intent(ComposeImageMemoryActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}