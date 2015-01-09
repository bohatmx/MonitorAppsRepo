package com.boha.monitor.reporter.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boha.proximity.data.ResponseDTO;
import com.boha.proximity.reporter.R;

import java.text.DecimalFormat;

/**
 * Created by aubreyM on 2014/04/09.
 */
public class ServerLogFragment extends Fragment implements MGPageFragment {


    @Override
    public void onAttach(Activity a) {
        Log.i(LOG,
                "onAttach ---- Fragment called and hosted by "
                        + a.getLocalClassName()
        );
        super.onAttach(a);
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle saved) {
        Log.e(LOG, "onCreateView.........");
        ctx = getActivity();
        inflater = getActivity().getLayoutInflater();
        view = inflater
                .inflate(R.layout.fragment_log, container, false);

        fragmentManager = getFragmentManager();
        setFields();
        if (response != null) {
            Log.e(LOG, "response not null in onCreateView");
            setList();
            return view;
        }
        if (saved != null) {
            Log.i(LOG, "onCreateView - getting saved response");
            response = (ResponseDTO) saved.getSerializable("response");
        } else {
            Bundle bundle = getArguments();
            if (bundle != null) {
                response = (ResponseDTO) bundle.getSerializable("response");
            }
        }
        Log.e(LOG, "about to setList in onCreateView");
        setList();

        return view;
    }



    @Override
    public void onSaveInstanceState(Bundle b) {
        b.putSerializable("response", response);
        super.onSaveInstanceState(b);
    }

    public void setFields() {
        Log.e(LOG, "setFields.........");
        txtLog = (TextView) view.findViewById(R.id.LOG_log);
        txtCount = (TextView) view.findViewById(R.id.LOG_count);


    }

    public void setList() {
        txtLog.setText(response.getLog());
        txtCount.setText(df.format(response.getLog().length()));

    }


    static final DecimalFormat df = new DecimalFormat("###,###,###,###,###,###");
    FragmentManager fragmentManager;
    TextView txtLog, txtCount;

    static final String LOG = "ServerLogFrag";
    Context ctx;
    View view;
    ResponseDTO response;


}
