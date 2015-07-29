package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.boha.monitor.library.adapters.PhotoAdapter;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.SpacesItemDecoration;
import com.boha.platform.library.R;

public class PhotoGridFragment extends Fragment implements PageFragment {

    ResponseDTO response;
    RecyclerView mRecyclerView;
    View view;
    PhotoAdapter adapter;
    static final String LOG = PhotoGridFragment.class.getSimpleName();

    public static PhotoGridFragment newInstance(ResponseDTO r) {
        PhotoGridFragment fragment = new PhotoGridFragment();
        Bundle args = new Bundle();
        args.putSerializable("response", r);
        fragment.setArguments(args);
        return fragment;
    }

    public PhotoGridFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG,"### onCreate");
        if (getArguments() != null) {
            response = (ResponseDTO) getArguments().getSerializable("response");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG,"### onCreateView");
        view = inflater.inflate(R.layout.fragment_photo_grid, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler);
        StaggeredGridLayoutManager x = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(2));
        mRecyclerView.setLayoutManager(x);

        if (response != null) {
            adapter = new PhotoAdapter(response.getPhotoUploadList(), PhotoAdapter.FULL_IMAGE, getActivity(), new PhotoAdapter.PictureListener() {
                @Override
                public void onPictureClicked(int position) {

                }
            });
            mRecyclerView.setAdapter(adapter);
        } else {
            Log.e(LOG,"--- response is NULL ... WTF?");
        }
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(LOG,"### onAttach");
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
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

}