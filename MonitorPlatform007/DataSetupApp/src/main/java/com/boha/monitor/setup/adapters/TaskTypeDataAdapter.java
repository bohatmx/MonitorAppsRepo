package com.boha.monitor.setup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.setup.R;

import java.util.List;

/**
 * Created by aubreyM on 14/12/17.
 */
public class TaskTypeDataAdapter extends RecyclerView.Adapter<TaskTypeDataAdapter.TaskTypeViewHolder> {

    public interface TaskTypeListener {
        void onTaskTypeClicked(TaskTypeDTO taskType);
        void onTaskCountClicked(TaskTypeDTO taskType);
        void onIconDeleteClicked(TaskTypeDTO taskType, int position);
        void onIconEditClicked(TaskTypeDTO taskType, int position);
        void setBusy(boolean busy);
    }

    private TaskTypeListener listener;
    private List<TaskTypeDTO> taskTypeList;
    private Context ctx;

    public TaskTypeDataAdapter(List<TaskTypeDTO> taskTypes,
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
        if (p.getSectionName() != null) {
            vh.sectionName.setVisibility(View.VISIBLE);
            vh.sectionName.setText(p.getSectionName());
        } else {
            vh.sectionName.setVisibility(View.GONE);
        }

        int subCount = 0;
        for (TaskDTO dto: p.getTaskList()) {
            if (dto.getSubTaskList() != null) {
                subCount += dto.getSubTaskList().size();
            }
        }
        vh.subTaskCount.setText("" + subCount);
        setListener(vh.name, p);
        setListener(vh.number, p);
        
        vh.taskCountLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTaskCountClicked(p);
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
        protected TextView name, number, taskCount, subTaskCount, sectionName;
        protected int position;
        protected View taskCountLayout;


        public TaskTypeViewHolder(View itemView) {
            super(itemView);
            iconDelete = (ImageView) itemView.findViewById(R.id.TASK_TYPE_delete);
            iconEDit = (ImageView) itemView.findViewById(R.id.TASK_TYPE_edit);
            name = (TextView) itemView.findViewById(R.id.TASK_TYPE_name);
            number = (TextView) itemView.findViewById(R.id.TASK_TYPE_number);
            taskCount = (TextView) itemView.findViewById(R.id.TASK_TYPE_taskCount);
            subTaskCount = (TextView) itemView.findViewById(R.id.TASK_TYPE_subtaskCount);
            sectionName = (TextView) itemView.findViewById(R.id.TASK_TYPE_section);
            taskCountLayout =  itemView.findViewById(R.id.TASK_TYPE_taskCountLayout);
        }

    }

    static final String LOG = TaskTypeDataAdapter.class.getSimpleName();
}
