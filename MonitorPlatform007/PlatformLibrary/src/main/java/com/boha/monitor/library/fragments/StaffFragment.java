package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;

import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.ArrayList;

import static com.boha.monitor.library.util.Util.showErrorToast;


public class StaffFragment extends Fragment implements PageFragment{


    private StaffFragmentListener listener;

    public StaffFragment() {
        // Required empty public constructor
    }

    public static StaffFragment newInstance(StaffDTO staff) {

        StaffFragment sf = new StaffFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("staff", staff);
        sf.setArguments(bundle);

        return sf;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    View view;
    EditText editFirst, editLast, editCell, editEmail;
    ImageView imgDelete;
    Button btnSave;

    StaffDTO companyStaff;
    Context ctx;
    ProgressBar progressBar;
    RadioButton radioActive, radioInactive;
    RadioButton radioExec, radioOps, radioProj;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.person_edit, container);
        ctx = getActivity();
        setFields();

        return view;
    }

    boolean isUpdate;
    private void sendData() {
        RequestDTO w = new RequestDTO();

        if (!isUpdate) {
            w.setRequestType(RequestDTO.ADD_STAFF);
            companyStaff = new StaffDTO();
            companyStaff.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());
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
            companyStaff.setActiveFlag(1);
        }
        if (radioInactive.isChecked()) {
            companyStaff.setActiveFlag(0);
        }
        companyStaff.setFirstName(editFirst.getText().toString());
        companyStaff.setLastName(editLast.getText().toString());
        companyStaff.setCellphone(editCell.getText().toString());
        companyStaff.setEmail(editEmail.getText().toString());

        w.setStaffList(new ArrayList<StaffDTO>());
        w.getStaffList().add(companyStaff);

        progressBar.setVisibility(View.VISIBLE);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
//                        if (!ErrorUtil.checkServerError(ctx, response)) {
//                            return;
//                        }
                        companyStaff = response.getStaffList().get(0);

                        if (isUpdate) {
                            listener.onStaffUpdated(companyStaff);
                        } else {
                            listener.onStaffAdded(companyStaff);
                        }
//                        if (radioExec.isChecked()) {
//                            listener.onAppInvitationRequested(companyStaff, Util.EXEC);
//                        }
//                        if (radioOps.isChecked()) {
//                            listener.onAppInvitationRequested(companyStaff, Util.OPS);
//                        }
//                        if (radioProj.isChecked()) {
//                            listener.onAppInvitationRequested(companyStaff, Util.PROJ);
//                        }


                    }
                });

            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        showErrorToast(ctx, message);

                    }
                });
            }

            @Override
            public void onWebSocketClose() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });

    }


    private void deleteStaff() {

    }

    public void setCompanyStaff( StaffDTO companyStaff) {
        this.companyStaff = companyStaff;
        if (companyStaff == null) {
            isUpdate = false;
            return;
        } else {
            isUpdate = true;
        }
        editFirst.setText(companyStaff.getFirstName());
        editLast.setText(companyStaff.getLastName());
        editCell.setText(companyStaff.getCellphone());
        editEmail.setText(companyStaff.getEmail());
        imgDelete.setVisibility(View.VISIBLE);
    }

    private void setFields() {
        editFirst = (EditText) view.findViewById(R.id.ED_PSN_firstName);
        editLast = (EditText) view.findViewById(R.id.ED_PSN_lastName);
        editCell = (EditText) view.findViewById(R.id.ED_PSN_cellphone);
        editEmail = (EditText) view.findViewById(R.id.ED_PSN_email);
        btnSave = (Button) view.findViewById(R.id.ED_PSN_btnSave);

        radioActive = (RadioButton) view.findViewById(R.id.ED_PSN_radioActive);
        radioInactive = (RadioButton) view.findViewById(R.id.ED_PSN_radioInactive);

        radioExec = (RadioButton) view.findViewById(R.id.ED_PSN_chkExec);
        radioOps = (RadioButton) view.findViewById(R.id.ED_PSN_chkOps);
        radioProj = (RadioButton) view.findViewById(R.id.ED_PSN_chkProj);

        imgDelete = (ImageView) view.findViewById(R.id.ED_PSN_imgDelete);
        imgDelete.setVisibility(View.GONE);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
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
    }

    @Override
    public void onAttach( Activity activity) {
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
    @Override public void onDestroy() {
        super.onDestroy();
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

    @Override
    public void setThemeColors(int primaryColor, int darkColor) {

    }

    public interface StaffFragmentListener {
        public void onStaffAdded(StaffDTO companyStaff);

        public void onStaffUpdated(StaffDTO companyStaff);

        public void onAppInvitationRequested(StaffDTO staff, int appType);

    }

}
