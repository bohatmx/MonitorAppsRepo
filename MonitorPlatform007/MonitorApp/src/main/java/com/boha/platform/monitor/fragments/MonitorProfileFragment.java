package com.boha.platform.monitor.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.platform.library.R;

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


    public static MonitorProfileFragment newInstance(MonitorDTO monitor) {
        MonitorProfileFragment fragment = new MonitorProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("monitor", monitor);
        fragment.setArguments(args);
        return fragment;
    }

    public MonitorProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_monitor_profile, container, false);
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
         void onProfileUpdated();
    }

}
