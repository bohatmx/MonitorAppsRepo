package com.boha.monitor.library.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.util.Statics;




import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DrawerAdapter extends ArrayAdapter<String> {


    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<String> mList;
    private Context ctx;
    private CompanyDTO company;

    public DrawerAdapter( Context context, int textViewResourceId,
                         List<String> list, CompanyDTO company) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        mList = list;
        ctx = context;
        this.company = company;
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


        Statics.setRobotoFontLight(ctx, item.txtName);
        switch (position) {
            case 0:
                if (company.getProjectList().isEmpty()) {
                    item.txtCount.setVisibility(View.GONE);
                } else {
                    item.txtCount.setVisibility(View.VISIBLE);
                    item.txtCount.setText("" + company.getProjectList().size());
                }
                item.txtCount.setTextColor(ctx.getResources().getColor(R.color.absa_red));
                item.icon.setImageDrawable(ctx.getResources()
                        .getDrawable(R.drawable.ic_action_calendar_day));
                break;
            case 2:
                if (company.getCompanyStaffList().isEmpty()) {
                    item.txtCount.setVisibility(View.GONE);
                } else {
                    item.txtCount.setVisibility(View.VISIBLE);
                    item.txtCount.setText("" + company.getCompanyStaffList().size());
                }
                item.txtCount.setTextColor(ctx.getResources().getColor(R.color.black));
                item.icon.setImageDrawable(ctx.getResources()
                        .getDrawable(R.drawable.ic_action_person));
                break;

            case 5:
                if (company.getTaskStatusList().isEmpty()) {
                    item.txtCount.setVisibility(View.GONE);
                } else {
                    item.txtCount.setVisibility(View.VISIBLE);
                    item.txtCount.setText("" + company.getTaskStatusList().size());
                }
                item.txtCount.setTextColor(ctx.getResources().getColor(R.color.teal));
                item.icon.setImageDrawable(ctx.getResources()
                        .getDrawable(R.drawable.ic_action_event));
                break;
            case 6:
                if (company.getProjectStatusTypeList().isEmpty()) {
                    item.txtCount.setVisibility(View.GONE);
                } else {
                    item.txtCount.setVisibility(View.VISIBLE);
                    item.txtCount.setText("" + company.getProjectStatusTypeList().size());
                }
                item.txtCount.setTextColor(ctx.getResources().getColor(R.color.indigo_700));
                item.icon.setImageDrawable(ctx.getResources()
                        .getDrawable(R.drawable.ic_action_globe));
                break;
            case 4:
                if (company.getTaskList().isEmpty()) {
                    item.txtCount.setVisibility(View.GONE);
                } else {
                    item.txtCount.setVisibility(View.VISIBLE);
                    item.txtCount.setText("" + company.getTaskList().size());
                }
                item.txtCount.setTextColor(ctx.getResources().getColor(R.color.blue));
                item.icon.setImageDrawable(ctx.getResources()
                        .getDrawable(R.drawable.ic_action_slideshow));
                break;
            case 1:
                item.txtCount.setVisibility(View.GONE);
                item.icon.setImageDrawable(ctx.getResources()
                        .getDrawable(R.drawable.ic_action_view_as_list));
                break;
            case 3:
                item.txtCount.setVisibility(View.GONE);
                item.icon.setImageDrawable(ctx.getResources()
                        .getDrawable(R.drawable.ic_chat_black_24dp));
                break;

        }

        return (convertView);
    }

    static final Locale x = Locale.getDefault();
    static final SimpleDateFormat y = new SimpleDateFormat("dd MMMM yyyy", x);
    static final DecimalFormat df = new DecimalFormat("###,###,##0.0");
}
