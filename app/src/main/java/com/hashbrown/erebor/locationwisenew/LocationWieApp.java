package com.hashbrown.erebor.locationwisenew;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.multidex.MultiDex;

import com.drivemode.android.typeface.TypefaceHelper;
import com.pixplicity.easyprefs.library.Prefs;

/**
 * Created by Erebor on 18/05/17.
 */

public class LocationWieApp extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the Prefs class
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        //typeface initialization
        TypefaceHelper.initialize(this);
    }
    @Override
    public void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
}
