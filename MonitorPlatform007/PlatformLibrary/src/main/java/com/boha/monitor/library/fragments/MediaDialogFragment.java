package com.boha.monitor.library.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.platform.library.R;

/**
 * Created by aubreyM on 15/10/17.
 */
public class MediaDialogFragment extends DialogFragment {

    ImageView iconCancel;
    TextView txtVideo, txtPhoto, txtMessage;
    public interface MediaDialogListener {
        void onVideoSelected();
        void onPhotoSelected();
    }

    MediaDialogListener listener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_media, container,
                false);
        getDialog().setTitle("What do you want to do?");

        iconCancel = (ImageView)rootView.findViewById(R.id.DM_cancel);
        txtMessage = (TextView)rootView.findViewById(R.id.DM_message);
        txtVideo = (TextView)rootView.findViewById(R.id.DM_video);
        txtPhoto = (TextView)rootView.findViewById(R.id.DM_photo);

        iconCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        txtPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                listener.onPhotoSelected();
            }
        });
        txtVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dismiss();
                listener.onVideoSelected();
            }
        });


        return rootView;
    }

    public MediaDialogListener getListener() {
        return listener;
    }

    public void setListener(MediaDialogListener listener) {
        this.listener = listener;
    }
}
