package com.hashbrown.erebor.locationwisenew.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * Created by Erebor on 15/05/17.
 */

public class ViewUtils {
    public static  void ShowSnackBar(String message,View view)
    {
        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_LONG);

        snackbar.setActionTextColor(Color.RED);

        snackbar.show();
    }
    public static void ShowToast(Context context, String Message)
    {
        if(!((Activity)context).isFinishing())
        {
            try
            {
                WeakReference<Context> weakReference=new WeakReference<Context>(context);
                Toast.makeText(weakReference.get(), Message, Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {

            }
        }
    }
}
