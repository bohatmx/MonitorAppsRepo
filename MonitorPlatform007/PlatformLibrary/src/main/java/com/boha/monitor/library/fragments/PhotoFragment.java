package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;

public class PhotoFragment extends Fragment implements PageFragment {

    PhotoUploadDTO photo;
    public static final int
            PHOTO_LOCAL = 1,
            PHOTO_REMOTE = 2;
    int type;
    String caption1, caption2;
    TextView txtCap1, txtCap2;
    ImageView image;
    String url;
    Context ctx;
    public static PhotoFragment newInstance(PhotoUploadDTO p) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putSerializable("photo", p);
        fragment.setArguments(args);
        return fragment;
    }

    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            photo = (PhotoUploadDTO) getArguments().getSerializable("photo");
        }
    }
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.image_full, container, false);
        if (getArguments() != null) {
            photo = (PhotoUploadDTO) getArguments().getSerializable("photo");
        }
        setFields();

        return view;
    }
    void setFields() {
        image = (ImageView)view.findViewById(R.id.IMI_image);
        txtCap1 = (TextView)view.findViewById(R.id.IMI_caption1);
        txtCap2 = (TextView)view.findViewById(R.id.IMI_caption2);
        txtCap1.setText("");
        txtCap2.setText("");
        setCaptions();

        if (photo.getThumbFilePath() != null) {
            setLocalImage();
        } else {
            setRemoteImage();
        }

        if (caption1 != null) {
            txtCap1.setText(caption1);
        }
        if (caption2 != null) {
            txtCap2.setText(caption2);
        }
    }
    private void setCaptions() {
        if (photo.getStaffID() != null) {
            caption1 = "Staff Profile Picture";
            caption2 = "";
        }
        if (photo.getProjectTaskID() != null) {
            caption1 = "Project Task Status";
            caption2 = "";
        }
        if (photo.getMonitorID() != null) {
            caption1 = "Monitor Profile Picture";
            caption2 = "";
        }
        if (photo.getProjectID() != null) {
            caption1 = "Project Image";
            caption2 = "";
        }
    }
    private void setLocalImage() {
        File file = new File(photo.getThumbFilePath());
        ImageLoader.getInstance().displayImage(Uri.fromFile(file).toString(), image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                image.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.under_construction));
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }
    private void setRemoteImage() {
        String u = Util.getPhotoURL(photo);
        ImageLoader.getInstance().displayImage(u, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                image.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.under_construction));
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void animateHeroHeight() {

    }

    @Override
    public void setPageTitle(String title) {

    }

    @Override
    public String getPageTitle() {
        return null;
    }
}
