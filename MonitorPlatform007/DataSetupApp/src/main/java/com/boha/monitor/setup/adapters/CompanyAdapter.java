package com.boha.monitor.setup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.fragments.CompanyListFragment;

import java.util.List;

/**
 * Created by aubreyM on 14/12/17.
 */
public class CompanyAdapter extends RecyclerView.Adapter<CompanyAdapter.CompanyViewHolder> {



    private CompanyListFragment.CompanyListener listener;
    private List<CompanyDTO> companyList;
    private Context ctx;

    public CompanyAdapter(List<CompanyDTO> companys,
                          Context context, CompanyListFragment.CompanyListener listener) {
        this.companyList = companys;
        this.ctx = context;
        this.listener = listener;
    }


    @Override
    public CompanyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CompanyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.company_item, parent, false));

    }

    @Override
    public void onBindViewHolder(final CompanyViewHolder vh, final int position) {

        final CompanyDTO p = companyList.get(position);
        vh.number.setText("" + (position + 1));
        vh.name.setText(p.getCompanyName());
        vh.position = position;
        vh.portfolioCount.setText("" + p.getPortfolioList().size());


        setListener(vh.name, p);
        setListener(vh.number, p);

        vh.portfolioLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPortfolioCountClicked(p);
            }
        });


        vh.iconDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onIconDeleteClicked(p, position);
            }
        });
        vh.iconEDit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onIconEditClicked(p, position);
            }
        });

    }

    private void setListener(View view, final CompanyDTO dto) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCompanyClicked(dto);
            }
        });
    }

    public int getItemCount() {
        return companyList == null ? 0 : companyList.size();
    }

    public class CompanyViewHolder extends RecyclerView.ViewHolder {
        protected ImageView iconDelete, iconEDit;
        protected TextView name, number, portfolioCount;
        protected int position;
        protected View portfolioLayout;


        public CompanyViewHolder(View itemView) {
            super(itemView);
            portfolioLayout = itemView.findViewById(R.id.CO_portfolioLayout);
            iconDelete = (ImageView) itemView.findViewById(R.id.CO_delete);
            iconEDit = (ImageView) itemView.findViewById(R.id.CO_edit);
            name = (TextView) itemView.findViewById(R.id.CO_name);
            number = (TextView) itemView.findViewById(R.id.CO_number);

            portfolioCount = (TextView) itemView.findViewById(R.id.CO_portfolioCount);
        }

    }

    static final String LOG = CompanyAdapter.class.getSimpleName();
}
