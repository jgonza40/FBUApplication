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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.memrecap.R;
import com.memrecap.SearchUsersAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    public static final String TAG = "SearchFragment";

    private EditText etSearchUsername;
    private ImageButton btnSearch;
    private RecyclerView rvUsers;
    private SearchUsersAdapter adapter;

    private List<ParseUser> allUsers;

    public SearchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etSearchUsername = view.findViewById(R.id.etSearchUsername);
        rvUsers = view.findViewById(R.id.rvUsers);
        btnSearch = view.findViewById(R.id.btnSearch);
        
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etSearchUsername.getText().toString();
                allUsers = new ArrayList<>();
                adapter = new SearchUsersAdapter(getContext(), allUsers);
                rvUsers.setAdapter(adapter);
                rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
                searchUser(username);
            }
        });
    }

    private void searchUser(String username) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereMatches("username", username);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    allUsers.addAll(users);
                    Log.i(TAG, users.toString());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(),"Query Not Successful",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}