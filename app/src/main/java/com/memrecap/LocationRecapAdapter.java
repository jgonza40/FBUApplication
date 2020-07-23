package com.memrecap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.memrecap.models.Memory;
import com.parse.ParseFile;

import java.util.List;


public class LocationRecapAdapter extends ArrayAdapter<Memory> {

    public static final String TAG = "LocationRecapAdapter";

    private static int TYPE_IMAGE = 1;
    private static int TYPE_QUOTE = 2;

    private TextView tvLocation;
    private ImageView ivLocationImg;

    public LocationRecapAdapter(Context context, int resourceId, List<Memory> memories) {
        super(context, resourceId, memories);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        Memory item_memory = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_location_recap, parent, false);
        }

        tvLocation = convertView.findViewById(R.id.tvLocation);
        ivLocationImg = convertView.findViewById(R.id.ivLocationImg);

        tvLocation.setText(item_memory.getMemoryTitle());
        ParseFile image = item_memory.getImage();
        if (image != null) {
            Glide.with(getContext())
                    .load(image.getUrl())
                    .into(ivLocationImg);
        }
        return convertView;
    }

    public int getItemViewType(Memory memory) {
        if (memory.getImage() == null) {
            return TYPE_QUOTE;
        } else {
            return TYPE_IMAGE;
        }
    }

}
