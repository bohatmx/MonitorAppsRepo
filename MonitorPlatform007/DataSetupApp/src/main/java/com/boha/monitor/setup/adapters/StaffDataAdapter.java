package com.boha.monitor.setup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.setup.R;

import java.util.List;

/**
 * Created by aubreyM on 14/12/17.
 */
public class StaffDataAdapter extends RecyclerView.Adapter<StaffDataAdapter.StaffViewHolder> {

    public interface StaffListener {
        void onStaffClicked(StaffDTO task);

        void onIconDeleteClicked(StaffDTO task, int position);

        void onIconEditClicked(StaffDTO task, int position);
    }

    private StaffListener listener;
    private List<StaffDTO> taskList;
    private Context ctx;

    public StaffDataAdapter(List<StaffDTO> tasks,
                            Context context, StaffListener listener) {
        this.taskList = tasks;
        this.ctx = context;
        this.listener = listener;
    }


    @Override
    public StaffViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StaffViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.person_item_card, parent, false));

    }

    @Override
    public void onBindViewHolder(final StaffViewHolder vh, final int position) {

        final StaffDTO p = taskList.get(position);
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

    private void setListener(View view, final StaffDTO dto) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onStaffClicked(dto);
            }
        });
    }

    public int getItemCount() {
        return taskList == null ? 0 : taskList.size();
    }

    public class StaffViewHolder extends RecyclerView.ViewHolder {
        protected ImageView iconDelete, iconEDit;
        protected TextView name, number, email;
        protected int position;


        public StaffViewHolder(View itemView) {
            super(itemView);
            iconDelete = (ImageView) itemView.findViewById(R.id.PERSON_delete);
            iconEDit = (ImageView) itemView.findViewById(R.id.PERSON_edit);
            name = (TextView) itemView.findViewById(R.id.PERSON_name);
            number = (TextView) itemView.findViewById(R.id.PERSON_number);
            email = (TextView) itemView.findViewById(R.id.PERSON_email);
        }

    }

    static final String LOG = StaffDataAdapter.class.getSimpleName();
}
