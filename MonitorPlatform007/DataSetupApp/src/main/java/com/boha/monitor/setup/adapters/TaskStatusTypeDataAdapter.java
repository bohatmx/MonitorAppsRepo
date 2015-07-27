package com.boha.monitor.setup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.setup.R;

import java.util.List;

/**
 * Created by aubreyM on 14/12/17.
 */
public class TaskStatusTypeDataAdapter extends RecyclerView.Adapter<TaskStatusTypeDataAdapter.TaskTypeViewHolder> {

    public interface TaskTypeListener {
        void onTaskTypeClicked(TaskTypeDTO taskType);
        void onTaskCountClicked(TaskTypeDTO taskType);
        void onIconDeleteClicked(TaskTypeDTO taskType, int position);
        void onIconEditClicked(TaskTypeDTO taskType, int position);
    }

    private TaskTypeListener listener;
    private List<TaskTypeDTO> taskTypeList;
    private Context ctx;

    public TaskStatusTypeDataAdapter(List<TaskTypeDTO> taskTypes,
                                     Context context, TaskTypeListener listener) {
        this.taskTypeList = taskTypes;
        this.ctx = context;
        this.listener = listener;
    }


    @Override
    public TaskTypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TaskTypeViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_type_item_card, parent, false));

    }

    @Override
    public void onBindViewHolder(final TaskTypeViewHolder vh, final int position) {

        final TaskTypeDTO p = taskTypeList.get(position);
        vh.number.setText("" + (position + 1));
        vh.name.setText(p.getTaskTypeName());
        vh.position = position;
        vh.taskCount.setText("" + p.getTaskList().size());
        
        setListener(vh.name, p);
        setListener(vh.number, p);
        
        vh.taskCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTaskCountClicked(p);
            }
        });

        if (p.getTaskList().isEmpty()) {
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

    private void setListener(View view, final TaskTypeDTO dto) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTaskTypeClicked(dto);
            }
        });
    }
    public int getItemCount() {
        return taskTypeList == null ? 0 : taskTypeList.size();
    }

    public class TaskTypeViewHolder extends RecyclerView.ViewHolder {
        protected ImageView iconDelete, iconEDit;
        protected TextView name, number, taskCount;
        protected int position;


        public TaskTypeViewHolder(View itemView) {
            super(itemView);
            iconDelete = (ImageView) itemView.findViewById(R.id.TASK_TYPE_delete);
            iconEDit = (ImageView) itemView.findViewById(R.id.TASK_TYPE_edit);
            name = (TextView) itemView.findViewById(R.id.TASK_TYPE_name);
            number = (TextView) itemView.findViewById(R.id.TASK_TYPE_number);
            taskCount = (TextView) itemView.findViewById(R.id.TASK_TYPE_taskCount);
        }

    }

    static final String LOG = TaskStatusTypeDataAdapter.class.getSimpleName();
}
