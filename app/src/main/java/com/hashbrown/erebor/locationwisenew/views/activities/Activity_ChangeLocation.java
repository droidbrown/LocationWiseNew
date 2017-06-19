package com.hashbrown.erebor.locationwisenew.views.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.drivemode.android.typeface.TypefaceHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hashbrown.erebor.locationwisenew.R;
import com.hashbrown.erebor.locationwisenew.utils.MessageEvent;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class Activity_ChangeLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;

    @BindView(R.id.change_container)
    CoordinatorLayout change_container;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.top_text)
    TextView textview;
    @BindView(R.id.search)
    EditText search;
    LatLng current;
    int distanceRadius=10000;
    MarkerOptions marker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__change_location);
        ButterKnife.bind(this);
        current=new LatLng(Prefs.getDouble("lat",0),Prefs.getDouble("long",0));
        TypefaceHelper.getInstance().setTypeface(textview,getString(R.string.book));
        TypefaceHelper.getInstance().setTypeface(search,getString(R.string.book));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        search.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                onSearchClick();
                return false;
            }
        });

    }

@OnClick(R.id.back)
void onback()
{
    Prefs.putDouble("distance",distance(Prefs.getDouble("lat",0),Prefs.getDouble("long",0),Prefs.getDouble("latMap",0),Prefs.getDouble("longMap",0)));
    finish();
}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Prefs.putDouble("distance",distance(Prefs.getDouble("lat",0),Prefs.getDouble("long",0),Prefs.getDouble("latMap",0),Prefs.getDouble("longMap",0)));
        EventBus.getDefault().postSticky(new MessageEvent("latlong_edit"));


    }




    //on map ready
    @Override
    public void onMapReady(GoogleMap googleMap) 
    {
        map = googleMap;
        current=new LatLng(Prefs.getDouble("lat",0),Prefs.getDouble("long",0));

        marker=new MarkerOptions().position(current).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_icon));
        map.addMarker(marker);
        map.moveCamera(CameraUpdateFactory.newLatLng(current));

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        distanceRadius = distanceRadius * 1000;
        CameraPosition cameraPosition1 = new CameraPosition.Builder()
                .target(current)
                .zoom(13)
                .build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition1));
        map.addCircle(new CircleOptions()
                .center(current)
                .radius(distanceRadius));
        // Add a marker in Sydney and move the camera

      //  final int finalDistanceRadius = distanceRadius;
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                Prefs.putDouble("latMap", latLng.latitude );
                Prefs.putDouble("longMap", latLng.longitude);
               map.clear();

                CameraPosition cameraPosition1 = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(13)
                        .build();

                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition1));
                map.addCircle(new CircleOptions()
                        .center(latLng));
                       // .radius(finalDistanceRadius));
// .strokeWidth(0f)
// .fillColor(0x550000FF));
                 marker=new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_icon));
                map.addMarker(marker);
                map.moveCamera(CameraUpdateFactory.newLatLng(latLng));





            }

        });
    }

    //if user search map by typing then this method is called
    void onSearchClick()
    {
        //   AppUtils.hidekeyBoard(this,et_location);

        String et_getData = search.getText().toString().trim();
        if (et_getData.equalsIgnoreCase(""))
        {
            Snackbar.make(change_container, "Enter data to search",
                    Snackbar.LENGTH_SHORT)
                    .show();
        } else {


            LatLng newSearchLatlong = getLocationFromAddress(this, et_getData);

            if (newSearchLatlong != null) {

                Prefs.putDouble("latMap", newSearchLatlong.latitude);
                Prefs.putDouble("longMap",newSearchLatlong.longitude );

                map.clear();
                CameraPosition cameraPosition1 = new CameraPosition.Builder()
                        .target(newSearchLatlong)
                        .zoom(13)
                        .build();
                int distanceRadius=10000;
                distanceRadius = distanceRadius * 1000;
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition1));
                map.addCircle(new CircleOptions()
                        .center(newSearchLatlong)
                        .radius(distanceRadius));
// .strokeWidth(0f)
// .fillColor(0x550000FF));


                marker=new MarkerOptions().position(newSearchLatlong).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_icon));
                map.addMarker(marker);
                map.moveCamera(CameraUpdateFactory.newLatLng(newSearchLatlong));





                search.setText("");


            }}}



    //if user search map by typing then this method is called
    public LatLng getLocationFromAddress(Context context, String strAddress)
    {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return p1;
    }
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

}
