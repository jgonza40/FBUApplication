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
    private static int TYPE_TITLE = 3;

    private static final String SELF_CARE = "selfCare";
    private static final String FOOD = "food";
    private static final String FAMILY = "family";
    private static final String STEPPING_STONE = "steppingStone";
    private static final String ACTIVE = "active";
    private static final String TRAVEL = "travel";

    private static final String SELF_CARE_TITLE = "self care";
    private static final String FOOD_TITLE = "food";
    private static final String FAMILY_TITLE = "family";
    private static final String STEPPING_STONE_TITLE = "stepping stone";
    private static final String ACTIVE_TITLE = "active";
    private static final String TRAVEL_TITLE = "travel";

    private TextView tvLocation;
    private ImageView ivLocationImg;
    private TextView tvLocationQuote;
    private TextView tvLocationName;
    private TextView tvCategoryTitle;

    public LocationRecapAdapter(Context context, int resourceId, List<Memory> memories) {
        super(context, resourceId, memories);
    }

    public View getView(int position, View item_view, ViewGroup parent){
        Memory item_memory = getItem(position);
        int type = getItemViewType(item_memory);

        if(type == TYPE_IMAGE){
            item_view = setImageLayout(item_view, item_memory, parent);
        } else if(type == TYPE_QUOTE){
            item_view = setQuoteLayout(item_view, item_memory, parent);
        } else{
            item_view = setTitleLayout(item_view, item_memory, parent);
        }

        return item_view;
    }

    private View setTitleLayout(View view, Memory memory, ViewGroup parent) {
        if(view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_title_recap, parent, false);
        }

        tvCategoryTitle = view.findViewById(R.id.tvCategoryTitle);

        tvCategoryTitle.setText(getCategoryTitle(memory.getCategory()) + " memories you have created! :)");

        return view;
    }

    private String getCategoryTitle(String category){
        String categoryTitle = "";
        if(category.equals(FOOD)){
            categoryTitle = FOOD_TITLE;
        } else if(category.equals(SELF_CARE)){
            categoryTitle = SELF_CARE_TITLE;
        } else if(category.equals(FAMILY)){
            categoryTitle = FAMILY_TITLE;
        } else if(category.equals(STEPPING_STONE)){
            categoryTitle = STEPPING_STONE_TITLE;
        } else if(category.equals(TRAVEL)){
            categoryTitle = TRAVEL_TITLE;
        } else{
            categoryTitle = ACTIVE_TITLE;
        }
        return categoryTitle;
    }

    private View setImageLayout(View view, Memory memory, ViewGroup parent){
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

    private View setQuoteLayout(View view, Memory memory, ViewGroup parent){
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
        if (memory.getImage() != null) {
            return TYPE_IMAGE;
        }
        if(memory.getQuote() != null){
            return TYPE_QUOTE;
        }
        return TYPE_TITLE;
    }
}
