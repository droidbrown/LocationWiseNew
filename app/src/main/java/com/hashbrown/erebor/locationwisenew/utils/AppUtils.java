package com.hashbrown.erebor.locationwisenew.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.pixplicity.easyprefs.library.Prefs;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Erebor on 18/04/17.
 */

public class AppUtils {
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else {
            return true;
        }
    }
    public static String convert(double latitude, double longitude) {
        StringBuilder builder = new StringBuilder();

        if (latitude < 0) {
            builder.append("S ");
        } else {
            builder.append("N ");
        }

        String latitudeDegrees = Location.convert(Math.abs(latitude), Location.FORMAT_SECONDS);
        String[] latitudeSplit = latitudeDegrees.split(":");
        builder.append(latitudeSplit[0]);
        builder.append("°");
        builder.append(latitudeSplit[1]);
        builder.append("'");
        builder.append(latitudeSplit[2]);
        builder.append("\"");

        builder.append(" ");

        if (longitude < 0) {
            builder.append("W ");
        } else {
            builder.append("E ");
        }

        String longitudeDegrees = Location.convert(Math.abs(longitude), Location.FORMAT_SECONDS);
        String[] longitudeSplit = longitudeDegrees.split(":");
        builder.append(longitudeSplit[0]);
        builder.append("°");
        builder.append(longitudeSplit[1]);
        builder.append("'");
        builder.append(longitudeSplit[2]);
        builder.append("\"");

        return builder.toString();
    }
    public static List<Address> getLocationData(Context context,Double latitude,Double longitude) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return addresses;
    }
    public static final int getColor(Context context, int id)
    {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }
     public static final void loadImage(ImageView iv, final String path,Context context) {
         Glide.with(context).load(Uri.fromFile(new File(path))).dontAnimate().into(iv);
    }




    public static void deleteBackupForloc_mid() {

        try{
            File dir = new File(Environment.getExternalStorageDirectory() + "/LocationWise/tmploc");
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static  void deleteBackupForbichooser() {

        try{
            File dir = new File(Environment.getExternalStorageDirectory() + "/bichooser");
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void delete_vid()
    {

        try{
            File dir = new File(Environment.getExternalStorageDirectory() + "/LocationWise/tmploc");
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public static void deleteBackupFromImage_watermark() {

        try {



            File fichero = new File(Prefs.getString("watermark",""));
            File carpeta = fichero.getParentFile();
            for (File file : carpeta.listFiles()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
