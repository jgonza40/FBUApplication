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

public class MemoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Memory> memories;
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static int TYPE_IMAGE = 1;
    private static int TYPE_QUOTE = 2;
    public static final String USER_PROFILE_PIC = "profilePicture";
    public static final String TAG = "MemoryAdapter";

    public MemoryAdapter(Context context, List<Memory> memories) {
        this.context = context;
        this.memories = memories;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_IMAGE) { // for call layout
            view = LayoutInflater.from(context).inflate(R.layout.image_memory_post, parent, false);
            return new ImageViewHolder(view);

        } else { // for email layout
            view = LayoutInflater.from(context).inflate(R.layout.quote_memory_post, parent, false);
            return new QuoteViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_IMAGE) {
            ((ImageViewHolder) holder).setImageDetails(memories.get(position));
        } else {
            ((QuoteViewHolder) holder).setQuoteDetails(memories.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return memories.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (memories.get(position).getImage() == null) {
            return TYPE_QUOTE;

        } else {
            return TYPE_IMAGE;
        }
    }

    // Need two viewholder classes because they each have different layouts
    class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivImgPostImage;
        private ImageView ivImgLocation;
        private ImageView ivImgProfilePic;
        private TextView tvImgUsername;
        private TextView tvImgCreatedAt;
        private TextView tvImgCaption;
        private TextView tvImgTripTitle;
        private TextView tvImgCategory;


        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImgPostImage = itemView.findViewById(R.id.ivImgPostImage);
            ivImgProfilePic = itemView.findViewById(R.id.ivImgProfilePic);
            ivImgLocation = itemView.findViewById(R.id.ivImgLocation);
            tvImgUsername = itemView.findViewById(R.id.tvImgUsername);
            tvImgCreatedAt = itemView.findViewById(R.id.tvImgCreatedAt);
            tvImgCaption = itemView.findViewById(R.id.tvImgCaption);
            tvImgTripTitle = itemView.findViewById(R.id.tvImgTripTitle);
            tvImgCategory = itemView.findViewById(R.id.tvQuoteCategory);
        }

        public void setImageDetails(Memory memory) {
            tvImgUsername.setText(memory.getUser().getUsername());
            tvImgCreatedAt.setText(getRelativeTimeAgo(memory.getCreatedAt().toString()));
            tvImgCaption.setText(memory.getDescription());
            tvImgCategory.setText("category: " + memory.getCategory());
            ParseFile image = memory.getImage();
            if (image != null) {
                Glide.with(context)
                        .load(image.getUrl())
                        .into(ivImgPostImage);
            }
            Glide.with(context)
                    .load(memory.getUser().getParseFile(USER_PROFILE_PIC))
                    .into(ivImgProfilePic);
            Glide.with(context)
                    .load(context.getResources().getDrawable(R.drawable.ic_location))
                    .placeholder(R.mipmap.ic_location)
                    .into(ivImgLocation);
        }
    }

    // Second viewholder class (quote posts)
    class QuoteViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivQuoteProfilePic;
        private ImageView ivQuoteLocation;
        private TextView tvQuoteUsername;
        private TextView tvQuoteCreatedAt;
        private TextView tvQuoteTripTitle;
        private TextView tvQuoteCategory;
        private TextView tvQuote;

        QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivQuoteProfilePic = itemView.findViewById(R.id.ivQuoteProfilePic);
            ivQuoteLocation = itemView.findViewById(R.id.ivQuoteLocation);
            tvQuoteUsername = itemView.findViewById(R.id.tvQuoteUsername);
            tvQuoteCreatedAt = itemView.findViewById(R.id.tvQuoteCreatedAt);
            tvQuoteTripTitle = itemView.findViewById(R.id.tvQuoteTripTitle);
            tvQuoteCategory = itemView.findViewById(R.id.tvQuoteCategory);
            tvQuote = itemView.findViewById(R.id.tvQuote);
        }

        public void setQuoteDetails(Memory memory) {
            tvQuoteUsername.setText(memory.getUser().getUsername());
            tvQuoteCreatedAt.setText(getRelativeTimeAgo(memory.getCreatedAt().toString()));
            tvQuoteCategory.setText("category: " + memory.getCategory());
            tvQuote.setText(memory.getQuote());
            Glide.with(context)
                    .load(memory.getUser().getParseFile(USER_PROFILE_PIC))
                    .into(ivQuoteProfilePic);
            Glide.with(context)
                    .load(context.getResources().getDrawable(R.drawable.ic_location))
                    .placeholder(R.mipmap.ic_location)
                    .into(ivQuoteLocation);
        }
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
