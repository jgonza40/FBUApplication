package com.memrecap;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.memrecap.models.Memory;
import com.parse.ParseFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.ViewHolder> {

    private Context context;
    private List<Memory> memories;
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    public static final String USER_PROFILE_PIC = "profilePicture";
    public static final String TAG = "MemoryAdapter";

    public MemoryAdapter(Context context, List<Memory> memories) {
        this.context = context;
        this.memories = memories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_memory_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Memory memory = memories.get(position);
        holder.bind(memory);
    }

    @Override
    public int getItemCount() {
        return memories.size();
    }

     class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView ivImgPostImage;
        private TextView tvImgUsername;
        private ImageView ivImgProfilePic;
        private TextView tvImgCreatedAt;
        private TextView tvImgCaption;
        private TextView tvImgTripTitle;
        private TextView tvImgCategory;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImgPostImage = itemView.findViewById(R.id.ivImgPostImage);
            ivImgProfilePic = itemView.findViewById(R.id.ivImgProfilePic);
            tvImgUsername = itemView.findViewById(R.id.tvImgUsername);
            tvImgCreatedAt = itemView.findViewById(R.id.tvImgCreatedAt);
            tvImgCaption = itemView.findViewById(R.id.tvImgCaption);
            tvImgTripTitle = itemView.findViewById(R.id.tvImgTripTitle);
            tvImgCategory = itemView.findViewById(R.id.tvImgCategory);
        }

        public void bind(Memory memory) {
            tvImgUsername.setText(memory.getUser().getUsername());
            tvImgCreatedAt.setText(getRelativeTimeAgo(memory.getCreatedAt().toString()));
            tvImgCaption.setText(memory.getDescription());
            tvImgCategory.setText("category: " + memory.getCategory());
            ParseFile image = memory.getImage();
            if(image!= null){
                Glide.with(context)
                        .load(image.getUrl())
                        .into(ivImgPostImage);
            }
            Glide.with(context)
                    .load(memory.getUser().getParseFile(USER_PROFILE_PIC))
                    .into(ivImgProfilePic);
        }

        // The purpose of this method is to get appropriate time stamps
        public String getRelativeTimeAgo(String rawJsonDate) {
            String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
            SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
            sf.setLenient(true);
            try {
                long time = sf.parse(rawJsonDate).getTime();
                long now = System.currentTimeMillis();
                final long diff = now - time;
                if (diff < MINUTE_MILLIS) {
                    return "just now";
                } else if (diff < 2 * MINUTE_MILLIS) {
                    return "a minute ago";
                } else if (diff < 50 * MINUTE_MILLIS) {
                    return diff / MINUTE_MILLIS + "m" + " ago";
                } else if (diff < 90 * MINUTE_MILLIS) {
                    return "an hour ago";
                } else if (diff < 24 * HOUR_MILLIS) {
                    return diff / HOUR_MILLIS + "h" + " ago";
                } else if (diff < 48 * HOUR_MILLIS) {
                    return "yesterday";
                } else {
                    return diff / DAY_MILLIS + "d" + " ago";
                }
            } catch (ParseException e) {
                Log.i(TAG, "getRelativeTimeAgo failed");
                e.printStackTrace();
            }
            return "";
        }
    }

}
