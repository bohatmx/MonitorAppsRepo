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

import com.boha.monitor.library.util.PopupItem;
import com.boha.monitor.library.util.Statics;
import com.boha.platform.library.R;

import java.util.List;

public class PopupListIconAdapter extends ArrayAdapter<PopupItem> {


    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<PopupItem> mList;
    private Context ctx;
    private int darkColor;

    static final String LOG = PopupListIconAdapter.class.getSimpleName();

    public PopupListIconAdapter(Context context, int textViewResourceId,
                                List<PopupItem> list, int darkColor) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        mList = list;
        ctx = context;
        this.darkColor = darkColor;
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



        final PopupItem p = mList.get(position);
        item.image.setImageDrawable(ContextCompat.getDrawable(ctx, p.getDrawableID()));
        item.txtString.setText(p.getText());

        if (darkColor != 0) {
            item.image.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN);
        }
        Statics.setRobotoFontLight(ctx, item.txtString);
        return (convertView);
    }

}
