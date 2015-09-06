package com.boha.monitor.staffapp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.staffapp.R;

import java.util.List;

public class NoProjectsAssignedFragment extends Fragment implements PageFragment {

//    private MonitorListListener mListener;
    private List<MonitorDTO> monitorDTOList;
    private ResponseDTO response;
    private View view;


    public static NoProjectsAssignedFragment newInstance() {
        NoProjectsAssignedFragment fragment = new NoProjectsAssignedFragment();
        return fragment;
    }

    public NoProjectsAssignedFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    ImageView imageView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_no_projects, container, false);
        final Button btn = (Button)view.findViewById(R.id.btnRed);
        imageView = (ImageView)view.findViewById(R.id.NOP_image);
        btn.setText("Notify Manager");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(btn, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        sendSOS();
                    }
                });
            }
        });
        return view;
    }

    private void sendSOS() {
        RequestDTO w = new RequestDTO(RequestDTO.NOTIFY_SUPERVISOR_NO_PROJECTS);
        w.setCompanyID(SharedUtil.getCompany(getActivity()).getCompanyID());
        w.setMonitorID(SharedUtil.getMonitor(getActivity()).getMonitorID());

        NetUtil.sendRequest(getActivity(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Util.showToast(getActivity(), getActivity().getString(R.string.notific_sent));
                        Snackbar
                                .make(imageView, "Notification sent", Snackbar.LENGTH_LONG)
                                .show();
                    }
                });
            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showErrorToast(getActivity(), message);

                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (MonitorListListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement MonitorListListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
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
}
