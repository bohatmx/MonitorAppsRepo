package com.boha.monitor.setup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.PortfolioDTO;
import com.boha.monitor.library.dto.ProgrammeDTO;
import com.boha.monitor.setup.R;

import java.util.List;

/**
 * Created by aubreyM on 14/12/17.
 */
public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder> {

    public interface PortfolioAdapterListener {
        void onPortfolioClicked(PortfolioDTO portfolio);
        void onProgramCountClicked(PortfolioDTO portfolio);
        void onIconDeleteClicked(PortfolioDTO portfolio, int position);
        void onIconEditClicked(PortfolioDTO portfolio, int position);
    }

    private PortfolioAdapterListener listener;
    private List<PortfolioDTO> portfolioList;
    private Context ctx;

    public PortfolioAdapter(List<PortfolioDTO> portfolios,
                            Context context, PortfolioAdapterListener listener) {
        this.portfolioList = portfolios;
        this.ctx = context;
        this.listener = listener;
    }


    @Override
    public PortfolioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PortfolioViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.portfolio_item, parent, false));

    }

    @Override
    public void onBindViewHolder(final PortfolioViewHolder vh, final int position) {

        final PortfolioDTO p = portfolioList.get(position);
        vh.number.setText("" + (position + 1));
        vh.name.setText(p.getPortfolioName());
        vh.position = position;
        vh.programCount.setText("" + p.getProgrammeList().size());

        int mCount = 0;
        for (ProgrammeDTO c: p.getProgrammeList()) {
            mCount += c.getProjectList().size();
        }
        vh.projectCount.setText("" + mCount);
        setListener(vh.name, p);
        setListener(vh.number, p);

        vh.programLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProgramCountClicked(p);
            }
        });
        vh.projectLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProgramCountClicked(p);
            }
        });

        if (p.getProgrammeList().isEmpty()) {
            vh.iconDelete.setVisibility(View.VISIBLE);
            vh.iconDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onIconDeleteClicked(p,position);
                }
            });
        } else {
            vh.iconDelete.setVisibility(View.GONE);
        }
        vh.iconEDit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onIconEditClicked(p,position);
            }
        });

    }

    private void setListener(View view, final PortfolioDTO dto) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPortfolioClicked(dto);
            }
        });
    }
    public int getItemCount() {
        return portfolioList == null ? 0 : portfolioList.size();
    }

    public class PortfolioViewHolder extends RecyclerView.ViewHolder {
        protected ImageView iconDelete, iconEDit;
        protected TextView name, number, projectCount, programCount;
        protected int position;
        protected View programLayout, projectLayout;


        public PortfolioViewHolder(View itemView) {
            super(itemView);
            programLayout = itemView.findViewById(R.id.PORT_programLayout);
            projectLayout = itemView.findViewById(R.id.PORT_projectLayout);
            iconDelete = (ImageView) itemView.findViewById(R.id.PORT_delete);
            iconEDit = (ImageView) itemView.findViewById(R.id.PORT_edit);
            name = (TextView) itemView.findViewById(R.id.PORT_name);
            number = (TextView) itemView.findViewById(R.id.PORT_number);
            projectCount = (TextView) itemView.findViewById(R.id.PORT_projectCount);
            programCount = (TextView) itemView.findViewById(R.id.PORT_programCount);
        }

    }

    static final String LOG = PortfolioAdapter.class.getSimpleName();
}
