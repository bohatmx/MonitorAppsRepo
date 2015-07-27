package com.boha.monitor.setup.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boha.monitor.library.dto.SubTaskDTO;
import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.setup.R;

import java.util.List;

/**
 * Created by aubreyM on 14/12/17.
 */
public class TaskDataAdapter extends RecyclerView.Adapter<TaskDataAdapter.TaskViewHolder> {

    public interface TaskListener {
        void onTaskClicked(TaskDTO task);

        void onIconDeleteClicked(TaskDTO task, int position);

        void onIconEditClicked(TaskDTO task, int position);
    }

    private TaskListener listener;
    private List<TaskDTO> taskList;
    private Context ctx;

    public TaskDataAdapter(List<TaskDTO> tasks,
                           Context context, TaskListener listener) {
        this.taskList = tasks;
        this.ctx = context;
        this.listener = listener;
    }


    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TaskViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item_card, parent, false));

    }

    @Override
    public void onBindViewHolder(final TaskViewHolder vh, final int position) {

        final TaskDTO p = taskList.get(position);
        vh.number.setText("" + (position + 1));
        vh.name.setText(p.getTaskName());
        vh.position = position;
        vh.subTaskCount.setText("" + p.getSubTaskList().size());
        vh.desc.setText(p.getDescription());

        if (p.getSubTaskList().isEmpty()) {
            vh.subtaskLayout.setVisibility(View.GONE);
        } else {
            vh.subtaskLayout.setVisibility(View.VISIBLE);
            vh.subtaskLayout.removeAllViews();
            for (SubTaskDTO c : p.getSubTaskList()) {
                TextView textView = new TextView(ctx);
                textView.setText(c.getSubTaskName());
                vh.subtaskLayout.addView(textView);
            }
        }
        setListener(vh.name, p);
        setListener(vh.number, p);
        setListener(vh.subTaskCount, p);
        setListener(vh.desc, p);


        vh.iconDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onIconDeleteClicked(p,position);
            }
        });
        vh.iconEDit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onIconEditClicked(p, position);
            }
        });

    }

    private void setListener(View view, final TaskDTO dto) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTaskClicked(dto);
            }
        });
    }

    public int getItemCount() {
        return taskList == null ? 0 : taskList.size();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        protected ImageView iconDelete, iconEDit;
        protected TextView name, number, desc, subTaskCount;
        protected int position;
        protected LinearLayout subtaskLayout;


        public TaskViewHolder(View itemView) {
            super(itemView);
            subtaskLayout = (LinearLayout) itemView.findViewById(R.id.TASK_subLayout);
            iconDelete = (ImageView) itemView.findViewById(R.id.TASK_delete);
            iconEDit = (ImageView) itemView.findViewById(R.id.TASK_edit);
            name = (TextView) itemView.findViewById(R.id.TASK_name);
            number = (TextView) itemView.findViewById(R.id.TASK_number);
            desc = (TextView) itemView.findViewById(R.id.TASK_desc);
            subTaskCount = (TextView) itemView.findViewById(R.id.TASK_subtaskCount);
        }

    }

    static final String LOG = TaskDataAdapter.class.getSimpleName();
}
