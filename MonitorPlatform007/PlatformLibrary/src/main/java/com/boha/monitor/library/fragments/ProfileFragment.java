package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
import com.boha.monitor.library.dto.Person;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.ImageUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileListener} interface
 * to handle interaction events.
 */
public class ProfileFragment extends Fragment implements PageFragment {


    private ProfileListener mListener;
    MonitorDTO monitor;
    StaffDTO staff;

    TextView txtName, txtTile;
    ImageView backDrop, roundImage, iconCamera;
    EditText eFirst, eLast, eAddress, eID, eCell, eMail;
    RadioButton radioMale, radioFemale,
            radioActive, radioInactive;
    Button btnSave;
    View view, box3,box5;

    public static final int ADD_PERSON = 1, UPDATE_PERSON = 2,
            MONITOR = 3, STAFF = 4, TAKE_SELFIE_FOR_PROFILE = 6;
    int editType;
    int personType, takeSelfie;
    String firstName, lastName;
    MonApp monApp;

    public void setMonApp(MonApp monApp) {
        this.monApp = monApp;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            monitor = (MonitorDTO) getArguments().getSerializable("monitor");
            editType = getArguments().getInt("editType", ADD_PERSON);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_monitor_details, container, false);

        setFields();
        return view;
    }

    private void sendPersonData() {
        RequestDTO w = new RequestDTO();
        switch (personType) {
            case MONITOR:
                MonitorDTO mon = new MonitorDTO();
                if (monitor != null) {
                    mon.setMonitorID(monitor.getMonitorID());
                    w.setRequestType(RequestDTO.UPDATE_MONITOR);
                } else {
                    w.setRequestType(RequestDTO.ADD_MONITORS);
                    mon.setActiveFlag(1);
                }
                mon.setCompanyID(SharedUtil.getCompany(getActivity()).getCompanyID());
                if (!eFirst.getText().toString().isEmpty()) {
                    mon.setFirstName(eFirst.getText().toString());
                } else {
                    Util.showToast(getActivity(), "Please enter First Name");
                }
                if (!eLast.getText().toString().isEmpty()) {
                    mon.setLastName(eLast.getText().toString());
                } else {
                    Util.showToast(getActivity(), "Please enter Last Name");
                }
                if (!eMail.getText().toString().isEmpty()) {
                    mon.setEmail(eMail.getText().toString());
                } else {
                    Util.showToast(getActivity(), "Please enter Last Name");
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
                if (radioActive.isChecked()) {
                    mon.setActiveFlag(1);
                }
                if (radioInactive.isChecked()) {
                    mon.setActiveFlag(0);
                }


                w.setMonitorList(new ArrayList<MonitorDTO>());
                w.getMonitorList().add(mon);
                break;
            case STAFF:
                StaffDTO s = new StaffDTO();
                if (staff != null) {
                    s.setStaffID(staff.getStaffID());
                    w.setRequestType(RequestDTO.UPDATE_COMPANY_STAFF);
                } else {
                    w.setRequestType(RequestDTO.ADD_STAFF);
                    s.setActiveFlag(1);
                }
                s.setCompanyID(SharedUtil.getCompany(getActivity()).getCompanyID());
                if (!eFirst.getText().toString().isEmpty()) {
                    s.setFirstName(eFirst.getText().toString());
                } else {
                    Util.showToast(getActivity(), "Please enter First Name");
                }
                if (!eLast.getText().toString().isEmpty()) {
                    s.setLastName(eLast.getText().toString());
                } else {
                    Util.showToast(getActivity(), "Please enter Last Name");
                }
                if (!eMail.getText().toString().isEmpty()) {
                    s.setEmail(eMail.getText().toString());
                } else {
                    Util.showToast(getActivity(), "Please enter Last Name");
                }
                if (!eCell.getText().toString().isEmpty()) {
                    s.setCellphone(eCell.getText().toString());
                }
                if (radioActive.isChecked()) {
                    s.setActiveFlag(1);
                }
                if (radioInactive.isChecked()) {
                    s.setActiveFlag(0);
                }

                w.setStaffList(new ArrayList<StaffDTO>());
                w.getStaffList().add(s);
                break;

        }


        btnSave.setVisibility(View.GONE);
        mListener.setBusy(true);

        NetUtil.sendRequest(getActivity(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.setBusy(false);
                        Log.w("", "Person has been saved or updated on server");
                        if (editType == UPDATE_PERSON) {
                            Util.showToast(getActivity(), "Person details have been updated");
                        } else {
                            Util.showToast(getActivity(), "Person details have been added");
                            switch (personType) {
                                case MONITOR:
                                    if (monitor == null) {
                                        MonitorDTO mon = response.getMonitorList().get(0);
                                        Util.sendAppInvitation(getActivity(), mon.getFullName(), mon.getEmail(),
                                                mon.getPin(), Util.MONITOR);
                                        Snappy.addMonitor(monApp, mon, new Snappy.SnappyWriteListener() {
                                            @Override
                                            public void onDataWritten() {
                                                Log.e("ProfileFragment", "Monitor added to disk cache");
                                            }

                                            @Override
                                            public void onError(String message) {

                                            }
                                        });
                                        mListener.onAdded(mon);
                                    } else {
                                        monitor = response.getMonitorList().get(0);
                                        mListener.onUpdated(monitor);
                                    }


                                    break;
                                case STAFF:
                                    if (staff == null) {
                                        StaffDTO x = response.getStaffList().get(0);
                                        Util.sendAppInvitation(getActivity(), x.getFullName(), x.getEmail(),
                                                x.getPin(), Util.STAFF);
                                        Snappy.addStaff(monApp, x, new Snappy.SnappyWriteListener() {
                                            @Override
                                            public void onDataWritten() {
                                                Log.e("ProfileFragment", "Monitor added to disk cache");
                                            }

                                            @Override
                                            public void onError(String message) {

                                            }
                                        });
                                        mListener.onAdded(x);
                                    } else {
                                        staff = response.getStaffList().get(0);
                                        mListener.onUpdated(staff);
                                    }


                                    break;
                            }


                        }
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

        });
    }


    private void setPersonName() {
            txtName.setText(eFirst.getText().toString() + " " + eLast.getText().toString());
    }
    private void setFields() {
        box3 = view.findViewById(R.id.box3);
        box5 = view.findViewById(R.id.box5);
        txtTile = (TextView) view.findViewById(R.id.FMP_title);
        txtName = (TextView) view.findViewById(R.id.FMP_person);
        txtName.setText("");
        btnSave = (Button) view.findViewById(R.id.FMP_btnSave);

        eFirst = (EditText) view.findViewById(R.id.FMP_editFirstName);
        eLast = (EditText) view.findViewById(R.id.FMP_editLastName);
        eID = (EditText) view.findViewById(R.id.FMP_editID);
        eAddress = (EditText) view.findViewById(R.id.FMP_editAddress);
        eCell = (EditText) view.findViewById(R.id.FMP_editCell);
        eMail = (EditText) view.findViewById(R.id.FMP_editEmail);

        roundImage = (ImageView) view.findViewById(R.id.FMP_personImage);
        backDrop = (ImageView) view.findViewById(R.id.FMP_backdrop);
        radioFemale = (RadioButton) view.findViewById(R.id.FMP_radioFemale);
        radioMale = (RadioButton) view.findViewById(R.id.FMP_radioMale);
        radioActive = (RadioButton) view.findViewById(R.id.FMP_radioActive);
        radioInactive = (RadioButton) view.findViewById(R.id.FMP_radioInactive);

        roundImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (monitor != null)
                    mListener.onPictureRequested(monitor);
                if (staff != null)
                    mListener.onPictureRequested(staff);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPersonData();
            }
        });
        eFirst.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setPersonName();
                btnSave.setVisibility(View.VISIBLE);
            }
        });
        eLast.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setPersonName();
                btnSave.setVisibility(View.VISIBLE);
            }
        });
        switch (personType) {
            case MONITOR:
                txtTile.setText("Monitor");
                break;
            case STAFF:
                txtTile.setText("Supervisor");
                radioMale.setVisibility(View.GONE);
                radioFemale.setVisibility(View.GONE);
                box3.setVisibility(View.GONE);
                box5.setVisibility(View.GONE);
                break;
        }
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
            if (monitor.getEmail() != null && !monitor.getEmail().isEmpty()) {
                eMail.setText(monitor.getEmail());
            }
            if (monitor.getGender() != null && monitor.getGender().intValue() == 1) {
                radioMale.setChecked(true);
            }
            if (monitor.getGender() != null && monitor.getGender().intValue() == 2) {
                radioFemale.setChecked(true);
            }
            if (monitor.getPhotoUploadList().isEmpty()) {
                getPhotosFromCache();
            } else {
                List<PhotoUploadDTO> mList = filter(monitor.getPhotoUploadList());
                if (!mList.isEmpty()) {
                    setPicture(mList.get(0));
                }
            }
        }
        if (staff != null) {
            txtName.setText(staff.getFullName());
            eFirst.setText(staff.getFirstName());
            eLast.setText(staff.getLastName());
            if (staff.getCellphone() != null && !staff.getCellphone().isEmpty()) {
                eCell.setText(staff.getCellphone());
            }


            if (staff.getEmail() != null && !staff.getEmail().isEmpty()) {
                eMail.setText(staff.getEmail());
            }


            if (staff.getPhotoUploadList().isEmpty()) {
                getPhotosFromCache();
            } else {
                List<PhotoUploadDTO> mList = filter(staff.getPhotoUploadList());
                if (!mList.isEmpty()) {
                    setPicture(mList.get(0));
                }
            }


        }

    }

    private List<PhotoUploadDTO> filter(List<PhotoUploadDTO> list) {
        List<PhotoUploadDTO> mList = new ArrayList<>();
        for (PhotoUploadDTO p: list) {
            if (p.getPictureType() == PhotoUploadDTO.STAFF_IMAGE
                    || p.getPictureType() == PhotoUploadDTO.MONITOR_IMAGE)
            mList.add(p);
        }
        return mList;
    }
    List<PhotoUploadDTO> profilePhotos;

    private void getPhotosFromCache() {
        if (monitor != null) {
            Snappy.getMonitorProfilePhotoList(monApp, monitor.getMonitorID(), new Snappy.PhotoListener() {
                @Override
                public void onPhotoAdded() {

                }

                @Override
                public void onPhotosFound(final List<PhotoUploadDTO> list) {
                    profilePhotos = list;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!list.isEmpty()) {
                                Collections.sort(list);
                                setPicture(list.get(0));
                            } else {
                                getRemotePhotos();
                            }
                        }
                    });
                }

                @Override
                public void onError(String message) {

                }
            });
        } else {
            if (staff != null) {
                Snappy.getStaffProfilePhotoList(monApp, staff.getStaffID(), new Snappy.PhotoListener() {
                    @Override
                    public void onPhotoAdded() {

                    }

                    @Override
                    public void onPhotosFound(final List<PhotoUploadDTO> list) {
                        profilePhotos = list;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!list.isEmpty()) {
                                    Collections.sort(list);
                                    PhotoUploadDTO photo = list.get(0);
                                    setPicture(photo);
                                } else {
                                    getRemotePhotos();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }
        }
    }

    private void getRemotePhotos() {

        RequestDTO w = new RequestDTO(RequestDTO.GET_MONITOR_PHOTOS);
        if (monitor != null) {
            w.setMonitorID(monitor.getMonitorID());
        }
        if (staff != null) {
            w.setStaffID(staff.getStaffID());
            w.setRequestType(RequestDTO.GET_STAFF_PHOTOS);
        }
        if (getActivity() == null) return;
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

        });
    }

    public void setPicture(String path) {
        try {
            File f = new File(path);
            if (f.exists()) {
                try {
                    Picasso.with(getContext())
                            .load(f)
                            .centerCrop().resize(100,100)
                            .into(roundImage);
                    Picasso.with(getContext())
                            .load(f)
                            .centerCrop()
                            .resize(600,800)
                            .into(backDrop);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {

        }
    }
    public void setPicture(PhotoUploadDTO photo) {

        if (photo.getThumbFilePath() == null) {
            if (photo.getUri() != null) {
                Picasso.with(getActivity())
                        .load(photo.getUri())
                        .centerCrop().resize(600,800)
                        .into(backDrop);
                Picasso.with(getActivity())
                        .load(photo.getUri())
                        .centerCrop().resize(100,100)
                        .into(roundImage);
            }

        } else {
            File f = new File(photo.getThumbFilePath());
            if (f.exists()) {
                try {
                    Picasso.with(getContext())
                            .load(f)
                            .centerCrop().resize(100,100)
                            .into(roundImage);
                    Picasso.with(getContext())
                            .load(f)
                            .centerCrop().resize(600,800)
                            .into(backDrop);
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
            mListener = (ProfileListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ProfileListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void animateHeroHeight() {
        Util.expand(backDrop, 500, null);
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

    public interface ProfileListener {
        void onUpdated(Person person);

        void onAdded(Person person);

        void onPictureRequested(Person person);

        void setBusy(boolean busy);

    }

    public void setListener(ProfileListener mListener) {
        this.mListener = mListener;
    }

    int primaryColor, darkColor;

    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }

    public void setStaff(StaffDTO staff) {
        this.staff = staff;
    }

    public void setMonitor(MonitorDTO monitor) {
        this.monitor = monitor;
    }

    public void setEditType(int editType) {
        this.editType = editType;
    }

    public void setPersonType(int personType) {
        this.personType = personType;
    }

    public void setTakeSelfie(int takeSelfie) {
        this.takeSelfie = takeSelfie;
    }

    public ProfileFragment() {
    }

}
