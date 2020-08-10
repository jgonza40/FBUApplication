package com.memrecap.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.memrecap.MemoryAdapter;
import com.memrecap.R;
import com.memrecap.StaticVariables;
import com.memrecap.activities.ProfileRecapActivity;
import com.memrecap.models.Friends;
import com.memrecap.models.Memory;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "ProfileFragment";

    private static final String USER_PROFILE_PIC = "profilePicture";
    private static final int MAX_POSTS = 20;

    private ImageView ivProfileImage;
    private TextView tvProfileUsername;
    private Button btnProfileRecap;
    private Button btnProfileFood;
    private Button btnProfileSelfCare;
    private Button btnProfileFamily;
    private Button btnProfileTravel;
    private Button btnProfileSteppingStone;
    private Button btnProfileActive;
    private RecyclerView rvCategoryMemories;
    private TextView tvNumFriends;
    private TextView tvNumPosts;

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
    private ArrayList<Boolean> booleans = new ArrayList<Boolean>(Arrays.asList(foodSelected,
            selfCareSelected, familySelected, travelSelected, steppingStoneSelected, activeSelected));

    protected MemoryAdapter adapter;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvProfileUsername = view.findViewById(R.id.tvProfileUsername);
        btnProfileRecap = view.findViewById(R.id.btnProfileRecap);
        btnProfileFood = view.findViewById(R.id.btnProfileFood);
        btnProfileSelfCare = view.findViewById(R.id.btnProfileSelfCare);
        btnProfileFamily = view.findViewById(R.id.btnProfileFamily);
        btnProfileTravel = view.findViewById(R.id.btnProfileTravel);
        btnProfileSteppingStone = view.findViewById(R.id.btnProfileSteppingStone);
        btnProfileActive = view.findViewById(R.id.btnProfileActive);
        btnProfileRecap = view.findViewById(R.id.btnProfileRecap);
        rvCategoryMemories = view.findViewById(R.id.rvProfileCategoryMemories);
        tvNumFriends = view.findViewById(R.id.tvNumFriends);
        tvNumPosts = view.findViewById(R.id.tvNumPosts);

        setNumFriends();

        btnProfileRecap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ProfileRecapActivity.class);
                startActivity(i);
            }
        });

        btnProfileFood.setOnClickListener(this);
        btnProfileSelfCare.setOnClickListener(this);
        btnProfileFamily.setOnClickListener(this);
        btnProfileTravel.setOnClickListener(this);
        btnProfileSteppingStone.setOnClickListener(this);
        btnProfileActive.setOnClickListener(this);

        resetBooleanValues();
        foodSelected = true;
        btnProfileFood.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.profile_recap_button));

        setProfileComponents(ivProfileImage, tvProfileUsername);
        recyclerViewSetup();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnProfileFood:
                resetBooleanValues();
                foodSelected = true;
                btnProfileFood.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.profile_recap_button));
                break;
            case R.id.btnProfileSelfCare:
                resetBooleanValues();
                selfCareSelected = true;
                btnProfileSelfCare.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.profile_recap_button));
                break;
            case R.id.btnProfileFamily:
                resetBooleanValues();
                familySelected = true;
                btnProfileFamily.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.profile_recap_button));
                break;
            case R.id.btnProfileTravel:
                resetBooleanValues();
                travelSelected = true;
                btnProfileTravel.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.profile_recap_button));
                break;
            case R.id.btnProfileSteppingStone:
                resetBooleanValues();
                steppingStoneSelected = true;
                btnProfileSteppingStone.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.profile_recap_button));
                break;
            case R.id.btnProfileActive:
            default:
                resetBooleanValues();
                activeSelected = true;
                btnProfileActive.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.profile_recap_button));
                break;
        }
        recyclerViewSetup();
    }

    private void resetBooleanValues() {
        foodSelected = false;
        btnProfileFood.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.location_custom_button));
        selfCareSelected = false;
        btnProfileSelfCare.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.location_custom_button));
        familySelected = false;
        btnProfileFamily.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.location_custom_button));
        travelSelected = false;
        btnProfileTravel.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.location_custom_button));
        steppingStoneSelected = false;
        btnProfileSteppingStone.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.location_custom_button));
        activeSelected = false;
        btnProfileActive.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.location_custom_button));
    }

    private void setProfileComponents(ImageView profImage, TextView username) {
        username.setText("@" + ParseUser.getCurrentUser().getUsername());
        Glide.with(getContext())
                .load(ParseUser.getCurrentUser().getParseFile(USER_PROFILE_PIC).getUrl())
                .into(profImage);
    }

    private void recyclerViewSetup() {
        foodMemories = new ArrayList<>();
        selfCareMemories = new ArrayList<>();
        familyMemories = new ArrayList<>();
        travelMemories = new ArrayList<>();
        steppingStoneMemories = new ArrayList<>();
        activeMemories = new ArrayList<>();
        finalList = new ArrayList<>();

        adapter = new MemoryAdapter(getContext(), finalList);
        rvCategoryMemories.setAdapter(adapter);
        rvCategoryMemories.setLayoutManager(new LinearLayoutManager(getContext()));
        queryPosts();
    }

    protected void queryPosts() {
        ParseQuery<Memory> query = ParseQuery.getQuery(Memory.class);
        query.include(Memory.KEY_USER);
        query.whereEqualTo(Memory.KEY_USER, ParseUser.getCurrentUser());
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
            tvNumPosts.setText("You do not have " + currMem + " memories :(");
        } else if (currentMemories.size() == 1) {
            tvNumPosts.setText("You have 1 " + currMem + " memory!");
        } else {
            tvNumPosts.setText("You have " + currentMemories.size() + " " + currMem + " memories!");
        }
    }

    private void setNumFriends() {
        ParseUser curr = ParseUser.getCurrentUser();
        ParseQuery<Friends> query = ParseQuery.getQuery(Friends.class);
        query.include(Friends.KEY_USER);
        query.whereEqualTo(Friends.KEY_USER, curr);
        query.findInBackground(new FindCallback<Friends>() {
            @Override
            public void done(List<Friends> friends, ParseException e) {
                Friends currFriendModel = friends.get(0);
                JSONObject friendsList = currFriendModel.getFriendsMap();
                if (friendsList == null) {
                    String sourceString = "<b>" + "Friends:" + "</b> " + "0";
                    tvNumFriends.setText(Html.fromHtml(sourceString));
                } else {
                    int numFriends = friendsList.length();
                    String sourceString = "<b>" + "Friends:" + "</b> " + Integer.toString(numFriends);
                    tvNumFriends.setText(Html.fromHtml(sourceString));
                }
            }
        });
    }
}