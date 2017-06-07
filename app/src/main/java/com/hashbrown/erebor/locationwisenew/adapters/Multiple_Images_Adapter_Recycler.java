package com.hashbrown.erebor.locationwisenew.adapters;

/**
 * Created by Erebor on 01/06/17.
 */

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.drivemode.android.typeface.TypefaceHelper;
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

public class Multiple_Images_Adapter_Recycler extends RecyclerView.Adapter<Multiple_Images_Adapter_Recycler.ViewHolder> {
    Context context;
    LayoutInflater inflater;
    ArrayList<String> arrayList;


    public Multiple_Images_Adapter_Recycler(Context context, ArrayList<String> arrayList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        //this.listRecipeCategoryWithRecipe = listRecipeCategoryWithRecipe;
        this.arrayList = arrayList;

    }

    @Override
    public Multiple_Images_Adapter_Recycler.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = inflater.inflate(R.layout.row_fragment_multiple_selected_images, parent, false);
        Multiple_Images_Adapter_Recycler.ViewHolder viewHolder = new Multiple_Images_Adapter_Recycler.ViewHolder(v);
        return viewHolder;


    }

    @Override
    public void onBindViewHolder(final Multiple_Images_Adapter_Recycler.ViewHolder holder, final int position)
    {

        Picasso.with(context)
                .load(Uri.fromFile(new File(arrayList.get(position))))
                .fit()
                .centerInside()
                .into(holder.selected_image, new Callback() {
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

    }


    @Override
    public int getItemCount() {


        return arrayList.size();

    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.selected_image)
        ImageView selected_image;

        View v;
        public ViewHolder(View itemView) {
            super(itemView);//textview=(TextView) itemView.findViewById(R.id.category_name);
            v=itemView;
            ButterKnife.bind(this, itemView);
        }
    }
}



