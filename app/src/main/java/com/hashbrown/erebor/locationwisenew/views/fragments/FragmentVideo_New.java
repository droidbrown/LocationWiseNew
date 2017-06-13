package com.hashbrown.erebor.locationwisenew.views.fragments;

import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hashbrown.erebor.locationwisenew.R;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import life.knowledge4.videotrimmer.K4LVideoTrimmer;
import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener;


public class FragmentVideo_New extends Fragment implements OnTrimVideoListener{

    @BindView(R.id.video)
    K4LVideoTrimmer mVideoTrimmer;
    @BindView(R.id.cancel)
    TextView cancel;
    @BindView(R.id.choose)
    TextView choose;
     Uri trimmed_final=null;
    private ProgressDialog mProgressDialog;
    public static FragmentVideo_New newInstance(String pth) {

        Bundle args = new Bundle();
        args.putString("path",pth);
        FragmentVideo_New fragment = new FragmentVideo_New();
        fragment.setArguments(args);
        return fragment;
    }


    String path;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_video_new,container,false);
        ButterKnife.bind(this,v);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        getActivity().getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        createFolder();
        //path
        path=this.getArguments().getString("path");


        //setting progressbar
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Trimming...");


           if (mVideoTrimmer != null) {
            mVideoTrimmer.setMaxDuration(10);
            mVideoTrimmer.setOnTrimVideoListener(this);
         //   mVideoTrimmer.setOnK4LVideoListener(this);
            mVideoTrimmer.setDestinationPath("/sdcard/tmploc/");
            mVideoTrimmer.setVideoURI(Uri.parse(path));
         //   mVideoTrimmer.setVideoInformationVisibility(true);

        }



        return v;
    }

    @OnClick(R.id.cancel)
    void oncancel()
    {
        FragmentHomeScreen fragment = FragmentHomeScreen.newInstance();
        FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
        fts.replace(R.id.home_coordinate, fragment);
        fts.commit();
    }
    @OnClick(R.id.choose)
    void onchoose()
    {
        afterchoose();
    }

String p="";
    @Override
    public void getResult(final Uri uri) {
        mProgressDialog.cancel();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                p=uri.getPath();


                MediaPlayer mp = new MediaPlayer();
                final String[] orientation = {null};

                try {
                    mp.setDataSource(p);
                    mp.prepare();
                    mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            if(width < height){
                                orientation[0] = "vertical";
                            } else {
                                orientation[0] = "horizontal";
                            }

                           // Toast.makeText(getActivity(), ""+orientation[0], Toast.LENGTH_SHORT).show();
                            Prefs.putString("video_path",p);
                           // Toast.makeText(getActivity(), uri.getPath(), Toast.LENGTH_SHORT).show();
                            FragmentVideo fragment = FragmentVideo.newInstance(Prefs.getString("video_path",""),orientation[0]);
                            FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
                            fts.replace(R.id.home_coordinate, fragment);
                            fts.commit();
                        }
                    });
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });


    }

    @Override
    public void cancelAction() {
        mProgressDialog.cancel();
        mVideoTrimmer.destroy();
        FragmentHomeScreen fragment = FragmentHomeScreen.newInstance();
        FragmentTransaction fts = getActivity().getSupportFragmentManager().beginTransaction();
        fts.replace(R.id.home_coordinate, fragment);
        fts.commit();
    }


    public void onError(final String message) {
        mProgressDialog.cancel();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onTrimStarted() {
        mProgressDialog.show();
    }

    void afterchoose()
    {
        if(trimmed_final!=null)
        {

        }
    }
    public static String getFileExt(String fileName) {
        return "."+fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
    }
    public void createFolder() {
        //create a folder
       File path_to_folder = new File("/sdcard/tmploc");

        if (path_to_folder.exists()) {
            // Toast.makeText(this, "Alreday Exists", Toast.LENGTH_SHORT).show();

        } else {
            boolean dir_success = path_to_folder.mkdirs();
            if (dir_success == true) {
                Prefs.putInt("number", 0);
            }
        }
    }
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
