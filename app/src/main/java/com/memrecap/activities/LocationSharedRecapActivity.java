package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.memrecap.R;
import com.memrecap.RecapAdapter;
import com.memrecap.models.Friends;
import com.memrecap.models.MarkerPoint;
import com.memrecap.models.Memory;
import com.memrecap.models.SharedMarker;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationSharedRecapActivity extends AppCompatActivity {

    public static final String TAG = "LocationSharedRecap";

    public static final String PASS_LAT = "markerClickedLat";
    public static final String PASS_LONG = "markerClickedLong";
    public static final String LOCATION = "location";
    public static final int MAX_POSTS = 20;

    private List<Memory> listMemories;
    private RecapAdapter adapter;
    private Map<String, Double> unvisitedMarkers;
    public Boolean done = false;
    private JSONObject friendsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_shared_recap);

        // Gets the previously created intent to get 2 marker values
        Intent myIntent = getIntent();
        final String markerLat = myIntent.getStringExtra(PASS_LAT);
        final String markerLong = myIntent.getStringExtra(PASS_LONG);

        getMarkerForPost(markerLat, markerLong);
    }

    public void setDone() {
        done = true;
    }

    public boolean getDone() {
        return done;
    }

    private void getMarkerForPost(final String markerLat, final String markerLong) {
        ParseQuery<SharedMarker> query = ParseQuery.getQuery(SharedMarker.class);
        query.include(SharedMarker.KEY_MARKER_LAT);
        query.whereEqualTo(SharedMarker.KEY_MARKER_LAT, markerLat);
        query.findInBackground(new FindCallback<SharedMarker>() {
            @Override
            public void done(List<SharedMarker> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "issue with getting shared marker", e);
                } else {
                    for (int i = 0; i < objects.size(); i++) {
                        if (objects.get(i).getMarkerLong().equals(markerLong)) {
                            SharedMarker currMarker = objects.get(i);
                            unvisitedMarkers = new HashMap<String, Double>();
                            setUserMarkersMap(currMarker);

                            listMemories = new ArrayList<Memory>();
                            getLocationPosts(currMarker);
                            adapter = new RecapAdapter(getApplicationContext(), R.layout.item_image_recap, listMemories, LOCATION);

                            setUpFlingAdapter(adapter);
                        }
                    }
                }
            }
        });
    }

    private void setUpFlingAdapter(RecapAdapter recapAdapter) {
        adapter = recapAdapter;
        adapter.notifyDataSetChanged();

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
                        ParseQuery<SharedMarker> query = ParseQuery.getQuery(SharedMarker.class);
                        SharedMarker nextMarker = query.get(nextMarkerId);
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

    private void getLocationPosts(final SharedMarker sharedMarker) {
        ParseQuery<Memory> query = ParseQuery.getQuery(Memory.class);
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
                    if (memory.getKeyMarker() == null) {
                        if (memory.getKeySharedMarkerId().equals(sharedMarker.getObjectId())) {
                            listMemories.add(memory);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setUserMarkersMap(final SharedMarker currSharedMarker) {
        final ParseUser currUser = ParseUser.getCurrentUser();
        ParseQuery<Friends> query = ParseQuery.getQuery(Friends.class);
        query.include(Friends.KEY_USER);
        query.whereEqualTo(Friends.KEY_USER, currUser);
        query.findInBackground(new FindCallback<Friends>() {
            @Override
            public void done(List<Friends> friends, ParseException e) {
                Friends currFriendModel = friends.get(0);
                friendsMap = currFriendModel.getFriendsMap();
                ParseQuery<SharedMarker> query = ParseQuery.getQuery(SharedMarker.class);
                query.findInBackground(new FindCallback<SharedMarker>() {
                    @Override
                    public void done(List<SharedMarker> sMarkers, ParseException e) {
                        for (int i = 0; i < sMarkers.size(); i++) {
                            SharedMarker curr = sMarkers.get(i);
                            if (currUser.getObjectId().equals(curr.getMarkerCreator().getObjectId())) {
                                Double distance = calculateDistance(currSharedMarker, curr);
                                unvisitedMarkers.put(curr.getObjectId(), distance);
                            } else {
                                if (friendsMap != null) {
                                    if (friendsMap.has(curr.getMarkerCreator().getObjectId())) {
                                        Double distance = calculateDistance(currSharedMarker, curr);
                                        unvisitedMarkers.put(curr.getObjectId(), distance);
                                    }
                                }
                            }
                        }
                        unvisitedMarkers.remove(currSharedMarker.getObjectId());
                        setUnvisited(unvisitedMarkers);
                    }
                });
            }
        });
    }

    private void setUnvisited(Map<String, Double> map) {
        unvisitedMarkers = map;
    }

    private void updateMarkersMap(SharedMarker currMarker) {
        for (String key : unvisitedMarkers.keySet()) {
            Double value = unvisitedMarkers.get(key);
            ParseQuery<SharedMarker> query = ParseQuery.getQuery(SharedMarker.class);
            SharedMarker loopMarker = null;
            try {
                loopMarker = query.get(key);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Double distance = calculateDistance(currMarker, loopMarker);
            unvisitedMarkers.put(key, distance);
        }
    }

    private static double calculateDistance(SharedMarker start, SharedMarker end) {
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
        ParseQuery<SharedMarker> query = ParseQuery.getQuery(SharedMarker.class);
        SharedMarker closestMarker = null;
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