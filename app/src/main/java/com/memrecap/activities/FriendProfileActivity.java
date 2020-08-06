package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.memrecap.MemoryAdapter;
import com.memrecap.R;
import com.memrecap.StaticVariables;
import com.memrecap.models.Friends;
import com.memrecap.models.Memory;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FriendProfileActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "FriendProfileActivity";

    private static final String USER_PROFILE_PIC = "profilePicture";
    private static final String OBJECT_ID = "objectId";
    private static final String FRIEND_ID = "friendId";
    private static final int MAX_POSTS = 20;

    private ImageView ivFriendImage;
    private TextView tvFriendUsername;
    private Button btnFriendRecap;
    private Button btnFriendFood;
    private Button btnFriendSelfCare;
    private Button btnFriendFamily;
    private Button btnFriendTravel;
    private Button btnFriendSteppingStone;
    private Button btnFriendActive;
    private RecyclerView rvCategoryMemories;
    private TextView tvNumFriends;
    private TextView tvNumPosts;
    private Button btnViewMap;

    protected List<Memory> foodMemories;
    protected List<Memory> selfCareMemories;
    protected List<Memory> familyMemories;
    protected List<Memory> travelMemories;
    protected List<Memory> steppingStoneMemories;
    protected List<Memory> activeMemories;
    protected List<Memory> finalList;

    private Boolean foodSelected;
    private Boolean selfCareSelected;
    private Boolean familySelected;
    private Boolean travelSelected;
    private Boolean steppingStoneSelected;
    private Boolean activeSelected;

    private ParseUser friendUser;

    protected MemoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);

        // Gets the previously created intent to get friend id
        Intent myIntent = getIntent();
        String friendUserId = myIntent.getStringExtra(FRIEND_ID);

        ivFriendImage = findViewById(R.id.ivFriendImage);
        tvFriendUsername = findViewById(R.id.tvFriendUsername);
        btnFriendRecap = findViewById(R.id.btnFriendRecap);
        btnFriendFood = findViewById(R.id.btnFriendFood);
        btnFriendSelfCare = findViewById(R.id.btnFriendSelfCare);
        btnFriendFamily = findViewById(R.id.btnFriendFamily);
        btnFriendTravel = findViewById(R.id.btnFriendTravel);
        btnFriendSteppingStone = findViewById(R.id.btnFriendSteppingStone);
        btnFriendActive = findViewById(R.id.btnFriendActive);
        btnFriendRecap = findViewById(R.id.btnFriendRecap);
        rvCategoryMemories = findViewById(R.id.rvFriendCategoryMemories);
        tvNumFriends = findViewById(R.id.tvNumFriends);
        tvNumPosts = findViewById(R.id.tvNumPosts);
        btnViewMap = findViewById(R.id.btnViewMap);

        btnFriendRecap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), FriendRecapActivity.class);
                i.putExtra(FRIEND_ID, friendUser.getObjectId());
                startActivity(i);
            }
        });

        btnViewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), ViewFriendMarkersActivity.class);
                i.putExtra(FRIEND_ID, friendUser.getObjectId());
                startActivity(i);
            }
        });

        btnFriendFood.setOnClickListener(this);
        btnFriendSelfCare.setOnClickListener(this);
        btnFriendFamily.setOnClickListener(this);
        btnFriendTravel.setOnClickListener(this);
        btnFriendSteppingStone.setOnClickListener(this);
        btnFriendActive.setOnClickListener(this);

        resetBooleanValues();
        foodSelected = true;
        btnFriendFood.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo(OBJECT_ID, friendUserId);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error while saving getting user", e);
                } else {
                    ParseUser currUser = objects.get(0);
                    setFriendUser(currUser);
                    setProfileComponents(ivFriendImage, tvFriendUsername, currUser);
                    recyclerViewSetup(currUser);
                    setNumFriends(currUser);
                }
            }
        });
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.btnFriendFood:
                resetBooleanValues();
                foodSelected = true;
                btnFriendFood.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                break;
            case R.id.btnFriendSelfCare:
                resetBooleanValues();
                selfCareSelected = true;
                btnFriendSelfCare.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                break;
            case R.id.btnFriendFamily:
                resetBooleanValues();
                familySelected = true;
                btnFriendFamily.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                break;
            case R.id.btnFriendTravel:
                resetBooleanValues();
                travelSelected = true;
                btnFriendTravel.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                break;
            case R.id.btnFriendSteppingStone:
                resetBooleanValues();
                steppingStoneSelected = true;
                btnFriendSteppingStone.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                break;
            case R.id.btnFriendActive:
            default:
                resetBooleanValues();
                activeSelected = true;
                btnFriendActive.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.profile_recap_button));
                break;
        }
        recyclerViewSetup(friendUser);
    }

    private void setFriendUser(ParseUser currUser) {
        friendUser = currUser;
    }

    private void resetBooleanValues() {
        foodSelected = false;
        btnFriendFood.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        selfCareSelected = false;
        btnFriendSelfCare.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        familySelected = false;
        btnFriendFamily.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        travelSelected = false;
        btnFriendTravel.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        steppingStoneSelected = false;
        btnFriendSteppingStone.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
        activeSelected = false;
        btnFriendActive.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.location_custom_button));
    }

    private void setProfileComponents(ImageView profImage, TextView username, ParseUser friendUser) {
        username.setText("@" + friendUser.getUsername());
        Glide.with(getApplicationContext())
                .load(friendUser.getParseFile(USER_PROFILE_PIC).getUrl())
                .into(profImage);
    }

    private void recyclerViewSetup(ParseUser friendUser) {
        foodMemories = new ArrayList<>();
        selfCareMemories = new ArrayList<>();
        familyMemories = new ArrayList<>();
        travelMemories = new ArrayList<>();
        steppingStoneMemories = new ArrayList<>();
        activeMemories = new ArrayList<>();
        finalList = new ArrayList<>();

        adapter = new MemoryAdapter(getApplicationContext(), finalList);
        rvCategoryMemories.setAdapter(adapter);
        rvCategoryMemories.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        queryPosts(friendUser);
    }

    protected void queryPosts(ParseUser friendUser) {
        ParseQuery<Memory> query = ParseQuery.getQuery(Memory.class);
        query.include(Memory.KEY_USER);
        query.whereEqualTo(Memory.KEY_USER, friendUser);
        query.setLimit(MAX_POSTS);
        query.addDescendingOrder(Memory.KEY_CREATED_AT);

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
                setFinalList();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setFinalList() {
        finalList.clear();
        if (foodSelected) {
            setTvNumPosts(foodMemories, StaticVariables.FOOD_STRING);
            finalList.addAll(foodMemories);
        } else if (selfCareSelected) {
            setTvNumPosts(selfCareMemories, StaticVariables.SELF_CARE_STRING);
            finalList.addAll(selfCareMemories);
        } else if (familySelected) {
            setTvNumPosts(familyMemories, StaticVariables.FAMILY_STRING);
            finalList.addAll(familyMemories);
        } else if (travelSelected) {
            setTvNumPosts(travelMemories, StaticVariables.TRAVEL_STRING);
            finalList.addAll(travelMemories);
        } else if (steppingStoneSelected) {
            setTvNumPosts(steppingStoneMemories, StaticVariables.STEPPING_STONE_STRING);
            finalList.addAll(steppingStoneMemories);
        } else {
            setTvNumPosts(activeMemories, StaticVariables.ACTIVE_STRING);
            finalList.addAll(activeMemories);
        }
    }

    private void setTvNumPosts(List<Memory> currentMemories, String currMem) {
        if (currentMemories.size() == 0) {
            tvNumPosts.setText("They do not have " + currMem + " memories :(");
        } else if (currentMemories.size() == 1) {
            tvNumPosts.setText("They have 1 " + currMem + " memory!");
        } else {
            tvNumPosts.setText("They have " + currentMemories.size() + currMem + "memories!");
        }
    }

    private void setNumFriends(ParseUser friendUserId) {
        ParseQuery<Friends> query = ParseQuery.getQuery(Friends.class);
        query.include(Friends.KEY_USER);
        query.whereEqualTo(Friends.KEY_USER, friendUserId);
        query.findInBackground(new FindCallback<Friends>() {
            @Override
            public void done(List<Friends> friends, ParseException e) {
                Friends currFriendModel = friends.get(0);
                JSONObject friendsList = currFriendModel.getFriendsMap();
                int numFriends = friendsList.length();
                String sourceString = "<b>" + "Friends:" + "</b> " + Integer.toString(numFriends);
                tvNumFriends.setText(Html.fromHtml(sourceString));
            }
        });
    }

    public void exitToPrev(View view) {
        finish();
    }
}