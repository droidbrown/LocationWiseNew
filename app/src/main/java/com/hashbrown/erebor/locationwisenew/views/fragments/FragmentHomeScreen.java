package com.hashbrown.erebor.locationwisenew.views.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drivemode.android.typeface.TypefaceHelper;
import com.hashbrown.erebor.locationwisenew.R;
import com.hashbrown.erebor.locationwisenew.utils.AppUtils;
import com.hashbrown.erebor.locationwisenew.utils.MessageEvent;
import com.hashbrown.erebor.locationwisenew.utils.constants;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenImages;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Erebor on 18/05/17.
 */

public class FragmentHomeScreen extends Fragment implements ImageChooserListener

{
    private static final String TAG = "Home_Frag";
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 222;
    @BindView(R.id.progressBar)
    ProgressBar progress;
    @BindView(R.id.takephoto)
    RelativeLayout takephoto;
    @BindView(R.id.browse)
    LinearLayout browse;
    @BindView(R.id.takephoto_text)
    TextView takephoto_text;
    @BindView(R.id.browse_text)
    TextView browse_text;
    private boolean isActivityResultOver = false;
    public String type = "";
    private String originalFilePath;
    private String thumbnailFilePath;
    private String thumbnailSmallFilePath;
    private int chooserType;
    private String filePath;
    public static final int MULTIPLE_PERMISSIONS = 1;
    ArrayList<String> multiple_images = new ArrayList<>();
    ArrayList<String> multiple_images_thumbnail = new ArrayList<>();
    ImageChooserManager imageChooserManager;
    List<Address> addresses;
    Double latitude, longitude;

    public static FragmentHomeScreen newInstance() {

        Bundle args = new Bundle();

        FragmentHomeScreen fragment = new FragmentHomeScreen();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       // getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        View v = inflater.inflate(R.layout.fragment_home_screen, container, false);
        Activity a = getActivity();
        if(a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        getActivity().getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        EventBus.getDefault().postSticky(new MessageEvent(constants.CLICK_SNAP));
        ButterKnife.bind(this, v);
        TypefaceHelper.getInstance().setTypeface(browse_text, getString(R.string.book));
        TypefaceHelper.getInstance().setTypeface(takephoto_text, getString(R.string.book));
        latitude = Prefs.getDouble("lat", 0);
        longitude = Prefs.getDouble("long", 0);
        addresses = getLocationData();
        if (addresses != null) {
//            String address = addresses.get(0).getAddressLine(0);
//            String city = addresses.get(0).getLocality();
//            String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName();

        }
        checkAndRequestPermissions();
        return v;


    }

    @OnClick(R.id.takephoto)
    void ontakephoto() {
        type = "camera";
        if (checkAndRequestPermissions()) {
            if (AppUtils.isNetworkConnected(getActivity()))
                takePicture();
            else
                Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show();

        } else {
            checkAndRequestPermissions();
        }
    }

    @OnClick(R.id.browse)
    void onbrowse() {
        type = "gallery";
        if (checkAndRequestPermissions()) {
            if (AppUtils.isNetworkConnected(getActivity()))
                chooseImage_Multiple();
            else
                Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show();

        } else {
            checkAndRequestPermissions();
        }
    }


    @Override
    public void onImageChosen(final ChosenImage image) {
        multiple_images.clear();
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                isActivityResultOver = true;
                originalFilePath = image.getFilePathOriginal();
                thumbnailFilePath = image.getFileThumbnail();
                thumbnailSmallFilePath = image.getFileThumbnailSmall();
                progress.setVisibility(View.GONE);
                if (image != null) {
                    Log.i(TAG, "Chosen Image: Is not null");
                    // textViewFile.setText(image.getFilePathOriginal());
                    // imageview.setVisibility(View.VISIBLE);
                    // loadImage(imageview, image.getFilePathOriginal());
                    // loadImage(imageViewThumbSmall, image.getFileThumbnailSmall());

                    multiple_images.add(image.getFilePathOriginal());
                    FragmentSelectedImage fragment = FragmentSelectedImage.newInstance(multiple_images);
                    FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
                    fts.replace(R.id.home_coordinate, fragment);
                    fts.addToBackStack(fragment.getClass().getSimpleName());
                    fts.commit();


                    // bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    // bottomSheetLayout.setVisibility(View.INVISIBLE);
                } else {
                    Log.i(TAG, "Chosen Image: Is null");
                }
            }
        });
    }

    @Override
    public void onError(String s) {

    }

    @Override
    public void onImagesChosen(final ChosenImages chosenImages) {

        multiple_images.clear();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ChosenImages chosenImages_inner = chosenImages;
                Log.i(TAG, "On Images Chosen: " + chosenImages_inner.size());


                if (chosenImages_inner.size() > 0) {

                    for (int i = 0; i < chosenImages_inner.size(); i++) {
                        multiple_images.add(chosenImages_inner.getImage(i).getFilePathOriginal());
                        multiple_images_thumbnail.add(chosenImages_inner.getImage(i).getFileThumbnail());
                        // System.out.println("image path"+chosenImages_inner.getImage(i).getFilePathOriginal());
                    }
                    progress.setVisibility(View.GONE);
                    FragmentMultipleSelectedImages fragment = FragmentMultipleSelectedImages.newInstance(multiple_images);
                    FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
                    fts.replace(R.id.home_coordinate, fragment);
                    fts.addToBackStack(fragment.getClass().getSimpleName());
                    fts.commit();
                    //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    // bottomSheetLayout.setVisibility(View.INVISIBLE);

                }
            }
        });
    }

    private void takePicture() {

        chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_CAPTURE_PICTURE, true);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imageChooserManager.setExtras(bundle);
        imageChooserManager.setImageChooserListener(this);
        try {
            progress.setVisibility(View.VISIBLE);
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reinitializeImageChooser() {
        imageChooserManager = new ImageChooserManager(this, chooserType, true);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imageChooserManager.setExtras(bundle);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.reinitialize(filePath);
    }

    //selecting multiple_select_image from gallery
    private void chooseImage_Multiple() {

        chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_PICK_PICTURE, true);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imageChooserManager.setExtras(bundle);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.clearOldFiles();
        try {
            progress.setVisibility(View.VISIBLE);
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //gallery permission
    private boolean checkAndRequestPermissions() {
        int gallery_permission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int camera_permission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (gallery_permission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (camera_permission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
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

                        Log.d("", " permissions are  granted ");
                        if (type.equals("camera")) {

                            takePicture();

                        } else if (type.equals("gallery")) {

                            chooseImage_Multiple();
                        }

                    } else {
                        Log.d("", "Some permissions are not granted ask again ");

                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
                            Toast.makeText(getActivity(), "Go to settings and enable permissions", Toast.LENGTH_LONG).show();

                        }
                    }
                }
            }

        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "OnActivityResult");
        Log.i(TAG, "File Path : " + filePath);
        Log.i(TAG, "Chooser Type: " + chooserType);
        if (resultCode == RESULT_OK && (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (imageChooserManager == null) {
                reinitializeImageChooser();
            }
            imageChooserManager.submit(requestCode, data);
        } else {

            progress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public List<Address> getLocationData() {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return addresses;
    }

}
