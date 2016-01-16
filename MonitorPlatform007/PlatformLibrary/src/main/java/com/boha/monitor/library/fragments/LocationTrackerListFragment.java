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
import android.widget.ImageView;

import com.boha.monitor.library.adapters.LocationTrackerListAdapter;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.SimpleDividerItemDecoration;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.List;

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
    private ResponseDTO response;
    List<String> list;
    List<LocationTrackerDTO> trackerDTOList;
    RecyclerView recyclerView;
    Context ctx;
    LocationTrackerDTO locationTrackerDTO;
    View view;
    ImageView hero;

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
            ResponseDTO w = (ResponseDTO) b.getSerializable("trackerDTOList");
            if (w != null) {
                trackerDTOList = w.getLocationTrackerList();
                return;
            }
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            ResponseDTO w = (ResponseDTO) bundle.getSerializable("trackerDTOList");
            if (w != null) {
                trackerDTOList = w.getLocationTrackerList();
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

        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));


        return view;
    }



       LocationTrackerListAdapter locationTrackerListAdapter;
    public void setList(List<LocationTrackerDTO> list) {
        Log.d(LOG, " setList: " + list.size());
        trackerDTOList = list;
        //Collections.sort(trackerDTOList);

        locationTrackerListAdapter = new LocationTrackerListAdapter(trackerDTOList, darkColor, ctx, new LocationTrackerListAdapter.LocationTrackerListListener() {
            @Override
            public void onClicked(LocationTrackerDTO tracker) {
                locationTrackerDTO = tracker;
                mListener.displayOnMap(tracker);
            }
        });

        recyclerView.setAdapter(locationTrackerListAdapter);
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
    public interface LocationTrackerListFragmentListener {
        void setBusy(boolean busy);
        void displayOnMap(LocationTrackerDTO tracker);
    }

    int primaryColor, darkColor;

    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }

}
