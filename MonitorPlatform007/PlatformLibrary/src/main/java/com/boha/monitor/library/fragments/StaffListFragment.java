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

import com.boha.monitor.library.activities.HighDefActivity;
import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.activities.MonitorMapActivity;
import com.boha.monitor.library.adapters.PopupListIconAdapter;
import com.boha.monitor.library.adapters.StaffListAdapter;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.SimpleMessageDTO;
import com.boha.monitor.library.dto.SimpleMessageDestinationDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.PopupItem;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A fragment that manages a list of Staff assigned to a project
 * <p/>
 * Activities containing this fragment MUST implement the CompanyStaffListListener
 * interface.
 */
public class StaffListFragment extends Fragment
        implements PageFragment {


    private CompanyStaffListListener mListener;
    MonApp monApp;

    public MonApp getMonApp() {
        return monApp;
    }

    public void setMonApp(MonApp monApp) {
        this.monApp = monApp;
    }
    /**
     * The fragment's ListView/GridView.
     */
    private RecyclerView mRecycler;

    public StaffListFragment() {
    }

    Context ctx;
    TextView txtCount, txtName, txtNumber;
    View view, topView, handle;
    FloatingActionButton fab;
    ImageView hero;
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

        paneLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        editMessage = (EditText) view.findViewById(R.id.FSL_message);
        btnSend = (Button) view.findViewById(R.id.FSL_btnSend);
        txtPerson = (TextView) view.findViewById(R.id.FSL_name);
        txtFromMsg = (TextView) view.findViewById(R.id.FSL_fromMessage);
        iconClose = (ImageView) view.findViewById(R.id.FSL_iconClose);
        txtFromMsg.setVisibility(View.GONE);
        paneLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        txtCount = (TextView) view.findViewById(R.id.FAB_text);
        txtName = (TextView) view.findViewById(R.id.STAFF_LIST_label);
        txtNumber = (TextView) view.findViewById(R.id.STAFF_LIST_number);
        handle = view.findViewById(R.id.handle);
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

        pageTitle = getString(R.string.staff);
        getStaffList();

        return view;
    }

    public void updateStaffList(List<StaffDTO> list) {
        staffList = list;
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setList();
                }
            });
        }

    }

    /**
     * Get list of Staff from cache and set the RecyclerView
     */
    public void getStaffList() {
        Snappy.getStaffList(monApp, new Snappy.SnappyReadListener() {
            @Override
            public void onDataRead(final ResponseDTO response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            staffList = response.getStaffList();
                            setList();
                        }
                    });

                }
            }

            @Override
            public void onError(String message) {

            }
        });

    }

    private void sendMessageToSelectedStaffMember() {
        StaffDTO from = SharedUtil.getCompanyStaff(getActivity());
        if (editMessage.getText().toString().isEmpty()) {
            Util.showToast(getActivity(), "Please enter message");
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
                        List<Integer> staffList = new ArrayList<>();
                        for (StaffDTO x : StaffListFragment.this.staffList) {
                            staffList.add(x.getStaffID());
                        }
                        mListener.onLocationSendRequired(new ArrayList<Integer>(), staffList);

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
        Log.d(LOG, "##### setList");
        staffAdapter = new StaffListAdapter(staffList, darkColor,
                getActivity(), new StaffListAdapter.StaffListListener() {
            @Override
            public void onStaffNameClicked(StaffDTO s) {
                staff = s;
                SharedUtil.saveLastStaffID(getActivity(),staff.getStaffID());
                showPopup(staff);
            }
            @Override
            public void onHighDefPhoto(PhotoUploadDTO photo) {
//                SharedUtil.saveLastStaffID(getActivity(),staff.getStaffID());
                Intent w = new Intent(getContext(), HighDefActivity.class);
                w.putExtra("photo",photo);
                startActivity(w);
            }
        });

        txtNumber.setText("" + staffList.size());
        LinearLayoutManager llm =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(llm);
        mRecycler.setAdapter(staffAdapter);

        int index = getIndex();
        if (index > 0) {
            mRecycler.scrollToPosition(index);
        }

    }
    private int getIndex() {
        if (getActivity() == null) return 0;
        Integer x = SharedUtil.getLastStaffID(getActivity());
        int index = 0;
        for (StaffDTO m: staffList) {
            if (m.getStaffID().intValue() == x.intValue()) {
                return index;
            }
            index++;
        }
        return 0;
    }
    private void showPopup(final StaffDTO staff) {
        popupItemList = new ArrayList<>();
        PopupItem item1 = new PopupItem(R.drawable.ic_action_location_on, ctx.getString(R.string.send_my_location));
        PopupItem item2 = new PopupItem(R.drawable.ic_action_email, ctx.getString(R.string.send_message));
        popupItemList.add(item2);
        popupItemList.add(item1);
        if (SharedUtil.getCompanyStaff(getActivity()) != null) {
            PopupItem item3 = new PopupItem(R.drawable.ic_action_location_on, ctx.getString(R.string.get_location));
            PopupItem item4 = new PopupItem(R.drawable.ic_action_add_circle, ctx.getString(R.string.proj_assgn));
            PopupItem item5 = new PopupItem(R.drawable.ic_action_edit, ctx.getString(R.string.upd_profile));
            PopupItem item6 = new PopupItem(R.drawable.ic_action_add_person, ctx.getString(R.string.generate_pin));

            popupItemList.add(item3);
            popupItemList.add(item4);
            popupItemList.add(item5);
            popupItemList.add(item6);
        }

        final ListPopupWindow pop = new ListPopupWindow(getActivity());
        LayoutInflater inf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inf.inflate(R.layout.hero_image_popup, null);
        TextView txt = (TextView) v.findViewById(R.id.HERO_caption);
        txt.setText(staff.getFullName());
        ImageView img = (ImageView) v.findViewById(R.id.HERO_image);
        CircleImageView cimg = (CircleImageView) v.findViewById(R.id.HERO_personImage);
        if (!staff.getPhotoUploadList().isEmpty()) {
            PhotoUploadDTO p = staff.getPhotoUploadList().get(0);
            cimg.setAlpha(1.0f);
            Picasso.with(getContext()).load(p.getSecureUrl()).into(cimg);
        }
        img.setImageDrawable(Util.getRandomBackgroundImage(ctx));

        pop.setPromptView(v);
        pop.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);

        pop.setAnchorView(handle);
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
                    sList.add(StaffListFragment.this.staff.getStaffID());
                    mListener.onLocationSendRequired(mList, sList);
                }
                if (item.getText().equalsIgnoreCase(ctx.getString(R.string.get_location))) {
                    getStaffLocationTracks(StaffListFragment.this.staff.getStaffID());
                }
                if (item.getText().equalsIgnoreCase(ctx.getString(R.string.proj_assgn))) {
                    mListener.onProjectAssigmentWanted(staff);
                }
                if (item.getText().equalsIgnoreCase(ctx.getString(R.string.upd_profile))) {
                    mListener.onStaffEditRequested(staff);
                }
                if (item.getText().equalsIgnoreCase(ctx.getString(R.string.generate_pin))) {
                    generatePIN(staff);
                }

            }
        });
        try {
            pop.show();
        } catch (Exception e) {
            Log.e(LOG, "-- popup failed, probably nullpointer", e);
        }
    }

    private void generatePIN(StaffDTO staff) {
        RequestDTO w = new RequestDTO(RequestDTO.GENERATE_STAFF_PIN);
        w.setStaffID(staff.getStaffID());

        mListener.setBusy(true);
        NetUtil.sendRequest(getActivity(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.setBusy(false);
                        if (response.getStatusCode() == 0) {
                            StaffDTO x = response.getStaff();
                            Util.sendNewPIN(getActivity(), x.getFullName(), x.getEmail(),
                                    x.getPin(), Util.STAFF);
                        }
                    }
                });

            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showErrorToast(getActivity(), message);
                    }
                });
            }
        });
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
                        Util.showErrorToast(getActivity(), message);
                    }
                });
            }


        });
    }

    StaffDTO staff;


    @Override
    public void animateHeroHeight() {
        if (hero != null) {
            Log.w(LOG,"animateHeroHeight, hero not null");
            hero.setImageDrawable(Util.getRandomBackgroundImage(getActivity()));
            Util.expand(hero, 500, null);
        } else {
            Log.e(LOG,"animateHeroHeight, hero is NULL");
        }


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


    public interface CompanyStaffListListener {
        void onNewCompanyStaff();

        void setBusy(boolean busy);

        void onProjectAssigmentWanted(StaffDTO staff);

        void onLocationSendRequired(List<Integer> staffList, List<Integer> monitorList);

        void onCompanyStaffInvitationRequested(List<StaffDTO> companyStaffList, int index);

        void onStaffPictureRequested(StaffDTO companyStaff);

        void onStaffEditRequested(StaffDTO companyStaff);

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
