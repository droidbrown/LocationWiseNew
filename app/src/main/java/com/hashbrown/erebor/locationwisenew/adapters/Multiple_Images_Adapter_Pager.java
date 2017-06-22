package com.hashbrown.erebor.locationwisenew.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hashbrown.erebor.locationwisenew.R;
import com.hashbrown.erebor.locationwisenew.utils.AppUtils;
import com.pixplicity.easyprefs.library.Prefs;


import java.io.File;
import java.util.ArrayList;

/**
 * Created by Erebor on 01/06/17.
 */

public class Multiple_Images_Adapter_Pager extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;

    public  ArrayList<String> multiple_images=new ArrayList<>();
    public Multiple_Images_Adapter_Pager(Context context, ArrayList<String> multiple_images) {
        this.multiple_images=multiple_images;
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return multiple_images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((FrameLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_image, container, false);
        itemView.setTag(position);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
        //Uri.fromFile(new File(multiple_images.get(position)))

//        Picasso.with(mContext)
//                .load(Uri.fromFile(new File(multiple_images.get(position))))
//                .fit()
//                .centerInside()
//                .into(imageView, new Callback() {
//                    @Override
//                    public void onSuccess() {
//                        Log.i("PREVIEW", "Picasso Success Loading Thumbnail - " + multiple_images.get(position));
//                    }
//
//                    @Override
//                    public void onError() {
//                        Log.i("PREVIEW", "Picasso Error Loading Thumbnail Small - " + multiple_images.get(position));
//                    }
//                });
        Glide.with(mContext).load(Uri.fromFile(new File(multiple_images.get(position)))).dontAnimate().into(imageView);
        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }
}
