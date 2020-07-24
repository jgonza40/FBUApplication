package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class ProfileRecapActivity extends AppCompatActivity {

    public static final String TAG = "ProfileRecapActivity";

    private static final String SELF_CARE = "selfCare";
    private static final String FOOD = "food";
    private static final String FAMILY = "family";
    private static final String STEPPING_STONE = "steppingStone";
    private static final String ACTIVE = "active";
    private static final String TRAVEL = "travel";
    public static final int MAX_POSTS = 20;
    public static final int NUM_OF_CATEGORIES = 6;

    protected List<Memory> foodMemories;
    protected List<Memory> selfCareMemories;
    protected List<Memory> familyMemories;
    protected List<Memory> travelMemories;
    protected List<Memory> steppingStoneMemories;
    protected List<Memory> activeMemories;
    private List<Memory> listMemories;
    private LocationRecapAdapter adapter;
    private int items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_recap);

        listMemories = new ArrayList<Memory>();
        foodMemories = new ArrayList<Memory>();
        selfCareMemories = new ArrayList<Memory>();
        familyMemories = new ArrayList<Memory>();
        travelMemories = new ArrayList<Memory>();
        steppingStoneMemories = new ArrayList<Memory>();
        activeMemories = new ArrayList<Memory>();

        adapter = new LocationRecapAdapter(this, R.layout.item_image_recap, listMemories);
        SwipeFlingAdapterView flingContainer;
        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);
        flingContainer.setAdapter(adapter);
        getProfilePosts();

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
                Toast.makeText(ProfileRecapActivity.this, "Clicked!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getProfilePosts() {
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
                    if (memory.getCategory().equals(FOOD)) {
                        foodMemories.add(memory);
                    } else if (memory.getCategory().equals(SELF_CARE)) {
                        selfCareMemories.add(memory);
                    } else if (memory.getCategory().equals(FAMILY)) {
                        familyMemories.add(memory);
                    } else if (memory.getCategory().equals(TRAVEL)) {
                        travelMemories.add(memory);
                    } else if (memory.getCategory().equals(STEPPING_STONE)) {
                        steppingStoneMemories.add(memory);
                    } else {
                        activeMemories.add(memory);
                    }
                }

                // Adding posts in the order that they should appear (order of category)
                setCategoryMemories(foodMemories);
                setCategoryMemories(selfCareMemories);
                setCategoryMemories(familyMemories);
                setCategoryMemories(travelMemories);
                setCategoryMemories(steppingStoneMemories);
                setCategoryMemories(activeMemories);

                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setCategoryMemories(List<Memory> list){
        if(list.size() == 0){
            return;
        }
        Memory titleMemory = new Memory();
        titleMemory.setCategory(list.get(0).getCategory());
        listMemories.add(titleMemory);
        listMemories.addAll(list);
    }
}