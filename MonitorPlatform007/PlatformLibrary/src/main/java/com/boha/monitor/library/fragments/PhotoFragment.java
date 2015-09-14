package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.TaskStatusTypeDTO;
import com.boha.platform.library.R;
import com.squareup.leakcanary.RefWatcher;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoFragment extends Fragment implements PageFragment {

    PhotoUploadDTO photo;
    TextView txtCap1, txtDate, txtNumberRed, txtNumberAmber,
            txtNumberGreen, txtNumberNone, txtName;
    ImageView image;
    int number;
    Context ctx;

    public static PhotoFragment newInstance(PhotoUploadDTO p, int number) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putSerializable("photo", p);
        args.putInt("number", number);
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
            number = getArguments().getInt("number", 0);
        }
    }

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.image_full, container, false);
        setFields();

        return view;
    }

    void setFields() {
        image = (ImageView) view.findViewById(R.id.PHOTO_image);
        txtCap1 = (TextView) view.findViewById(R.id.PHOTO_caption);
        txtNumberRed = (TextView) view.findViewById(R.id.PHOTO_numberRed);
        txtNumberAmber = (TextView) view.findViewById(R.id.PHOTO_numberAmber);
        txtNumberGreen = (TextView) view.findViewById(R.id.PHOTO_numberGreen);
        txtNumberNone = (TextView) view.findViewById(R.id.PHOTO_numberNone);
        txtDate = (TextView) view.findViewById(R.id.PHOTO_date);
        txtName = (TextView) view.findViewById(R.id.PHOTO_name);

        txtCap1.setText("");
        txtDate.setText("");
        txtName.setText("");

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
                    txtNumberNone.setVisibility(View.GONE);
                    txtNumberRed.setText("" + number);
                    break;
                case TaskStatusTypeDTO.STATUS_COLOR_AMBER:
                    txtNumberRed.setVisibility(View.GONE);
                    txtNumberNone.setVisibility(View.GONE);
                    txtNumberAmber.setVisibility(View.VISIBLE);
                    txtNumberGreen.setVisibility(View.GONE);
                    txtNumberAmber.setText("" + number);
                    break;
                case TaskStatusTypeDTO.STATUS_COLOR_GREEN:
                    txtNumberRed.setVisibility(View.GONE);
                    txtNumberNone.setVisibility(View.GONE);
                    txtNumberAmber.setVisibility(View.GONE);
                    txtNumberGreen.setVisibility(View.VISIBLE);
                    txtNumberGreen.setText("" + number);
                    break;
            }
        } else {
            txtNumberRed.setVisibility(View.GONE);
            txtNumberAmber.setVisibility(View.GONE);
            txtNumberGreen.setVisibility(View.GONE);
            txtNumberNone.setVisibility(View.VISIBLE);
            txtNumberNone.setText("" + number);
        }
        setTxtName();

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onBigPhotoClicked(photo);
            }
        });

    }

    private void setTxtName() {
        txtName.setVisibility(View.GONE);
        if (photo.getStaffName() != null) {
            txtName.setText(photo.getStaffName());
            txtName.setVisibility(View.VISIBLE);
        }
        if (photo.getMonitorName() != null) {
            txtName.setText(photo.getMonitorName());
            txtName.setVisibility(View.VISIBLE);
        }
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
        Picasso.with(ctx).load(file).into(image);
    }

    private void setRemoteImage() {

        Picasso.with(getActivity())
                .load(photo.getUri())
                .resize(640, 640)
                .into(image);

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
        image = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MonApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
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
