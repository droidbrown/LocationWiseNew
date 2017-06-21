package com.hashbrown.erebor.locationwisenew.views.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hashbrown.erebor.locationwisenew.R;
import com.hashbrown.erebor.locationwisenew.adapters.Multiple_Images_Adapter_Pager;
import com.hashbrown.erebor.locationwisenew.adapters.Multiple_Images_Adapter_Recycler;
import com.hashbrown.erebor.locationwisenew.database.db_locationwise;
import com.hashbrown.erebor.locationwisenew.database.pic_details;
import com.hashbrown.erebor.locationwisenew.utils.AppUtils;
import com.hashbrown.erebor.locationwisenew.utils.MessageEvent;
import com.hashbrown.erebor.locationwisenew.views.activities.Activity_EditDetails;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

import static com.hashbrown.erebor.locationwisenew.utils.AppUtils.getLocationData;

/**
 * Created by Erebor on 01/06/17.
 */

public class FragmentMultipleSelectedImages extends Fragment {

    @BindView(R.id.selected_image_list)
    RecyclerView selected_image_list;
    @BindView(R.id.frame)
    FrameLayout frame;
    @BindView(R.id.cross)
    ImageView cross;
    @BindView(R.id.edit)
    ImageView edit;
    @BindView(R.id.save)
    ImageView save;
    @BindView(R.id.pager)
    ViewPager pager;
    @BindView(R.id.switch1)
    SwitchCompat aSwitch;
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.next)
    ImageView next;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.watermark_image)
    ImageView watermark;
    Multiple_Images_Adapter_Recycler recycler_adapter;
    Multiple_Images_Adapter_Pager pager_adapter;
    ArrayList<String> multiple_images = new ArrayList<>();
    ArrayList<String> multiple_images_thumbnail = new ArrayList<>();
    String address, city, state, country, postalCode, knownName, date, time, coordinates;
    Double latitude, longitude;
    List<Address> addresses;
    File path_to_file, path_to_folder;
    boolean dir_success = false;
    String filename;
    db_locationwise db_locationwise;
    ProgressDialog pd;
    int item, pos;
    @BindView(R.id.distance_text)
    TextView distance_text;

    public static FragmentMultipleSelectedImages newInstance() {
        
        Bundle args = new Bundle();
        
        FragmentMultipleSelectedImages fragment = new FragmentMultipleSelectedImages();
        fragment.setArguments(args);
        return fragment;
    }
    public static FragmentMultipleSelectedImages newInstance(ArrayList<String> multiple_images) {

        Bundle args = new Bundle();
        args.putStringArrayList("images", multiple_images);
        args.putStringArrayList("thumbnails", multiple_images);
        FragmentMultipleSelectedImages fragment = new FragmentMultipleSelectedImages();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

       // getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        View v = inflater.inflate(R.layout.fragment_multiple_selected_images, container, false);
        ButterKnife.bind(this, v);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        getActivity().getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //database
        db_locationwise = new db_locationwise(getActivity());

        //progress dialog
        pd = new ProgressDialog(getActivity());
        pd.setMessage("Saving...");

        //get images and thumbnails from bundle
        multiple_images = this.getArguments().getStringArrayList("images");
        multiple_images_thumbnail = this.getArguments().getStringArrayList("thumbnails");

        //adapter for pager
        pager_adapter = new Multiple_Images_Adapter_Pager(getActivity(), multiple_images);
        pager.setAdapter(pager_adapter);

        //adapter for recycler view
        recycler_adapter = new Multiple_Images_Adapter_Recycler(getActivity(), multiple_images_thumbnail);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        selected_image_list.setLayoutManager(layoutManager);
        selected_image_list.setHasFixedSize(true);
        selected_image_list.setItemAnimator(new DefaultItemAnimator());
        selected_image_list.setAdapter(recycler_adapter);

        //swithc for text background

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

        //text on image
        latitude = Prefs.getDouble("lat", 0);
        longitude = Prefs.getDouble("long", 0);
        addresses = AppUtils.getLocationData(getActivity(), latitude, longitude);
        setAddress();


        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                if (position == 0 && multiple_images.size()==1) {
                    back.setVisibility(View.INVISIBLE);
                    next.setVisibility(View.INVISIBLE);
                }
                else if (position == 0) {
                    back.setVisibility(View.INVISIBLE);
                    next.setVisibility(View.VISIBLE);
                }
                else if (position == multiple_images.size() - 1) {
                    back.setVisibility(View.VISIBLE);
                    next.setVisibility(View.INVISIBLE);
                } else if (multiple_images.size() == 1) {
                    back.setVisibility(View.INVISIBLE);
                    next.setVisibility(View.INVISIBLE);
                }

                else {
                    back.setVisibility(View.VISIBLE);
                    next.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageSelected(int position) {
                item = pager.getCurrentItem();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return v;
    }

    @OnClick(R.id.cross)
    void onCross() {

        FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() >= 1) {
            fragmentManager.popBackStackImmediate();

            // fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fts.commit();
            deleteBackupFromImage();
            AppUtils.deleteBackupFromImage_watermark();
            AppUtils.deleteBackupForbichooser();
            AppUtils.deleteBackupForloc_mid();
        } else {
            getActivity().finish();
            deleteBackupFromImage();
            AppUtils.deleteBackupFromImage_watermark();
            AppUtils.deleteBackupForbichooser();
            AppUtils.deleteBackupForloc_mid();
        }

    }

    @OnClick(R.id.edit)
    void onEdit() {
        Intent i = new Intent(getActivity(), Activity_EditDetails.class);
        getActivity().startActivity(i);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @OnClick(R.id.back)
    void onBack() {
        pos = pager.getCurrentItem();
        int to_show = pos - 1;
        pager.setCurrentItem(to_show, true);
    }

    @OnClick(R.id.next)
    void onNext() {
        pos = pager.getCurrentItem();
        int to_show = pos + 1;
        pager.setCurrentItem(to_show, true);


    }

    @OnClick(R.id.save)
    void onSave() {
   item = pager.getCurrentItem();
        aSwitch.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);
        cross.setVisibility(View.INVISIBLE);
        edit.setVisibility(View.INVISIBLE);
        next.setVisibility(View.INVISIBLE);
        back.setVisibility(View.INVISIBLE);
        new saveImage().execute();
    }




    public void setAddress() {
        if (addresses != null)
            if (addresses.size() > 0) {
                if (addresses.get(0).getAddressLine(0) != null && !addresses.get(0).getAddressLine(0).equals("null"))
                    address = addresses.get(0).getAddressLine(0) + ",";
                else
                    address = "";

                if (addresses.get(0).getLocality() != null && !addresses.get(0).getLocality().equals("null"))
                    city = addresses.get(0).getLocality() + ",";
                else
                    city = "";

                if (addresses.get(0).getAdminArea() != null && !addresses.get(0).getAdminArea().equals("null"))
                    state = addresses.get(0).getAdminArea() + ",";
                else
                    state = "";

                if (addresses.get(0).getCountryName() != null && !addresses.get(0).getCountryName().equals("null"))
                    country = addresses.get(0).getCountryName() + ",";
                else
                    country = "";

                if (addresses.get(0).getPostalCode() != null && !addresses.get(0).getPostalCode().equals("null"))
                    postalCode = "Postal Code: " + addresses.get(0).getPostalCode();
                else
                    postalCode = "";


                coordinates = AppUtils.convert(latitude, longitude);
                Prefs.putString("address", address + city + state + country + postalCode);
                text.setText(Prefs.getString("time", "") + " | " + Prefs.getString("date", "") + " |\n" + latitude + " " + longitude + " | " + coordinates + " |\n" + Prefs.getString("address", ""));
            }
    }


    public void createFolder() {
        //create a folder
        path_to_folder = new File("/sdcard/LocationWise/LocationWiseImages");

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
        filename = "sdcard/LocationWise/LocationWiseImages/" + seconds + ".png";
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

    boolean saved = false;

    void writeFile() {


        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            frame.setDrawingCacheEnabled(true);

            FrameLayout frame1 = frame;
            saved = frame1.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 100, out);


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

    private class saveImage extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            createFolder();
            return "";
        }

        @Override
        protected void onPostExecute(String result) {


            if (saved) {
                pd.hide();

                try {

                    File fichero = new File(multiple_images.get(item));
                    File carpeta = fichero.getParentFile();
                    for (File file : carpeta.listFiles()) {
                        file.delete();
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }

                // removePage();
                //database
                String final_adress = "";
                Toast.makeText(getActivity(), "File Saved in folder ", Toast.LENGTH_SHORT).show();
                if (addresses != null) {

                    final_adress = address + city + state + country + postalCode;
                }
                pic_details pic_details = new pic_details(longitude, latitude, final_adress, date, time, filename, coordinates, coordinates);
                boolean a = db_locationwise.add_pic_details(pic_details);


                try
                {
                    multiple_images.remove(item);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                if (multiple_images.size() == 0) {
                    //deleteBackupFromImage_watermark();
                    FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    if (fragmentManager.getBackStackEntryCount() >= 1) {
                        fragmentManager.popBackStackImmediate();
                        fts.commit();
                    }
                }
                else {

                    pager_adapter = new Multiple_Images_Adapter_Pager(getActivity(), multiple_images);
                    pager.setAdapter(pager_adapter);
                 //   pager_adapter.notifyDataSetChanged();
                    aSwitch.setVisibility(View.VISIBLE);
                    save.setVisibility(View.VISIBLE);
                    cross.setVisibility(View.VISIBLE);
                    edit.setVisibility(View.VISIBLE);
                    next.setVisibility(View.VISIBLE);
                    back.setVisibility(View.VISIBLE);


                }






            }
        }

        @Override
        protected void onPreExecute() {
            pd.show();


        }

        @Override
        protected void onProgressUpdate(Void... values) {
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
        deleteBackupFromImage();
        AppUtils.deleteBackupFromImage_watermark();
        AppUtils.deleteBackupForbichooser();
        AppUtils.deleteBackupForloc_mid();

    }

    public void onEventMainThread(MessageEvent messageEvent) {

        if (messageEvent.message.contains("datetime")) {
            latitude = Prefs.getDouble("lat", 0);
            longitude = Prefs.getDouble("long", 0);
            AppUtils.loadImage(watermark,Prefs.getString("watermark",""),getActivity());
            addresses = getLocationData(getActivity(), latitude, longitude);
            setAddress();


            if(Prefs.getString("watermark","")!="")
            {
                watermark.setVisibility(View.VISIBLE);
                AppUtils.loadImage(watermark,Prefs.getString("watermark",""),getActivity());

            }
            else
            {
                watermark.setVisibility(View.GONE);
            }
            if(Prefs.getDouble("distance",0)!=0)
            {
                distance_text.setVisibility(View.VISIBLE);


//                //color for distance text
//                int[] color = {AppUtils.getColor(getActivity(),R.color.white),Color.DKGRAY};
//                float[] position = {0, 1};
//                Shader.TileMode tile_mode = Shader.TileMode.REPEAT;
//                LinearGradient lin_grad = new LinearGradient(0, 0, 0, 35,color,position, tile_mode);
//                Shader shader_gradient = lin_grad;
//                distance_text.getPaint().setShader(shader_gradient);

                //value
                double val=Prefs.getDouble("distance",0);
                distance_text.setText( new DecimalFormat("#.##").format(val)+"KM  ");
            }
            EventBus.getDefault().removeStickyEvent(messageEvent);
        }

    }

    private void deleteBackupFromImage() {

        try {
            for (int i = 0; i < multiple_images.size(); i++) {
                File fichero = new File(multiple_images.get(i));
                File carpeta = fichero.getParentFile();
                for (File file : carpeta.listFiles()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
