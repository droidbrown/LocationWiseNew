package com.hashbrown.erebor.locationwisenew.views.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.hashbrown.erebor.locationwisenew.R;
import com.hashbrown.erebor.locationwisenew.utils.AppUtils;
import com.hashbrown.erebor.locationwisenew.utils.MessageEvent;
import com.hashbrown.erebor.locationwisenew.views.activities.Activity_EditDetails;
import com.hashbrown.erebor.locationwisenew.views.activities.Activity_HomeScreen;
import com.pixplicity.easyprefs.library.Prefs;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import de.greenrobot.event.EventBus;

import static android.view.View.DRAWING_CACHE_QUALITY_HIGH;
import static com.drivemode.android.typeface.TypefaceHelper.TAG;
import static com.hashbrown.erebor.locationwisenew.utils.AppUtils.getLocationData;
import static java.util.Calendar.getAvailableLocales;
import static java.util.Calendar.getInstance;

/**
 * Created by Erebor on 05/06/17.
 */

public class FragmentVideo extends Fragment {

    @BindView(R.id.video)
    VideoView video;
    @BindView(R.id.cross)
    ImageView cross;
    @BindView(R.id.edit)
    ImageView edit;
    @BindView(R.id.save)
    ImageView save;
    @BindView(R.id.controlbutton)
    ImageView playpauseButton;
    @BindView(R.id.switch1)
    SwitchCompat aSwitch;
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.watermark_image)
    ImageView watermark;
