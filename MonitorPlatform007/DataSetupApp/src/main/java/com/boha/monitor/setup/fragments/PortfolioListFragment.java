package com.boha.monitor.setup.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boha.monitor.library.dto.PortfolioDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
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
public class PortfolioListFragment extends Fragment implements PageFragment {

    private PortfolioFragmentListener mListener;
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

    public static PortfolioListFragment newInstance(List<PortfolioDTO> list) {
        PortfolioListFragment fragment = new PortfolioListFragment();
        Bundle args = new Bundle();
        ResponseDTO response = new ResponseDTO();
        response.setPortfolioList(list);
        args.putSerializable("portfolioList", response);
        fragment.setArguments(args);
        return fragment;
    }

    public PortfolioListFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ResponseDTO w = (ResponseDTO)getArguments().getSerializable("portfolioList");
            portfolioList = w.getPortfolioList();
            return;
        }
        if (savedInstanceState != null) {
            ResponseDTO w = (ResponseDTO)savedInstanceState.getSerializable("portfolioList");
            portfolioList = w.getPortfolioList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG, "PortfolioListFragment onCreateView");
        ctx = getActivity();
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_portfolio_coord, container, false);
        setFields();

        setList();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        ResponseDTO w = new ResponseDTO();
        w.setPortfolioList(portfolioList);
        b.putSerializable("portfolioList", w);

        super.onSaveInstanceState(b);
    }
    private void setFields() {
        fab = (FloatingActionButton)view.findViewById(R.id.fab);
        subLayout = (LinearLayout)view.findViewById(R.id.emptyLayout);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        txtCount = (TextView) view.findViewById(R.id.count);
        txtHeader = (TextView) view.findViewById(R.id.headerText);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        recyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getActivity())
                        .color(R.color.green_100)
                        .sizeResId(R.dimen.mon_divider)
                        .marginResId(R.dimen.mon_divider, R.dimen.mon_divider)
                        .build());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = inflater.inflate(R.layout.edit_name, null);
                view.setVisibility(View.VISIBLE);
                editName = (EditText) view.findViewById(R.id.ENAME_editName);
                btn = (Button) view.findViewById(R.id.ENAME_btnSubmit);
                Button btnCancel = (Button) view.findViewById(R.id.ENAME_btnCancel);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendData();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        view.setVisibility(View.GONE);
                    }
                });
                subLayout.removeAllViews();
                subLayout.addView(view);
            }
        });
    }

    public void setPortfolioList(List<PortfolioDTO> portfolioList) {
        this.portfolioList = portfolioList;
        setList();
    }

    private void sendData() {
        RequestDTO w = new RequestDTO(RequestDTO.ADD_PORTFOLOIOS);
        w.setPortfolioList(new ArrayList<PortfolioDTO>());
        final PortfolioDTO portfolio = new PortfolioDTO();
        portfolio.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());
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


        });
    }
    private void setList() {
        adapter = new PortfolioAdapter(portfolioList, ctx, new PortfolioAdapter.PortfolioAdapterListener() {
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
//        txtHeader.setText(SharedUtil.getCompany(ctx).getCompanyName());
        txtCount.setText("" + portfolioList.size());
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

    @Override
    public void animateHeroHeight() {

    }
    String pageTitle = "Portfolios";
    @Override
    public void setPageTitle(String title) {
        pageTitle = title;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    int primaryColor, darkColor;
    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
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

    static final String LOG = PortfolioListFragment.class.getSimpleName();

}
