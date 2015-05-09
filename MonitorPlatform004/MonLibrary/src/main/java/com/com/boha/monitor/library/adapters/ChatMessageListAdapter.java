package com.com.boha.monitor.library.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.dto.ChatMessageDTO;
import com.com.boha.monitor.library.dto.CompanyStaffDTO;
import com.com.boha.monitor.library.util.SharedUtil;
import com.com.boha.monitor.library.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

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
    private CompanyStaffDTO companyStaff;

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
    public View getView(int position, View convertView, ViewGroup parent) {
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


        if (p.getCompanyStaffID().intValue() == companyStaff.getCompanyStaffID().intValue()) {
            item.fromLayout.setVisibility(View.GONE);
            item.toLayout.setVisibility(View.VISIBLE);
            item.txtStaffTo.setText(p.getStaffName());
            getProfileImage(item.profileIconTo, p.getCompanyStaffID());
            if (p.getDateSent() != null) {
                item.txtDateTo.setText(sdfDate.format(p.getDateSent()));
                item.txtTimeTo.setText(sdfTime.format(p.getDateSent()));
            } else {
                item.txtDateTo.setVisibility(View.GONE);
                item.txtTimeTo.setVisibility(View.GONE);
            }
            item.txtMessageTo.setText(p.getMessage());
            getColor(item.colorTo, p.getChatColor());
            item.chatNameTo.setText(p.getChatName());

        } else {
            item.fromLayout.setVisibility(View.VISIBLE);
            item.toLayout.setVisibility(View.GONE);
            item.txtStaffFrom.setText(p.getStaffName());
            item.chatNameFrom.setText(p.getChatName());
            getProfileImage(item.profileIconFrom, p.getCompanyStaffID());
            if (p.getDateSent() != null) {
                item.txtDateFrom.setText(sdfDate.format(p.getDateSent()));
                item.txtTimeFrom.setText(sdfTime.format(p.getDateSent()));
            } else {
                item.txtDateFrom.setVisibility(View.GONE);
                item.txtTimeFrom.setVisibility(View.GONE);
            }
            item.txtMessageFrom.setText(p.getMessage());
            getColor(item.colorFrom, p.getChatColor());
        }



        return (convertView);
    }

    private void getColor(View colorLayout, int number) {
        View inside = colorLayout.findViewById(R.id.COLOR_inside);
        switch (number) {
            case 1:
                inside.setBackground(ctx.getResources().getDrawable(R.drawable.xindigo_oval_small));
                break;
            case 2:
                inside.setBackground(ctx.getResources().getDrawable(R.drawable.xblack_oval_small));
                break;
            case 3:
                inside.setBackground(ctx.getResources().getDrawable(R.drawable.xred_oval_small));
                break;
            case 4:
                inside.setBackground(ctx.getResources().getDrawable(R.drawable.xblue_oval_small));
                break;
            case 5:
                inside.setBackground(ctx.getResources().getDrawable(R.drawable.xorange_oval_small));
                break;
            case 6:
                inside.setBackground(ctx.getResources().getDrawable(R.drawable.xindigo_oval_small));
                break;
        }
    }
    private void getProfileImage(CircleImageView view, Integer companyStaffID) {

        String url = Util.getStaffImageURL(ctx, companyStaffID);
        Log.w("ChatMessageListAdapter", "## getting image: " + url);
        ImageLoader.getInstance().displayImage(url,view, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                Log.w("ChatMessageListAdapter", "## onLoadingFailed, reason: " + failReason.getType().name());
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
//                Log.w("ChatMessageListAdapter", "## onLoadingComplete, s: " + s);
//                RoundedBitmapDrawable x = RoundedBitmapDrawableFactory.create(ctx.getResources(),bitmap);
//                item.profileIcon.setImageDrawable(x);
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                Log.w("ChatMessageListAdapter", "## onLoadingCancelled");
            }
        });
    }
    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, dd MMMM yyyy", loc);
    static final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss", loc);
}
