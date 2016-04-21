package com.example.manatee.pomodoro;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by manatee on 2016/4/22.
 */
public class CloudFrag extends Fragment implements View.OnClickListener {
    private View fragView;
    private Button uploadButton;
    private Button downloadButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.fragment_cloud, container, false);
        uploadButton = (Button) fragView.findViewById(R.id.Button_upload);
        uploadButton.setOnClickListener(this);
        downloadButton = (Button) fragView.findViewById(R.id.Button_download);
        downloadButton.setOnClickListener(this);
        return fragView;
    }

    @Override
    public void onClick(View v) {
        BmobDataBase remoteDB = new BmobDataBase(fragView.getContext());
        switch (v.getId()){
            case R.id.Button_upload:
                remoteDB.upload();
                break;
            case R.id.Button_download:
                remoteDB.download();
                break;
            default:
                break;
        }
    }
}
