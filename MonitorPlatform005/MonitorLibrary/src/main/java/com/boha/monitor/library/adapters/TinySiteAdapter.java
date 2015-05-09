package com.boha.monitor.library.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.util.Statics;




import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/16.
 */
public class TinySiteAdapter extends ArrayAdapter<ProjectSiteDTO> {

    public TinySiteAdapter( Context context, int textViewResourceId,
                              List<ProjectSiteDTO> list) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        mList = list;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    View view;


    static class ViewHolderItem {
        TextView txtNumber, txtSite, txtBeneficiary;

    }

    @Override
    public View getView(final int position,  View convertView, ViewGroup parent) {
        final ViewHolderItem item;

        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            item = new ViewHolderItem();
            item.txtNumber = (TextView) convertView
                    .findViewById(R.id.TINY_number);
            item.txtSite = (TextView) convertView
                    .findViewById(R.id.TINY_siteName);
            item.txtBeneficiary = (TextView) convertView
                    .findViewById(R.id.TINY_beneficiary);
            convertView.setTag(item);
        } else {
            item = (ViewHolderItem)convertView.getTag();
        }
        final ProjectSiteDTO p = mList.get(position);
        item.txtNumber.setText("" +(position + 1));
        item.txtSite.setText(p.getProjectSiteName());
        item.txtBeneficiary.setText(p.getBeneficiary().getFullName());

        Statics.setRobotoFontLight(ctx,item.txtBeneficiary);
        Statics.setRobotoFontLight(ctx,item.txtSite);
        Statics.setRobotoFontLight(ctx,item.txtNumber);
        if (p.isSelected()) {
            convertView.setBackgroundColor(ctx.getResources().getColor(R.color.beige_pale));
        } else {
            convertView.setBackgroundColor(ctx.getResources().getColor(R.color.white));
        }

        return convertView;
    }


    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<ProjectSiteDTO> mList;
    private Context ctx;
    static final Locale x = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm", x);
    static final DecimalFormat df = new DecimalFormat("###,###,##0.00");


}
