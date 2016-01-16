package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.ImageUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.boha.monitor.library.util.Util.showErrorToast;


public class StaffProfileFragment extends Fragment implements PageFragment {


    private StaffFragmentListener listener;

    public StaffProfileFragment() {
        // Required empty public constructor
    }

    public static StaffProfileFragment newInstance(StaffDTO staff) {

        StaffProfileFragment sf = new StaffProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("staff", staff);
        sf.setArguments(bundle);

        return sf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            staff = (StaffDTO)getArguments().getSerializable("staff");
        }
    }

    View view;
    EditText editFirst, editLast, editCell, editEmail;
    ImageView hero;
    TextView staffName;
    CircleImageView roundImage;
    Button btnSave;

    StaffDTO staff;
    Context ctx;
    RadioButton radioActive, radioInactive;
    RadioButton radioExec, radioStaff;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_staff_details, container, false);
        ctx = getActivity();
        setFields();

        if (staff != null) {
            setStaff(staff);
        }
        PhotoUploadDTO x = SharedUtil.getPhoto(ctx);
        if (x != null) {
            setPicture(x);
        } else {
            getRemotePhotos();
        }

        return view;
    }

    boolean isUpdate;

    private void sendData() {
        RequestDTO w = new RequestDTO();

        if (!isUpdate) {
            w.setRequestType(RequestDTO.ADD_STAFF);
            staff = new StaffDTO();
            staff.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());
        } else {
            w.setRequestType(RequestDTO.UPDATE_COMPANY_STAFF);
        }
        if (editFirst.getText().toString().isEmpty()) {
            Util.showToast(ctx, "Enter first name");
            return;
        }

        if (editLast.getText().toString().isEmpty()) {
            Util.showToast(ctx, "Enter surname");
            return;
        }

        if (editEmail.getText().toString().isEmpty()) {
            Util.showToast(ctx, "Enter email address");
            return;
        }

        if (editCell.getText().toString().isEmpty()) {
            Util.showToast(ctx, "Enter cellphone number");
            return;
        }

        if (radioActive.isChecked()) {
            staff.setActiveFlag(1);
        }
        if (radioInactive.isChecked()) {
            staff.setActiveFlag(0);
        }
        staff.setFirstName(editFirst.getText().toString());
        staff.setLastName(editLast.getText().toString());
        staff.setCellphone(editCell.getText().toString());
        staff.setEmail(editEmail.getText().toString());

        w.setStaffList(new ArrayList<StaffDTO>());
        w.getStaffList().add(staff);

        listener.setBusy(true);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.setBusy(false);
                        staff = response.getStaffList().get(0);

                        if (isUpdate) {
                            listener.onStaffUpdated(staff);
                        } else {
                            listener.onStaffAdded(staff);
                        }
                    }
                });

            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.setBusy(false);
                        showErrorToast(ctx, message);

                    }
                });
            }

            @Override
            public void onWebSocketClose() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.setBusy(false);
                    }
                });
            }
        });

    }

    private void getRemotePhotos() {
        Log.d(LOG,".............getRemotePhotos starting");
        RequestDTO w = new RequestDTO(RequestDTO.GET_STAFF_PHOTOS);
        w.setStaffID(SharedUtil.getCompanyStaff(getActivity()).getStaffID());

        listener.setBusy(true);
        NetUtil.sendRequest(getActivity(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.setBusy(false);
                        if (response.getStatusCode() == 0) {
                            if (!response.getPhotoUploadList().isEmpty()) {
                                SharedUtil.savePhoto(getActivity(), response.getPhotoUploadList().get(0));
                                setPicture(response.getPhotoUploadList().get(0));
                            }
                        }
                    }
                });

            }

            @Override
            public void onError(String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.setBusy(false);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }

    static final String LOG = "StaffProfileFragment";
    public void setPicture(PhotoUploadDTO photo) {
        Log.d(LOG,"setPicture " + photo.getThumbFilePath() + " uri: " + photo.getUri());
        if (photo.getThumbFilePath() == null) {
            if (photo.getUri() != null) {
                Picasso.with(getActivity()).load(photo.getUri()).into(hero);
                Picasso.with(getActivity()).load(photo.getUri()).into(roundImage);
            }

        } else {
            File f = new File(photo.getThumbFilePath());
            if (f.exists()) {
                try {
                    Bitmap bm = ImageUtil.getBitmapFromUri(getActivity(), Uri.fromFile(f));
                    hero.setImageBitmap(bm);
                    roundImage.setImageBitmap(bm);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Util.expand(hero, 500, null);
    }

    public void setStaff(StaffDTO staff) {
        this.staff = staff;
        if (staff == null) {
            isUpdate = false;
            return;
        } else {
            isUpdate = true;
        }
        editFirst.setText(staff.getFirstName());
        editLast.setText(staff.getLastName());
        editCell.setText(staff.getCellphone());
        editEmail.setText(staff.getEmail());
        staffName.setText(staff.getFullName());
    }

    private void setFields() {
        editFirst = (EditText) view.findViewById(R.id.FSP_editFirstName);
        editLast = (EditText) view.findViewById(R.id.FSP_editLastName);
        editCell = (EditText) view.findViewById(R.id.FSP_editCell);
        editEmail = (EditText) view.findViewById(R.id.FSP_editEmail);
        btnSave = (Button) view.findViewById(R.id.FSP_btnSave);
        staffName = (TextView) view.findViewById(R.id.FSP_name);

        radioActive = (RadioButton) view.findViewById(R.id.FSP_radioActive);
        radioInactive = (RadioButton) view.findViewById(R.id.FSP_radioInactive);

        radioExec = (RadioButton) view.findViewById(R.id.FSP_radioExec);
        radioStaff = (RadioButton) view.findViewById(R.id.FSP_radioStaff);

        hero = (ImageView) view.findViewById(R.id.FSP_backdrop);
        roundImage = (CircleImageView) view.findViewById(R.id.FSP_personImage);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(btnSave, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        sendData();
                    }
                });
            }
        });

        roundImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onStaffPictureRequired(staff);
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (StaffFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement StaffFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void animateHeroHeight() {

        Util.expand(hero,500,null);
    }

    @Override
    public void setPageTitle(String title) {

    }

    @Override
    public String getPageTitle() {
        return null;
    }

    @Override
    public void setThemeColors(int primaryColor, int darkColor) {

    }

    public interface StaffFragmentListener {
        void setBusy(boolean busy);
        void onStaffPictureRequired(StaffDTO staff);

        void onStaffAdded(StaffDTO staff);

        void onStaffUpdated(StaffDTO staff);

        void onAppInvitationRequested(StaffDTO staff, int appType);

    }

}
