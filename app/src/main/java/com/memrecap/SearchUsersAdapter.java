package com.memrecap;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.memrecap.activities.FriendProfileActivity;
import com.memrecap.models.Friends;
import com.memrecap.models.MarkerPoint;
import com.memrecap.models.MemRequest;
import com.memrecap.models.Memory;
import com.memrecap.models.PendingRequests;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SearchUsersAdapter extends RecyclerView.Adapter<SearchUsersAdapter.SearchUserViewHolder> {

    public static final String TAG = "SearchUsersAdapter";

    private static final String USER_PROFILE_PIC = "profilePicture";
    private static final String USER_FRIENDS = "friends";
    private static final String OBJECT_ID = "objectId";
    private static final String FRIEND_ID = "friendID";
    private static final String PENDING_REQUESTS = "pendingRequests";
    private static final String PENDING_REQUESTS_MAP = "pendingRequestsMap";
    private static final String FRIENDS_MAP = "friendsMap";
    private static final String FRIENDS = "friends";
    private static final String USER = "user";

    private static final String MEM_REQUEST_TITLE = "Mem-Request";
    private static final String ACCEPT_REQUEST_TITLE = "Accept";
    private static final String PENDING_REQUEST_TITLE = "Pending";
    private static final String VIEW_PROFILE_TITLE = "View Profile";

    private Context context;
    private List<ParseUser> users;

    public SearchUsersAdapter(Context context, List<ParseUser> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public SearchUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_user, parent, false);
        return new SearchUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchUserViewHolder holder, int position) {
        ParseUser user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class SearchUserViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivSearchUserImage;
        private TextView tvSearchUsername;
        public Button btnRequest;

        public SearchUserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSearchUserImage = itemView.findViewById(R.id.ivSearchUserImage);
            tvSearchUsername = itemView.findViewById(R.id.tvSearchUsername);
            btnRequest = itemView.findViewById(R.id.btnRequest);
        }

        public void bind(final ParseUser user) {
            tvSearchUsername.setText(user.getUsername());
            Glide.with(context)
                    .load(user.getParseFile(USER_PROFILE_PIC).getUrl())
                    .into(ivSearchUserImage);

            final ParseUser currentUser = ParseUser.getCurrentUser();
            btnRequest.setVisibility(View.INVISIBLE);
            setButtonRequests(currentUser, user);
        }

        private void setButtonRequests(final ParseUser currUser, final ParseUser searchUser) {
            ParseQuery<Friends> query = ParseQuery.getQuery(Friends.class);
            query.include(Friends.KEY_USER);
            query.whereEqualTo(Friends.KEY_USER, currUser);
            query.findInBackground(new FindCallback<Friends>() {
                @Override
                public void done(List<Friends> friends, ParseException e) {
                    Friends currFriendModel = friends.get(0);
                    JSONObject map = currFriendModel.getFriendsMap();
                    if (map != null) {
                        if (map.has(searchUser.getObjectId())) {
                            setViewProfile(btnRequest, searchUser);
                        } else {
                            determineRequest(currUser, searchUser);
                        }
                    } else {
                        determineRequest(currUser, searchUser);
                    }
                }
            });
        }

        private void determineRequest(final ParseUser currUser, final ParseUser searchUser) {
            determineIfAcceptRequest(currUser, searchUser);
            determineIfPendingRequest(currUser, searchUser);

            btnRequest.setVisibility(View.VISIBLE);
            btnRequest.setText(MEM_REQUEST_TITLE);
            createRequestOnClick(btnRequest, searchUser);
        }

        private void determineIfAcceptRequest(final ParseUser currUser, final ParseUser searchUser){
            ParseQuery<PendingRequests> queryPending = ParseQuery.getQuery(PendingRequests.class);
            queryPending.include(PendingRequests.KEY_USER);
            queryPending.whereEqualTo(PendingRequests.KEY_USER, currUser);
            queryPending.findInBackground(new FindCallback<PendingRequests>() {
                @Override
                public void done(List<PendingRequests> requests, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error getting pending request for determineRequest", e);
                    } else {
                        PendingRequests requestModel = requests.get(0);
                        JSONObject currUserRequests = requestModel.getPendingRequestsMap();
                        if (currUserRequests != null) {
                            if (currUserRequests.has(searchUser.getObjectId())) {
                                try {
                                    String memRequestId = currUserRequests.optString(searchUser.getObjectId());
                                    ParseQuery<MemRequest> query = ParseQuery.getQuery(MemRequest.class);
                                    MemRequest memRequest = query.get(memRequestId);
                                    if (memRequest.getStatus().equals(StaticVariables.STATUS_PENDING)) {
                                        setAcceptRequest(memRequest, searchUser, currUser);
                                    }
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
        }

        private void determineIfPendingRequest(final ParseUser currUser, final ParseUser searchUser){
            ParseQuery<PendingRequests> queryAccept = ParseQuery.getQuery(PendingRequests.class);
            queryAccept.include(PendingRequests.KEY_USER);
            queryAccept.whereEqualTo(PendingRequests.KEY_USER, searchUser);
            queryAccept.findInBackground(new FindCallback<PendingRequests>() {
                @Override
                public void done(List<PendingRequests> requests, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error getting pending request for determineRequest", e);
                    } else {
                        if(requests.size() != 0) {
                            PendingRequests requestModel = requests.get(0);
                            JSONObject searchUserRequests = requestModel.getPendingRequestsMap();
                            if (searchUserRequests != null) {
                                if (searchUserRequests.has(currUser.getObjectId())) {
                                    try {
                                        String memRequestId = searchUserRequests.optString(currUser.getObjectId());
                                        ParseQuery<MemRequest> query = ParseQuery.getQuery(MemRequest.class);
                                        MemRequest memRequest = query.get(memRequestId);
                                        if (memRequest.getStatus().equals(StaticVariables.STATUS_PENDING)) {
                                            setPendingRequest(btnRequest);
                                        }
                                    } catch (ParseException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

        private void setAcceptRequest(final MemRequest currentRequest, final ParseUser searchUser, final ParseUser currUser) {
            btnRequest.setVisibility(View.VISIBLE);
            btnRequest.setText(ACCEPT_REQUEST_TITLE);
            btnRequest.setBackground(ContextCompat.getDrawable(context, R.drawable.pending_button));
            btnRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentRequest.setStatus(StaticVariables.STATUS_ACCEPTED);
                    currentRequest.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error while saving status", e);
                            }
                        }
                    });
                    addFriends(currUser, searchUser);
                    addFriends(searchUser, currUser);
                    setViewProfile(btnRequest, searchUser);
                }
            });
        }

        private void addFriends(final ParseUser user, final ParseUser friend) {
            ParseQuery<Friends> query = ParseQuery.getQuery(Friends.class);
            query.include(Friends.KEY_USER);
            query.whereEqualTo(Friends.KEY_USER, user);
            query.findInBackground(new FindCallback<Friends>() {
                @Override
                public void done(List<Friends> friends, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error while getting friends for addFriends", e);
                    }
                    Friends friendModel = friends.get(0);
                    JSONObject map = friendModel.getFriendsMap();
                    if (map == null) {
                        map = new JSONObject();
                        friendModel.put(FRIENDS_MAP, map);
                        friendModel.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                Log.e(TAG, "Error while saving addFriends", e);
                            }
                        });
                    }
                    try {
                        map.put(friend.getObjectId(), friend.getObjectId());
                        friendModel.put(FRIENDS_MAP, map);
                        friendModel.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                Log.e(TAG, "Error while saving addFriends", e);
                            }
                        });
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        private void createRequestOnClick(Button btn, final ParseUser searchUser) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MemRequest newRequest = new MemRequest();
                    newRequest.setFromUser(ParseUser.getCurrentUser());
                    newRequest.setToUser(searchUser);
                    newRequest.setStatus(StaticVariables.STATUS_PENDING);
                    newRequest.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error while saving new request", e);
                            }
                        }
                    });
                    createPendingRequest(searchUser, newRequest);
                    setPendingRequest(btnRequest);
                    btnRequest.setClickable(false);
                }
            });
        }

        private void createPendingRequest(final ParseUser searchUser, final MemRequest memRequest) {
            ParseQuery<PendingRequests> query = ParseQuery.getQuery(PendingRequests.class);
            query.include(PendingRequests.KEY_USER);
            query.whereEqualTo(PendingRequests.KEY_USER, searchUser);
            query.findInBackground(new FindCallback<PendingRequests>() {
                @Override
                public void done(List<PendingRequests> requests, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error while getting requests in createPendingRequest", e);
                    } else {
                        PendingRequests requestModel = requests.get(0);
                        JSONObject requestsMap = requestModel.getPendingRequestsMap();
                        if (requestsMap == null) {
                            requestsMap = new JSONObject();
                            requestModel.put(PENDING_REQUESTS_MAP, requestsMap);
                            requestModel.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Log.e(TAG, "Error while creating addFriendsMap object", e);
                                    }
                                }
                            });
                        }
                        try {
                            requestsMap.put(memRequest.getFromUser().getObjectId(), memRequest.getObjectId());
                            requestModel.put(PENDING_REQUESTS_MAP, requestsMap);
                            requestModel.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Log.e(TAG, "Error while saving addFriendsMap", e);
                                    }
                                }
                            });
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
        }

        private void setViewProfile(Button button, final ParseUser user) {
            btnRequest.setVisibility(View.VISIBLE);
            button.setText(VIEW_PROFILE_TITLE);
            btnRequest.setBackground(ContextCompat.getDrawable(context, R.drawable.profile_recap_button));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, FriendProfileActivity.class);
                    i.putExtra(FRIEND_ID, user.getObjectId());
                    context.startActivity(i);
                }
            });
        }

        private void setPendingRequest(Button btn) {
            btnRequest.setVisibility(View.VISIBLE);
            btn.setText(PENDING_REQUEST_TITLE);
            btnRequest.setBackground(ContextCompat.getDrawable(context, R.drawable.pending_button));
        }
    }
}
