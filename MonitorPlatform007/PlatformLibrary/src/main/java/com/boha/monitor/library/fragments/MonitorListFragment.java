package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.activities.MonitorMapActivity;
import com.boha.monitor.library.adapters.MonitorAdapter;
import com.boha.monitor.library.adapters.PopupListIconAdapter;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.SimpleMessageDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.PopupItem;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MonitorListListener} interface
 * to handle interaction events.
 * Use the {@link MonitorListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonitorListFragment extends Fragment implements PageFragment {

    private MonitorListListener mListener;
    private ResponseDTO response;
    List<String> list;
    List<MonitorDTO> monitorList;
    MonitorAdapter monitorAdapter;
    ListView mListView;
    Context ctx;
    MonitorDTO monitor;
    TextView txtCount, txtName;
    View view, topView;
    FloatingActionButton fab;
    ImageView hero;
    int type;

    ImageView iconClose;
    TextView txtPerson, txtFromMsg;
    EditText editMessage;
    Button btnSend;
    SlidingUpPanelLayout paneLayout;

    public static final int STAFF = 1, MONITOR = 2;
    static final String LOG = MonitorListFragment.class.getSimpleName();

    public static MonitorListFragment newInstance(List<MonitorDTO> list, int type) {
        MonitorListFragment fragment = new MonitorListFragment();
        Bundle args = new Bundle();
        ResponseDTO w = new ResponseDTO();
        w.setMonitorList(list);
        args.putSerializable("monitorList", w);
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    public MonitorListFragment() {
    }


    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        Log.d(LOG, "MonitorListFragment onCreate");
        if (b != null) {
            ResponseDTO w = (ResponseDTO) b.getSerializable("monitorList");
            type = b.getInt("type", 0);
            if (w != null) {
                monitorList = w.getMonitorList();
                return;
            }
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            ResponseDTO w = (ResponseDTO) bundle.getSerializable("monitorList");
            type = bundle.getInt("type", 0);
            if (w != null) {
                monitorList = w.getMonitorList();
                return;
            }
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(LOG, "MonitorListFragment onCreateView");
        ctx = getActivity();
        view = inflater.inflate(R.layout.fragment_monitor_list, container, false);

        paneLayout = (SlidingUpPanelLayout)view.findViewById(R.id.sliding_layout);
        editMessage = (EditText) view.findViewById(R.id.FSL_message);
        btnSend = (Button) view.findViewById(R.id.FSL_btnSend);
        txtPerson = (TextView) view.findViewById(R.id.FSL_name);
        txtFromMsg = (TextView) view.findViewById(R.id.FSL_fromMessage);
        iconClose = (ImageView) view.findViewById(R.id.FSL_iconClose);
        txtFromMsg.setVisibility(View.GONE);
        paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);


        txtCount = (TextView) view.findViewById(R.id.FAB_text);
        txtName = (TextView) view.findViewById(R.id.MONITOR_LIST_label);
        hero = (ImageView) view.findViewById(R.id.MONITOR_LIST_backDrop);
        topView = view.findViewById(R.id.MONITOR_LIST_top);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        mListView = (ListView) view.findViewById(R.id.MONITOR_LIST_list);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                sendMessageToSelectedMonitor();
            }
        });
        setList();
        return view;
    }

    private void showDialog() {
        final AlertDialog.Builder x = new AlertDialog.Builder(getActivity());
        x.setTitle("Broadcast Your Location")
                .setMessage("Do you want to broadcast your current location to other Monitors?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        List<Integer> mList = new ArrayList<>();
                        for (MonitorDTO x : monitorList) {
                            mList.add(x.getMonitorID());
                        }
                        mListener.onLocationSendRequired(mList, new ArrayList<Integer>());

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }
    private void sendMessageToSelectedMonitor() {
        SimpleMessageDTO z = new SimpleMessageDTO();
        switch (type) {
            case STAFF:
                StaffDTO fromx = SharedUtil.getCompanyStaff(getActivity());
                z.setStaffID(fromx.getStaffID());
                z.setStaffName(fromx.getFullName());
                z.setMonitorList(new ArrayList<Integer>());
                z.getMonitorList().add(monitor.getMonitorID());
                z.setStaffList(new ArrayList<Integer>());
                Collections.sort(fromx.getPhotoUploadList());
                if (!fromx.getPhotoUploadList().isEmpty()) {
                    z.setUrl(fromx.getPhotoUploadList().get(0).getUri());
                }
                break;
            case MONITOR:
                MonitorDTO from = SharedUtil.getMonitor(getActivity());
                z.setMonitorID(from.getMonitorID());
                z.setMonitorName(from.getFullName());
                z.setMonitorList(new ArrayList<Integer>());
                z.getMonitorList().add(monitor.getMonitorID());
                z.setStaffList(new ArrayList<Integer>());
                Collections.sort(from.getPhotoUploadList());
                if (!from.getPhotoUploadList().isEmpty()) {
                    z.setUrl(from.getPhotoUploadList().get(0).getUri());
                }
                break;
        }

        if (editMessage.getText().toString().isEmpty()) {
            Util.showToast(getActivity(), "Please enter message");
            return;
        }

        z.setMessage(editMessage.getText().toString());

        RequestDTO w = new RequestDTO(RequestDTO.SEND_SIMPLE_MESSAGE);
        w.setSimpleMessage(z);

        mListener.setBusy(true);
        hideKeyboard();
        NetUtil.sendRequest(getActivity(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                Log.i(LOG, "simple message sent OK");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.setBusy(false);
                        paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                    }
                });

            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showErrorToast(getActivity(), message);
                        mListener.setBusy(false);
                        paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }
    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editMessage.getWindowToken(), 0);
    }
    @Override
    public void onSaveInstanceState(Bundle b) {
        Log.i(LOG, "## onSaveInstanceState");
        ResponseDTO w = new ResponseDTO();
        w.setMonitorList(monitorList);
        b.putSerializable("monitorList", w);
        super.onSaveInstanceState(b);
    }

    private void setList() {
        Log.d(LOG, "MonitorListFragment setList: " + monitorList.size());
        Collections.sort(monitorList);
        monitorAdapter = new MonitorAdapter(ctx, R.layout.monitor_card, monitorList, new MonitorAdapter.MonitorAdapterListener() {
            @Override
            public void onPictureRequested(MonitorDTO staff) {

            }

            @Override
            public void onStatusUpdatesRequested(MonitorDTO staff) {

            }
        });
        mListView.setAdapter(monitorAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mListener) {
                    monitor = monitorList.get(position);
                    showPopup(monitor);
                }
            }
        });
    }

    private void showPopup(final MonitorDTO monitor) {
        final ListPopupWindow pop = new ListPopupWindow(getActivity());
        LayoutInflater inf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inf.inflate(R.layout.hero_image_popup, null);
        TextView txt = (TextView) v.findViewById(R.id.HERO_caption);
        txt.setText("To: " + monitor.getFullName());
        ImageView img = (ImageView) v.findViewById(R.id.HERO_image);
        img.setImageDrawable(Util.getRandomBackgroundImage(ctx));

        pop.setPromptView(v);
        pop.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);

        pop.setAnchorView(txtName);
        pop.setHorizontalOffset(Util.getPopupHorizontalOffset(getActivity()));
        pop.setModal(true);
        pop.setWidth(Util.getPopupWidth(getActivity()));

        final List<PopupItem> pList = new ArrayList<>();
        pList.add(new PopupItem(R.drawable.ic_action_email, ctx.getString(R.string.send_message)));

        switch (type) {
            case MONITOR:
                pList.add(new PopupItem(R.drawable.ic_action_location_on, ctx.getString(R.string.send_my_location)));
                break;
            case STAFF:
                pList.add(new PopupItem(R.drawable.ic_action_location_on, ctx.getString(R.string.get_location)));
                pList.add(new PopupItem(R.drawable.ic_action_view_as_list, ctx.getString(R.string.get_updates)));
                break;
        }
        pop.setAdapter(new PopupListIconAdapter(ctx, R.layout.xxsimple_spinner_item,
                pList, darkColor));
        pop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pop.dismiss();
                PopupItem item = pList.get(position);
                if (item.getText().equalsIgnoreCase(ctx.getString(R.string.send_message))) {
                    txtPerson.setText(monitor.getFullName());
                    paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }
                if (item.getText().equalsIgnoreCase(ctx.getString(R.string.send_my_location))) {
                    List<Integer> mList = new ArrayList<>();
                    List<Integer> sList = new ArrayList<>();
                    mList.add(monitor.getMonitorID());
                    mListener.onLocationSendRequired(mList, sList);
                }
                if (item.getText().equalsIgnoreCase(ctx.getString(R.string.get_location))) {
                    getMonitorLocationTracks(monitor.getMonitorID());
                }
                if (item.getText().equalsIgnoreCase(ctx.getString(R.string.get_updates))) {

                }
                switch (position) {
                    case 0:
                        mListener.onMessagingRequested(monitor);
                        break;
                    case 1:

                        break;
                }
            }
        });
        try {
            pop.show();
        } catch (Exception e) {
            Log.e(LOG, "-- popup failed, probably nullpointer", e);
        }
    }

    private void getMonitorLocationTracks(Integer monitorID) {
        RequestDTO w = new RequestDTO(RequestDTO.GET_LOCATION_TRACK_BY_MONITOR_IN_PERIOD);
        w.setMonitorID(monitorID);

        NetUtil.sendRequest(getActivity(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                if (response.getLocationTrackerList() != null
                        && !response.getLocationTrackerList().isEmpty()) {
                    Intent w = new Intent(getActivity(), MonitorMapActivity.class);
                    w.putExtra("response",response);
                    startActivity(w);
                } else {
                    Util.showToast(getActivity(), "Location not available");
                }
            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MonitorListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MonitorListListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MonApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

    @Override
    public void animateHeroHeight() {

        hero.setImageDrawable(Util.getRandomBackgroundImage(getActivity()));
        Util.expand(hero, 500, null);
    }

    String pageTitle = "Monitors";

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
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface MonitorListListener {
        void setBusy(boolean busy);
        void onMonitorSelected(MonitorDTO monitor);

        void onMonitorPhotoRequired(MonitorDTO monitor);

        void onMonitorEditRequested(MonitorDTO monitor);

        void onMessagingRequested(MonitorDTO monitor);

        void onLocationSendRequired(List<Integer> monitorList,
                                    List<Integer> staffList);
    }

    int primaryColor, darkColor;

    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }

}
