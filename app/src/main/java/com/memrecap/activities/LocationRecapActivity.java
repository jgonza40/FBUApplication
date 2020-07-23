package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.memrecap.LocationRecapAdapter;
import com.memrecap.R;
import com.memrecap.models.MarkerPoint;
import com.memrecap.models.Memory;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class LocationRecapActivity extends AppCompatActivity {

    public static final String TAG = "LocationRecapActivity";

    public static final String PASS_LAT = "markerClickedLat";
    public static final String PASS_LONG = "markerClickedLong";
    public static final String MARKERS_ARRAY = "markers";
    public static final int MAX_POSTS = 20;

    private MarkerPoint marker;

    private List<Memory> listMemories;
    private LocationRecapAdapter adapter;
    private int items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_recap);

        // Gets the previously created intent to get 2 marker values
        Intent myIntent = getIntent();
        final String markerLat = myIntent.getStringExtra(PASS_LAT);
        final String markerLong= myIntent.getStringExtra(PASS_LONG);

        try {
            marker = getMarkerForPost(markerLat, markerLong);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        listMemories = new ArrayList<Memory>();
        getLocationPosts(marker);
        adapter = new LocationRecapAdapter(this, R.layout.item_location_recap, listMemories);
        SwipeFlingAdapterView flingContainer;
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);
        flingContainer.setAdapter(adapter);

        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                listMemories.remove(0);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                Toast.makeText(LocationRecapActivity.this, "Left!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object o) {
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });

        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Toast.makeText(LocationRecapActivity.this, "Clicked!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLocationPosts(final MarkerPoint markerPoint) {
        ParseQuery<Memory> query = ParseQuery.getQuery(Memory.class);
        query.include(Memory.KEY_USER);
        query.whereEqualTo(Memory.KEY_USER, ParseUser.getCurrentUser());
        query.addDescendingOrder(Memory.KEY_CREATED_AT);
        query.setLimit(MAX_POSTS);
        query.findInBackground(new FindCallback<Memory>() {
            @Override
            public void done(List<Memory> memories, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                for (Memory memory : memories) {
                    if (memory.getKeyMarkerId().equals(markerPoint.getObjectId())) {
                        if(memory.getImage() != null){
                            listMemories.add(memory);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private MarkerPoint getMarkerForPost(String markerLat, String markerLong) throws JSONException, ParseException {
        JSONArray userMarkers = ParseUser.getCurrentUser().getJSONArray(MARKERS_ARRAY);
        MarkerPoint correspondingMarker = null;
        for (int i = 0; i < userMarkers.length(); i++) {
            String marker = userMarkers.getJSONObject(i).getString("objectId");
            ParseQuery<MarkerPoint> query = ParseQuery.getQuery(MarkerPoint.class);
            MarkerPoint currMarker = query.get(marker);
            if (currMarker.getMarkerLat().equals(markerLat) && currMarker.getMarkerLong().equals(markerLong)) {
                correspondingMarker = currMarker;
            }
        }
        return correspondingMarker;
    }
}