package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.memrecap.RecapAdapter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationRecapActivity extends AppCompatActivity {

    public static final String TAG = "LocationRecapActivity";

    public static final String PASS_LAT = "markerClickedLat";
    public static final String PASS_LONG = "markerClickedLong";
    public static final String MARKERS_ARRAY = "markers";
    public static final String OBJECT_ID = "objectId";
    public static final String LOCATION = "location";
    public static final int MAX_POSTS = 20;

    private List<Memory> listMemories;
    private RecapAdapter adapter;
    private Map<String, Double> unvisitedMarkers;
    public Boolean done = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_recap);

        // Gets the previously created intent to get 2 marker values
        Intent myIntent = getIntent();
        final String markerLat = myIntent.getStringExtra(PASS_LAT);
        final String markerLong = myIntent.getStringExtra(PASS_LONG);
        MarkerPoint currMarker = null;
        try {
            currMarker = getMarkerForPost(markerLat, markerLong);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        unvisitedMarkers = new HashMap<String, Double>();
        setUserMarkersMap(currMarker);
        unvisitedMarkers.remove(currMarker.getObjectId());

        listMemories = new ArrayList<Memory>();
        getLocationPosts(currMarker);
        adapter = new RecapAdapter(this, R.layout.item_image_recap, listMemories, LOCATION);
        SwipeFlingAdapterView flingContainer;
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);
        flingContainer.setAdapter(adapter);

        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                listMemories.remove(0);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
            }

            @Override
            public void onRightCardExit(Object o) {
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                if (listMemories.size() != 0 && unvisitedMarkers.size() != 0 && !done) {
                    try {
                        String nextMarkerId = getNextMarkerMemories();
                        ParseQuery<MarkerPoint> query = ParseQuery.getQuery(MarkerPoint.class);
                        MarkerPoint nextMarker = query.get(nextMarkerId);
                        unvisitedMarkers.remove(nextMarkerId);
                        updateMarkersMap(nextMarker);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (unvisitedMarkers.size() == 0) {
                        Memory done = new Memory();
                        done.setDone(true);
                        listMemories.add(done);
                        setDone();
                        // Adding to prevent more done cards from populating
                        unvisitedMarkers.put("done", 0.0);
                    }
                }
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }
        });

        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
            }
        });
    }

    public void setDone() {
        done = true;
    }

    public boolean getDone() {
        return done;
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
                        listMemories.add(memory);
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
            String marker = userMarkers.getJSONObject(i).getString(OBJECT_ID);
            ParseQuery<MarkerPoint> query = ParseQuery.getQuery(MarkerPoint.class);
            MarkerPoint currMarker = query.get(marker);
            if (currMarker.getMarkerLat().equals(markerLat) && currMarker.getMarkerLong().equals(markerLong)) {
                correspondingMarker = currMarker;
            }
        }
        return correspondingMarker;
    }

    private void setUserMarkersMap(MarkerPoint currMarker) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        JSONArray markersArray = currentUser.getJSONArray(MARKERS_ARRAY);
        for (int i = 0; i < markersArray.length(); i++) {
            String markerId = null;
            try {
                markerId = markersArray.getJSONObject(i).getString(OBJECT_ID);
                ParseQuery<MarkerPoint> query = ParseQuery.getQuery(MarkerPoint.class);
                MarkerPoint loopMarker = query.get(markerId);
                Double distance = calculateDistance(currMarker, loopMarker);
                unvisitedMarkers.put(markerId, distance);
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateMarkersMap(MarkerPoint currMarker) {
        for (String key : unvisitedMarkers.keySet()) {
            Double value = unvisitedMarkers.get(key);
            ParseQuery<MarkerPoint> query = ParseQuery.getQuery(MarkerPoint.class);
            MarkerPoint loopMarker = null;
            try {
                loopMarker = query.get(key);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Double distance = calculateDistance(currMarker, loopMarker);
            unvisitedMarkers.put(key, distance);
        }
    }

    private static double calculateDistance(MarkerPoint start, MarkerPoint end) {
        double lat1 = Double.parseDouble(start.getMarkerLat());
        double lon1 = Double.parseDouble(start.getMarkerLong());
        double lat2 = Double.parseDouble(end.getMarkerLat());
        double lon2 = Double.parseDouble(end.getMarkerLong());

        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            return (dist);
        }
    }

    private String getNextMarkerMemories() {
        String minKey = null;
        Double minValue = Double.MAX_VALUE;
        for (String key : unvisitedMarkers.keySet()) {
            Double value = unvisitedMarkers.get(key);
            if (value < minValue) {
                minValue = value;
                minKey = key;
            }
        }
        ParseQuery<MarkerPoint> query = ParseQuery.getQuery(MarkerPoint.class);
        MarkerPoint closestMarker = null;
        try {
            closestMarker = query.get(minKey);
            Memory continueMemory = new Memory();
            continueMemory.setMemoryTitle(closestMarker.getMarkerTitle());
            listMemories.add(continueMemory);
            getLocationPosts(closestMarker);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return closestMarker.getObjectId();
    }

    public void exitToMapFragment(View view) {
        finish();
    }
}