/*    @BindView(R.id.lefttap)
    LinearLayout left_tap;
    @BindView(R.id.righttap)
    LinearLayout right_tap;*/

    @BindView(R.id.ll_bottontext)
    LinearLayout ll_bottontext;


    Double latitude, longitude;
    List<Address> addresses;
    String address, city, state, country, postalCode, knownName, date, time, coordinates;
    int position;
    boolean touch=false;
    String path, orientation;
    View v = null;
    int duration;
    public static FragmentVideo newInstance(String pth, String orientation) {

        Bundle args = new Bundle();
        args.putString("path", pth);
        args.putString("orientation", orientation);
        FragmentVideo fragment = new FragmentVideo();
        fragment.setArguments(args);
        return fragment;
    }
    int heightDevice,widthDevice;
    int heightVideo,widthVideo;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        //path and settting orientation
        path = this.getArguments().getString("path");
        orientation = this.getArguments().getString("orientation");
        if (orientation.equals("horizontal") ) {
            Activity a = getActivity();
            if (a != null)
                a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            v = inflater.inflate(R.layout.fragment_video, container, false);

        } else if (orientation.equals("vertical")) {
            Activity a = getActivity();
            if (a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            v = inflater.inflate(R.layout.fragment_video, container, false);

        }
        //bind knife
        ButterKnife.bind(this, v);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {
            text.setTextSize(8);
            text.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            text.setDrawingCacheEnabled(true);
        }
        text.setDrawingCacheEnabled(true);


       // ((Activity_HomeScreen)getActivity()).hideStatusBar();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        heightDevice = displayMetrics.heightPixels;
        widthDevice= displayMetrics.widthPixels;

        // video initial settigs
        video.setVideoPath(path);
        video.start();
        show();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hide();
                touch=true;
            }
        }, 100);

        //listeners
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                show();
                video.setVideoPath(path);
                playpauseButton.setImageResource(R.drawable.play);

            }
        });

        //text of address
        latitude = Prefs.getDouble("lat", 0);
        longitude = Prefs.getDouble("long", 0);
        addresses = AppUtils.getLocationData(getActivity(), latitude, longitude);
        setAddress();


        //datetime
        DateFormat df = new SimpleDateFormat("HH:mm:ss a");
        DateFormat df1 = new SimpleDateFormat("d MMM yyyy");
        time = df1.format(getInstance().getTime());
        date = df.format(getInstance().getTime());
        Prefs.putString("date", date);
        Prefs.putString("time", time);


        //background color of the textview
        aSwitch.setChecked(true);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    text.setBackgroundColor(AppUtils.getColor(getActivity(), R.color.appcolor));

                } else {
                    text.setBackgroundColor(AppUtils.getColor(getActivity(), R.color.appcolor_transparent));

                }
            }
        });

        //video listeners
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener()  {
            @Override
            public void onPrepared(MediaPlayer mp) {
                 duration = video.getDuration();
              widthVideo=  video.getWidth();
                heightVideo = video.getHeight();
            }
        });

        //video listeners
        video.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent)
            {

                    if (touch==true) {
                        show();
                        touch = false;
                        return false;
                    } else {
                        hide();
                        touch = true;
                        return false;
                    }


            }
        });

        //tap events for left and right views
  /*      left_tap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return left_gesture.onTouchEvent(event);

            }
        });

        right_tap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return right_gesture.onTouchEvent(event);

            }
        });*/
        return v;
    }


    @OnClick(R.id.controlbutton)
    void onControl() {
        if (video.isPlaying())
        {
            video.pause();
            playpauseButton.setImageResource(R.drawable.play);
            position = video.getCurrentPosition();


        }
        else
        {
            playpauseButton.setImageResource(R.drawable.pause);
            video.seekTo(position);
            video.start();

        }
    }
    public void show()
    {
       //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

       // ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        edit.setVisibility(View.VISIBLE);
        save.setVisibility(View.VISIBLE);
        cross.setVisibility(View.VISIBLE);
        aSwitch.setVisibility(View.VISIBLE);
        playpauseButton.setVisibility(View.VISIBLE);
    }
    public void hide()
    {


       // ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        edit.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);
        cross.setVisibility(View.INVISIBLE);
        playpauseButton.setVisibility(View.INVISIBLE);
        aSwitch.setVisibility(View.INVISIBLE);

    }



    @OnClick(R.id.edit)
    void onEdit() {
        video.pause();
        Intent i = new Intent(getActivity(), Activity_EditDetails.class);
        getActivity().startActivity(i);
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }



    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().registerSticky(this);
        }

    }


    public void forward()
    {

        // 5 seconds.
        int SUBTRACT_TIME = 5000;
        position = video.getCurrentPosition();
        if(position - SUBTRACT_TIME > 0 )  {
           video.seekTo(position - SUBTRACT_TIME);
        }
    }

    public void backward()
    {

        // 5 seconds.
        int ADD_TIME = 5000;
        position = video.getCurrentPosition();
        if(position + ADD_TIME < duration)  {
            video.seekTo(position + ADD_TIME);
        }
    }


    final GestureDetector left_gesture = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener(){


        //here is the method for double tap


        @Override
        public boolean onDoubleTap(MotionEvent e) {

            Toast.makeText(getActivity(), "Double tap on left", Toast.LENGTH_SHORT).show();
       backward();

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);

        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }


    });

    final GestureDetector right_gesture = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener(){


        //here is the method for double tap


        @Override
        public boolean onDoubleTap(MotionEvent e) {

            Toast.makeText(getActivity(), "double tap at right", Toast.LENGTH_SHORT).show();
            forward();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);

        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }


    });


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        //destroyMediaProjection();
    }

    public void onEventMainThread(MessageEvent messageEvent) {

        if (messageEvent.message.contains("datetime")) {
            video.start();
            latitude = Prefs.getDouble("lat", 0);
            longitude = Prefs.getDouble("long", 0);
            addresses = getLocationData(getActivity(), latitude, longitude);
            setAddress();
            AppUtils.loadImage(watermark, Prefs.getString("watermark", ""), getActivity());
            EventBus.getDefault().removeStickyEvent(messageEvent);
        }
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

    @OnClick(R.id.cross)
    void oncancel() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/LocationWise/tmploc");
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
        FragmentHomeScreen fragment = FragmentHomeScreen.newInstance();
        FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
        fts.replace(R.id.home_coordinate, fragment);
        fts.commit();
    }




    @OnClick(R.id.save)
    void onSave() {
        video.pause();
        edit.setVisibility(View.INVISIBLE);
        aSwitch.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);
        //output file name
        createFolder();
        long seconds = System.currentTimeMillis();
        String output_filename = "sdcard/LocationWise/LocationWiseVideos/" + seconds + ".mp4";

        //for converting video to audio
        // String[] complexCommand = {"-y", "-i", path, "-vn", "-ar", "44100", "-ac", "2", "-b:a", "256k", "-f", "mp3", "sdcard/loc_video/track.mp3"};
        Bitmap b = text.getDrawingCache();

        try {
            b.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream("/sdcard/LocationWise/tmploc/test.png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String[] cmd=null;
        //final command
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            cmd = new String[]{"-i", path, "-i", "sdcard/LocationWise/tmploc/test.png","-preset","ultrafast","-filter_complex","overlay=0:main_h-overlay_h", output_filename};

        }
        else
        {

        cmd = new String[]{"-i", path, "-i", "sdcard/LocationWise/tmploc/test.png","-preset","ultrafast","-filter_complex","overlay=(main_w-overlay_w)/2:main_h-overlay_h", output_filename};

        }







        loadFFMpegBinary();
        execFFmpegBinary(cmd);


        b.recycle();


    }

    FFmpeg ffmpeg;
    ProgressDialog pd;

    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {
                Log.d(TAG, "ffmpeg : null");
                ffmpeg = FFmpeg.getInstance(getActivity());
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Toast.makeText(getActivity(), "failed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "ffmpeg : correct Loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();

        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    System.out.println("failedreason" + s);
                    Toast.makeText(getActivity(), "failed", Toast.LENGTH_SHORT).show();
                    if (pd.isShowing()) {
                        pd.hide();
                    }

                }

                @Override
                public void onSuccess(String s) {
                    Toast.makeText(getActivity(), "Saved In Gallery", Toast.LENGTH_SHORT).show();

                    if (pd.isShowing()) {
                        pd.hide();
                    }
                    FragmentHomeScreen fragment = FragmentHomeScreen.newInstance();
                    FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
                    fts.replace(R.id.home_coordinate, fragment);
                    fts.commit();

                }


                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + Arrays.toString(command));
                    Log.d(TAG, "progress : " + s);
                    Pattern timePattern = Pattern.compile("(?<=time=)[\\d:.]*");
                    Scanner sc = new Scanner(s);

                    String match = sc.findWithinHorizon(timePattern, 0);
                    if (match != null) {
                        String[] matchSplit = match.split(":");
                        if (duration != 0) {
                            float progress = (Integer.parseInt(matchSplit[0]) * 3600 +
                                    Integer.parseInt(matchSplit[1]) * 60 +
                                    Float.parseFloat(matchSplit[2])) / duration;
                            float showProgress = (progress * 100);
                            Log.d(TAG, "=======PROGRESS======== " + showProgress);
                            String numberD = String.valueOf(showProgress);
                            numberD = numberD.substring ( numberD.indexOf ( "." ) );
                            String upToNCharacters = numberD.substring(0, Math.min(numberD.length(), 4));
                            upToNCharacters=upToNCharacters.replace(".","");
                            upToNCharacters=upToNCharacters.replaceFirst("^0+(?!$)", "");
                            int val=Integer.parseInt(upToNCharacters);
                            pd.setProgress(val);


                        }


                    }



                }

                @Override
                public void onStart() {
                    if (pd == null) {
                        pd = new ProgressDialog(getActivity());
                        pd.setMessage("Processing Video...");
                        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        pd.setProgress(0);
                        pd.show();

                        pd.setCancelable(false);
                    } else {
                        pd.setCancelable(false);
                        pd.show();
                    }
                    System.out.println("started " + "started");

                }

                @Override
                public void onFinish() {
                   // Toast.makeText(getActivity(), "Saving Failed", Toast.LENGTH_SHORT).show();
                    if (pd.isShowing()) {
                        pd.hide();
                    }
                    FragmentHomeScreen fragment = FragmentHomeScreen.newInstance();
                    FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
                    fts.replace(R.id.home_coordinate, fragment);
                    fts.commit();


                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
// do nothing for now
        }
    }
    public void createFolder() {
        //create a folder
        File path_to_folder = new File("/sdcard/LocationWise/LocationWIseVideos");

        if (path_to_folder.exists()) {
            // Toast.makeText(this, "Alreday Exists", Toast.LENGTH_SHORT).show();

        } else {
          boolean  dir_success = path_to_folder.mkdirs();
            if (dir_success == true) {
                Prefs.putInt("number", 0);


            }
        }
    }

