package com.com.boha.monitor.library.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.dto.ChatMessageDTO;
import com.com.boha.monitor.library.dto.CompanyStaffDTO;
import com.com.boha.monitor.library.util.SharedUtil;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends ArrayAdapter<ChatMessageDTO> {

    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<ChatMessageDTO> mList;
    private String title;
    private Context ctx;
    private CompanyStaffDTO companyStaff;

    public ChatListAdapter(Context context, int textViewResourceId,
                           List<ChatMessageDTO> list) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        mList = list;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        companyStaff = SharedUtil.getCompanyStaff(context);
    }

    View view;


    static class ViewHolderItem {
        View fromLayout, toLayout;
        TextView txtStaffTo, txtDateTo, txtTimeTo, txtMessageTo;
        TextView txtStaffFrom, txtDateFrom, txtTimeFrom, txtMessageFrom;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolderItem item;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            item = new ViewHolderItem();
            item.fromLayout = convertView.findViewById(R.id.CHATMSG_fromLayout);
            item.toLayout = convertView.findViewById(R.id.CHATMSG_toLayout);

            item.txtStaffTo = (TextView) convertView
                    .findViewById(R.id.CHATMSG_toStaff);
            item.txtStaffFrom = (TextView) convertView
                    .findViewById(R.id.CHATMSG_fromStaff);

            item.txtDateFrom = (TextView) convertView
                    .findViewById(R.id.CHATMSG_messageFromDate);
            item.txtDateTo = (TextView) convertView
                    .findViewById(R.id.CHATMSG_messageToDate);

            item.txtTimeFrom = (TextView) convertView
                    .findViewById(R.id.CHATMSG_messageFromTime);
            item.txtTimeTo = (TextView) convertView
                    .findViewById(R.id.CHATMSG_messageToTime);

            item.txtMessageFrom = (TextView) convertView
                    .findViewById(R.id.CHATMSG_messageFrom);
            item.txtMessageTo = (TextView) convertView
                    .findViewById(R.id.CHATMSG_messageTo);


            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }

        ChatMessageDTO p = mList.get(position);
        if (p.getCompanyStaffID().intValue() == companyStaff.getCompanyStaffID().intValue()) {
            item.fromLayout.setVisibility(View.GONE);
            item.toLayout.setVisibility(View.VISIBLE);
            item.txtStaffTo.setText(p.getStaffName());
            if (p.getDateSent() != null) {
                item.txtDateTo.setText(sdfDate.format(p.getDateSent()));
                item.txtTimeTo.setText(sdfTime.format(p.getDateSent()));
            }
            item.txtMessageTo.setText(p.getMessage());
        } else {
            item.fromLayout.setVisibility(View.VISIBLE);
            item.toLayout.setVisibility(View.GONE);
            item.txtStaffFrom.setText(p.getStaffName());
            if (p.getDateSent() != null) {
                item.txtDateFrom.setText(sdfDate.format(p.getDateSent()));
                item.txtTimeFrom.setText(sdfTime.format(p.getDateSent()));
            }
            item.txtMessageFrom.setText(p.getMessage());
        }



//        Statics.setRobotoFontLight(ctx, item.txtNumber);
//        Statics.setRobotoFontLight(ctx, item.txtName);

        return (convertView);
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, dd MMMM yyyy", loc);
    static final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", loc);
}
