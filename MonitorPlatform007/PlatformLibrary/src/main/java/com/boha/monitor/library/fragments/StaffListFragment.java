package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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

import com.boha.monitor.library.activities.MonitorMapActivity;
import com.boha.monitor.library.adapters.PopupListIconAdapter;
import com.boha.monitor.library.adapters.StaffListAdapter;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.SimpleMessageDTO;
import com.boha.monitor.library.dto.SimpleMessageDestinationDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.PopupItem;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A fragment that manages a list of Staff assigned to a project

 * Activities containing this fragment MUST implement the CompanyStaffListListener
 * interface.
 */
public class StaffListFragment extends Fragment
        implements PageFragment {


    private CompanyStaffListListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private RecyclerView mRecycler;

    public StaffListFragment() {
    }

    public static StaffListFragment newInstance(List<StaffDTO> list) {
        StaffListFragment d = new StaffListFragment();
        ResponseDTO w = new ResponseDTO();
        w.setStaffList(list);
        Bundle bundle = new Bundle();
        bundle.putSerializable("staffList", w);
        d.setArguments(bundle);
        return d;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.d(LOG, "StaffListFragment onCreate");
        if (bundle != null) {
            ResponseDTO w = (ResponseDTO) bundle.getSerializable("staffList");
            if (w != null) {
                staffList = w.getStaffList();
                return;
            }
        }
        Bundle c = getArguments();
        if (c != null) {
            ResponseDTO w = (ResponseDTO) c.getSerializable("staffList");
            if (w != null) {
                staffList = w.getStaffList();
                return;
            }
        }

    }

    Context ctx;
    TextView txtCount, txtName;
    View view, topView, handle;
    FloatingActionButton fab;
    ImageView  hero;
    ResponseDTO response;

    ImageView iconClose;
    TextView txtPerson, txtFromMsg;
    EditText editMessage;
    Button btnSend;
    SlidingUpPanelLayout paneLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_staff_list, container, false);
        ctx = getActivity();

        paneLayout = (SlidingUpPanelLayout)view.findViewById(R.id.sliding_layout);
        editMessage = (EditText) view.findViewById(R.id.FSL_message);
        btnSend = (Button) view.findViewById(R.id.FSL_btnSend);
        txtPerson = (TextView) view.findViewById(R.id.FSL_name);
        txtFromMsg = (TextView) view.findViewById(R.id.FSL_fromMessage);
        iconClose = (ImageView) view.findViewById(R.id.FSL_iconClose);
        txtFromMsg.setVisibility(View.GONE);
        paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        txtCount = (TextView) view.findViewById(R.id.FAB_text);
        txtName = (TextView) view.findViewById(R.id.STAFF_LIST_label);
        handle = view.findViewById(R.id.STAFF_LIST_handle);
        topView = view.findViewById(R.id.STAFF_LIST_top);
        hero = (ImageView) view.findViewById(R.id.STAFF_LIST_backDrop);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        mRecycler = (RecyclerView) view.findViewById(R.id.STAFF_LIST_list);

        iconClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                sendMessageToSelectedStaffMember();
            }
        });

        setList();

        return view;
    }

    private void sendMessageToSelectedStaffMember() {
        StaffDTO from = SharedUtil.getCompanyStaff(getActivity());
        if (editMessage.getText().toString().isEmpty()) {
            Util.showToast(getActivity(),"Please enter message");
            return;
        }
        SimpleMessageDTO z = new SimpleMessageDTO();
        z.setSimpleMessageDestinationList(new ArrayList<SimpleMessageDestinationDTO>());
        z.setMessage(editMessage.getText().toString());
        z.setStaffID(from.getStaffID());
        z.setStaffName(from.getFullName());
        SimpleMessageDestinationDTO dest = new SimpleMessageDestinationDTO();
        dest.setStaffID(staff.getStaffID());
        z.getSimpleMessageDestinationList().add(dest);
        Collections.sort(from.getPhotoUploadList());
        if (!from.getPhotoUploadList().isEmpty()) {
            z.setUrl(from.getPhotoUploadList().get(0).getUri());
        }

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
    private void showDialog() {
        final AlertDialog.Builder x = new AlertDialog.Builder(getActivity());
        x.setTitle("Broadcast Your Location")
                .setMessage("Do you want to broadcast your current location to other Staff?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        List<Integer> mList = new ArrayList<>();
                        for (StaffDTO x : staffList) {
                            mList.add(x.getStaffID());
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
    public void refreshStaffList(List<StaffDTO> staffList) {
        this.staffList = staffList;
        if (mRecycler != null) {
            setList();
        }
    }
    @Override
    public void onSaveInstanceState(Bundle b) {
        Log.i(LOG, "## onSaveInstanceState");
        ResponseDTO w = new ResponseDTO();
        w.setStaffList(staffList);
        b.putSerializable("staffList", w);
        super.onSaveInstanceState(b);
    }


    public void setStaffList(List<StaffDTO> staffList) {
        this.staffList = staffList;
        setList();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (CompanyStaffListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Host " + activity.getLocalClassName()
                    + " must implement CompanyStaffListListener");
        }
        Log.i("StaffListFragment", "## onAttach, mListener = " + activity.getLocalClassName());
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

    List<PopupItem> popupItemList;


    private void setList() {
        Log.d(LOG,"##### setList");
        staffAdapter = new StaffListAdapter(staffList, darkColor, getActivity(), new StaffListAdapter.StaffListListener() {
            @Override
            public void onStaffNameClicked(StaffDTO s) {
                staff = s;
                showPopup(staff);
            }
        });

        LinearLayoutManager llm =
                new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(llm);
        mRecycler.setAdapter(staffAdapter);

    }
    private void showPopup(final StaffDTO staff) {
        popupItemList = new ArrayList<>();
        PopupItem item1 = new PopupItem(R.drawable.ic_action_location_on, ctx.getString(R.string.send_my_location));
        PopupItem item2 = new PopupItem(R.drawable.ic_action_email, ctx.getString(R.string.send_message));
        PopupItem item3 = new PopupItem(R.drawable.ic_action_location_on, ctx.getString(R.string.get_location));
        popupItemList.add(item1);
        popupItemList.add(item2);
        popupItemList.add(item3);
        final ListPopupWindow pop = new ListPopupWindow(getActivity());
        LayoutInflater inf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inf.inflate(R.layout.hero_image_popup, null);
        TextView txt = (TextView) v.findViewById(R.id.HERO_caption);
        txt.setText("To: " + staff.getFullName());
        ImageView img = (ImageView) v.findViewById(R.id.HERO_image);
        img.setImageDrawable(Util.getRandomBackgroundImage(ctx));

        pop.setPromptView(v);
        pop.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);

        pop.setAnchorView(txtName);
        pop.setHorizontalOffset(Util.getPopupHorizontalOffset(getActivity()));
        pop.setModal(true);
        pop.setWidth(Util.getPopupWidth(getActivity()));


        pop.setAdapter(new PopupListIconAdapter(ctx, R.layout.xxsimple_spinner_item,
                popupItemList, darkColor));
        pop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pop.dismiss();
                PopupItem item = popupItemList.get(position);
                if (item.getText().equalsIgnoreCase(ctx.getString(R.string.send_message))) {
                    txtPerson.setText(staff.getFullName());
                    paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }
                if (item.getText().equalsIgnoreCase(ctx.getString(R.string.send_my_location))) {
                    List<Integer> mList = new ArrayList<>();
                    List<Integer> sList = new ArrayList<>();
                    mList.add(StaffListFragment.this.staff.getStaffID());
                    mListener.onLocationSendRequired(mList, sList);
                }
                if (item.getText().equalsIgnoreCase(ctx.getString(R.string.get_location))) {
                    getStaffLocationTracks(StaffListFragment.this.staff.getStaffID());
                }

            }
        });
        try {
            pop.show();
        } catch (Exception e) {
            Log.e(LOG, "-- popup failed, probably nullpointer", e);
        }
    }

    private void getStaffLocationTracks(Integer staffID) {
        RequestDTO w = new RequestDTO(RequestDTO.GET_LOCATION_TRACK_BY_STAFF_IN_PERIOD);
        w.setStaffID(staffID);

        mListener.setBusy(true);
        NetUtil.sendRequest(getActivity(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.getLocationTrackerList() != null
                                && !response.getLocationTrackerList().isEmpty()) {
                            Intent w = new Intent(getActivity(), MonitorMapActivity.class);
                            w.putExtra("response", response);
                            startActivity(w);
                        } else {
                            Util.showToast(getActivity(), "Location not available");
                        }
                    }
                });

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

    StaffDTO staff;


    @Override
    public void animateHeroHeight() {
        Util.expand(hero, 500, null);

    }

    String pageTitle = "Staff";

    @Override
    public void setPageTitle(String title) {
        pageTitle = title;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }


    public interface CompanyStaffListListener {
        void onNewCompanyStaff();

        void setBusy(boolean busy);
        void onLocationSendRequired(List<Integer> staffList, List<Integer> monitorList);

        void onCompanyStaffInvitationRequested(List<StaffDTO> companyStaffList, int index);

        void onCompanyStaffPictureRequested(StaffDTO companyStaff);

        void onCompanyStaffEditRequested(StaffDTO companyStaff);

    }

    int primaryColor, darkColor;

    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }

    List<StaffDTO> staffList;
    StaffListAdapter staffAdapter;
    static final String LOG = StaffListFragment.class.getSimpleName();
}
