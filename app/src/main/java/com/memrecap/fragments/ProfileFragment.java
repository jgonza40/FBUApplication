package com.memrecap.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.memrecap.models.Memory;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "ProfileFragment";

    public static final String USER_PROFILE_PIC = "profilePicture";
    public static final String SELF_CARE = "selfCare";
    public static final String FOOD = "food";
    public static final String FAMILY = "family";
    public static final String STEPPING_STONE = "steppingStone";
    public static final String ACTIVE = "active";
    public static final String TRAVEL = "travel";
    public static final int MAX_POSTS = 20;

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
        rvCategoryMemories = view.findViewById(R.id.rvCategoryMemories);

        btnProfileFood.setOnClickListener(this);
        btnProfileSelfCare.setOnClickListener(this);
        btnProfileFamily.setOnClickListener(this);
        btnProfileTravel.setOnClickListener(this);
        btnProfileSteppingStone.setOnClickListener(this);
        btnProfileActive.setOnClickListener(this);

        resetBooleanValues();
        foodSelected = true;

        setProfileComponents(ivProfileImage, tvProfileUsername);

        recyclerViewSetup();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnProfileFood:
                resetBooleanValues();
                foodSelected = true;
                recyclerViewSetup();
                break;
            case R.id.btnProfileSelfCare:
                resetBooleanValues();
                selfCareSelected = true;
                recyclerViewSetup();
                break;
            case R.id.btnProfileFamily:
                resetBooleanValues();
                familySelected = true;
                recyclerViewSetup();
                break;
            case R.id.btnProfileTravel:
                resetBooleanValues();
                travelSelected = true;
                recyclerViewSetup();
                break;
            case R.id.btnProfileSteppingStone:
                resetBooleanValues();
                steppingStoneSelected = true;
                recyclerViewSetup();
                break;
            case R.id.btnProfileActive:
            default:
                resetBooleanValues();
                activeSelected = true;
                recyclerViewSetup();
                break;
        }
    }

    private void resetBooleanValues() {
        foodSelected = false;
        selfCareSelected = false;
        familySelected = false;
        travelSelected = false;
        steppingStoneSelected = false;
        activeSelected = false;
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

                setFinalList();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setFinalList() {
        if (foodSelected) {
            finalList.clear();
            finalList.addAll(foodMemories);
        } else if (selfCareSelected) {
            finalList.clear();
            finalList.addAll(selfCareMemories);
        } else if (familySelected) {
            finalList.clear();
            finalList.addAll(familyMemories);
        } else if (travelSelected) {
            finalList.clear();
            finalList.addAll(travelMemories);
        } else if (steppingStoneSelected) {
            finalList.clear();
            finalList.addAll(steppingStoneMemories);
        } else {
            finalList.clear();
            finalList.addAll(activeMemories);
        }
    }
}