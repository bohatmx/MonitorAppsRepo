package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.TextView;

import com.boha.monitor.library.adapters.MonitorListAdapter;
import com.boha.monitor.library.adapters.PopupListIconAdapter;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.SimpleMessageDTO;
import com.boha.monitor.library.dto.SimpleMessageDestinationDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.PopupItem;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.SimpleDividerItemDecoration;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Fragment that manages a list of Monitors assigned to a project
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
    RecyclerView recyclerView;
    Context ctx;
    MonitorDTO monitor;
    TextView txtCount, txtName, txtSelected;
    View view, topView;
    FloatingActionButton fab, fab2;
    ImageView hero;
    View actionsView;
    int type;

    ImageView iconClose, iconCloseActions;
    TextView txtPerson, txtFromMsg;
    EditText editMessage;
    Button btnSend, btnLoc, btnMsg;
    SlidingUpPanelLayout paneLayout;

    public static final int STAFF = 1, MONITOR = 2;
    static final String LOG = MonitorListFragment.class.getSimpleName();

    public static MonitorListFragment newInstance(List<MonitorDTO> list, int type) {
        MonitorListFragment fragment = new MonitorListFragment();
        Bundle args = new Bundle();
        ResponseDTO w = new ResponseDTO();
        w.setMonitorList(list);
        args.putSerializable("trackerDTOList", w);
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
            ResponseDTO w = (ResponseDTO) b.getSerializable("trackerDTOList");
            type = b.getInt("type", 0);
            if (w != null) {
                monitorList = w.getMonitorList();
                return;
            }
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            ResponseDTO w = (ResponseDTO) bundle.getSerializable("trackerDTOList");
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
        iconCloseActions = (ImageView) view.findViewById(R.id.MONITOR_LIST_iconClear);


        txtFromMsg.setVisibility(View.GONE);
        paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);


        txtCount = (TextView) view.findViewById(R.id.FAB_text);
        txtName = (TextView) view.findViewById(R.id.MONITOR_LIST_label);
        txtSelected = (TextView) view.findViewById(R.id.MONITOR_LIST_selected);
        hero = (ImageView) view.findViewById(R.id.MONITOR_LIST_backDrop);
        topView = view.findViewById(R.id.MONITOR_LIST_top);
        actionsView = view.findViewById(R.id.MONITOR_LIST_actions);
        btnLoc = (Button) view.findViewById(R.id.MONITOR_LIST_btnLoc);
        btnMsg = (Button) view.findViewById(R.id.MONITOR_LIST_btnMsg);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab2 = (FloatingActionButton) view.findViewById(R.id.fab2);
        recyclerView = (RecyclerView) view.findViewById(R.id.MONITOR_LIST_list);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        actionsView.setVisibility(View.GONE);
        txtSelected.setText("");

        btnLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> mons = new ArrayList<>();
                for (MonitorDTO m : selectedMonitors) {
                    mons.add(m.getMonitorID());
                }

                mListener.onLocationSendRequired(mons, new ArrayList<Integer>());
            }
        });
        btnMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                StringBuilder sb = new StringBuilder();
                for (MonitorDTO m: monitorList) {
                    selectedMonitors.add(m);
                    sb.append(m.getFullName()).append(", ");
                }
                txtPerson.setText(sb.toString());
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                sendMessageToSelectedPeople();
            }
        });
        iconCloseActions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtSelected.setText("");
                txtPerson.setText("");
                Util.collapse(actionsView, 1000, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        Util.expand(topView, 1000, null);
                        fab.setVisibility(View.VISIBLE);
                        fab2.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        setList();
        return view;
    }

    public void refreshMonitorList(List<MonitorDTO> monitorList) {
        this.monitorList = monitorList;
        if (recyclerView != null) {
            setList();
        }
    }

    private void showDialog() {
        final AlertDialog.Builder x = new AlertDialog.Builder(getActivity());
        x.setTitle("Broadcast Your Location")
                .setMessage("Do you want to broadcast your current location to the Monitors in the list?")
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
    private void sendMessageToSelectedPeople() {
        if (SharedUtil.getMonitor(getActivity()) != null) {
            type = MONITOR;
        } else {
            type = STAFF;
        }
        SimpleMessageDTO simpleMsg = new SimpleMessageDTO();
        List<SimpleMessageDestinationDTO> destList = new ArrayList<>();
        for (MonitorDTO m: selectedMonitors) {
            SimpleMessageDestinationDTO dest = new SimpleMessageDestinationDTO();
            dest.setMonitorID(m.getMonitorID());
            destList.add(dest);
        }
        switch (type) {
            case STAFF:
                StaffDTO fromx = SharedUtil.getCompanyStaff(getActivity());
                simpleMsg.setStaffID(fromx.getStaffID());
                simpleMsg.setStaffName(fromx.getFullName());
                Collections.sort(fromx.getPhotoUploadList());
                if (!fromx.getPhotoUploadList().isEmpty()) {
                    simpleMsg.setUrl(fromx.getPhotoUploadList().get(0).getUri());
                }
                break;
            case MONITOR:
                MonitorDTO from = SharedUtil.getMonitor(getActivity());
                simpleMsg.setMonitorID(from.getMonitorID());
                simpleMsg.setMonitorName(from.getFullName());
                Collections.sort(from.getPhotoUploadList());
                if (!from.getPhotoUploadList().isEmpty()) {
                    simpleMsg.setUrl(from.getPhotoUploadList().get(0).getUri());
                }
                break;
        }

        if (editMessage.getText().toString().isEmpty()) {
            Util.showToast(getActivity(), "Please enter message");
            return;
        }

        simpleMsg.setMessage(editMessage.getText().toString());

        RequestDTO w = new RequestDTO(RequestDTO.SEND_SIMPLE_MESSAGE);
        w.setSimpleMessage(simpleMsg);

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
                        txtSelected.setText("");
                        txtPerson.setText("");
                        Util.collapse(actionsView, 1000, new Util.UtilAnimationListener() {
                            @Override
                            public void onAnimationEnded() {
                                Util.expand(topView, 1000, null);
                                fab.setVisibility(View.VISIBLE);
                                fab2.setVisibility(View.VISIBLE);
                            }
                        });
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
        b.putSerializable("trackerDTOList", w);
        super.onSaveInstanceState(b);
    }

    List<MonitorDTO> selectedMonitors = new ArrayList<>();
    MonitorListAdapter monitorListAdapter;
    private void setList() {
        Log.d(LOG, "MonitorListFragment setList: " + monitorList.size());
        Collections.sort(monitorList);

        monitorListAdapter = new MonitorListAdapter(monitorList, darkColor, getActivity(), new MonitorListAdapter.MonitorListener() {
            @Override
            public void onPictureRequested(MonitorDTO monitor) {

            }

            @Override
            public void onStatusUpdatesRequested(MonitorDTO monitor) {

            }

            @Override
            public void onCheckBoxClicked(MonitorDTO monitor) {
                selectedMonitors.clear();
                StringBuilder sb = new StringBuilder();

                for (MonitorDTO dto: monitorList) {
                    if (dto.isSelected()) {
                        selectedMonitors.add(dto);
                        sb.append(dto.getFullName()).append(", ");
                    }
                }
                if (selectedMonitors.isEmpty()) {
                    txtSelected.setText("");
                    txtPerson.setText("");
                    Util.collapse(actionsView, 1000, new Util.UtilAnimationListener() {
                        @Override
                        public void onAnimationEnded() {
                            Util.expand(topView, 1000, null);
                            fab.setVisibility(View.VISIBLE);
                            fab2.setVisibility(View.VISIBLE);
                        }
                    });

                    return;
                }
                txtSelected.setText(sb.toString().trim());
                txtPerson.setText("" + selectedMonitors.size() + " recipients");
                Util.collapse(topView, 1000, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        Util.expand(actionsView, 500, null);
                        fab.setVisibility(View.GONE);
                        fab2.setVisibility(View.GONE);
                    }
                });

            }

            @Override
            public void onMonitorNameClicked(MonitorDTO monitor) {
                showPopup(monitor);
            }
        });

        recyclerView.setAdapter(monitorListAdapter);
    }

    private void showPopup(final MonitorDTO monitor) {
        final ListPopupWindow pop = new ListPopupWindow(getActivity());
        LayoutInflater inf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inf.inflate(R.layout.hero_image_popup, null);
        TextView txt = (TextView) v.findViewById(R.id.HERO_caption);
        CircleImageView photo = (CircleImageView) v.findViewById(R.id.HERO_personImage);
        txt.setText("To: " + monitor.getFullName());
        ImageView img = (ImageView) v.findViewById(R.id.HERO_image);
        img.setImageDrawable(Util.getRandomBackgroundImage(ctx));

        if (monitor.getPhotoUploadList() == null || monitor.getPhotoUploadList().isEmpty()) {
            photo.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.boy));
            photo.setAlpha(0.3f);
        } else {
            photo.setAlpha(1.0f);
            Picasso.with(ctx)
                    .load(monitor.getPhotoUploadList().get(0).getUri())
                    .into(photo);
        }

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
                pList.add(new PopupItem(R.drawable.ic_action_location_on, ctx.getString(R.string.send_my_location)));
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
        SimpleMessageDTO msg = new SimpleMessageDTO();
        msg.setSimpleMessageDestinationList(new ArrayList<SimpleMessageDestinationDTO>());
        StaffDTO s = SharedUtil.getCompanyStaff(
                getActivity());
        if (s != null) {
            msg.setStaffID(s.getStaffID());
            msg.setStaffName(s.getFullName());
        }
        MonitorDTO m = SharedUtil.getMonitor(
                getActivity());
        if (m != null) {
            msg.setMonitorID(m.getMonitorID());
            msg.setMonitorName(m.getFullName());
        }
        SimpleMessageDestinationDTO dest = new SimpleMessageDestinationDTO();
        dest.setMonitorID(monitorID);
        msg.getSimpleMessageDestinationList().add(dest);
        msg.setLocationRequest(Boolean.TRUE);

        RequestDTO w = new RequestDTO(RequestDTO.SEND_SIMPLE_MESSAGE);
        w.setSimpleMessage(msg);

        NetUtil.sendRequest(getActivity(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                Log.i(LOG,"request for location sent");
            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showErrorToast(getActivity(),message);
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
//        RefWatcher refWatcher = MonApp.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }

    @Override
    public void animateHeroHeight() {

        if (hero != null) {
            hero.setImageDrawable(Util.getRandomBackgroundImage(getActivity()));
            Util.expand(hero, 500, null);
        }
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
