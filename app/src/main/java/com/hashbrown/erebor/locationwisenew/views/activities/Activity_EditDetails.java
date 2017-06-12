package com.hashbrown.erebor.locationwisenew.views.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.drivemode.android.typeface.TypefaceHelper;
import com.hashbrown.erebor.locationwisenew.R;
import com.hashbrown.erebor.locationwisenew.datetimepicker.DateTimePicker;
import com.hashbrown.erebor.locationwisenew.datetimepicker.SimpleDateTimePicker;
import com.hashbrown.erebor.locationwisenew.utils.AppUtils;
import com.hashbrown.erebor.locationwisenew.utils.MessageEvent;
import com.hashbrown.erebor.locationwisenew.utils.ViewUtils;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenImages;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class Activity_EditDetails extends AppCompatActivity implements DateTimePicker.OnDateTimeSetListener,ImageChooserListener {

    private static final String TAG ="edit details" ;
    @BindView(R.id.progressBar)
    ProgressBar progress;
    @BindView(R.id.top_text)
    TextView top_text;
    @BindView(R.id.head1)
    TextView datetime_heading;
    @BindView(R.id.head2)
    TextView address_heading;
    @BindView(R.id.head3)
    TextView gps_heading;
    @BindView(R.id.head4)
    TextView latlong_heading;
    @BindView(R.id.head5)
    TextView watermark_heading;
    @BindView(R.id.val1)
    TextView datetime_value;
    @BindView(R.id.val2)
    TextView address_value;
    @BindView(R.id.val3)
    TextView gps_value;
    @BindView(R.id.val4)
    TextView latlong_value;
    @BindView(R.id.edit_datetime)
    ImageView edit_datetime;
    @BindView(R.id.editaddress)
    ImageView edit_address;
    @BindView(R.id.save)
    Button save;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.watermark)
    LinearLayout watermark_view;
    Double latitude,longitude;
    String date,time;
    @BindView(R.id.edit_parent)
    CoordinatorLayout edit_parent;
    @BindView(R.id.watermarkimage)
    ImageView watermark_image;
    private String originalFilePath;
    private String thumbnailFilePath;
    private String thumbnailSmallFilePath=null;
    ImageChooserManager imageChooserManager;
    private boolean isActivityResultOver = false;
    private int chooserType;
    private String filePath;
    String address,city,state,country,postalCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__edit_details);
        ButterKnife.bind(this);
        latitude= Prefs.getDouble("lat",0);
        longitude=Prefs.getDouble("long",0);
        latlong_value.setText(latitude+" "+longitude);
        gps_value.setText(convert(latitude,longitude));
        date=Prefs.getString("date","");
        time=Prefs.getString("time","");
        datetime_value.setText(time+"| "+date);
        address_value.setText(Prefs.getString("address",""));
        TypefaceHelper.getInstance().setTypeface(top_text,getString(R.string.book));
        TypefaceHelper.getInstance().setTypeface(datetime_heading,getString(R.string.book));
        TypefaceHelper.getInstance().setTypeface(address_heading,getString(R.string.book));
        TypefaceHelper.getInstance().setTypeface(gps_heading,getString(R.string.book));
        TypefaceHelper.getInstance().setTypeface(latlong_heading,getString(R.string.book));
        TypefaceHelper.getInstance().setTypeface(watermark_heading,getString(R.string.book));
        TypefaceHelper.getInstance().setTypeface(datetime_value,getString(R.string.light));
        TypefaceHelper.getInstance().setTypeface(address_value,getString(R.string.light));
        TypefaceHelper.getInstance().setTypeface(gps_value,getString(R.string.light));
        TypefaceHelper.getInstance().setTypeface(latlong_value,getString(R.string.light));
        TypefaceHelper.getInstance().setTypeface(save,getString(R.string.book));

        if(Prefs.getString("watermark","")!="")
        {
            AppUtils.loadImage(watermark_image,Prefs.getString("watermark",""),this);
        }


    }


    @OnClick(R.id.back)
    void onback()
    {

        finish();
       // overridePendingTransition(R.anim.push_out_right,R.anim.pull_in_left);

    }
    @OnClick(R.id.save)
    void onsave()
    {
/*save date and time*/
        Prefs.putString("date",date);
        Prefs.putString("time",time);
        /*save address*/
        Prefs.putString("address", address  + city  + state + country  + postalCode);

        /*save lat long value*/

        Prefs.putDouble("lat", latitude );
        Prefs.putDouble("long",longitude);
        if(thumbnailSmallFilePath!=null)
        Prefs.putString("watermark",thumbnailSmallFilePath);

        EventBus.getDefault().postSticky(new MessageEvent("datetime"));

        finish();


    }

    @OnClick(R.id.watermark)
    void onWatermark()
    {
        chooseImage_Multiple();
    }
    @OnClick(R.id.edit_datetime)
    void ondatetime()
    {
        SimpleDateTimePicker simpleDateTimePicker = SimpleDateTimePicker.make(
                getResources().getString(R.string.select_time),
                new Date(),
                this,getSupportFragmentManager()
        );

        simpleDateTimePicker.show();
    }
     @OnClick(R.id.editaddress)
    void onaddress()
    {
        if(AppUtils.isNetworkConnected(this)) {
            Intent i = new Intent(this, Activity_ChangeLocation.class);
            this.startActivity(i);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }
        else
        {
            ViewUtils.ShowSnackBar("No Internet Connected",edit_parent);
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();


        finish();
       // overridePendingTransition(R.anim.push_out_right,R.anim.pull_in_left);

    }

    private String convert(double latitude, double longitude) {
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

    @Override
    public void DateTimeSet(Date updated_date) {
        DateFormat df = new SimpleDateFormat("HH:mm:ss a");
        DateFormat df1 = new SimpleDateFormat("d MMM yyyy");
        date =df.format(updated_date);
        time=df1.format(updated_date);
        datetime_value.setText(time+"| "+date);



    }


    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().registerSticky(this);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

    }
    List<Address> addresses;
    public void onEventMainThread(MessageEvent messageEvent) {


        if (messageEvent.message.contains("latlong_edit"))
        {

            latitude= Prefs.getDouble("latMap",0);
            longitude=Prefs.getDouble("longMap",0);

            addresses=getLocationData();
            if(addresses.size()>0)
            {   if (addresses.get(0).getAddressLine(0) != null && !addresses.get(0).getAddressLine(0).equals("null"))
                address=addresses.get(0).getAddressLine(0)+",";
            else
                address="";

                if (addresses.get(0).getLocality() != null && !addresses.get(0).getLocality().equals("null"))
                    city=addresses.get(0).getLocality()+",";
                else
                    city="";

                if (addresses.get(0).getAdminArea() != null && !addresses.get(0).getAdminArea().equals("null"))
                    state=addresses.get(0).getAdminArea()+",";
                else
                    state="";

                if (addresses.get(0).getCountryName() != null && !addresses.get(0).getCountryName().equals("null"))
                    country=addresses.get(0).getCountryName()+",";
                else
                    country="";

                if (addresses.get(0).getPostalCode() != null && !addresses.get(0).getPostalCode().equals("null"))
                    postalCode="Postal Code: "+addresses.get(0).getPostalCode();
                else
                    postalCode="";





                String coordinates = convert(latitude, longitude);

                address_value.setText(address  + city  + state + country  + postalCode);

                gps_value.setText(coordinates);
            }

            latlong_value.setText(latitude+" "+longitude);


            EventBus.getDefault().removeStickyEvent(messageEvent);
        }

    }

    public List<Address> getLocationData()
    {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return addresses;
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
    private void chooseImage_Multiple()
    {

        chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_PICK_PICTURE, true);
        Bundle bundle = new Bundle();
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
    @Override
    public void onImageChosen(final ChosenImage chosenImage) {
       this.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                isActivityResultOver = true;
                originalFilePath = chosenImage.getFilePathOriginal();
                thumbnailFilePath = chosenImage.getFileThumbnail();
                thumbnailSmallFilePath = chosenImage.getFileThumbnailSmall();
                progress.setVisibility(View.GONE);
                if (chosenImage != null)
                {
                    Log.i(TAG, "Chosen Image: Is not null");
                    AppUtils.loadImage(watermark_image,thumbnailSmallFilePath,getApplicationContext());

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
    public void onImagesChosen(ChosenImages chosenImages) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "OnActivityResult");
        Log.i(TAG, "File Path : " + filePath);
        Log.i(TAG, "Chooser Type: " + chooserType);
        if (resultCode == RESULT_OK && (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (imageChooserManager == null) {
                reinitializeImageChooser();
            }
            imageChooserManager.submit(requestCode, data);
        }
        else
        {
            progress.setVisibility(View.GONE);
        }
    }
}
