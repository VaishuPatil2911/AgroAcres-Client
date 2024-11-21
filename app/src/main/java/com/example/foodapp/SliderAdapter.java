package com.example.foodapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.foodapp.Model.SliderModel;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;


public class SliderAdapter extends PagerAdapter {

    Context context;
    ArrayList<SliderModel> list;

    LayoutInflater layoutInflater;

    public SliderAdapter(Context context, ArrayList<SliderModel> list) {
        this.context = context;
        this.list = list;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;

    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        // inflating the item.xml
        View itemView = layoutInflater.inflate(R.layout.item_slider, container, false);


        SliderModel model = list.get(position);
        assert model !=null;
        // referencing the image view from the item.xml file
        RoundedImageView imageView = itemView.findViewById(R.id.sliderImage);

        try {
            Picasso.get().load(model.getLinks()).placeholder(R.color.purple_200)
                    .into(imageView);
        }catch (Exception e){
            e.printStackTrace();
        }


        // Adding the View
        Objects.requireNonNull(container).addView(itemView);

        return itemView;
    }

    public static void openCustomTab(Context activity, CustomTabsIntent customTabsIntent, Uri uri) {
        String packageName = "com.android.chrome";
        customTabsIntent.intent.setPackage(packageName);
        customTabsIntent.launchUrl(activity, uri);
    }
    @Override
    public void destroyItem(@NonNull View container, int position, @NonNull Object object) {
        ((ViewPager) container).removeView((View) object);
    }


}
