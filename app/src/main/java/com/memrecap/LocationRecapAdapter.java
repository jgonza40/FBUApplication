package com.memrecap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.memrecap.models.Memory;
import com.parse.ParseFile;

import java.util.List;


public class LocationRecapAdapter extends ArrayAdapter<Memory> {

    public static final String TAG = "LocationRecapAdapter";

    private static int TYPE_IMAGE = 1;
    private static int TYPE_QUOTE = 2;

    private TextView tvLocation;
    private ImageView ivLocationImg;
    private TextView tvLocationQuote;
    private TextView tvLocationName;

    public LocationRecapAdapter(Context context, int resourceId, List<Memory> memories) {
        super(context, resourceId, memories);
    }

    public View getView(int position, View item_view, ViewGroup parent){
        Memory item_memory = getItem(position);
        int type = getItemViewType(item_memory);

        if(type == TYPE_IMAGE){
            item_view = setImageLayout(item_view, item_memory, parent);
        } else{
            item_view = setQuoteLayout(item_view, item_memory, parent);
        }

        return item_view;
    }

    public View setImageLayout(View view, Memory memory, ViewGroup parent){
        if(view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_image_recap, parent, false);
        }

        tvLocation = view.findViewById(R.id.tvLocation);
        ivLocationImg = view.findViewById(R.id.ivLocationImg);

        tvLocation.setText(memory.getMemoryTitle());
        ParseFile image = memory.getImage();
        if (image != null) {
            Glide.with(getContext())
                    .load(image.getUrl())
                    .into(ivLocationImg);
        }

        return view;
    }

    public View setQuoteLayout(View view, Memory memory, ViewGroup parent){
        if(view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_quote_recap, parent, false);
        }

        tvLocationQuote = view.findViewById(R.id.tvLocationQuote);
        tvLocationName = view.findViewById(R.id.tvLocationName);

        tvLocationQuote.setText(memory.getQuote());
        tvLocationName.setText(memory.getMemoryTitle());

        return view;
    }

    public int getItemViewType(Memory memory) {
        if (memory.getImage() == null) {
            return TYPE_QUOTE;
        } else {
            return TYPE_IMAGE;
        }
    }
}
