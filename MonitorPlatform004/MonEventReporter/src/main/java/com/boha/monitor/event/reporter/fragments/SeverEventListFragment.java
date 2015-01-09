package com.boha.monitor.event.reporter.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.boha.monitor.event.reporter.R;
import com.boha.monitor.event.reporter.adapters.ErrorStoreAdapter;
import com.com.boha.monitor.library.dto.ErrorStoreDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aubreyM on 2014/04/09.
 */
public class SeverEventListFragment extends Fragment implements PageFragment {


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
                .inflate(R.layout.fragment_android, container, false);

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
                if (response.getErrorStoreList() == null)
                    errorStoreList = new ArrayList<ErrorStoreDTO>();
                else
                    errorStoreList = response.getErrorStoreList();
            }
        }
        Log.e(LOG, "about to setList in onCreateView");
        setList();

        return view;
    }

    public void setErrorList(List<ErrorStoreDTO> list) {
        Log.i(LOG, "setting errorList ....");
        this.errorStoreList = list;
        setList();

    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        b.putSerializable("response", response);
        super.onSaveInstanceState(b);
    }

    public void setFields() {
        Log.e(LOG, "setFields.........");
        listView = (ListView) view.findViewById(R.id.ERR_list);
        txtCount = (TextView) view.findViewById(R.id.ERR_count);
        TextView label = (TextView) view.findViewById(R.id.ERR_label);
        label.setText("Server Event List");

    }

    public void setList() {
        Log.e(LOG, "setList.........");
        if (errorStoreList == null)
            errorStoreList = new ArrayList<ErrorStoreDTO>();
        errorStoreAdapter = new ErrorStoreAdapter(ctx, R.layout.error_item, errorStoreList);
        listView.setAdapter(errorStoreAdapter);
        txtCount.setText("" + errorStoreList.size());

    }


    ListView listView;
    TextView txtHeader, txtCount;

    static final String LOG = "ServerEventListFrag";
    Context ctx;
    View view;
    ResponseDTO response;
    List<ErrorStoreDTO> errorStoreList;
    ErrorStoreAdapter errorStoreAdapter;

}
