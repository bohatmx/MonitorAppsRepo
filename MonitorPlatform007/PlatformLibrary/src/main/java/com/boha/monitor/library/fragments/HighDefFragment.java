package com.boha.monitor.library.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.platform.library.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PictureListener} interface
 * to handle interaction events.
 */
public class HighDefFragment extends Fragment {

    private PictureListener mListener;

    ImageView image, locationIcon, shareIcon;
    TextView txtText;
    PhotoUploadDTO photo;

    public HighDefFragment() {
    }


    View view;

    public static HighDefFragment newInstance(PhotoUploadDTO photo) {
        HighDefFragment highDefFragment = new HighDefFragment();
        Bundle b = new Bundle();
        b.putSerializable("photo", photo);
        highDefFragment.setArguments(b);
        return highDefFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            photo = (PhotoUploadDTO) getArguments().getSerializable("photo");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_high_def, container, false);

        setFields();
        return view;
    }

    private void setFields() {
        image = (ImageView) view.findViewById(R.id.image);
        locationIcon = (ImageView) view.findViewById(R.id.locationIcon);
        shareIcon = (ImageView) view.findViewById(R.id.shareIcon);

        txtText = (TextView) view.findViewById(R.id.text);

        locationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void setPicture() {

        if (photo.getThumbFilePath() == null) {
            if (photo.getUri() != null) {
                Picasso.with(getActivity())
                        .load(photo.getUri())
                        .centerCrop().resize(600,800)
                        .into(image);

            }

        } else {
            File f = new File(photo.getThumbFilePath());
            if (f.exists()) {
                try {
                    Picasso.with(getContext())
                            .load(f)
                            .centerCrop().resize(600,800)
                            .into(image);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void setPhoto(PhotoUploadDTO photo) {
        this.photo = photo;
        if (image != null) {
            setPicture();
            Date date = new Date(photo.getDateTaken());
            txtText.setText(sdf.format(date));
            if (photo.getLatitude() == null) {
                locationIcon.setVisibility(View.GONE);
            }
        }
    }
    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", loc);
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof VideoListener) {
//            mListener = (VideoListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement VideoListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface PictureListener {

    }
}
