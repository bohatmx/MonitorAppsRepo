package com.boha.monitor.setup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.setup.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProjectImportAdapter extends ArrayAdapter<ProjectDTO> {

    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<ProjectDTO> mList;
    private Context ctx;

    public ProjectImportAdapter(Context context, int textViewResourceId,
                                List<ProjectDTO> list) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        mList = list;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    View view;


    static class ViewHolderItem {
        TextView txtProjectName, txtNumber, txtCityID;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem item;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            item = new ViewHolderItem();
            item.txtProjectName = (TextView) convertView
                    .findViewById(R.id.PROJ_IMP_name);
            item.txtNumber = (TextView) convertView
                    .findViewById(R.id.PROJ_IMP_number);
            item.txtCityID = (TextView) convertView
                    .findViewById(R.id.PROJ_IMP_cityID);
            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }
        final ProjectDTO p = mList.get(position);
        item.txtProjectName.setText(p.getProjectName());
        item.txtNumber.setText("" + (position + 1));
        item.txtCityID.setText("" + p.getCityID().intValue());

        return (convertView);
    }

}
