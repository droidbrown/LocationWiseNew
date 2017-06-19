package com.hashbrown.erebor.locationwisenew.views.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drivemode.android.typeface.TypefaceHelper;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.hashbrown.erebor.locationwisenew.R;
import com.hashbrown.erebor.locationwisenew.utils.AppUtils;
import com.hashbrown.erebor.locationwisenew.views.fragments.FragmentHomeScreen;
import com.hashbrown.erebor.locationwisenew.views.fragments.FragmentMultipleClickImagesNew;
import com.hashbrown.erebor.locationwisenew.views.fragments.FragmentMultipleClickImagesOld;
import com.hashbrown.erebor.locationwisenew.views.fragments.FragmentVideo;
import com.hashbrown.erebor.locationwisenew.views.fragments.FragmentVideo_New;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static java.util.Calendar.getInstance;

public class Activity_HomeScreen extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,View.OnClickListener {
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int MULTIPLE_PERMISSIONS =1 ;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 1;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;
    @BindView(R.id.home_coordinate)
    CoordinatorLayout home_coordinate;
     @BindView(R.id.top_text)
    TextView top_text;
    BottomSheetBehavior bottomSheetBehavior;
    @BindView(R.id.bottomSheetLayout)
    RelativeLayout bottomSheetLayout;
    @BindView(R.id.saved)
    ImageView saved;
    @BindView(R.id.multiple)
     ImageView multiple;
    DrawerLayout drawer;
    List<Address> addresses = null;
    TextView shoot,browse,close;
    public void hideStatusBar()
    {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void showStatusBar()
    {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(uiOptions);
    }
    @Override

    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        ButterKnife.bind(this);


        //datetime
        DateFormat df = new SimpleDateFormat("HH:mm:ss a");
        DateFormat df1 = new SimpleDateFormat("d MMM yyyy");
        String time = df1.format(getInstance().getTime());
        String date = df.format(getInstance().getTime());
        Prefs.putString("date", date);
        Prefs.putString("time", time);

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
        FragmentHomeScreen fragment= FragmentHomeScreen.newInstance();
        FragmentTransaction fts = getSupportFragmentManager().beginTransaction();
        fts.add(R.id.home_coordinate, fragment);
     //   fts.addToBackStack(fragment.getClass().getSimpleName());
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

        //bottom sheet
        //bottom sheet behave
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        shoot=(TextView)bottomSheetLayout.findViewById(R.id.shoot);
        browse=(TextView)bottomSheetLayout.findViewById(R.id.browse);
        close=(TextView)bottomSheetLayout.findViewById(R.id.close);
        shoot.setOnClickListener(this);
        browse.setOnClickListener(this);
        close.setOnClickListener(this);

        firstTimeLoadFFMPEG();
    }

