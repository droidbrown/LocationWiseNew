package com.hashbrown.erebor.locationwisenew.views.fragments;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hashbrown.erebor.locationwisenew.R;
import com.hashbrown.erebor.locationwisenew.utils.AppUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

;/**
 * Created by Erebor on 06/06/17.
 */

public class FragmentMultipleClickImagesOld extends Fragment
{
    public static FragmentMultipleClickImagesOld newInstance(int id) {
        
        Bundle args = new Bundle();
        args.putInt("id",id);
        FragmentMultipleClickImagesOld fragment = new FragmentMultipleClickImagesOld();
        fragment.setArguments(args);
        return fragment;
    }
    @BindView(R.id.btn_takepicture)
    ImageView captureButton;
    @BindView(R.id.camera_preview)
    FrameLayout preview;
    @BindView(R.id.done)
    ImageView done;
    @BindView(R.id.count)
    ImageView count;
    @BindView(R.id.count_number)
    TextView count_number;
    @BindView(R.id.changecamera)
    ImageView changecamera;
    private static Camera mCamera;
    private CameraPreview mPreview;
    static ArrayList<String> filenames=new ArrayList<>();
    static int i=0;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_multiple_click_old,container,false);

        //bind views
        ButterKnife.bind(this,v);

        count_number.setText(String.valueOf(i));
        try
        {
            int l = filenames.size();
            if(l>1)
            {
                done.setVisibility(View.VISIBLE);
                count.setVisibility(View.VISIBLE);
            }
            AppUtils.loadImage(count, filenames.get(l - 1), getActivity());
        }
        catch (Exception e)
        {

        }
        // Create an instance of Camera
        findFrontFacingCamera();
        camid=this.getArguments().getInt("id");
        mCamera = this.getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(getActivity(), mCamera,camid);
        preview.addView(mPreview);

        if(Camera.getNumberOfCameras()==0)
        {
            changecamera.setVisibility(View.GONE);
        }



        return v;
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {

            c = Camera.open(camid);

            Camera.Parameters params = c.getParameters();
           if(camid == Camera.CameraInfo.CAMERA_FACING_BACK)
           {
               params.setRotation(90);

           }
           else
            {
                params.setRotation(270);

            }
            c.setParameters(params);// attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            try {
                createFolder(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @OnClick(R.id.btn_takepicture)
    void takepicture()
    {
        mCamera.takePicture(null, null, mPicture);
    }
    @OnClick(R.id.cancel)
    void oncancel() {
        deleteBackupFromImage();
        FragmentHomeScreen fragment = FragmentHomeScreen.newInstance();
        FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
        fts.replace(R.id.home_coordinate, fragment);
        fts.commit();

        i=0;
        filenames.clear();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        deleteBackupFromImage();
    }

    @OnClick(R.id.done)
    void onDone()
    {
        mCamera.stopPreview();
        for (int i = 0; i < filenames.size(); i++) {
            System.out.println(filenames.get(i));
        }

        FragmentMultipleSelectedImages fragment = FragmentMultipleSelectedImages.newInstance(filenames);
        FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
        fts.replace(R.id.home_coordinate, fragment);

        fts.commit();
    }


    //for saving
    public void createFolder(byte[] bytes) throws IOException {
        //create a folder
        File path_to_folder = new File("/sdcard/tmploc");

        if (path_to_folder.exists()) {
            // Toast.makeText(this, "Alreday Exists", Toast.LENGTH_SHORT).show();
            manageFile(bytes);
        } else {
            boolean dir_success = path_to_folder.mkdirs();
            if (dir_success == true) {

                manageFile(bytes);

            } else {
                //Toast.makeText(this, "Folder Not Created", Toast.LENGTH_SHORT).show();
            }
        }
    }

    String filename;

    public void manageFile(byte[] bytes) throws IOException {

        //name with milli seconds
        long seconds = System.currentTimeMillis();
        filename = "sdcard/tmploc/" + seconds + ".png";
        filenames.add(filename);
        File path_to_file = new File(filename);
        if (path_to_file.exists()) {
            boolean delete = path_to_file.delete();
            if (delete == true) {
                writeFile(bytes);

            }

        } else {
            writeFile(bytes);

        }
    }

    void writeFile(byte[] bytes) throws IOException {
        OutputStream output = null;
        try {
            output = new FileOutputStream(filename);
            output.write(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            if (null != output) {
                done.setVisibility(View.VISIBLE);
                count.setVisibility(View.VISIBLE);
                i=i+1;
                count_number.setText(String.valueOf(i));
                Glide.with(getActivity()).load(Uri.fromFile(new File(filename))).dontAnimate().into(count);
                mCamera.startPreview();
                output.close();
            }
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.release();
        }
deleteBackupFromImage();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
        }
    }



    @OnClick(R.id.changecamera)
    void onchange()
    {


        if(camid == Camera.CameraInfo.CAMERA_FACING_BACK){
            camid = Camera.CameraInfo.CAMERA_FACING_FRONT;
            FragmentMultipleClickImagesOld fragment = FragmentMultipleClickImagesOld.newInstance(camid);
            FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
            fts.replace(R.id.home_coordinate, fragment);
            //fts.addToBackStack(fragment.getClass().getSimpleName());
            fts.commit();
        }
        else {
            camid = Camera.CameraInfo.CAMERA_FACING_BACK;
            FragmentMultipleClickImagesOld fragment = FragmentMultipleClickImagesOld.newInstance(camid);
            FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
            fts.replace(R.id.home_coordinate, fragment);
            // fts.addToBackStack(fragment.getClass().getSimpleName());
            fts.commit();
        }






    }


     int front=7;
    static int camid=7;
     int back;
    public void findFrontFacingCamera() {

        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                front = i;
                System.out.println("front id"+front);
            }
            else if(info.facing==CameraInfo.CAMERA_FACING_BACK)
            {
                back=i;System.out.println("back id"+back);
            }
        }

    }


    private void deleteBackupFromImage() {

        try {
            for (int i = 0; i < filenames.size(); i++) {
                File fichero = new File(filenames.get(i));
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
