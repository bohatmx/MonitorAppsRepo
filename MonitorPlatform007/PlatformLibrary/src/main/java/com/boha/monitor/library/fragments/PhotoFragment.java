package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.TaskStatusTypeDTO;
import com.boha.platform.library.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoFragment extends Fragment implements PageFragment {

    PhotoUploadDTO photo;
    TextView txtCap1, txtDate, txtNumberRed, txtNumberAmber, txtNumberGreen, txtNumberNone;
    ImageView image;
    int number;
    Context ctx;
    public static PhotoFragment newInstance(PhotoUploadDTO p, int number) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putSerializable("photo", p);
        args.putInt("number",number);
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
            number = getArguments().getInt("number",0);
        }
    }
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.image_full, container, false);
        setFields();

//        Log.d("PhotoFragment", "## onCreateView");
        return view;
    }
    void setFields() {
        image = (ImageView)view.findViewById(R.id.PHOTO_image);
        txtCap1 = (TextView)view.findViewById(R.id.PHOTO_caption);
        txtNumberRed = (TextView)view.findViewById(R.id.PHOTO_numberRed);
        txtNumberAmber = (TextView)view.findViewById(R.id.PHOTO_numberAmber);
        txtNumberGreen = (TextView)view.findViewById(R.id.PHOTO_numberGreen);
        txtNumberNone = (TextView)view.findViewById(R.id.PHOTO_numberNone);
        txtDate = (TextView)view.findViewById(R.id.PHOTO_date);

        txtCap1.setText("");
        txtDate.setText("");

        if (photo.getThumbFilePath() != null) {
            setLocalImage();
        } else {
            setRemoteImage();
        }
        txtDate.setText(df.format(new Date(photo.getDateTaken())));
        if (photo.getTaskName() != null) {
            txtCap1.setText(photo.getTaskName());
        } else {
            if (photo.getProjectName() != null) {
                txtCap1.setText(photo.getProjectName());
            }
        }
        hideColors();
        if (photo.getProjectTaskStatus() != null) {
            switch (photo.getProjectTaskStatus().getTaskStatusType().getStatusColor()) {
                case TaskStatusTypeDTO.STATUS_COLOR_RED:
                    txtNumberRed.setVisibility(View.VISIBLE);
                    txtNumberAmber.setVisibility(View.GONE);
                    txtNumberGreen.setVisibility(View.GONE);
                    txtNumberRed.setText("" + number);
                    break;
                case TaskStatusTypeDTO.STATUS_COLOR_AMBER:
                    txtNumberRed.setVisibility(View.GONE);
                    txtNumberAmber.setVisibility(View.VISIBLE);
                    txtNumberGreen.setVisibility(View.GONE);
                    txtNumberAmber.setText("" + number);
                    break;
                case TaskStatusTypeDTO.STATUS_COLOR_GREEN:
                    txtNumberRed.setVisibility(View.GONE);
                    txtNumberAmber.setVisibility(View.GONE);
                    txtNumberGreen.setVisibility(View.VISIBLE);
                    txtNumberGreen.setText("" + number);
                    break;
            }
        }

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onBigPhotoClicked(photo);
            }
        });

    }
    private void hideColors() {
        txtNumberAmber.setVisibility(View.GONE);
        txtNumberRed.setVisibility(View.GONE);
        txtNumberGreen.setVisibility(View.GONE);
        txtNumberNone.setVisibility(View.GONE);
    }
    public PhotoUploadDTO getPhoto() {
        return photo;
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
        String u = photo.getUri();
        ImageLoader.getInstance().displayImage(u, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                try {
                    image.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.under_construction));
                } catch (Exception e) {
                    Log.w("PhotoFragment", "image failed", e);
                }
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }

    static final SimpleDateFormat df = new SimpleDateFormat("EEEE dd MMMM yyyy HH:mm");
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PhotoFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PhotoFragmentListener");
        }
    }

    PhotoFragmentListener mListener;
    @Override
    public void onDetach() {
        super.onDetach();
//        Log.w("PhotoFragment", "## onDetach");
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
    int primaryColor, darkColor;
    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }
    public interface PhotoFragmentListener {
        void onBigPhotoClicked(PhotoUploadDTO photo);
    }
}
