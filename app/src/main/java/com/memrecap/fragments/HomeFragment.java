package com.memrecap.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.memrecap.MemoryImageAdapter;
import com.memrecap.models.Memory;
import com.memrecap.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    public static final int MAX_POSTS = 20;
    private RecyclerView rvPosts;
    protected MemoryImageAdapter imgAdapter;
    protected List<Memory> imgMemories;
    private SwipeRefreshLayout swipeContainer;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvPosts = view.findViewById(R.id.rvPosts);
        imgMemories = new ArrayList<>();
        imgAdapter = new MemoryImageAdapter(getContext(), imgMemories);
        rvPosts.setAdapter(imgAdapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        queryPosts();
    }

    // Getting posts from the Parse backend
    protected void queryPosts() {
        // Specify which class to query
        ParseQuery<Memory> query = ParseQuery.getQuery(Memory.class);
        query.include(Memory.KEY_USER);
        query.setLimit(MAX_POSTS);
        query.addDescendingOrder(Memory.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Memory>() {
            @Override
            public void done(List<Memory> memories, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }
                List<Memory> imageMems = new ArrayList<Memory>();
                List<Memory> quoteMems = new ArrayList<Memory>();
                for (Memory memory : memories) {
                    if(memory.getImage() != null){
                        imageMems.add(memory);
                    } else{
                        quoteMems.add(memory);
                    }
                    // Will add all memories in parse to display in all
                }
                imgMemories.addAll(imageMems);
                imgAdapter.notifyDataSetChanged();
//                quoteMemories.addAll(quoteMems);
//                quoteAdapter.notifyDataSetChanged();
            }
        });
    }
}