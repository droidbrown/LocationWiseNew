package com.hashbrown.erebor.locationwisenew.views.activities;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.drivemode.android.typeface.TypefaceHelper;
import com.hashbrown.erebor.locationwisenew.R;
import com.hashbrown.erebor.locationwisenew.adapters.ImageList_Adapter;
import com.hashbrown.erebor.locationwisenew.database.db_locationwise;
import com.hashbrown.erebor.locationwisenew.listeners.OnSavedClick;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Activity_SavedImages extends AppCompatActivity implements OnSavedClick,View.OnClickListener {
    ArrayList<String> f = new ArrayList<String>();
    File[] listFile;
    ImageList_Adapter adapter;

    @BindView(R.id.noimage)
    TextView noimage;
    @BindView(R.id.top_text)
    TextView top_text;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.saved_list)
    RecyclerView image_list;
    @BindView(R.id.bottomSheetLayout)
    RelativeLayout bottomSheetLayout;
    @BindView(R.id.container)
    FrameLayout mContainer;
    @BindView(R.id.progressBarLong)
    CircleProgressBar progressZoomImage;
    BottomSheetBehavior bottomSheetBehavior;
    TextView share,delete,close;
    db_locationwise db;
    private Animator mCurrentAnimatorEffect;
    private int mShortAnimationDurationEffect;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__saved_images);
        ButterKnife.bind(this);
        TypefaceHelper.getInstance().setTypeface(top_text,getString(R.string.book));
        TypefaceHelper.getInstance().setTypeface(noimage,getString(R.string.book));

        //bottom sheet behave
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        share=(TextView)bottomSheetLayout.findViewById(R.id.share);
        delete=(TextView)bottomSheetLayout.findViewById(R.id.delete);
        close=(TextView)bottomSheetLayout.findViewById(R.id.close);
        share.setOnClickListener(this);
        delete.setOnClickListener(this);
        close.setOnClickListener(this);


        db=new db_locationwise(this);
    //    Toast.makeText(this, "data size"+db.getAllindexes_LawDict().length, Toast.LENGTH_SHORT).show();
        // recycler view
        image_list.setItemAnimator(new DefaultItemAnimator());
        image_list.setHasFixedSize(true);

        image_list.setLayoutManager(new LinearLayoutManager(this));
        adapter=new ImageList_Adapter(Activity_SavedImages.this,f,this);
        image_list.setAdapter(adapter);
        getFromSdcard();

        mShortAnimationDurationEffect = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

    }
    public void getFromSdcard()
    {
        File file= new File(android.os.Environment.getExternalStorageDirectory(),"LocationWise/LocationWiseImages");

        if (file.isDirectory())
        {
            listFile = file.listFiles();


            for (int i = 0; i < listFile.length; i++)
            {

                f.add(listFile[i].getAbsolutePath());



            }
            if(f.size()>0)
            {
                adapter.notifyDataSetChanged();
            }
            else if(f.size()==0)
            {
                image_list.setVisibility(View.INVISIBLE);
                noimage.setVisibility(View.VISIBLE);
            }
            else
            {
                image_list.setVisibility(View.INVISIBLE);
                noimage.setVisibility(View.VISIBLE);
            }
        }
    }



    @OnClick(R.id.back)
    void onBack()
    {
        overridePendingTransition(R.anim.slide_out_up,R.anim.slide_in_up);
        finish();
    }

    String location;
    String local_location;
    int position;
    @Override
    public void openBottomSheet(String path,int pos)
    {
        location=path;
         position=pos;
        local_location=path.substring(path.lastIndexOf("/") + 1).trim();
        //  System.out.println("loc   "+loc);
        local_location="sdcard/LocationWise/LocationWiseImages/"+local_location;
        bottomSheetLayout.setVisibility(View.VISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }

    @Override
    public void onImageZoom(ImageView iv, String path) {
        zoomImageFromThumb(iv, path);
    }


    @Override
    public void onClick(View v)
    {
        if(v==share)
        {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            bottomSheetLayout.setVisibility(View.INVISIBLE);
            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpg");
            final File photoFile = new File( location);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(photoFile));
            startActivity(Intent.createChooser(shareIntent, "Share image using"));
        }
        else if(v==delete)
        {

            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete this Image?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            File file = new File(location);
                            file.delete();
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            bottomSheetLayout.setVisibility(View.INVISIBLE);
                            try
                            {
                                db.deleteimagedata(local_location);

                            }
                            catch (Exception e)
                            {

                            }
                           // Toast.makeText(Activity_SavedImages.this, ""+local_location, Toast.LENGTH_SHORT).show();
                            f.remove(position);
                            if(f.size()>0)
                            {
                                adapter.notifyDataSetChanged();
                            }
                            else if(f.size()==0)
                            {
                                image_list.setVisibility(View.INVISIBLE);
                                noimage.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                image_list.setVisibility(View.INVISIBLE);
                                noimage.setVisibility(View.VISIBLE);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            bottomSheetLayout.setVisibility(View.INVISIBLE);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();



        }
        else if(v==close)
        {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            bottomSheetLayout.setVisibility(View.INVISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void zoomImageFromThumb(final View thumbView, String url) {
        if (mCurrentAnimatorEffect != null) {
            mCurrentAnimatorEffect.cancel();
        }

        final ImageView expandedImageView = (ImageView)findViewById(R.id.expanded_image);
        // expandedImageView.setImageBitmap(bitmap);

        Glide.with(this).
                load(url)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressZoomImage.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressZoomImage.setVisibility(View.GONE);
                        return false;
                    }
                })
                .crossFade(500).dontAnimate()

                .into(expandedImageView);


        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        thumbView.setAlpha(1f);
        expandedImageView.setVisibility(View.VISIBLE);
        mContainer.setVisibility(View.VISIBLE);
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDurationEffect);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimatorEffect = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimatorEffect = null;
            }
        });
        set.start();
        mCurrentAnimatorEffect = set;

        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimatorEffect != null) {
                    mCurrentAnimatorEffect.cancel();
                }

                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDurationEffect);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mContainer.setVisibility(View.GONE);
                        mCurrentAnimatorEffect = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mContainer.setVisibility(View.GONE);
                        mCurrentAnimatorEffect = null;
                    }
                });
                set.start();
                mCurrentAnimatorEffect = set;
            }
        });
    }
}
