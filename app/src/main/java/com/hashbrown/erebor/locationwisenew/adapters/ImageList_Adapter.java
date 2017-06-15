package com.hashbrown.erebor.locationwisenew.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.drivemode.android.typeface.TypefaceHelper;
import com.etsy.android.grid.util.DynamicHeightImageView;
import com.hashbrown.erebor.locationwisenew.R;

import com.hashbrown.erebor.locationwisenew.database.db_locationwise;
import com.hashbrown.erebor.locationwisenew.listeners.OnSavedClick;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Erebor on 16/05/17.
 */

public class ImageList_Adapter extends RecyclerView.Adapter<ImageList_Adapter.ViewHolder> {
    Context context;
    LayoutInflater inflater;
    ArrayList<String> arrayList;
    db_locationwise db_locationwise;
    OnSavedClick onSavedClick;
    public ImageList_Adapter(Context context, ArrayList<String> arrayList, OnSavedClick onSavedClick) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        //this.listRecipeCategoryWithRecipe = listRecipeCategoryWithRecipe;
        this.arrayList = arrayList;
        db_locationwise=new db_locationwise(context);
        this.onSavedClick=onSavedClick;
    }

    @Override
    public ImageList_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = inflater.inflate(R.layout.activity_saved_imags_row, parent, false);
        ImageList_Adapter.ViewHolder viewHolder = new ImageList_Adapter.ViewHolder(v);
        return viewHolder;


    }

    @Override
    public void onBindViewHolder(final ImageList_Adapter.ViewHolder holder, final int position)
    {


        TypefaceHelper.getInstance().setTypeface(holder.address,context.getString(R.string.light));
        TypefaceHelper.getInstance().setTypeface(holder.tap,context.getString(R.string.book));
        Picasso.with(context)
                .load(Uri.fromFile(new File(arrayList.get(position))))
                .fit()
                .centerInside()
                .into(holder.saved_image, new Callback() {
                    @Override
                    public void onSuccess()
                    {
                        //Log.i("PREVIEW", "Picasso Success Loading Thumbnail - " + arrayList.get(position));
                    }

                    @Override
                    public void onError()
                    {
                      //  Log.i("PREVIEW", "Picasso Error Loading Thumbnail Small - " + arrayList.get(position));
                    }
                });

               String loc=arrayList.get(position).substring(arrayList.get(position).lastIndexOf("/") + 1).trim();
             //  System.out.println("loc   "+loc);
               loc="sdcard/LocationWise/LocationWiseImages/"+loc;
        try
        {
            holder.address.setText(db_locationwise.getAddress(loc));
        }
         catch (Exception e)
         {

         }
               holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               onSavedClick.openBottomSheet(arrayList.get(position),position);
               }
        });
        final String finalLoc = loc;
        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSavedClick.onImageZoom(holder.saved_image, finalLoc);
            }
        });

    }


    @Override
    public int getItemCount() {


        return arrayList.size();

    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.saved_image)
        ImageView saved_image;
        @BindView(R.id.menu)
         ImageView menu;
        @BindView(R.id.address)
        TextView address;
        @BindView(R.id.tap)
         TextView tap;
        View v;
        public ViewHolder(View itemView) {
            super(itemView);//textview=(TextView) itemView.findViewById(R.id.category_name);
            v=itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}


