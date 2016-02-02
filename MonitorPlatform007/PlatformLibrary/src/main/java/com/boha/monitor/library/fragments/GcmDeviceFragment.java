package com.boha.monitor.library.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.boha.monitor.library.dto.GcmDeviceDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.SimpleDividerItemDecoration;
import com.boha.platform.library.R;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link GcmDeviceListener}
 * interface.
 */
public class GcmDeviceFragment extends Fragment {

    private ResponseDTO response;
    private GcmDeviceListener mListener;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GcmDeviceFragment() {
    }

    public static GcmDeviceFragment newInstance(ResponseDTO response) {
        GcmDeviceFragment fragment = new GcmDeviceFragment();
        Bundle args = new Bundle();
        args.putSerializable("response", response);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            response = (ResponseDTO) getArguments().getSerializable("response");
            gcmDeviceList = response.getGcmDeviceList();
        }
    }

    List<GcmDeviceDTO> gcmDeviceList;
    RecyclerView recyclerView;
    GcmDeviceListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gcmdevice_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        setList();

        return view;
    }

    private void setList() {
        adapter = new GcmDeviceListAdapter(gcmDeviceList, new GcmDeviceListener() {
            @Override
            public void onDeviceClicked(GcmDeviceDTO device) {
                mListener.onDeviceClicked(device);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    public void setListener(GcmDeviceListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof GcmDeviceListener) {
            mListener = (GcmDeviceListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement GcmDeviceListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface GcmDeviceListener {
        void onDeviceClicked(GcmDeviceDTO device);
    }
}
