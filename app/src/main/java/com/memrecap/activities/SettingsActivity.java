package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.memrecap.R;
import com.memrecap.SettingsAdapter;
import com.memrecap.StaticVariables;
import com.memrecap.models.Friends;
import com.memrecap.models.MemRequest;
import com.memrecap.models.PendingRequests;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    public static final String TAG = "SettingsActivity";

    private Button btnLogout;
    private RecyclerView rvPendingRequests;
    private TextView tvPendingRequests;
    private SettingsAdapter adapter;

    private List<MemRequest> allPendingRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        btnLogout = findViewById(R.id.btnLogout);
        rvPendingRequests = findViewById(R.id.rvPendingRequests);
        tvPendingRequests = findViewById(R.id.tvPendingRequests);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
                goLoginActivity();
            }

        });

        allPendingRequests = new ArrayList<>();
        adapter = new SettingsAdapter(getApplicationContext(), allPendingRequests);
        rvPendingRequests.setAdapter(adapter);
        rvPendingRequests.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        setPendingRequests();
    }

    private void logoutUser() {
        ParseUser.logOut();
    }

    private void goLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    private void setPendingRequests() {
        final ParseUser currUser = ParseUser.getCurrentUser();
        ParseQuery<PendingRequests> query = ParseQuery.getQuery(PendingRequests.class);
        query.include(PendingRequests.KEY_USER);
        query.whereEqualTo(PendingRequests.KEY_USER, currUser);
        query.findInBackground(new FindCallback<PendingRequests>() {
            @Override
            public void done(List<PendingRequests> requests, ParseException e) {
                int numRequests = 0;
                if (e != null) {
                    Toast.makeText(getApplicationContext(), "Query Not Successful", Toast.LENGTH_LONG).show();
                } else {
                    PendingRequests requestModel = requests.get(0);
                    JSONObject requestsMap = requestModel.getPendingRequestsMap();
                    if (requestsMap != null) {
                        Iterator<String> iterator = requestsMap.keys();
                        while (iterator.hasNext()) {
                            String currKey = iterator.next();
                            String memRequestId = requestsMap.optString(currKey);
                            ParseQuery<MemRequest> query = ParseQuery.getQuery(MemRequest.class);
                            try {
                                MemRequest currMemRequest = query.get(memRequestId);
                                if (currMemRequest.getStatus().equals(StaticVariables.STATUS_PENDING)) {
                                    allPendingRequests.add(currMemRequest);
                                    numRequests++;
                                }
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
                if(numRequests == 0){
                    tvPendingRequests.setText("You do not have pending friend requests");
                } else if(numRequests == 1){
                    tvPendingRequests.setText("You have " + numRequests + " pending friend request!");
                } else {
                    tvPendingRequests.setText("You have " + numRequests + " pending friend requests!");
                }
            }
        });
    }

    public void goBackToMain(View view) {
        this.finish();
    }
}