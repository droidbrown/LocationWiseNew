package com.hashbrown.erebor.locationwisenew.views.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hashbrown.erebor.locationwisenew.R;
import com.hashbrown.erebor.locationwisenew.database.db_locationwise;
import com.hashbrown.erebor.locationwisenew.database.pic_details;
import com.hashbrown.erebor.locationwisenew.utils.MessageEvent;
import com.hashbrown.erebor.locationwisenew.utils.constants;
import com.hashbrown.erebor.locationwisenew.views.activities.Activity_EditDetails;
import com.pixplicity.easyprefs.library.Prefs;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

import static java.util.Calendar.getInstance;


/**
 * Created by Erebor on 18/05/17.
 */

public class fragment_selected_image extends Fragment {

    private static final String TAG = "selected_image";
    @BindView(R.id.cross)
    ImageView cross;
    @BindView(R.id.edit)
    ImageView edit;
    @BindView(R.id.save)
    ImageView save;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.frame)
    FrameLayout frame;
    @BindView(R.id.switch1)
    SwitchCompat aSwitch;
    Double latitude, longitude;
    List<Address> addresses;
    File path_to_folder;
    int name;
    String filename;
    boolean dir_success;
    File path_to_file;
    db_locationwise db_locationwise;
    String address, city, state, country, postalCode, knownName, date, time, coordinates;
    ProgressDialog pd;
    public static fragment_selected_image newInstance() {

        Bundle args = new Bundle();

        fragment_selected_image fragment = new fragment_selected_image();
        fragment.setArguments(args);
        return fragment;
    }

    ArrayList<String> paths = new ArrayList<>();

    public static fragment_selected_image newInstance(ArrayList<String> paths) {

        Bundle args = new Bundle();
        args.putStringArrayList("paths", paths);
        fragment_selected_image fragment = new fragment_selected_image();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_selected_image, container, false);
        ButterKnife.bind(this, v);
        db_locationwise = new db_locationwise(getActivity());
        paths = this.getArguments().getStringArrayList("paths");
        pd=new ProgressDialog(getActivity());
        pd.setMessage("Saving Image...");
        //datetime
        DateFormat df = new SimpleDateFormat("HH:mm:ss a");
        DateFormat df1 = new SimpleDateFormat("d MMM yyyy");
        time = df1.format(getInstance().getTime());
        date = df.format(getInstance().getTime());
        Prefs.putString("date", date);
        Prefs.putString("time", time);

        loadImage(image, paths.get(0));
        latitude = Prefs.getDouble("lat", 0);
        longitude = Prefs.getDouble("long", 0);
        addresses = getLocationData();
        if(addresses!=null)
        if (addresses.size() > 0)
        {
            if (addresses.get(0).getAddressLine(0) != null && !addresses.get(0).getAddressLine(0).equals("null"))
                        address=addresses.get(0).getAddressLine(0);
                    else
                    address="";

                    if (addresses.get(0).getLocality() != null && !addresses.get(0).getLocality().equals("null"))
                        city=addresses.get(0).getLocality();
                   else
                    city="";

                if (addresses.get(0).getAdminArea() != null && !addresses.get(0).getAdminArea().equals("null"))
                    state=addresses.get(0).getAdminArea();
                else
                    state="";

                if (addresses.get(0).getCountryName() != null && !addresses.get(0).getCountryName().equals("null"))
                    country=addresses.get(0).getCountryName();
                else
                    country="";

                if (addresses.get(0).getPostalCode() != null && !addresses.get(0).getPostalCode().equals("null"))
                    postalCode=addresses.get(0).getPostalCode();
                else
                    postalCode="";





            coordinates = convert(latitude, longitude);
            Prefs.putString("address", address + ", " + city + ", " + state + ", " + country + ", Postal Code :" + postalCode);
            text.setText(Prefs.getString("time", "09:00 am") + " | " + Prefs.getString("date", "01- Jan-2001") + " |\n" + latitude + " " + longitude + " | " + coordinates + " |\n" + Prefs.getString("address", ""));

        }

        aSwitch.setChecked(true);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    text.setBackgroundColor(getColor(getActivity(),R.color.appcolor));

                }
                else
                {
                    text.setBackgroundColor(getColor(getActivity(),R.color.appcolor_transparent));

                }
            }
        });



        return v;
    }
    public static final int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }
    private void loadImage(ImageView iv, final String path) {
        Picasso.with(getActivity())
                .load(Uri.fromFile(new File(path)))
                .fit()
                .centerInside()
                .into(iv, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "Picasso Success Loading Thumbnail - " + path);
                    }

                    @Override
                    public void onError() {
                        Log.i(TAG, "Picasso Error Loading Thumbnail Small - " + path);
                    }
                });
    }

    @OnClick(R.id.save)
    void onSave() {
        aSwitch.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);
        cross.setVisibility(View.INVISIBLE);
        edit.setVisibility(View.INVISIBLE);
        new saveImage().execute("");
    }

    @OnClick(R.id.cross)
    void onCross() {


        FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() >= 1) {
            fragmentManager.popBackStackImmediate();
            fts.commit();
        } else {
            getActivity().finish();
        }
        deleteBackupFromImage();

    }

    @OnClick(R.id.edit)
    void onEdit() {
        Intent i = new Intent(getActivity(), Activity_EditDetails.class);
        getActivity().startActivity(i);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
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


    public void createFolder() {
        //create a folder
        path_to_folder = new File("/sdcard/LocationWise");

        if (path_to_folder.exists()) {
            // Toast.makeText(this, "Alreday Exists", Toast.LENGTH_SHORT).show();
            manageFile();
        } else {
            dir_success = path_to_folder.mkdirs();
            if (dir_success == true) {
                Prefs.putInt("number", 0);
                //   Toast.makeText(this, "Folder Successfully Created", Toast.LENGTH_SHORT).show();
                manageFile();

            } else {
                //Toast.makeText(this, "Folder Not Created", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void manageFile() {
        //name like 1.png
        //name= Prefs.getInt("number",0);
        // name=name+1;
        //filename="sdcard/LocationWise/"+name+".png";
        //path_to_file = new File(filename);
        // Prefs.putInt("number",name);


        //name with milli seconds
        long seconds = System.currentTimeMillis();
        filename = "sdcard/LocationWise/" + seconds + ".png";
        path_to_file = new File(filename);
        if (path_to_file.exists()) {
            boolean delete = path_to_file.delete();
            if (delete == true) {
                writeFile();
                // Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show();

            } else {

                // Toast.makeText(this, "Deletion Failed", Toast.LENGTH_SHORT).show();
            }

        } else {
            writeFile();

        }
    }
    boolean saved=false;
    void writeFile() {



        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            frame.setDrawingCacheEnabled(true);

            FrameLayout frame1 = frame;
             saved = frame1.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 100, out);


            save.setVisibility(View.INVISIBLE);
            cross.setVisibility(View.VISIBLE);
            edit.setVisibility(View.INVISIBLE);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public void onEventMainThread(MessageEvent messageEvent) {

        if (messageEvent.message.contains("datetime")) {
            latitude = Prefs.getDouble("lat", 0);
            longitude = Prefs.getDouble("long", 0);
            addresses = getLocationData();
            if(addresses!=null)
            if (addresses.size() > 0) {
                if (addresses.get(0).getAddressLine(0) != null && !addresses.get(0).getAddressLine(0).equals("null"))
                    address=addresses.get(0).getAddressLine(0);
                else
                    address="";

                if (addresses.get(0).getLocality() != null && !addresses.get(0).getLocality().equals("null"))
                    city=addresses.get(0).getLocality();
                else
                    city="";

                if (addresses.get(0).getAdminArea() != null && !addresses.get(0).getAdminArea().equals("null"))
                    state=addresses.get(0).getAdminArea();
                else
                    state="";

                if (addresses.get(0).getCountryName() != null && !addresses.get(0).getCountryName().equals("null"))
                    country=addresses.get(0).getCountryName();
                else
                    country="";

                if (addresses.get(0).getPostalCode() != null && !addresses.get(0).getPostalCode().equals("null"))
                    postalCode=addresses.get(0).getPostalCode();
                else
                    postalCode="";
                coordinates = convert(latitude, longitude);
                Prefs.putString("address", address + ", " + city + ", " + state + ", " + country + ", Postal Code :" + postalCode);
                text.setText(Prefs.getString("time", "") + " | " + Prefs.getString("date", "") + " |\n" + latitude + " " + longitude + " | " + coordinates + " |\n" + Prefs.getString("address", ""));
            }
            EventBus.getDefault().removeStickyEvent(messageEvent);
        } else if (messageEvent.message.contains("latlong")) {
            latitude = Prefs.getDouble("lat", 0);
            longitude = Prefs.getDouble("long", 0);
            addresses = getLocationData();
            if(addresses!=null)
                if (addresses.size() > 0) {
                    if (addresses.get(0).getAddressLine(0) != null && !addresses.get(0).getAddressLine(0).equals("null"))
                        address=addresses.get(0).getAddressLine(0);
                    else
                        address="";

                    if (addresses.get(0).getLocality() != null && !addresses.get(0).getLocality().equals("null"))
                        city=addresses.get(0).getLocality();
                    else
                        city="";

                    if (addresses.get(0).getAdminArea() != null && !addresses.get(0).getAdminArea().equals("null"))
                        state=addresses.get(0).getAdminArea();
                    else
                        state="";

                    if (addresses.get(0).getCountryName() != null && !addresses.get(0).getCountryName().equals("null"))
                        country=addresses.get(0).getCountryName();
                    else
                        country="";

                    if (addresses.get(0).getPostalCode() != null && !addresses.get(0).getPostalCode().equals("null"))
                        postalCode=addresses.get(0).getPostalCode();
                    else
                        postalCode="";
                    coordinates = convert(latitude, longitude);
                    Prefs.putString("address", address + ", " + city + ", " + state + ", " + country + ", Postal Code :" + postalCode);
                    text.setText(Prefs.getString("time", "") + " | " + Prefs.getString("date", "") + " |\n" + latitude + " " + longitude + " | " + coordinates + " |\n" + Prefs.getString("address", ""));
                }
            EventBus.getDefault().removeStickyEvent(messageEvent);
        }
    }




    private class saveImage extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params)
        {
            createFolder();
            return "";
        }

        @Override
        protected void onPostExecute(String result)
        {


                if (saved)
                {

                     pd.hide();
                    String final_adress="";
                    Toast.makeText(getActivity(), "File Saved in folder ", Toast.LENGTH_SHORT).show();
                    if(addresses!=null) {

                         final_adress = address + ", " + city + ", " + state + ", " + country + ", Postal Code :" + postalCode;
                    }
                    pic_details pic_details = new pic_details(longitude, latitude, final_adress, date, time, filename, coordinates, coordinates);
                  //  Toast.makeText(getActivity(), ""+final_adress, Toast.LENGTH_SHORT).show();
                    boolean a = db_locationwise.add_pic_details(pic_details);
                 //    Toast.makeText(getActivity(), "in db saved ?? "+a, Toast.LENGTH_SHORT).show();
                    FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    if (fragmentManager.getBackStackEntryCount() >= 1) {
                        fragmentManager.popBackStackImmediate();
                        fts.commit();
                    }

                     deleteBackupFromImage();
                }
        }

        @Override
        protected void onPreExecute()
        {
            pd.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private void deleteBackupFromImage() {

        try {
            File fichero = new File(paths.get(0));
            File carpeta = fichero.getParentFile();
            for (File file : carpeta.listFiles()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}

