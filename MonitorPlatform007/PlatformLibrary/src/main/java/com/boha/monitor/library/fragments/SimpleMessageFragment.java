package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.adapters.SimpleMessageAdapter;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.SimpleMessageDTO;
import com.boha.monitor.library.dto.SimpleMessageDestinationDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.SimpleDividerItemDecoration;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collections;
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
    List<MonitorDTO> monitorList;
    List<StaffDTO> staffList;

    public List<StaffDTO> getStaffList() {
        return staffList;
    }

    public void setStaffList(List<StaffDTO> staffList) {
        this.staffList = staffList;
    }
    public List<MonitorDTO> getMonitorList() {
        return monitorList;
    }

    public void setMonitorList(List<MonitorDTO> monitorList) {
        this.monitorList = monitorList;
    }


    public SimpleMessageFragment() {
        // Required empty public constructor
    }

    public static SimpleMessageFragment newInstance(List<StaffDTO> staffList,List<MonitorDTO> monitorList) {
        SimpleMessageFragment fragment = new SimpleMessageFragment();
        Bundle b = new Bundle();
        ResponseDTO r = new ResponseDTO();
        r.setStaffList(staffList);
        r.setMonitorList(monitorList);
        b.putSerializable("response",r);
        fragment.setArguments(b);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ResponseDTO r = (ResponseDTO)getArguments().getSerializable("response");
            staffList = r.getStaffList();
            monitorList = r.getMonitorList();
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

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (staffList != null) {
                    for (StaffDTO x: staffList) {
                        SimpleMessageDestinationDTO dest = new SimpleMessageDestinationDTO();
                        dest.setStaffID(x.getStaffID());
                    }
                }
                if (monitorList != null) {
                    for (MonitorDTO m: monitorList) {
                        SimpleMessageDestinationDTO dest = new SimpleMessageDestinationDTO();
                        dest.setMonitorID(m.getMonitorID());
                    }
                }
                sendMessage();
            }
        });
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
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

        Collections.sort(simpleMessageList);
        adapter = new SimpleMessageAdapter(simpleMessageList, darkColor, getActivity(), new SimpleMessageAdapter.SimpleMessageListener() {
            @Override
            public void onResponseRequested(SimpleMessageDTO message, int position) {
                respond(message, position);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    public void openMessageToMonitors(List<MonitorDTO> monitorList) {

        this.monitorList = monitorList;
        paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        txtFromMsg.setVisibility(View.VISIBLE);
        StringBuilder sb = new StringBuilder();
        for (MonitorDTO m: monitorList) {
            sb.append(m.getFullName()).append(", ");
        }
        txtPerson.setText(sb.toString().trim());

        txtFromMsg.setText(SharedUtil.getMonitor(getActivity()).getFullName());
        editMessage.setText("");
    }
    public void openMessageToStaff(List<StaffDTO> staffList) {

        this.staffList = staffList;
        paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        txtFromMsg.setVisibility(View.VISIBLE);
        StringBuilder sb = new StringBuilder();
        for (StaffDTO m: staffList) {
            sb.append(m.getFullName()).append(", ");
        }
        txtPerson.setText(sb.toString().trim());

        txtFromMsg.setText(SharedUtil.getCompanyStaff(getActivity()).getFullName());
        editMessage.setText("");
    }
    private void respond(SimpleMessageDTO message, int position) {
        paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        txtFromMsg.setVisibility(View.VISIBLE);
        if (message.getStaffName() != null) {
            txtPerson.setText(message.getStaffName());
            SimpleMessageDestinationDTO dest = new SimpleMessageDestinationDTO();
            dest.setStaffID(message.getStaffID());
        }
        if (message.getMonitorName() != null) {
            txtPerson.setText(message.getMonitorName());
            SimpleMessageDestinationDTO dest = new SimpleMessageDestinationDTO();
            dest.setMonitorID(message.getMonitorID());
        }
        txtFromMsg.setText(message.getMessage());
        editMessage.setText("");

    }

    List<SimpleMessageDestinationDTO> messageDestinationList = new ArrayList<>();
    private void sendMessage() {
        if (editMessage.getText().toString().isEmpty()) {
            Util.showToast(getActivity(),"Please enter message");
            return;
        }
        final SimpleMessageDTO msg = new SimpleMessageDTO();
        MonitorDTO monitor = SharedUtil.getMonitor(getActivity());
        StaffDTO staff = SharedUtil.getCompanyStaff(getActivity());
        if (monitor != null) {
            msg.setMonitorID(monitor.getMonitorID());
            msg.setMonitorName(monitor.getFullName());
        }
        if (staff != null) {
            msg.setStaffID(staff.getStaffID());
            msg.setStaffName(staff.getFullName());
        }
        msg.setSimpleMessageDestinationList(messageDestinationList);
        msg.setMessage(editMessage.getText().toString());
        RequestDTO w = new RequestDTO(RequestDTO.SEND_SIMPLE_MESSAGE);
        w.setSimpleMessage(msg);
        mListener.setBusy(true);
        NetUtil.sendRequest(getActivity(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                Log.e(LOG, "##sendMessage, statusCode: " + response.getStatusCode() +
                        " message: " + response.getMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.setBusy(false);
                        if (response.getStatusCode() == 0) {
                            if (simpleMessageList == null) {
                                simpleMessageList = new ArrayList<>();
                            }
                            simpleMessageList.add(msg);
                            adapter.notifyDataSetChanged();
                            CacheUtil.addMessage(getActivity(), msg,null);
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

            @Override
            public void onWebSocketClose() {

            }
        });
    }
    static final String LOG = SimpleMessageFragment.class.getSimpleName();
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
