package com.boha.monitor.setup.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.adapters.CompanyAdapter;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CompanyListener} interface
 * to handle interaction events.
 * Use the {@link CompanyListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CompanyListFragment extends Fragment {

    private CompanyListener mListener;
    private ResponseDTO response;
    private RecyclerView recyclerView;
    private TextView txtHeader, txtCount;
    private View view;
    private CompanyAdapter adapter;
    private Context ctx;


    public static CompanyListFragment newInstance(ResponseDTO response) {
        CompanyListFragment fragment = new CompanyListFragment();
        Bundle args = new Bundle();
        args.putSerializable("response", response);
        fragment.setArguments(args);
        return fragment;
    }

    public CompanyListFragment() {
    }

    public void setResponse(ResponseDTO response) {
        this.response = response;
        setList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getActivity();
        if (getArguments() != null) {
            response = (ResponseDTO) getArguments().getSerializable("response");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_company_list, container, false);
        setFields();
        getCachedData();
        return view;
    }
    private void getCachedData() {
        CacheUtil.getCachedData(ctx, CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO r) {
                if (r != null) {
                    if (r.getCompanyList() != null) {
                        response = r;
                        setList();
                        return;
                    }
                }
                getCompanyList();
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {
                getCompanyList();

            }
        });
    }
    public void refreshCompanyData(CompanyDTO company) {
        for (CompanyDTO c: response.getCompanyList()) {
            if (company.getCompanyID().intValue() == c.getCompanyID().intValue()) {
                c = company;
                break;
            }
        }
        setList();
    }
    public void getCompanyList() {
        RequestDTO w = new RequestDTO(RequestDTO.GET_COMPANY_LIST);
        mListener.setBusy(true);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO r) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.setBusy(false);
                        response = r;
                        setList();
                        CacheUtil.cacheData(ctx, response, CacheUtil.CACHE_DATA,
                                new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {

                            }

                            @Override
                            public void onDataCached() {

                            }

                            @Override
                            public void onError() {

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
                        mListener.setBusy(false);
                        Snackbar.make(recyclerView,message,Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }
    private void setFields() {

        recyclerView = (RecyclerView)view.findViewById(R.id.FCL_list);
        txtCount = (TextView)view.findViewById(R.id.FCL_count);
        txtHeader = (TextView)view.findViewById(R.id.FCL_title);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(RecyclerView.ViewHolder holder) {
            }
        });
        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getActivity())
                        .color(R.color.green_100)
                        .sizeResId(R.dimen.mon_divider)
                        .marginResId(R.dimen.mon_divider, R.dimen.mon_divider)
                        .build());
    }

    private void setList() {
        adapter = new CompanyAdapter(response.getCompanyList(), ctx, new CompanyListener() {
            @Override
            public void onCompanyClicked(CompanyDTO company) {
                mListener.onCompanyClicked(company);
            }

            @Override
            public void onPortfolioCountClicked(CompanyDTO company) {
                mListener.onPortfolioCountClicked(company);
            }

            @Override
            public void onIconDeleteClicked(CompanyDTO company, int position) {
                mListener.onIconDeleteClicked(company, position);
            }

            @Override
            public void onIconEditClicked(CompanyDTO company, int position) {
                mListener.onIconEditClicked(company, position);
            }

            @Override
            public void setBusy(boolean busy) {
                mListener.setBusy(busy);
            }
        });
        recyclerView.setAdapter(adapter);
//        txtHeader.setText("Subscriber Organisations");
//        statusCount.setText("" + response.getCompanyList().size());
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (CompanyListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.getLocalClassName()
                    + " must implement CompanyListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public interface CompanyListener {
        void onCompanyClicked(CompanyDTO company);

        void onPortfolioCountClicked(CompanyDTO company);

        void onIconDeleteClicked(CompanyDTO company, int position);

        void onIconEditClicked(CompanyDTO company, int position);
        void setBusy(boolean busy);
    }
}
