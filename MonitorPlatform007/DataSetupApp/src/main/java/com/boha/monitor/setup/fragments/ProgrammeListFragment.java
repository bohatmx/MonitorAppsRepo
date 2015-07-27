package com.boha.monitor.setup.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boha.monitor.library.dto.PortfolioDTO;
import com.boha.monitor.library.dto.ProgrammeDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.adapters.ProgrammeAdapter;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class ProgrammeListFragment extends Fragment {

    private ProgrammeAdapter.ProgrammeListener mListener;
    private PortfolioDTO portfolio;
    private RecyclerView recyclerView;
    private TextView txtHeader, txtCount;
    private View view;
    private EditText editName;
    private Button btn;
    private LinearLayout subLayout;
    private Context ctx;
    private FloatingActionButton fab;
    private LayoutInflater inflater;
    private ProgrammeAdapter adapter;
    private List<ProgrammeDTO> programmeList;

    public static ProgrammeListFragment newInstance(PortfolioDTO portfolio) {
        ProgrammeListFragment fragment = new ProgrammeListFragment();
        Bundle args = new Bundle();
        args.putSerializable("portfolio", portfolio);
        fragment.setArguments(args);
        return fragment;
    }

    public ProgrammeListFragment() {
    }

    public void setPortfolio(PortfolioDTO portfolio) {
        this.portfolio = portfolio;
        programmeList = portfolio.getProgrammeList();
        setList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            portfolio = (PortfolioDTO) getArguments().getSerializable("portfolio");
            programmeList = portfolio.getProgrammeList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ctx = getActivity();
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_list, container, false);
        setFields();
        if (portfolio != null) {
            programmeList = portfolio.getProgrammeList();
            setList();
        }
        return view;
    }

    private void setFields() {
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        subLayout = (LinearLayout) view.findViewById(R.id.FCL_emptyLayout);
        recyclerView = (RecyclerView) view.findViewById(R.id.FCL_list);
        txtCount = (TextView) view.findViewById(R.id.FCL_count);
        txtHeader = (TextView) view.findViewById(R.id.FCL_title);

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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = inflater.inflate(R.layout.edit_name, null);
                editName = (EditText) view.findViewById(R.id.ENAME_editName);
                btn = (Button) view.findViewById(R.id.ENAME_btn);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendData();
                    }
                });
                subLayout.removeAllViews();
                subLayout.addView(view);
            }
        });
    }

    private ResponseDTO response;

    private void sendData() {
        RequestDTO w = new RequestDTO(RequestDTO.ADD_PROGRAMMES);
        w.setProgrammeList(new ArrayList<ProgrammeDTO>());
        final ProgrammeDTO prog = new ProgrammeDTO();
        prog.setPortfolioID(portfolio.getPortfolioID());
        prog.setProgrammeName(editName.getText().toString());
        w.getProgrammeList().add(prog);

        mListener.setBusy(true);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO r) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.setBusy(false);
                        response = r;
                        for (PortfolioDTO x : response.getPortfolioList()) {
                            if (x.getPortfolioID().intValue() == portfolio.getPortfolioID().intValue()) {
                                portfolio = x;
                                programmeList = portfolio.getProgrammeList();
                                setList();
                                mListener.onCompanyDataRefreshed(r, portfolio.getCompanyID());
                                break;
                            }
                        }

                    }
                });

            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.setBusy(false);
                        Util.showErrorToast(ctx, message);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }

    private void setList() {
        adapter = new ProgrammeAdapter(programmeList, ctx, new ProgrammeAdapter.ProgrammeListener() {
            @Override
            public void onProgrammeClicked(ProgrammeDTO programme) {

            }

            @Override
            public void onProjectCountClicked(ProgrammeDTO programme) {
                mListener.onProjectCountClicked(programme);
            }

            @Override
            public void onTaskTypeCountClicked(ProgrammeDTO programme) {
                mListener.onTaskTypeCountClicked(programme);
            }

            @Override
            public void onTaskImportRequested(ProgrammeDTO programme) {
                mListener.onTaskImportRequested(programme);
            }

            @Override
            public void onProjectImportRequested(ProgrammeDTO programme) {
                mListener.onProjectImportRequested(programme);
            }

            @Override
            public void onIconDeleteClicked(ProgrammeDTO programme, int position) {

            }

            @Override
            public void onIconEditClicked(ProgrammeDTO programme, int position) {

            }

            @Override
            public void onCompanyDataRefreshed(ResponseDTO response, Integer companyID) {
                mListener.onCompanyDataRefreshed(response, companyID);
            }

            @Override
            public void setBusy(boolean busy) {
                mListener.setBusy(true);
            }
        });
        txtHeader.setText("Programmes: ");
        txtCount.setText("" + programmeList.size());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ProgrammeAdapter.ProgrammeListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.getLocalClassName()
                    + " must implement ProgrammeAdapter.ProgrammeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void refreshProgramme(ProgrammeDTO programme) {
        for (ProgrammeDTO x: programmeList) {
            if (x.getProgrammeID().intValue() == programme.getProgrammeID().intValue()) {
                x = programme;
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }
}
