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

import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.PortfolioDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.adapters.PortfolioAdapter;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PortfolioFragmentListener} interface
 * to handle interaction events.
 * Use the {@link PortfolioListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PortfolioListFragment extends Fragment {

    private PortfolioFragmentListener mListener;
    private CompanyDTO company;
    private RecyclerView recyclerView;
    private TextView txtHeader, txtCount;
    private View view;
    private EditText editName;
    private Button btn;
    private LinearLayout subLayout;
    private Context ctx;
    private FloatingActionButton fab;
    private LayoutInflater inflater;
    private PortfolioAdapter adapter;
    private List<PortfolioDTO> portfolioList;

    public static PortfolioListFragment newInstance(CompanyDTO company) {
        PortfolioListFragment fragment = new PortfolioListFragment();
        Bundle args = new Bundle();
        args.putSerializable("company", company);
        fragment.setArguments(args);
        return fragment;
    }

    public PortfolioListFragment() {
    }

    public void setCompany(CompanyDTO company) {
        this.company = company;
        portfolioList = company.getPortfolioList();
        setList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            company = (CompanyDTO) getArguments().getSerializable("company");
            portfolioList = company.getPortfolioList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ctx = getActivity();
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_list, container, false);
        setFields();
        if (company != null) {
            setList();
        }
        return view;
    }

    private void setFields() {
        fab = (FloatingActionButton)view.findViewById(R.id.fab);
        subLayout = (LinearLayout)view.findViewById(R.id.FCL_emptyLayout);
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



    private void sendData() {
        RequestDTO w = new RequestDTO(RequestDTO.ADD_PORTFOLOIOS);
        w.setPortfolioList(new ArrayList<PortfolioDTO>());
        final PortfolioDTO portfolio = new PortfolioDTO();
        portfolio.setCompanyID(company.getCompanyID());
        portfolio.setPortfolioName(editName.getText().toString());
        w.getPortfolioList().add(portfolio);

        mListener.setBusy(true);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.setBusy(false);
                        if (response.getPortfolioList() != null) {
                            portfolioList = response.getPortfolioList();
                            setList();
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
                        Util.showErrorToast(ctx,message);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }
    private void setList() {
        adapter = new PortfolioAdapter(company.getPortfolioList(), ctx, new PortfolioAdapter.PortfolioAdapterListener() {
            @Override
            public void onPortfolioClicked(PortfolioDTO portfolio) {
                mListener.onPortfolioClicked(portfolio);
            }

            @Override
            public void onProgramCountClicked(PortfolioDTO portfolio) {
                mListener.onProgrammeCountClicked(portfolio);
            }

            @Override
            public void onIconDeleteClicked(PortfolioDTO portfolio, int position) {
                mListener.onIconDeleteClicked(portfolio, position);
            }

            @Override
            public void onIconEditClicked(PortfolioDTO portfolio, int position) {
                mListener.onIconEditClicked(portfolio, position);
            }
        });
        txtHeader.setText("Portfolios: ");
        txtCount.setText("" + company.getPortfolioList().size());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PortfolioFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.getLocalClassName()
                    + " must implement PortfolioListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface PortfolioFragmentListener {
        void setBusy(boolean busy);
        void onPortfolioClicked(PortfolioDTO portfolio);
        void onProgrammeCountClicked(PortfolioDTO portfolio);

        void onIconDeleteClicked(PortfolioDTO portfolio, int position);

        void onIconEditClicked(PortfolioDTO portfolio, int position);
    }

}
