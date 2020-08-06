package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.memrecap.R;
import com.memrecap.RecapAdapter;
import com.memrecap.StaticVariables;
import com.memrecap.models.MarkerPoint;
import com.memrecap.models.Memory;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FriendRecapActivity extends AppCompatActivity {
    public static final String TAG = "ProfileRecapActivity";

    private static final String FRIEND_ID = "friendId";

    public static final int MAX_POSTS = 20;
    public static final int NUM_OF_CATEGORIES = 6;

    public static final String PROFILE = "profile";

    protected List<Memory> foodMemories;
    protected List<Memory> selfCareMemories;
    protected List<Memory> familyMemories;
    protected List<Memory> travelMemories;
    protected List<Memory> steppingStoneMemories;
    protected List<Memory> activeMemories;
    private List<Memory> listMemories;
    private ParseUser currFriend;

    private RecapAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_recap);

        listMemories = new ArrayList<Memory>();
        foodMemories = new ArrayList<Memory>();
        selfCareMemories = new ArrayList<Memory>();
        familyMemories = new ArrayList<Memory>();
        travelMemories = new ArrayList<Memory>();
        steppingStoneMemories = new ArrayList<Memory>();
        activeMemories = new ArrayList<Memory>();

        Intent myIntent = getIntent();
        String friendUserId = myIntent.getStringExtra(FRIEND_ID);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        ParseUser friendUser = null;
        try {
            friendUser = query.get(friendUserId);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        currFriend = friendUser;

        adapter = new RecapAdapter(this, R.layout.item_image_recap, listMemories, PROFILE);
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
            }
        });
    }

    private void getProfilePosts() {
        ParseQuery<Memory> query = ParseQuery.getQuery(Memory.class);
        query.include(Memory.KEY_USER);
        query.whereEqualTo(Memory.KEY_USER, currFriend);
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
                    switch (memory.getCategory()) {
                        case StaticVariables.FOOD:
                            foodMemories.add(memory);
                            break;
                        case StaticVariables.SELF_CARE:
                            selfCareMemories.add(memory);
                            break;
                        case StaticVariables.FAMILY:
                            familyMemories.add(memory);
                            break;
                        case StaticVariables.TRAVEL:
                            travelMemories.add(memory);
                            break;
                        case StaticVariables.STEPPING_STONE:
                            steppingStoneMemories.add(memory);
                            break;
                        case StaticVariables.ACTIVE:
                        default:
                            activeMemories.add(memory);
                            break;
                    }
                }

                // Adding posts in the order that they should appear (order of category)
                setCategoryMemories(foodMemories);
                setCategoryMemories(selfCareMemories);
                setCategoryMemories(familyMemories);
                setCategoryMemories(travelMemories);
                setCategoryMemories(steppingStoneMemories);
                setCategoryMemories(activeMemories);
                Memory done = new Memory();
                done.setDone(true);
                listMemories.add(done);

                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setCategoryMemories(List<Memory> list) {
        if (list.size() == 0) {
            return;
        }
        Memory titleMemory = new Memory();
        titleMemory.setCategory(list.get(0).getCategory());
        listMemories.add(titleMemory);
        listMemories.addAll(list);
    }

    public void exitToFriendProfile(View view) {
        finish();
    }
}