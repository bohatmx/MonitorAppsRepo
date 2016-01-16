package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.ImageUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MonitorProfileListener} interface
 * to handle interaction events.
 * Use the {@link MonitorProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonitorProfileFragment extends Fragment implements PageFragment {


    private MonitorProfileListener mListener;
    MonitorDTO monitor;
    TextView txtName;
    ImageView backDrop, roundImage, iconCamera;
    EditText eFirst, eLast, eAddress, eID, eCell;
    RadioButton radioMale, radioFemale;
    Button btnSave;
    View view;


    public static MonitorProfileFragment newInstance(MonitorDTO monitor) {
        MonitorProfileFragment fragment = new MonitorProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("locationTrackerDTO", monitor);
        fragment.setArguments(args);
        return fragment;
    }

    public MonitorProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            monitor = (MonitorDTO) getArguments().getSerializable("locationTrackerDTO");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_monitor_details, container, false);

        setFields();
        return view;
    }

    private void updateMonitor() {
        MonitorDTO mon = new MonitorDTO();
        mon.setMonitorID(monitor.getMonitorID());

        if (!eFirst.getText().toString().isEmpty()) {
            mon.setFirstName(eFirst.getText().toString());
        }
        if (!eLast.getText().toString().isEmpty()) {
            mon.setLastName(eLast.getText().toString());
        }
        if (!eCell.getText().toString().isEmpty()) {
            mon.setCellphone(eCell.getText().toString());
        }
        if (!eAddress.getText().toString().isEmpty()) {
            mon.setAddress(eAddress.getText().toString());
        }
        if (!eID.getText().toString().isEmpty()) {
            mon.setIDNumber(eID.getText().toString());
        }
        if (!radioMale.isChecked() && !radioFemale.isChecked()) {
            Util.showToast(getActivity(), "Please select your gender");
            return;
        }
        if (radioFemale.isChecked()) {
            mon.setGender(new Short("2"));
        }
        if (radioMale.isChecked()) {
            mon.setGender(new Short("1"));
        }
        RequestDTO w = new RequestDTO(RequestDTO.UPDATE_MONITOR);
        w.setMonitorList(new ArrayList<MonitorDTO>());
        w.getMonitorList().add(mon);

        mListener.setBusy(true);
        NetUtil.sendRequest(getActivity(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.setBusy(false);
                        Util.showToast(getActivity(), "Monitor details have been updated");
                    }
                });
            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.setBusy(false);
                        Util.showErrorToast(getActivity(), message);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }

    private void setFields() {
        txtName = (TextView) view.findViewById(R.id.FMP_name);
        btnSave = (Button) view.findViewById(R.id.FMP_btnSave);

        eFirst = (EditText) view.findViewById(R.id.FMP_editFirstName);
        eLast = (EditText) view.findViewById(R.id.FMP_editLastName);
        eID = (EditText) view.findViewById(R.id.FMP_editID);
        eAddress = (EditText) view.findViewById(R.id.FMP_editAddress);
        eCell = (EditText) view.findViewById(R.id.FMP_editCell);

        roundImage = (ImageView) view.findViewById(R.id.FMP_personImage);
        backDrop = (ImageView) view.findViewById(R.id.FMP_backdrop);
        radioFemale = (RadioButton) view.findViewById(R.id.FMP_radioFemale);
        radioMale = (RadioButton) view.findViewById(R.id.FMP_radioMale);


        roundImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onMonitorPictureRequested(monitor);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateMonitor();
            }
        });
        if (monitor != null) {
            txtName.setText(monitor.getFirstName() + " " + monitor.getLastName());
            eFirst.setText(monitor.getFirstName());
            eLast.setText(monitor.getLastName());
            if (monitor.getCellphone() != null && !monitor.getCellphone().isEmpty()) {
                eCell.setText(monitor.getCellphone());
            }
            if (monitor.getIDNumber() != null && !monitor.getIDNumber().isEmpty()) {
                eID.setText(monitor.getIDNumber());
            }
            if (monitor.getAddress() != null && !monitor.getAddress().isEmpty()) {
                eAddress.setText(monitor.getAddress());
            }
            if (monitor.getGender() != null && monitor.getGender().intValue() == 1) {
                radioMale.setChecked(true);
            }
            if (monitor.getGender() != null && monitor.getGender().intValue() == 2) {
                radioFemale.setChecked(true);
            }
        }
        PhotoUploadDTO x = SharedUtil.getPhoto(getActivity());
        if (x != null) {
            setPicture(x);
        } else {
            getRemotePhotos();
        }
    }

    private void getRemotePhotos() {

        RequestDTO w = new RequestDTO(RequestDTO.GET_MONITOR_PHOTOS);
        w.setMonitorID(SharedUtil.getMonitor(getActivity()).getMonitorID());

        NetUtil.sendRequest(getActivity(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                if (response.getStatusCode() == 0) {
                    if (!response.getPhotoUploadList().isEmpty()) {
                        SharedUtil.savePhoto(getActivity(), response.getPhotoUploadList().get(0));
                        setPicture(response.getPhotoUploadList().get(0));
                    }
                }
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }

    public void setPicture(PhotoUploadDTO photo) {

        if (photo.getThumbFilePath() == null) {
            if (photo.getUri() != null) {
                Picasso.with(getActivity()).load(photo.getUri()).fit().into(backDrop);
                Picasso.with(getActivity()).load(photo.getUri()).fit().into(roundImage);
            }

        } else {
            File f = new File(photo.getThumbFilePath());
            if (f.exists()) {
                try {
                    Bitmap bm = ImageUtil.getBitmapFromUri(getActivity(), Uri.fromFile(f));
                    backDrop.setImageBitmap(bm);
                    roundImage.setImageBitmap(bm);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MonitorProfileListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MonitorProfileListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void animateHeroHeight() {
        Util.expand(backDrop,500,null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        RefWatcher refWatcher = MonApp.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }
    String pageTitle;

    @Override
    public void setPageTitle(String title) {
        pageTitle = title;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    public interface MonitorProfileListener {
        void onProfileUpdated(MonitorDTO monitor);

        void onMonitorPictureRequested(MonitorDTO monitor);

        void setBusy(boolean busy);
    }
    int primaryColor, darkColor;
    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }
}
