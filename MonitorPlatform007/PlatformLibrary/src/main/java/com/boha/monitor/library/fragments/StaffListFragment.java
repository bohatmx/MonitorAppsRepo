package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.boha.monitor.library.adapters.StaffAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a taskStatusList of Items.
 * <project/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <project/>
 * Activities containing this fragment MUST implement the ProjectSiteListListener
 * interface.
 */
public class StaffListFragment extends Fragment
        implements  PageFragment {


    private CompanyStaffListListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

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
        Log.d(LOG,"StaffListFragment onCreate");
        if (bundle != null) {
            ResponseDTO w = (ResponseDTO)bundle.getSerializable("staffList");
            if (w != null) {
                staffList = w.getStaffList();
                return;
            }
        }
        Bundle c = getArguments();
        if (c != null) {
            ResponseDTO w = (ResponseDTO)c.getSerializable("staffList");
            if (w != null) {
                staffList = w.getStaffList();
                return;
            }
        }

    }

    Context ctx;
    TextView txtCount, txtName;
    View view, topView, fab;
    ImageView icon;
    ResponseDTO response;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG, "###### StaffListFragment onCreateView");
        view = inflater.inflate(R.layout.fragment_staff_list, container, false);
        ctx = getActivity();

        txtCount = (TextView) view.findViewById(R.id.FAB_text);
        txtName = (TextView) view.findViewById(R.id.STAFF_LIST_label);
        topView = view.findViewById(R.id.STAFF_LIST_top);
        fab = view.findViewById(R.id.FAB);
        icon = (ImageView)view.findViewById(R.id.STAFF_LIST_icon);
        mListView = (ListView) view.findViewById(R.id.STAFF_LIST_list);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(fab, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        mListener.onNewCompanyStaff();
                    }
                });
            }
        });

        setList();

        return view;
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
    public void onAttach( Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (CompanyStaffListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Host " + activity.getLocalClassName()
                    + " must implement CompanyStaffListListener");
        }
        Log.i("StaffListFragment","## onAttach, mListener = " + activity.getLocalClassName());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override public void onDestroy() {
        super.onDestroy();
//        RefWatcher refWatcher = MonApp.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }

    List<String> list;
    private void setList() {
        Log.e(LOG, "setList staffList: " + staffList.size());
        staffAdapter = new StaffAdapter(ctx, R.layout.monitor_card,
                staffList, new StaffAdapter.StaffAdapterListener() {
            @Override
            public void onPictureRequested(StaffDTO staff) {
                mListener.onCompanyStaffPictureRequested(staff);
            }

            @Override
            public void onStatusUpdatesRequested(StaffDTO staff) {

            }
        });

        mListView.setAdapter(staffAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mListener) {
                    staff = staffList.get(position);
                    list = new ArrayList<>();
                    list.add(ctx.getString(R.string.get_status));
                    list.add(ctx.getString(R.string.take_picture));
                    list.add(ctx.getString(R.string.send_app_link));
                    list.add(ctx.getString(R.string.edit));
                    View v = Util.getHeroView(ctx, ctx.getString(R.string.select_action));

                    Util.showPopupBasicWithHeroImage(ctx,getActivity(),list,txtName, staff.getFullName(),new Util.UtilPopupListener() {
                        @Override
                        public void onItemSelected(int index) {
                            switch (index) {
                                case 0:
                                    Util.showToast(ctx,getString(R.string.under_cons));
                                    break;
                                case 1:
                                    mListener.onCompanyStaffPictureRequested(staff);
                                    break;
                                case 2:
                                    int index2 = 0;
                                    for (StaffDTO s: staffList) {
                                        if (s.getStaffID().intValue() == staff.getStaffID().intValue()) {
                                            break;
                                        }
                                        index2++;
                                    }
                                    mListener.onCompanyStaffInvitationRequested(staffList,index2);
                                    break;
                                case 3:
                                    mListener.onCompanyStaffEditRequested(staff);
                                    break;
                            }
                        }
                    });
                }
            }
        });
    }

    StaffDTO staff;


    @Override
    public void animateHeroHeight() {
        Util.fadeIn(topView);
        Util.rotateViewWithDelay(getActivity(),fab,500,1000, new Util.UtilAnimationListener() {
            @Override
            public void onAnimationEnded() {
                Util.flashOnce(icon,300,null);
            }
        });

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

    public void addCompanyStaff( StaffDTO staff) {
        if (staffList == null) {
            staffList = new ArrayList<>();
        }
        staffList.add(staff);
//        Collections.sort(monitorList);
        staffAdapter.notifyDataSetChanged();
        txtCount.setText("" + staffList.size());
        Util.preen(txtCount, 300, 4, new Util.UtilAnimationListener() {
            @Override
            public void onAnimationEnded() {
                Util.animateRotationY(txtCount, 500);
            }
        });
        int index = 0;
        for (StaffDTO s: staffList) {
            if (s.getStaffID().intValue() == staff.getStaffID().intValue()) {
                break;
            }
            index++;
        }
        mListView.setSelection(index);

    }

    public void refreshList( StaffDTO staff) {

        String url = Util.getStaffImageURL(ctx,staff.getStaffID());
        MemoryCacheUtils.removeFromCache(url, ImageLoader.getInstance().getMemoryCache());
        DiskCacheUtils.removeFromCache(url, ImageLoader.getInstance().getDiskCache());
        Log.w("","### refreshList, image removed from caches ... " + staff.getFullName());
        setList();

        int index = 0;
        for (StaffDTO c: staffList) {
            if (staff.getStaffID() == c.getStaffID()) {
                break;
            }
            index++;
        }
        if (index < staffList.size()) {
            mListView.setSelection(index);
        }
    }

    public interface CompanyStaffListListener {
        public void onNewCompanyStaff();
        public void onCompanyStaffInvitationRequested(List<StaffDTO> companyStaffList, int index);
        public void onCompanyStaffPictureRequested(StaffDTO companyStaff);
        public void onCompanyStaffEditRequested(StaffDTO companyStaff);

    }
    ProjectDTO project;
    List<StaffDTO> staffList;
    StaffAdapter staffAdapter;
    static final String LOG = StaffListFragment.class.getSimpleName();
}
