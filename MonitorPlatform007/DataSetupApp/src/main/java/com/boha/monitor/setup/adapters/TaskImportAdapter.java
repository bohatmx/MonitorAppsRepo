package com.boha.monitor.setup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.setup.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TaskImportAdapter extends ArrayAdapter<TaskTypeDTO> {

    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<TaskTypeDTO> mList;
    private Context ctx;

    public TaskImportAdapter(Context context, int textViewResourceId,
                             List<TaskTypeDTO> list) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        mList = list;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    View view;


    static class ViewHolderItem {
        TextView txtTypeName, txtNumber, txtTotalTasks, subTasks;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem item;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            item = new ViewHolderItem();
            item.txtTypeName = (TextView) convertView
                    .findViewById(R.id.TASK_TYPE_name);
            item.txtNumber = (TextView) convertView
                    .findViewById(R.id.TASK_TYPE_number);
            item.txtTotalTasks = (TextView) convertView
                    .findViewById(R.id.TASK_TYPE_taskCount);
            item.subTasks = (TextView) convertView
                    .findViewById(R.id.TASK_TYPE_subtaskCount);
            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }
        final TaskTypeDTO p = mList.get(position);
        item.txtTypeName.setText(p.getTaskTypeName());
        item.txtNumber.setText("" + (position + 1));
        if (p.getTaskList() != null) {
            item.txtTotalTasks.setText("" + p.getTaskList().size());
            int count = 0;
            for (TaskDTO t: p.getTaskList()) {
                if (t.getSubTaskList() != null) {
                    count += t.getSubTaskList().size();
                }
            }
            item.subTasks.setText("" + count);
        } else {
            item.txtTotalTasks.setText("0");
            item.subTasks.setText("0");
        }

        return (convertView);
    }

    static final Locale x = Locale.getDefault();
    static final SimpleDateFormat y = new SimpleDateFormat("dd MMMM yyyy", x);
    static final DecimalFormat df = new DecimalFormat("###,###,###,###,###,###,##0.00");
}
