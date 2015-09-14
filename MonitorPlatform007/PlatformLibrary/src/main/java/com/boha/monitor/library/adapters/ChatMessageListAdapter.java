package com.boha.monitor.library.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.boha.monitor.library.dto.ChatMessageDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatMessageListAdapter extends ArrayAdapter<ChatMessageDTO> {


    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<ChatMessageDTO> mList;
    private String title;
    private Context ctx;

    private StaffDTO companyStaff;

    public ChatMessageListAdapter(Context context, int textViewResourceId,
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
        View fromLayout, toLayout, colorTo,colorFrom;
        CircleImageView profileIconFrom,profileIconTo;
        TextView txtStaffTo, txtDateTo, txtTimeTo, txtMessageTo, chatNameTo, chatNameFrom;
        TextView txtStaffFrom, txtDateFrom, txtTimeFrom, txtMessageFrom;
    }


    @Override
    public View getView(int position,  View convertView, ViewGroup parent) {
        final ViewHolderItem item;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            item = new ViewHolderItem();
            item.fromLayout = convertView.findViewById(R.id.CHATMSG_fromLayout);
            item.toLayout = convertView.findViewById(R.id.CHATMSG_toLayout);
            item.colorTo = convertView.findViewById(R.id.CHATMSG_colorTo);
            item.colorFrom = convertView.findViewById(R.id.CHATMSG_colorFrom);

            item.chatNameFrom = (TextView)convertView.findViewById(R.id.CHATMSG_chatNameFrom);
            item.chatNameTo = (TextView)convertView.findViewById(R.id.CHATMSG_chatNameTo);

            item.profileIconTo = (CircleImageView)convertView.findViewById(R.id.CHATMSG_iconTo);
            item.profileIconFrom = (CircleImageView)convertView.findViewById(R.id.CHATMSG_iconFrom);
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


        if (p.getStaff().getStaffID().intValue() == companyStaff.getStaffID().intValue()) {
            item.fromLayout.setVisibility(View.GONE);
            item.toLayout.setVisibility(View.VISIBLE);
            item.txtStaffTo.setText(p.getStaff().getFirstName() + " " + p.getStaff().getLastName());
            getProfileImage(item.profileIconTo, p.getStaff().getStaffID());
            if (p.getDateSent() != null) {
                item.txtDateTo.setText(sdfDate.format(p.getDateSent()));
                item.txtTimeTo.setText(sdfTime.format(p.getDateSent()));
            } else {
                item.txtDateTo.setVisibility(View.GONE);
                item.txtTimeTo.setVisibility(View.GONE);
            }
            item.txtMessageTo.setText(p.getMessage());
//            getColor(item.colorTo, p.getChatColor());
            item.chatNameTo.setText(p.getChat().getChatName());

        } else {
            item.fromLayout.setVisibility(View.VISIBLE);
            item.toLayout.setVisibility(View.GONE);
            item.txtStaffFrom.setText(p.getStaff().getFirstName() + " " + p.getStaff().getLastName());
            item.chatNameFrom.setText(p.getChat().getChatName());
            getProfileImage(item.profileIconFrom, p.getStaff().getStaffID());
            if (p.getDateSent() != null) {
                item.txtDateFrom.setText(sdfDate.format(p.getDateSent()));
                item.txtTimeFrom.setText(sdfTime.format(p.getDateSent()));
            } else {
                item.txtDateFrom.setVisibility(View.GONE);
                item.txtTimeFrom.setVisibility(View.GONE);
            }
            item.txtMessageFrom.setText(p.getMessage());
//            getColor(item.colorFrom, p.getChatColor());
        }



        return (convertView);
    }

    private void getColor( View colorLayout, int number) {
        View inside = colorLayout.findViewById(R.id.COLOR_inside);
        switch (number) {
            case 1:
                inside.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xindigo_oval_small));
                break;
            case 2:
                inside.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xblack_oval_small));
                break;
            case 3:
                inside.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xred_oval_small));
                break;
            case 4:
                inside.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xblue_oval_small));
                break;
            case 5:
                inside.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xorange_oval_small));
                break;
            case 6:
                inside.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xindigo_oval_small));
                break;
        }
    }
    private void getProfileImage( CircleImageView view, Integer staffID) {

        String url = Util.getStaffImageURL(ctx, staffID);
        Picasso.with(ctx).load(url).into(view);
    }
    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, dd MMMM yyyy", loc);
    static final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", loc);
}
