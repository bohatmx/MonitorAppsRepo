package com.boha.platform.monitor.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.platform.library.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DrawerMonitorAdapter extends ArrayAdapter<String> {


    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<String> mList;
    private Context ctx;
    private int darkColor;

    public DrawerMonitorAdapter(Context context, int textViewResourceId,
                                List<String> list, int darkColor) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        this.darkColor = darkColor;
        mList = list;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    View view;


    static class ViewHolderItem {
        TextView txtName, txtCount;
        ImageView icon;
    }


    @Override
    public View getView(int position,  View convertView, ViewGroup parent) {
        ViewHolderItem item;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            item = new ViewHolderItem();
            item.txtName = (TextView) convertView
                    .findViewById(R.id.DI_txtTitle);
            item.icon = (ImageView) convertView
                    .findViewById(R.id.DI_icon);
            item.txtCount = (TextView) convertView
                    .findViewById(R.id.DI_txtCount);

            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }

        String p = mList.get(position);
        item.txtName.setText(p);

        item.txtCount.setVisibility(View.GONE);

        if (p.equalsIgnoreCase(ctx.getString(R.string.projects))) {
            item.icon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_action_calendar_day));
        }
        if (p.equalsIgnoreCase(ctx.getString(R.string.monitors))) {
            item.icon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_action_person));
        }
        if (p.equalsIgnoreCase(ctx.getString(R.string.messaging))) {
            item.icon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_action_email));
        }
        if (p.equalsIgnoreCase(ctx.getString(R.string.profile))) {
            item.icon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_action_add_person));
        }

        if (darkColor != 0) {
            item.icon.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
        }

        return (convertView);
    }

    static final Locale x = Locale.getDefault();
    static final SimpleDateFormat y = new SimpleDateFormat("dd MMMM yyyy", x);
    static final DecimalFormat df = new DecimalFormat("###,###,##0.0");
}
