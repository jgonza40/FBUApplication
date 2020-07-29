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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.memrecap.activities.FriendProfileActivity;
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

import java.util.List;

public class SearchUsersAdapter extends RecyclerView.Adapter<SearchUsersAdapter.SearchUserViewHolder> {

    public static final String TAG = "SearchUsersAdapter";

    private static final String USER_PROFILE_PIC = "profilePicture";
    private static final String USER_FRIENDS = "friends";
    private static final String OBJECT_ID = "objectId";
    private static final String VIEW_PROFILE = "view profile";
    private static final String FRIEND_ID = "friendID";
    private static final String PENDING_REQUESTS = "pendingRequests";
    private static final String MEM_REQUEST_TITLE = "MemRequest";
    private static final String ACCEPT_REQUEST_TITLE = "accept request";
    private static final String PENDING_REQUEST_TITLE = "pending request";
    private static final String USER = "user";

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
            JSONArray friendsArray = currentUser.getJSONArray(USER_FRIENDS);

            if (friendsArray != null) {
                if (areFriends(user, friendsArray)) {
                    setViewProfile(btnRequest, user);
                } else {
                    determineRequest(currentUser, user);
                }
            } else {
                determineRequest(currentUser, user);
            }
        }

        private void acceptRequest(final MemRequest currentRequest, final ParseUser toUser, final ParseUser currUser){
            btnRequest.setText(ACCEPT_REQUEST_TITLE);
            btnRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentRequest.setStatus(StaticVariables.STATUS_ACCEPTED);
                    int requestIndex = Integer.parseInt(currentRequest.getIndex());
                    // TODO: get the PendingRequests, and remove
                    currUser.getJSONArray(PENDING_REQUESTS).remove(requestIndex);
                    // TODO: add friends to their friends lists
                    setViewProfile(btnRequest, toUser);
                }
            });
        }

        private void determineRequest(final ParseUser currUser, final ParseUser otherUser) {
            ParseQuery<MemRequest> query = ParseQuery.getQuery(MemRequest.class);
            query.findInBackground(new FindCallback<MemRequest>() {
                @Override
                public void done(List<MemRequest> objects, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error while saving marker", e);
                    } else {
                        for (int i = 0; i < objects.size(); i++) {
                            MemRequest currMemRequest = objects.get(i);
                            ParseUser fromUser = objects.get(i).getFromUser();
                            ParseUser toUser = objects.get(i).getToUser();
                            if (fromUser.getObjectId().equals(currUser.getObjectId())
                                    && toUser.getObjectId().equals(otherUser.getObjectId())) {
                                setPendingRequest(btnRequest);
                                return;
                            } else if (fromUser.getObjectId().equals(otherUser.getObjectId())
                                    && toUser.getObjectId().equals(currUser.getObjectId())){
                                acceptRequest(currMemRequest, otherUser, currUser);
                                return;
                            }
                        }
                        btnRequest.setText(MEM_REQUEST_TITLE);
                        createRequestOnClick(btnRequest, otherUser);
                    }
                }
            });
        }

        private Boolean areFriends(ParseUser user, JSONArray friendsArray) {
            for (int i = 0; i < friendsArray.length(); i++) {
                try {
                    if (friendsArray.getJSONObject(i).getString(OBJECT_ID).equals(user.getObjectId())) {
                        return true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        private void createRequestOnClick(Button btn, final ParseUser toUser) {
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MemRequest newRequest = new MemRequest();
                    newRequest.setFromUser(ParseUser.getCurrentUser());
                    newRequest.setToUser(toUser);
                    newRequest.setStatus(StaticVariables.STATUS_PENDING);
                    newRequest.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error while saving marker", e);
                            }
                        }
                    });
                    createPendingRequest(toUser, newRequest);
                    setPendingArrayIndex(toUser, newRequest);
                    setPendingRequest(btnRequest);
                    btnRequest.setClickable(false);
                }
            });
        }

        private void setPendingArrayIndex(ParseUser toUser, final MemRequest newRequest) {
            ParseQuery<PendingRequests> query = ParseQuery.getQuery(PendingRequests.class);
            query.include(PendingRequests.KEY_USER);
            query.whereEqualTo(PendingRequests.KEY_USER, toUser);
            query.findInBackground(new FindCallback<PendingRequests>() {
                @Override
                public void done(List<PendingRequests> objects, ParseException e) {
                    newRequest.setIndex(Integer.toString(objects.size()));
                    newRequest.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error while saving marker", e);
                            }
                        }
                    });
                }
            });
        }

        private void createPendingRequest(final ParseUser user, final MemRequest memRequest) {
            ParseQuery<PendingRequests> query = ParseQuery.getQuery(PendingRequests.class);
            query.include(PendingRequests.KEY_USER);
            query.whereEqualTo(PendingRequests.KEY_USER, user);
            query.findInBackground(new FindCallback<PendingRequests>() {
                @Override
                public void done(List<PendingRequests> objects, ParseException e) {
                    if (objects.size() == 0) {
                        PendingRequests newPendingRequest = new PendingRequests();
                        newPendingRequest.setPendingRequestsUser(user);
                        newPendingRequest.add(PENDING_REQUESTS, memRequest);
                        newPendingRequest.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "Error while saving new pending request", e);
                                }
                            }
                        });
                    } else {
                        for (int i = 0; i < objects.size(); i++) {
                            if (objects.get(i).getPendingRequestsUser().getObjectId().equals(user.getObjectId())) {
                                objects.get(i).add(PENDING_REQUESTS, memRequest);
                                objects.get(i).saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e != null) {
                                            Log.e(TAG, "Error while saving pending request", e);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }

        private void setViewProfile(Button button, final ParseUser user) {
            button.setText(VIEW_PROFILE);
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
            btn.setText(PENDING_REQUEST_TITLE);
            btn.setBackgroundColor(context.getResources().getColor(R.color.red));
        }
    }
}
