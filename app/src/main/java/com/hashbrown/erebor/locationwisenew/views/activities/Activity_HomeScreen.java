package com.hashbrown.erebor.locationwisenew.views.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drivemode.android.typeface.TypefaceHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.text.Text;
import com.hashbrown.erebor.locationwisenew.R;
import com.hashbrown.erebor.locationwisenew.utils.AppUtils;
import com.hashbrown.erebor.locationwisenew.utils.MessageEvent;
import com.hashbrown.erebor.locationwisenew.utils.constants;
import com.hashbrown.erebor.locationwisenew.views.fragments.fragment_home_screen;
import com.hashbrown.erebor.locationwisenew.views.fragments.fragment_selected_image;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class Activity_HomeScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;
    @BindView(R.id.home_coordinate)
    CoordinatorLayout home_coordinate;
     @BindView(R.id.top_text)
    TextView top_text;

    @BindView(R.id.saved)
    ImageView saved;
    @BindView(R.id.multiple)
     ImageView multiple;
    DrawerLayout drawer;
    List<Address> addresses = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        ButterKnife.bind(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        TypefaceHelper.getInstance().setTypeface(top_text,getString(R.string.book));



        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        toggle.setDrawerIndicatorEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_action_menu_icon);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                drawer.openDrawer(Gravity.LEFT);
            }
        });
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header=navigationView.getHeaderView(0);
/*View view=navigationView.inflateHeaderView(R.layout.nav_header_main);*/
        TextView nav_top = (TextView)header.findViewById(R.id.nav_top);
        TypefaceHelper.getInstance().setTypeface(nav_top,getString(R.string.book));

        //adding first fragment
        fragment_home_screen fragment= fragment_home_screen.newInstance();
        FragmentTransaction fts = getSupportFragmentManager().beginTransaction();
        fts.add(R.id.home_coordinate, fragment);
        //fts.addToBackStack(fragment.getClass().getSimpleName());
        fts.commit();

        checkInternet();

        //initialize google api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();


        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


    }

    @OnClick(R.id.saved)
    void onSaved()
    {
        Intent i=new Intent(this,Activity_SavedImages.class);
        startActivity(i);
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }

    //on resume method of activity
    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();

    }

    //on pause method of activity

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.home)
        {
            fragment_home_screen fragment= fragment_home_screen.newInstance();
            FragmentTransaction fts = getSupportFragmentManager().beginTransaction();
            fts.replace(R.id.home_coordinate, fragment);
            //fts.addToBackStack(fragment.getClass().getSimpleName());
            fts.commit();
        }

        else if (id == R.id.nav_slideshow)
        {
            Intent i=new Intent(this,Activity_SavedImages.class);
            startActivity(i);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }
        else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();


            addresses = getLocationData();

            if(Prefs.getString("comingFrom","").equalsIgnoreCase("splash")) {
                Prefs.putDouble("lat", currentLatitude);
                Prefs.putDouble("long", currentLongitude);

                Prefs.putString("comingFrom","");
            }



        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try
            {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            }
            catch (IntentSender.SendIntentException e)
            {

                e.printStackTrace();
            }
        } else {
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {



    }
    public List<Address> getLocationData() {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return addresses;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fragment_home_screen fragment_products = fragment_home_screen.newInstance();
        if (fragment_products == getVisibleFragment())
            fragment_products.onActivityResult(requestCode, resultCode, data);

    }
    public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible())
                    return fragment;
            }
        }
        return null;
    }


    public void checkInternet()
    {
        if(!AppUtils.isNetworkConnected(this))
        {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle("Checking Internet")
                    .setCancelable(false)
                    .setMessage("Your Internet seems to be disabled, do you want to enable it?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_SETTINGS));
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }
    }


}
