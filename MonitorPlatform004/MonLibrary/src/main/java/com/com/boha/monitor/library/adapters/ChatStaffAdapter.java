package com.com.boha.monitor.library.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.dto.CompanyStaffDTO;
import com.com.boha.monitor.library.util.Util;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatStaffAdapter extends ArrayAdapter<CompanyStaffDTO> {

    public interface ChatStaffAdapterListener {
        public void onStaffSelected(CompanyStaffDTO companyStaff, int index);
    }

    ChatStaffAdapterListener mListener;
    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<CompanyStaffDTO> mList;
    private Context ctx;

    public ChatStaffAdapter(Context context, int textViewResourceId,
                            List<CompanyStaffDTO> list, ChatStaffAdapterListener listener) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        mList = list;
        mListener = listener;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    View view;


    static class ViewHolderItem {
        ImageView image;
        TextView txtStaff;
        CheckBox checkBox;
        View main;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolderItem item;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            item = new ViewHolderItem();
            item.image = (ImageView)convertView.findViewById(R.id.MSTF_image);
            item.checkBox = (CheckBox)convertView.findViewById(R.id.MSTF_checkBox);
            item.main = convertView.findViewById(R.id.MSTF_main);

            item.txtStaff = (TextView) convertView
                    .findViewById(R.id.MSTF_name);


            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }

        final CompanyStaffDTO p = mList.get(position);
        if (p.getSelected() == null) {
            p.setSelected(Boolean.FALSE);
        }
        if (p.getSelected()) {
            item.checkBox.setChecked(true);
            Util.flashOnce(item.image,300,null);
        } else {
            item.checkBox.setChecked(false);
        }
        item.checkBox.setEnabled(false);
        item.txtStaff.setText(p.getFullName());

        String url = Util.getStaffImageURL(ctx,p.getCompanyStaffID());
        ImageLoader.getInstance().displayImage(url, item.image);

        item.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(item.image,300,new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        mListener.onStaffSelected(p,position);
                    }
                });
            }
        });
        item.main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(item.image,300,new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        mListener.onStaffSelected(p,position);
                    }
                });

            }
        });
        item.txtStaff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(item.image,300,new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        mListener.onStaffSelected(p,position);
                    }
                });
            }
        });

        return (convertView);
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, dd MMMM yyyy", loc);
    static final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", loc);
}
