package com.memrecap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseUser;

import java.util.List;

public class SearchUsersAdapter extends RecyclerView.Adapter<SearchUsersAdapter.SearchUserViewHolder> {

    public static final String TAG = "SearchUsersAdapter";

    public static final String USER_PROFILE_PIC = "profilePicture";

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
        private Button btnRequest;

        public SearchUserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSearchUserImage = itemView.findViewById(R.id.ivSearchUserImage);
            tvSearchUsername = itemView.findViewById(R.id.tvSearchUsername);
            btnRequest = itemView.findViewById(R.id.btnRequest);
        }

        public void bind(ParseUser user) {
            tvSearchUsername.setText(user.getUsername());
            Glide.with(context)
                    .load(user.getParseFile(USER_PROFILE_PIC).getUrl())
                    .into(ivSearchUserImage);
        }
    }
}
