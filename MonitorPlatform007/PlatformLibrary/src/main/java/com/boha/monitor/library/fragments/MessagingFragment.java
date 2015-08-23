package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

import com.boha.monitor.library.dto.ChatMessageDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.platform.library.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MessagingListener} interface
 * to handle interaction events.
 * Use the {@link MessagingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessagingFragment extends Fragment implements PageFragment{

    private MessagingListener mListener;
    private ResponseDTO response;
    private View view, peopleLayout;
    private ListView listView;
    private List<MonitorDTO> monitorList;
    private ImageView iconCamera, iconAdd, iconRemove;
    private Spinner spinner;
    private Context ctx;

    public static MessagingFragment newInstance(List<MonitorDTO> list) {
        MessagingFragment fragment = new MessagingFragment();
        ResponseDTO w = new ResponseDTO();
        Bundle args = new Bundle();
        w.setMonitorList(list);
        args.putSerializable("monitorList", w);
        fragment.setArguments(args);
        return fragment;
    }

    public MessagingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            response = (ResponseDTO) getArguments().getSerializable("monitorList");
            monitorList = response.getMonitorList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_messaging, container, false);
        ctx = getActivity();
        setFields();
        setSpinner();
        return view;
    }

    private void setFields() {
        spinner = (Spinner)view.findViewById(R.id.MSG_spinner);
        peopleLayout = view.findViewById(R.id.MSG_peopleLayout);
        iconAdd = (ImageView)view.findViewById(R.id.MSG_iconAdd);
        iconCamera = (ImageView)view.findViewById(R.id.MSG_icon);
        iconRemove = (ImageView)view.findViewById(R.id.MSG_iconAdd);
        listView = (ListView)view.findViewById(R.id.MSG_messageList);

    }
    private void setSpinner() {
        List<String> list = new ArrayList<>();
        for (MonitorDTO x: monitorList) {
            list.add(x.getFirstName() + " " + x.getLastName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ctx,android.R.layout.simple_spinner_item, list);
        spinner.setAdapter(adapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MessagingListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MessagingListener");
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
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface MessagingListener {
         void onMessageSelected(ChatMessageDTO message);
    }

}