//    //save
//
//    private static final String TAG = "MainActivity";
//    private static final int REQUEST_CODE = 1000;
//    private int mScreenDensity;
//    private MediaProjectionManager mProjectionManager;
//    private static final int DISPLAY_WIDTH = 720;
//    private static final int DISPLAY_HEIGHT = 1280;
//    private MediaProjection mMediaProjection;
//    private VirtualDisplay mVirtualDisplay;
//    private MediaProjectionCallback mMediaProjectionCallback;
//
//    private MediaRecorder mMediaRecorder;
//    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
//    private static final int REQUEST_PERMISSIONS = 10;
//
//    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 90);
//        ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        ORIENTATIONS.append(Surface.ROTATION_180, 270);
//        ORIENTATIONS.append(Surface.ROTATION_270, 180);
//    }
//
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public void save()
//    {
//        DisplayMetrics metrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        mScreenDensity = metrics.densityDpi;
//
//        mMediaRecorder = new MediaRecorder();
//
//        mProjectionManager = (MediaProjectionManager) getActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
//
//
//        //start record
//        initRecorder();
//        shareScreen();
//        video.start();
//
//
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void shareScreen() {
//        if (mMediaProjection == null) {
//            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
//            return;
//        }
//        mVirtualDisplay = createVirtualDisplay();
//        mMediaRecorder.start();
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private VirtualDisplay createVirtualDisplay() {
//        return mMediaProjection.createVirtualDisplay("MainActivity",
//                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
//                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
//                mMediaRecorder.getSurface(), null /*Callbacks*/, null
//                /*Handler*/);
//    }
//
//    private void initRecorder() {
//        try {
//            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
//            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            mMediaRecorder.setOutputFile(Environment
//                    .getExternalStoragePublicDirectory(Environment
//                            .DIRECTORY_DOWNLOADS) + "/video.mp4");
//            mMediaRecorder.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
//            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//            mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
//            mMediaRecorder.setVideoFrameRate(30);
//            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
//            int orientation = ORIENTATIONS.get(rotation + 90);
//            mMediaRecorder.setOrientationHint(orientation);
//            mMediaRecorder.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private class MediaProjectionCallback extends MediaProjection.Callback {
//        @Override
//        public void onStop() {
//
//                mMediaRecorder.stop();
//                mMediaRecorder.reset();
//                Log.v(TAG, "Recording Stopped");
//
//            mMediaProjection = null;
//            stopScreenSharing();
//        }
//    }
//
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void stopScreenSharing() {
//        if (mVirtualDisplay == null) {
//            return;
//        }
//        mVirtualDisplay.release();
//        //mMediaRecorder.release(); //If used: mMediaRecorder object cannot
//        // be reused again
//        destroyMediaProjection();
//    }
//
//
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void destroyMediaProjection() {
//        if (mMediaProjection != null) {
//            mMediaProjection.unregisterCallback(mMediaProjectionCallback);
//            mMediaProjection.stop();
//            mMediaProjection = null;
//        }
//        Log.i(TAG, "MediaProjection Stopped");
//    }
//
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode != REQUEST_CODE) {
//            Log.e(TAG, "Unknown request code: " + requestCode);
//            return;
//        }
//        if (resultCode != RESULT_OK) {
//            Toast.makeText(getActivity(),
//                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
//
//            return;
//        }
//        mMediaProjectionCallback = new MediaProjectionCallback();
//        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
//        mMediaProjection.registerCallback(mMediaProjectionCallback, null);
//        mVirtualDisplay = createVirtualDisplay();
//        mMediaRecorder.start();
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        Activity a = getActivity();
//        if(a != null) a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//    }
}
