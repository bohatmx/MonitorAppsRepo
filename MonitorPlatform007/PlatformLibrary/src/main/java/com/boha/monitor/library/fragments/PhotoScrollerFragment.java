package com.boha.monitor.library.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.TaskStatusTypeDTO;
import com.boha.platform.library.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.view.GestureDetector.SimpleOnGestureListener;

/**
 * Fragment that allows user to swipe through a list of PhotoUploadDTO
 * Activities that contain this fragment must implement the
 * {@link PhotoListener} interface
 * to handle interaction events.
 * Use the {@link PhotoScrollerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhotoScrollerFragment extends Fragment implements View.OnTouchListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PHOTOS = "photos";
    TextView txtCap1, txtDate, txtNumberRed, txtNumberAmber,
            txtNumberGreen, txtNumberNone, txtName;
    ImageView image;
    int number, position;
    Context ctx;
    View view, photoAndCaption;
    private List<PhotoUploadDTO> photoList;
    private PhotoUploadDTO photo;
    private PhotoListener mListener;

    public PhotoScrollerFragment() {
    }

    /**
     * @param resp
     * @return
     */
    public static PhotoScrollerFragment newInstance(ResponseDTO resp, int position) {
        PhotoScrollerFragment fragment = new PhotoScrollerFragment();
        Bundle args = new Bundle();
        args.putSerializable(PHOTOS, resp);
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ResponseDTO r = (ResponseDTO) getArguments().getSerializable(PHOTOS);
            photoList = r.getPhotoUploadList();
            position = getArguments().getInt("position");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_photo_scroller, container, false);
        gDetect = new GestureDetectorCompat(getActivity(), new GestureListener(new SwipeListener() {
            @Override
            public void onForwardSwipe() {
                if (position == photoList.size() - 1) {
                    Snackbar.make(image, "No more photographs this way", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                position++;
                if (position < photoList.size()) {
                    setPosition(position);
                    photoAndCaption.startAnimation(AnimationUtils
                            .loadAnimation(getActivity(), R.anim.slide_in_right));
                    return;
                }

            }

            @Override
            public void onBackwardSwipe() {
                if (position == 0) {
                    Snackbar.make(image, "No more photographs this way", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                position--;
                if (position > -1) {
                    setPosition(position);
                    photoAndCaption.startAnimation(AnimationUtils
                            .loadAnimation(getActivity(), R.anim.slide_in_left));
                    return;
                }
            }
        }));

        setFields();
        setPosition(position);
        return view;
    }

    void setFields() {
        image = (ImageView) view.findViewById(R.id.PHOTO_image);
        photoAndCaption = view.findViewById(R.id.PHOTO_top);
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

        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gDetect.onTouchEvent(motionEvent);
                return true;
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

    public void setPosition(int position) {
        this.position = position;
        if (photoList == null) {
            return;
        }
        hideColors();
        if (position < photoList.size()) {
            photo = photoList.get(position);
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
            number = photoList.size() - position;
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
        }

    }

    private void setLocalImage() {
        File file = new File(photo.getThumbFilePath());
        Picasso.with(ctx).load(file).fit().into(image);
    }


    private void setRemoteImage() {

        Picasso.with(getActivity())
                .load(photo.getUri())

                .into(image);

        ImageView tempImage = new ImageView(getActivity());
        try {
            int index = position - 1;
            if (index > -1) {
                Picasso.with(getActivity())
                        .load(photoList.get(index).getUri())
                        .into(tempImage);
            }
            index = position - 2;
            if (index > -1) {
                Picasso.with(getActivity())
                        .load(photoList.get(index).getUri())
                        .into(tempImage);
            }
            index = position + 1;
            if (index < photoList.size()) {
                Picasso.with(getActivity())
                        .load(photoList.get(index).getUri())
                        .into(tempImage);
            }
            index = position + 2;
            if (index < photoList.size()) {
                Picasso.with(getActivity())
                        .load(photoList.get(index).getUri())
                        .into(tempImage);
            }
        } catch (Exception e) {
            Log.e(TAG, "force cache failed: ", e);
        }

    }

    static final SimpleDateFormat df = new SimpleDateFormat("EEEE dd MMMM yyyy HH:mm");

    public void setPhotoList(List<PhotoUploadDTO> photoList) {
        this.photoList = photoList;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PhotoListener) {
            mListener = (PhotoListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PhotoListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        gDetect.onTouchEvent(motionEvent);
        return true;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface PhotoListener {
        void onPhotoClicked(PhotoUploadDTO photo);
    }

    public interface SwipeListener {
        void onForwardSwipe();

        void onBackwardSwipe();
    }

    private GestureDetectorCompat gDetect;

    public class GestureListener extends SimpleOnGestureListener {
        private float flingMin = 100;
        private float velocityMin = 100;
        private SwipeListener listener;

        public GestureListener(SwipeListener listener) {
            this.listener = listener;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            boolean forward = false;
            boolean backward = false;
            //calculate the change in X position within the fling gesture
            float horizontalDiff = event2.getX() - event1.getX();
            float verticalDiff = event2.getY() - event1.getY();

            float absHDiff = Math.abs(horizontalDiff);
            float absVDiff = Math.abs(verticalDiff);
            float absVelocityX = Math.abs(velocityX);
            float absVelocityY = Math.abs(velocityY);

            if (absHDiff > absVDiff && absHDiff > flingMin && absVelocityX > velocityMin) {
                if (horizontalDiff > 0)
                    backward = true;
                else
                    forward = true;
            } else if (absVDiff > flingMin && absVelocityY > velocityMin) {
                if (verticalDiff > 0)
                    backward = true;
                else
                    forward = true;
            }
            if (forward) {
                listener.onForwardSwipe();
            } else if (backward) {
                listener.onBackwardSwipe();
            }

            return true;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

    }

    private static final String TAG = "PhotoScrollerFragment";
}
