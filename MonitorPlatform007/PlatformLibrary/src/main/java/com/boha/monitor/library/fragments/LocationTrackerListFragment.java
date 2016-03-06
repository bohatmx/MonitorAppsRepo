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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.adapters.LocationTrackerListAdapter;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.SimpleDividerItemDecoration;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Fragment that manages a list of Monitors assigned to a project
 * Activities that contain this fragment must implement the
 * {@link LocationTrackerListFragmentListener} interface
 * to handle interaction events.
 * Use the {@link LocationTrackerListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationTrackerListFragment extends Fragment implements PageFragment {

    private LocationTrackerListFragmentListener mListener;

    private List<LocationTrackerDTO> locationTrackerList;
    private RecyclerView recyclerView;
    private Context ctx;
    private LocationTrackerDTO locationTrackerDTO;
    private View view;
    private ImageView hero;
    private AutoCompleteTextView auto;
    private TextView txtCount;
    private LocationTrackerListAdapter locationTrackerListAdapter;
    private List<String> nameList;

    static final String LOG = LocationTrackerListFragment.class.getSimpleName();

    public static LocationTrackerListFragment newInstance(List<LocationTrackerDTO> list) {
        LocationTrackerListFragment fragment = new LocationTrackerListFragment();
        Bundle args = new Bundle();
        ResponseDTO w = new ResponseDTO();
        w.setLocationTrackerList(list);
        args.putSerializable("locationTrackerList", w);
        fragment.setArguments(args);
        return fragment;
    }

    public LocationTrackerListFragment() {
    }


    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        Log.d(LOG, "LocationTrackerListFragment onCreate");
        if (b != null) {
            ResponseDTO w = (ResponseDTO) b.getSerializable("locationTrackerList");
            if (w != null) {
                locationTrackerList = w.getLocationTrackerList();
                return;
            }
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            ResponseDTO w = (ResponseDTO) bundle.getSerializable("locationTrackerList");
            if (w != null) {
                locationTrackerList = w.getLocationTrackerList();
                return;
            }
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(LOG, "LocationTrackerListFragment onCreateView");
        ctx = getActivity();
        view = inflater.inflate(R.layout.location_tracker_fragment, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        auto = (AutoCompleteTextView) view.findViewById(R.id.autocomplete_project);
        txtCount = (TextView) view.findViewById(R.id.count);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        setList(locationTrackerList);
        return view;
    }




    private void setNameList() {
        nameList = new ArrayList<>(locationTrackerList.size());
        for (LocationTrackerDTO p: locationTrackerList) {
            if (p.getStaffName() != null) {
                nameList.add(p.getStaffName());
            }
            if (p.getMonitorName() != null) {
                nameList.add(p.getMonitorName());
            }
        }
    }

    public void setList(List<LocationTrackerDTO> list) {
        locationTrackerList = list;
        Log.d(LOG, " setList: " + locationTrackerList.size());
        txtCount.setText("" + locationTrackerList.size());
        if (locationTrackerList.size() > 2) {
            setNameList();
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    R.layout.simple_spinner_item, nameList);
            auto.setAdapter(adapter);
            auto.setHint("Search Devices");
            auto.setThreshold(1);
            auto.setVisibility(View.VISIBLE);

            auto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    hideKeyboard();
                    recyclerView.scrollToPosition(i);
                    auto.setText("");

                    int index = 0;
                    String name = adapter.getItem(i);
                    for (LocationTrackerDTO p : locationTrackerList) {
                        if (p.getStaffName() != null) {
                            if (p.getStaffName().equalsIgnoreCase(name)) {
                                Log.d(LOG, "... scrolling to index " + index + " " + p.getStaffName());
                                recyclerView.scrollToPosition(index);
                                auto.setText("");
                                break;
                            }
                            index++;
                        }
                        if (p.getMonitorName() != null) {
                            if (p.getMonitorName().equalsIgnoreCase(name)) {
                                Log.d(LOG, "... scrolling to index " + index + " " + p.getMonitorName());
                                recyclerView.scrollToPosition(index);
                                auto.setText("");
                                break;
                            }
                            index++;
                        }
                    }
                }
            });

        } else {
            auto.setVisibility(View.GONE);
        }

        locationTrackerListAdapter = new LocationTrackerListAdapter(locationTrackerList, darkColor, ctx, new LocationTrackerListAdapter.LocationTrackerListListener() {
            @Override
            public void onClicked(LocationTrackerDTO tracker) {
                locationTrackerDTO = tracker;
                mListener.displayOnMap(tracker);
            }
        });

        recyclerView.setAdapter(locationTrackerListAdapter);
    }

    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(auto.getWindowToken(), 0);
    }

    public void setListener(LocationTrackerListFragmentListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (LocationTrackerListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement LocationTrackerListFragmentListener");
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

    String pageTitle = "LocationTracker";

    @Override
    public void setPageTitle(String title) {
        pageTitle = title;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }
    public interface LocationTrackerListFragmentListener {
        void displayOnMap(LocationTrackerDTO tracker);
    }

    int primaryColor, darkColor;

    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }

}
