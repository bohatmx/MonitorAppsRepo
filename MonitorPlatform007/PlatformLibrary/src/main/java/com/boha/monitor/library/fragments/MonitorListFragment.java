package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.boha.monitor.library.adapters.MonitorAdapter;
import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MonitorListListener} interface
 * to handle interaction events.
 * Use the {@link MonitorListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonitorListFragment extends Fragment implements PageFragment{

    private MonitorListListener mListener;
    private ResponseDTO response;
    List<String> list;
    List<MonitorDTO> monitorList;
    MonitorAdapter monitorAdapter;
    ListView mListView;
    Context ctx;
    MonitorDTO monitor;
    TextView txtCount, txtName;
    View view, topView, fab;
    ImageView icon;
    public static MonitorListFragment newInstance() {
        MonitorListFragment fragment = new MonitorListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MonitorListFragment() {
    }
    CompanyDTO company;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ctx = getActivity();
        view = inflater.inflate(R.layout.fragment_monitor_list, container, false);
        txtCount = (TextView) view.findViewById(R.id.FAB_text);
        txtName = (TextView) view.findViewById(R.id.MONITOR_LIST_label);
        topView = view.findViewById(R.id.MONITOR_LIST_top);
        fab = view.findViewById(R.id.FAB);
        icon = (ImageView)view.findViewById(R.id.MONITOR_LIST_icon);
        mListView = (ListView) view.findViewById(R.id.MONITOR_LIST_list);

        getCachedData();
        return view;
    }


    private void getCachedData() {
        CacheUtil.getCachedPortfolioList(getActivity(), new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getMonitorList() != null) {
                    monitorList = response.getMonitorList();
                    setList();
                }
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
                    list = new ArrayList<>();
                    list.add(ctx.getString(R.string.get_status));
                    list.add(ctx.getString(R.string.take_picture));
                    list.add(ctx.getString(R.string.send_app_link));
                    list.add(ctx.getString(R.string.edit));
                    View v = Util.getHeroView(ctx, ctx.getString(R.string.select_action));

                    Util.showPopupBasicWithHeroImage(ctx,getActivity(),list,txtName, monitor.getFullName(),new Util.UtilPopupListener() {
                        @Override
                        public void onItemSelected(int index) {
                            switch (index) {
                                case 0:
                                    Util.showToast(ctx,getString(R.string.under_cons));
                                    break;
                                case 1:
                                    mListener.onMonitorPhotoRequired(monitor);
                                    break;
                                case 2:
                                    int index2 = 0;
                                    for (MonitorDTO s: monitorList) {
                                        if (s.getMonitorID().intValue() == monitor.getMonitorID().intValue()) {
                                            break;
                                        }
                                        index2++;
                                    }
                                    break;
                                case 3:
                                    mListener.onMonitorEditRequested(monitor);
                                    break;
                            }
                        }
                    });
                }
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
    public void animateHeroHeight() {

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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface MonitorListListener {
         void onMonitorSelected(MonitorDTO monitor);
         void onMonitorPhotoRequired(MonitorDTO monitor);
         void onMonitorEditRequested(MonitorDTO monitor);
    }

}