    private void firstTimeLoadFFMPEG() {
        String status=Prefs.getString("ffmpeg","");
        if(status.equals("notdone"))
        {
            FFmpeg ffmpeg = FFmpeg.getInstance(this);
            try {
                ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onFailure() {
                    }

                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFinish() {

                        Prefs.putString("ffmpeg", "done");
                    }
                });
            } catch (FFmpegNotSupportedException e) {
                // Handle if FFmpeg is not supported by device
            }
        }
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
        }

         else if(bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED)
        {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            bottomSheetLayout.setVisibility(View.INVISIBLE);
        }
          else
        {
            FragmentTransaction fts = this.getSupportFragmentManager().beginTransaction();
            FragmentManager fragmentManager = this.getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() >= 1) {
                fragmentManager.popBackStackImmediate();

                // fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fts.commit();


            } else {
                AppUtils.deleteBackupForloc_mid();
                AppUtils.deleteBackupForbichooser();
                AppUtils.deleteBackupFromImage_watermark();
                AppUtils.delete_vid();
                finish();


            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppUtils.deleteBackupForloc_mid();
        AppUtils.deleteBackupForbichooser();
        AppUtils.deleteBackupFromImage_watermark();
        AppUtils.delete_vid();

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

                if (AppUtils.isNetworkConnected(this))
                {
                    try {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            FragmentMultipleClickImagesNew fragment = FragmentMultipleClickImagesNew.newInstance();
                            FragmentTransaction fts = this.getSupportFragmentManager().beginTransaction();
                            fts.replace(R.id.home_coordinate, fragment);
                            fts.addToBackStack(fragment.getClass().getSimpleName());
                            fts.commit();
                        }
                        else {
                            FragmentMultipleClickImagesOld fragment = FragmentMultipleClickImagesOld.newInstance(0);
                            FragmentTransaction fts =this.getSupportFragmentManager().beginTransaction();
                            fts.replace(R.id.home_coordinate, fragment);
                            fts.addToBackStack(fragment.getClass().getSimpleName());
                            fts.commit();
                        }

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                    Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();

            }


        else if (id == R.id.nav_slideshow)
        {

            bottomSheetLayout.setVisibility(View.VISIBLE);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);


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
                Prefs.putString("watermark","");
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
        FragmentHomeScreen fragment_products = FragmentHomeScreen.newInstance();
        if (fragment_products == getVisibleFragment())
            fragment_products.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK && requestCode == 1010) {

            Uri uri = data.getData();
            String path=  getPath(this,uri);
            FragmentVideo_New fragment = FragmentVideo_New.newInstance(path);
            FragmentTransaction fts = this.getSupportFragmentManager().beginTransaction();
            fts.add(R.id.home_coordinate, fragment);
            fts.addToBackStack(fragment.getClass().getSimpleName());
            fts.commit();
        }

        if (resultCode == RESULT_OK && requestCode == 1001) {

            Uri uri = data.getData();
            String path=  getPath(this,uri);
            FragmentVideo_New fragment = FragmentVideo_New.newInstance(path);
            FragmentTransaction fts = this.getSupportFragmentManager().beginTransaction();
            fts.add(R.id.home_coordinate, fragment);
            fts.addToBackStack(fragment.getClass().getSimpleName());
            fts.commit();
        }
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

    //gallery permission
    private boolean checkAndRequestPermissions() {
        int gallery_permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int camera_permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (gallery_permission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (camera_permission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d("", "Permission callback called-------");
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                        if (AppUtils.isNetworkConnected(this))
                        {
                            try {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    FragmentMultipleClickImagesNew fragment = FragmentMultipleClickImagesNew.newInstance();
                                    FragmentTransaction fts = this.getSupportFragmentManager().beginTransaction();
                                    fts.replace(R.id.home_coordinate, fragment);
                                    fts.addToBackStack(fragment.getClass().getSimpleName());
                                    fts.commit();
                                }
                                else {
                                    FragmentMultipleClickImagesOld fragment = FragmentMultipleClickImagesOld.newInstance(0);
                                    FragmentTransaction fts =this.getSupportFragmentManager().beginTransaction();
                                    fts.replace(R.id.home_coordinate, fragment);
                                    fts.addToBackStack(fragment.getClass().getSimpleName());
                                    fts.commit();
                                }

                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();


                    } else {
                        Log.d("", "Some permissions are not granted ask again ");

                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK("Storage and Camera Services Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();

                        }
                    }
                }
            }

        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    @Override
    public void onClick(View v) {
        if(v==shoot)
        {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            bottomSheetLayout.setVisibility(View.INVISIBLE);
            dispatchTakeVideoIntent();
        }
        else if(v==browse)
        {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            bottomSheetLayout.setVisibility(View.INVISIBLE);
             pickImageOrVideo();
        }
        else if(v==close)
        {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            bottomSheetLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, 1001);
        }
    }

    private void pickImageOrVideo() {
        if (Build.VERSION.SDK_INT < 19) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("video/*");
            startActivityForResult(photoPickerIntent,1010);
        } else {
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.setType("video/*");
            startActivityForResult(photoPickerIntent, 1010);
        }
    }


    public static String getFileExt(String fileName) {
        return "."+fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
