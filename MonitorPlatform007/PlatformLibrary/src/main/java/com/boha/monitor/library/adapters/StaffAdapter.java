package com.boha.monitor.library.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.Statics;
import com.boha.platform.library.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class StaffAdapter extends ArrayAdapter<StaffDTO> {

    public interface StaffAdapterListener {
        public void onPictureRequested(StaffDTO staff);
        public void onStatusUpdatesRequested(StaffDTO staff);
    }


    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<StaffDTO> mList;
    private Context ctx;
    private StaffAdapterListener listener;

    public StaffAdapter( Context context, int textViewResourceId,
                        List<StaffDTO> list,
                        StaffAdapterListener listener) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        this.listener = listener;
        mList = list;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    View view;


    static class ViewHolderItem {
        TextView txtName;
        CircleImageView photo;
        TextView txtCount;
    }


    @Override
    public View getView(int position,  View convertView, ViewGroup parent) {
        final ViewHolderItem item;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            item = new ViewHolderItem();
            item.txtName = (TextView) convertView
                    .findViewById(R.id.PSN_txtName);

            item.txtCount = (TextView) convertView
                    .findViewById(R.id.PSN_txtCounter);

            item.photo = (CircleImageView) convertView
                    .findViewById(R.id.PSN_imagex);

            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }

        final StaffDTO p = mList.get(position);
        item.txtName.setText(p.getFirstName() + " " + p.getLastName());
        item.txtCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onStatusUpdatesRequested(p);
            }
        });
        item.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPictureRequested(p);
            }
        });
        Statics.setRobotoFontLight(ctx, item.txtName);
        item.photo.setAlpha(0.3f);
        if (p.getPhotoUploadList() != null && !p.getPhotoUploadList().isEmpty()) {
            item.photo.setAlpha(1.0f);
            String url = p.getPhotoUploadList().get(0).getUri();
            Picasso.with(ctx).load(url).into(item.photo);
        } else {
            item.photo.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.black_woman));
        }




        return (convertView);
    }

    static final Locale x = Locale.getDefault();
    static final SimpleDateFormat y = new SimpleDateFormat("dd MMMM yyyy", x);
    static final DecimalFormat df = new DecimalFormat("###,###,##0.0");
}
