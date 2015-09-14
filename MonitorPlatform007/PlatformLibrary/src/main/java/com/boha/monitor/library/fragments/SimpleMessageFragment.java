package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.adapters.SimpleMessageAdapter;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.SimpleMessageDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.SpacesItemDecoration;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

public class SimpleMessageFragment extends Fragment implements PageFragment {

    Context ctx;
    View view;
    RecyclerView recyclerView;
    SimpleMessageAdapter adapter;
    List<SimpleMessageDTO> simpleMessageList;
    int darkColor, primaryColor;
    ImageView iconClose;
    TextView txtPerson, txtFromMsg;
    EditText editMessage;
    Button btnSend;
    View topView;
    SlidingUpPanelLayout paneLayout;

    public SimpleMessageFragment() {
        // Required empty public constructor
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
        ctx = getActivity();
        view = inflater.inflate(R.layout.fragment_message_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.AM_recycler);
        paneLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        editMessage = (EditText) view.findViewById(R.id.FSL_message);
        btnSend = (Button) view.findViewById(R.id.FSL_btnSend);
        txtPerson = (TextView) view.findViewById(R.id.FSL_name);
        iconClose = (ImageView) view.findViewById(R.id.FSL_iconClose);
        txtFromMsg = (TextView) view.findViewById(R.id.FSL_fromMessage);
        topView = view.findViewById(R.id.AM_top);
        txtFromMsg.setVisibility(View.GONE);
        paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.mon_divider_small);
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        getCachedMessages();
        return view;
    }

    private void getCachedMessages() {
        CacheUtil.getCachedMessages(getActivity(), new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getSimpleMessageList() == null) {
                    response.setSimpleMessageList(new ArrayList<SimpleMessageDTO>());
                }
                simpleMessageList = response.getSimpleMessageList();
                setList();
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

    private void setList() {

        adapter = new SimpleMessageAdapter(simpleMessageList, darkColor, getActivity(), new SimpleMessageAdapter.SimpleMessageListener() {
            @Override
            public void onResponseRequested(SimpleMessageDTO message, int position) {
                respond(message, position);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void respond(SimpleMessageDTO message, int position) {
        paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        txtFromMsg.setVisibility(View.VISIBLE);
        if (message.getStaffName() != null) {
            txtPerson.setText(message.getStaffName());
        }
        if (message.getMonitorName() != null) {
            txtPerson.setText(message.getMonitorName());
        }
        txtFromMsg.setText(message.getMessage());
        editMessage.setText("");

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SimpleMessageFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SimpleMessageFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    SimpleMessageFragmentListener mListener;

    @Override
    public void animateHeroHeight() {
        Util.expand(topView, 500, null);
    }

    String title;

    @Override
    public void setPageTitle(String title) {
        this.title = title;
    }

    @Override
    public String getPageTitle() {
        return title;
    }

    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.darkColor = darkColor;
        this.primaryColor = primaryColor;
    }

    public interface SimpleMessageFragmentListener {
        void setBusy(boolean busy);
    }

}
