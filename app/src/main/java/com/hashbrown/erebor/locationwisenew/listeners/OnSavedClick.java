package com.hashbrown.erebor.locationwisenew.listeners;

import android.widget.ImageView;

/**
 * Created by Erebor on 19/05/17.
 */

public interface OnSavedClick
{
    void openBottomSheet(String path,int position);
    void onImageZoom(ImageView iv,String path);
}
