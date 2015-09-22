package com.boha.monitor.library.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.util.Statics;
import com.boha.platform.library.R;

import java.util.List;

public class PopupMonitorListAdapter extends ArrayAdapter<MonitorDTO> {


    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private int color;
    private List<MonitorDTO> mList;
    private Context ctx;

    static final String LOG = PopupMonitorListAdapter.class.getSimpleName();

    public PopupMonitorListAdapter(Context context, int textViewResourceId, int color,
                                   List<MonitorDTO> list) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        mList = list;
        this.color = color;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    View view;


    static class ViewHolderItem {
        TextView txtString;
        ImageView image;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }


    public View getCustomView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem item;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            item = new ViewHolderItem();
            item.txtString = (TextView) convertView
                    .findViewById(R.id.text1);

            item.image = (ImageView) convertView
                    .findViewById(R.id.image1);

            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }



        final MonitorDTO p = mList.get(position);
        if (p.isSelected()) {
            item.image.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_action_clear));
            item.image.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        } else {
            item.image.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_action_new));
            item.image.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
        item.txtString.setText(p.getFullName());
        Statics.setRobotoFontLight(ctx, item.txtString);
        return (convertView);
    }

}
