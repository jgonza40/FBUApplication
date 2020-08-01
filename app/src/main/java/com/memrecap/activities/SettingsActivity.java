package com.memrecap.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    public static final String TAG = "SettingsActivity";

    public static final String PENDING_REQUESTS_ARRAY = "pendingRequests";
    private static final String OBJECT_ID = "objectId";

    private Button btnLogout;
    private RecyclerView rvPendingRequests;
    private SettingsAdapter adapter;

    private List<MemRequest> allPendingRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        btnLogout = findViewById(R.id.btnLogout);
        rvPendingRequests = findViewById(R.id.rvPendingRequests);

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
            public void done(List<PendingRequests> pendingRequests, ParseException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(), "Query Not Successful", Toast.LENGTH_LONG).show();
                } else {
                    if(pendingRequests.size() != 0){
                        PendingRequests currUserPendingRequests = pendingRequests.get(0);
                        JSONArray requestsList = currUserPendingRequests.getJSONArray(PENDING_REQUESTS_ARRAY);
                        for(int i = 0; i < requestsList.length(); i++){
                            try {
                                String memRequestId = requestsList.getJSONObject(i).getString(OBJECT_ID);
                                ParseQuery<MemRequest> friendQuery = ParseQuery.getQuery(MemRequest.class);
                                MemRequest currMemRequest = friendQuery.get(memRequestId);
                                if(currMemRequest.getStatus().equals(StaticVariables.STATUS_PENDING)){
                                    allPendingRequests.add(currMemRequest);
                                }
                            } catch (JSONException | ParseException ex) {
                                ex.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }
}