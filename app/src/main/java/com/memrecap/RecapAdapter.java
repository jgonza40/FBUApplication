package com.memrecap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.memrecap.activities.ImageMemoryDetailsActivity;
import com.memrecap.activities.LocationRecapActivity;
import com.memrecap.activities.MainActivity;
import com.memrecap.activities.ProfileRecapActivity;
import com.memrecap.models.Memory;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class RecapAdapter extends ArrayAdapter<Memory> {

    public static final String TAG = "RecapAdapter";

    private static int TYPE_IMAGE = 1;
    private static int TYPE_QUOTE = 2;
    private static int TYPE_TITLE = 3;
    private static int TYPE_LOCATION = 4;
    private static int TYPE_DONE = 5;

    private static String LOCATION = "location";
    private static String PROFILE = "profile";

    private static final String SELF_CARE = "selfCare";
    private static final String FOOD = "food";
    private static final String FAMILY = "family";
    private static final String STEPPING_STONE = "steppingStone";
    private static final String ACTIVE = "active";
    private static final String TRAVEL = "travel";

    private static final String SELF_CARE_TITLE = "self care";
    private static final String FOOD_TITLE = "food";
    private static final String FAMILY_TITLE = "family";
    private static final String STEPPING_STONE_TITLE = "milestone";
    private static final String ACTIVE_TITLE = "active";
    private static final String TRAVEL_TITLE = "travel";

    private TextView tvLocation;
    private ImageView ivLocationImg;
    private TextView tvLocationQuote;
    private TextView tvLocationName;
    private TextView tvCategoryTitle;
    private TextView tvContinueTitle;
    private Button btnHome;
    private TextView tvDoneTitle;
    private Button btnDone;
    private TextView tvImageDate;
    private TextView tvQuoteCreatedAt;

    private String type;
    private Context context;

    public RecapAdapter(Context context, int resourceId, List<Memory> memories, String type) {
        super(context, resourceId, memories);
        this.type = type;
        this.context = context;
    }

    public View getView(int position, View itemView, ViewGroup parent) {
        Memory item_memory = getItem(position);
        int type = getItemViewType(item_memory);

        if (type == TYPE_IMAGE) {
            itemView = setImageLayout(itemView, item_memory, parent);
        } else if (type == TYPE_QUOTE) {
            itemView = setQuoteLayout(itemView, item_memory, parent);
        } else if (type == TYPE_TITLE) {
            itemView = setTitleLayout(itemView, item_memory, parent);
        } else if (type == TYPE_LOCATION) {
            itemView = setLocationLayout(itemView, item_memory, parent);
        } else {
            itemView = setDoneLayout(itemView, item_memory, parent);
        }
        return itemView;
    }

    private View setTitleLayout(View view, Memory memory, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_title_recap, parent, false);
        }

        tvCategoryTitle = view.findViewById(R.id.tvCategoryTitle);
        String categoryTitle = "Swipe to view " + "<b>" + getCategoryTitle(memory.getCategory()) + "</b> " + " memories you have created!";
        tvCategoryTitle.setText(Html.fromHtml(categoryTitle));

        return view;
    }

    private String getCategoryTitle(String category) {
        String categoryTitle = "";
        if (category.equals(FOOD)) {
            categoryTitle = FOOD_TITLE;
        } else if (category.equals(SELF_CARE)) {
            categoryTitle = SELF_CARE_TITLE;
        } else if (category.equals(FAMILY)) {
            categoryTitle = FAMILY_TITLE;
        } else if (category.equals(STEPPING_STONE)) {
            categoryTitle = STEPPING_STONE_TITLE;
        } else if (category.equals(TRAVEL)) {
            categoryTitle = TRAVEL_TITLE;
        } else {
            categoryTitle = ACTIVE_TITLE;
        }
        return categoryTitle;
    }

    private View setImageLayout(View view, Memory memory, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_image_recap, parent, false);
        }

        tvLocation = view.findViewById(R.id.tvLocation);
        ivLocationImg = view.findViewById(R.id.ivLocationImg);
        tvImageDate = view.findViewById(R.id.tvImageDate);

        tvLocation.setText(memory.getMemoryTitle());
        String rawJsonDate = memory.getCreatedAt().toString();
        String[] parts = rawJsonDate.split(" ");
        tvImageDate.setText(parts[2] + " " + parts[1] + " " + parts[5]);
        ParseFile image = memory.getImage();
        if (image != null) {
            Glide.with(getContext())
                    .load(image.getUrl())
                    .into(ivLocationImg);
        }
        return view;
    }

    private View setQuoteLayout(View view, Memory memory, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_quote_recap, parent, false);
        }

        tvLocationQuote = view.findViewById(R.id.tvLocationQuote);
        tvLocationName = view.findViewById(R.id.tvLocationName);
        tvQuoteCreatedAt = view.findViewById(R.id.tvQuoteCreatedAt);

        tvLocationQuote.setText(" \" " + memory.getQuote() + " \" ");
        tvLocationName.setText(memory.getMemoryTitle());
        String rawJsonDate = memory.getCreatedAt().toString();
        String[] parts = rawJsonDate.split(" ");
        tvQuoteCreatedAt.setText(parts[2] + " " + parts[1] + " " + parts[5]);

        return view;
    }

    private View setLocationLayout(View view, Memory memory, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_continue_location_recap, parent, false);
        }

        tvContinueTitle = view.findViewById(R.id.tvContinueTitle);
        btnHome = view.findViewById(R.id.btnHome);

        tvContinueTitle.setText("swipe to view " + memory.getMemoryTitle() + " memories!");
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                getContext().startActivity(intent);
            }
        });
        return view;
    }

    private View setDoneLayout(View view, Memory memory, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_recap_done, parent, false);
        }

        tvDoneTitle = view.findViewById(R.id.tvDoneTitle);
        btnDone = view.findViewById(R.id.btnDone);

        if (type.equals(LOCATION)) {
            btnDone.setText("Go Map");
        } else {
            btnDone.setText("Go Profile");
        }

        tvDoneTitle.setText("MemRecap Complete!");
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Activity) context).finish();
            }
        });
        return view;
    }

    public int getItemViewType(Memory memory) {
        if (memory.getDone()) {
            return TYPE_DONE;
        }

        if (memory.getImage() != null) {
            return TYPE_IMAGE;
        }

        if (memory.getQuote() != null) {
            return TYPE_QUOTE;
        }

        if (type.equals(LOCATION)) {
            return TYPE_LOCATION;
        } else {
            return TYPE_TITLE;
        }
    }
}
