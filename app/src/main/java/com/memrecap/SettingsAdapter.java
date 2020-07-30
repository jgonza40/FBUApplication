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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.memrecap.activities.FriendProfileActivity;
import com.memrecap.models.Friends;
import com.memrecap.models.MemRequest;
import com.memrecap.models.PendingRequests;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    public static final String TAG = "SettingsAdapter";

    private static final String USER_PROFILE_PIC = "profilePicture";
    private static final String ACCEPT_REQUEST_TITLE = "accept";
    private static final String FRIENDS = "friends";
    private static final String VIEW_PROFILE = "view profile";
    private static final String FRIEND_ID = "friendID";
    private static final String OBJECT_ID = "objectId";

    private Context context;
    private List<MemRequest> requests;

    public SettingsAdapter(Context context, List<MemRequest> requests) {
        this.context = context;
        this.requests = requests;
    }

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_user, parent, false);
        return new SettingsAdapter.SettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, int position) {
        MemRequest user = requests.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class SettingsViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivSearchUserImage;
        private TextView tvSearchUsername;
        public Button btnRequest;

        public SettingsViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSearchUserImage = itemView.findViewById(R.id.ivSearchUserImage);
            tvSearchUsername = itemView.findViewById(R.id.tvSearchUsername);
            btnRequest = itemView.findViewById(R.id.btnRequest);
        }

        public void bind(final MemRequest request) {
            try {
                final ParseUser fromUser = request.getFromUser().fetchIfNeeded();
                final ParseUser currUser = request.getToUser().fetchIfNeeded();
                tvSearchUsername.setText(fromUser.getUsername());
                Glide.with(context)
                        .load(fromUser.getParseFile(USER_PROFILE_PIC).getUrl())
                        .into(ivSearchUserImage);
                btnRequest.setText(ACCEPT_REQUEST_TITLE);
                btnRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        request.setStatus(StaticVariables.STATUS_ACCEPTED);
                        request.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "Error while saving status", e);
                                }
                            }
                        });
                        addFriends(currUser, fromUser);
                        addFriends(fromUser, currUser);
                        setViewProfile(btnRequest, fromUser);
                    }
                });
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        private void addFriends(final ParseUser user, final ParseUser friend) {
            ParseQuery<Friends> query = ParseQuery.getQuery(Friends.class);
            query.include(Friends.KEY_USER);
            query.whereEqualTo(Friends.KEY_USER, user);
            query.findInBackground(new FindCallback<Friends>() {
                @Override
                public void done(List<Friends> objects, ParseException e) {
                    if (objects.size() == 0) {
                        Friends friends = new Friends();
                        friends.setFriendsUser(user);
                        friends.add(FRIENDS, friend);
                        friends.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "Error while saving new pending request", e);
                                }
                            }
                        });
                    } else {
                        Friends currUserFriendModel = objects.get(0);
                        currUserFriendModel.add(FRIENDS, friend);
                        currUserFriendModel.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "Error while saving pending request", e);
                                }
                            }
                        });
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
    }
}
