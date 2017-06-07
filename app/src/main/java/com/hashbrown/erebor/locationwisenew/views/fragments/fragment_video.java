package com.hashbrown.erebor.locationwisenew.views.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.hashbrown.erebor.locationwisenew.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Erebor on 05/06/17.
 */

public class fragment_video extends Fragment {
    public static fragment_video newInstance() {

        Bundle args = new Bundle();

        fragment_video fragment = new fragment_video();
        fragment.setArguments(args);
        return fragment;
    }


    String path;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_video,container,false);
        ButterKnife.bind(this,v);



        return v;
    }
}
