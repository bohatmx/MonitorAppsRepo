package com.boha.monitor.library.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.adapters.LocalPictureRecyclerAdapter;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.util.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aubreyM on 14/12/17.
 */
public class LocalGalleryFragment extends Fragment implements PageFragment{
    View view;
    Context ctx;
    RecyclerView list;
    TextView title;
    List<String> pathList = new ArrayList<>();
    LocalPictureRecyclerAdapter adapter;
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_picture_recycler_grid, container, false);
        list = (RecyclerView) view.findViewById(R.id.FI_recyclerView);
        title = (TextView) view.findViewById(R.id.RCV_title);
        ctx = getActivity();

        list.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        list.setItemAnimator(new DefaultItemAnimator());
        list.addItemDecoration(new DividerItemDecoration(ctx, RecyclerView.VERTICAL));


        if (getArguments() != null) {
            ResponseDTO r = (ResponseDTO)getArguments().getSerializable("response");
            pathList = r.getImageFileNameList();

        }
        adapter = new LocalPictureRecyclerAdapter(pathList,1,getActivity());
        list.setAdapter(adapter);

        return view;
    }

    public void addPicture(String path) {
        pathList.add(0,path);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
    @Override
    public void animateHeroHeight() {

    }

    @Override
    public void setPageTitle(String title) {

    }

    @Override
    public String getPageTitle() {
        return null;
    }
}
