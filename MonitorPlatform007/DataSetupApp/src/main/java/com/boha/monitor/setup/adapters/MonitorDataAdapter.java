package com.boha.monitor.setup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.setup.R;

import java.util.List;

/**
 * Created by aubreyM on 14/12/17.
 */
public class MonitorDataAdapter extends RecyclerView.Adapter<MonitorDataAdapter.MonitorViewHolder> {

    public interface MonitorListener {
        void onMonitorClicked(MonitorDTO task);

        void onIconDeleteClicked(MonitorDTO task, int position);

        void onIconEditClicked(MonitorDTO task, int position);
    }

    private MonitorListener listener;
    private List<MonitorDTO> taskList;
    private Context ctx;

    public MonitorDataAdapter(List<MonitorDTO> tasks,
                              Context context, MonitorListener listener) {
        this.taskList = tasks;
        this.ctx = context;
        this.listener = listener;
    }


    @Override
    public MonitorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MonitorViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.person_item_card, parent, false));

    }

    @Override
    public void onBindViewHolder(final MonitorViewHolder vh, final int position) {

        final MonitorDTO p = taskList.get(position);
        vh.number.setText("" + (position + 1));
        vh.name.setText(p.getFirstName() + " " + p.getLastName());
        vh.position = position;
        vh.email.setText(p.getEmail());

        setListener(vh.name, p);
        setListener(vh.number, p);
        setListener(vh.email, p);


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

    private void setListener(View view, final MonitorDTO dto) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onMonitorClicked(dto);
            }
        });
    }

    public int getItemCount() {
        return taskList == null ? 0 : taskList.size();
    }

    public class MonitorViewHolder extends RecyclerView.ViewHolder {
        protected ImageView iconDelete, iconEDit;
        protected TextView name, number, email;
        protected int position;


        public MonitorViewHolder(View itemView) {
            super(itemView);
            iconDelete = (ImageView) itemView.findViewById(R.id.PERSON_delete);
            iconEDit = (ImageView) itemView.findViewById(R.id.PERSON_edit);
            name = (TextView) itemView.findViewById(R.id.PERSON_name);
            number = (TextView) itemView.findViewById(R.id.PERSON_number);
            email = (TextView) itemView.findViewById(R.id.PERSON_email);
        }

    }

    static final String LOG = MonitorDataAdapter.class.getSimpleName();
}
