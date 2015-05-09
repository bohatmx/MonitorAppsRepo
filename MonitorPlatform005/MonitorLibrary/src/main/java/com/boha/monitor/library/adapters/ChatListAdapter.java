package com.boha.monitor.library.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.ChatDTO;




import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends ArrayAdapter<ChatDTO> {


    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<ChatDTO> mList;
    private String title;
    private Context ctx;

    public ChatListAdapter( Context context, int textViewResourceId,
                           List<ChatDTO> list) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        mList = list;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    View view;


    static class ViewHolderItem {
        View dateLayout;
        TextView txtChatName, txtColor, txtDate, txtMessageCount,txtMemberCount;
    }


    @Override
    public View getView(int position,  View convertView, ViewGroup parent) {
        final ViewHolderItem item;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            item = new ViewHolderItem();
            item.dateLayout = convertView.findViewById(R.id.CI_dateLayout);

            item.txtChatName = (TextView) convertView
                    .findViewById(R.id.CI_chatName);
            item.txtColor = (TextView) convertView
                    .findViewById(R.id.CI_color);

            item.txtDate = (TextView) convertView
                    .findViewById(R.id.CI_msgDate);
            item.txtMessageCount = (TextView) convertView
                    .findViewById(R.id.CI_msgCount);

            item.txtMemberCount = (TextView) convertView
                    .findViewById(R.id.CI_memCount);

            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }

        ChatDTO p = mList.get(position);
        item.txtChatName.setText(p.getChatName());

        item.txtMemberCount.setText("" + p.getChatMemberList().size());
        item.txtMessageCount.setText("" + p.getChatMessageList().size());
        if (!p.getChatMessageList().isEmpty()) {
            item.txtDate.setText(sdfDate.format(p.getChatMessageList().get(0).getDateSent()));
            item.dateLayout.setVisibility(View.VISIBLE);
        } else {
            item.dateLayout.setVisibility(View.GONE);
        }
        if (p.getAvatarNumber() != null) {
            switch (p.getAvatarNumber()) {
                case 1:
                    item.txtColor.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xgreen_oval_small));
                    break;
                case 2:
                    item.txtColor.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xblack_oval_small));
                    break;
                case 3:
                    item.txtColor.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xred_oval_small));
                    break;
                case 4:
                    item.txtColor.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xblue_oval_small));
                    break;
                case 5:
                    item.txtColor.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xorange_oval_small));
                    break;
                case 6:
                    item.txtColor.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xindigo_oval_small));
                    break;
                default:
                    item.txtColor.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xgrey_oval_small));
                    break;
            }
        }

        return (convertView);
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss", loc);
}
