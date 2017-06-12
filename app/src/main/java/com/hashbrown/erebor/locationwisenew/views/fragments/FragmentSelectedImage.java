package com.hashbrown.erebor.locationwisenew.views.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hashbrown.erebor.locationwisenew.R;
import com.hashbrown.erebor.locationwisenew.database.db_locationwise;
import com.hashbrown.erebor.locationwisenew.database.pic_details;
import com.hashbrown.erebor.locationwisenew.utils.AppUtils;
import com.hashbrown.erebor.locationwisenew.utils.MessageEvent;
import com.hashbrown.erebor.locationwisenew.views.activities.Activity_EditDetails;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

import static com.hashbrown.erebor.locationwisenew.utils.AppUtils.getLocationData;
import static java.util.Calendar.getInstance;


/**
 * Created by Erebor on 18/05/17.
 */

public class FragmentSelectedImage extends Fragment {

    private static final String TAG = "selected_image";
    @BindView(R.id.cross)
    ImageView cross;
    @BindView(R.id.edit)
    ImageView edit;
    @BindView(R.id.save)
    ImageView save;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.watermark_image)
    ImageView watermark;
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
    public static FragmentSelectedImage newInstance() {

        Bundle args = new Bundle();

        FragmentSelectedImage fragment = new FragmentSelectedImage();
        fragment.setArguments(args);
        return fragment;
    }

    ArrayList<String> paths = new ArrayList<>();

    public static FragmentSelectedImage newInstance(ArrayList<String> paths) {

        Bundle args = new Bundle();
        args.putStringArrayList("paths", paths);
        FragmentSelectedImage fragment = new FragmentSelectedImage();
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

        //load image
        AppUtils.loadImage(image, paths.get(0),getActivity());

        //latitude longitude and display address
        latitude = Prefs.getDouble("lat", 0);
        longitude = Prefs.getDouble("long", 0);
        addresses = AppUtils.getLocationData(getActivity(),latitude,longitude);
        setAddress();

        //background color of the textview
        aSwitch.setChecked(true);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    text.setBackgroundColor(AppUtils.getColor(getActivity(),R.color.appcolor));

                }
                else
                {
                    text.setBackgroundColor(AppUtils.getColor(getActivity(),R.color.appcolor_transparent));

                }
            }
        });



        return v;
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




    public void createFolder() {
        //create a folder
        path_to_folder = new File("/sdcard/LocationWise/Images");

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
        filename = "sdcard/LocationWise/Images/" + seconds + ".png";
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
        deleteBackupForloc_mid();
        deleteBackupFromImage();
        deleteBackupFromImage_watermark();
        deleteBackupForbichooser();
    }

    public void onEventMainThread(MessageEvent messageEvent) {

        if (messageEvent.message.contains("datetime"))
        {
            latitude = Prefs.getDouble("lat", 0);
            longitude = Prefs.getDouble("long", 0);
            addresses = getLocationData(getActivity(),latitude,longitude);
            setAddress();
            AppUtils.loadImage(watermark,Prefs.getString("watermark",""),getActivity());
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

                         final_adress = address  + city + state  + country  + postalCode;
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
                     Prefs.putString("watermark","");
                     deleteBackupFromImage();
                     deleteBackupFromImage_watermark();
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

    private void deleteBackupFromImage_watermark() {

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
    public void setAddress()
    {
        if(addresses!=null)
            if (addresses.size() > 0) {
                if (addresses.get(0).getAddressLine(0) != null && !addresses.get(0).getAddressLine(0).equals("null"))
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





                coordinates = AppUtils.convert(latitude, longitude);
                Prefs.putString("address", address  + city  + state + country  + postalCode);
                text.setText(Prefs.getString("time", "") + " | " + Prefs.getString("date", "") + " |\n" + latitude + " " + longitude + " | " + coordinates + " |\n" + Prefs.getString("address", ""));
            }
    }

    private void deleteBackupForloc_mid()
    {
        File dir = new File(Environment.getExternalStorageDirectory()+"/tmploc");
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }
    }
    private void deleteBackupForbichooser()
    {
        File dir = new File(Environment.getExternalStorageDirectory()+"/bichooser");
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }
    }

}

