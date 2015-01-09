package com.boha.monitor.reporter.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.boha.proximity.data.ErrorStoreAndroidDTO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AndroidCrashAdapter extends ArrayAdapter<ErrorStoreAndroidDTO> {

    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<ErrorStoreAndroidDTO> mList;
    private Context ctx;

    public AndroidCrashAdapter(Context context, int textViewResourceId,
                               List<ErrorStoreAndroidDTO> list) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        mList = list;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    View view;


    static class ViewHolderItem {
        TextView txtGroupName, txtAppVersion, txtErrorDate, txtPackage, txtNum;
        TextView txtAndroidVersion, txtPhone, txtLogcat, txtStackTrace, txtBrand;
        ImageView btnExample;
        NetworkImageView image;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolderItem vhItem;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            vhItem = new ViewHolderItem();
            vhItem.txtGroupName = (TextView) convertView
                    .findViewById(com.boha.proximity.library.R.id.AE_group);
            vhItem.txtAppVersion = (TextView) convertView
                    .findViewById(com.boha.proximity.library.R.id.AE_appVersion);
            vhItem.txtErrorDate = (TextView) convertView
                    .findViewById(com.boha.proximity.library.R.id.AE_errorDate);
            vhItem.txtLogcat= (TextView) convertView
                    .findViewById(com.boha.proximity.library.R.id.AE_logcat);
            vhItem.txtStackTrace = (TextView) convertView
                    .findViewById(com.boha.proximity.library.R.id.AE_stackTrace);
            vhItem.txtBrand = (TextView) convertView
                    .findViewById(com.boha.proximity.library.R.id.AE_brand);
            vhItem.txtPackage = (TextView) convertView
                    .findViewById(com.boha.proximity.library.R.id.AE_package);
            vhItem.txtPhone = (TextView) convertView
                    .findViewById(com.boha.proximity.library.R.id.AE_phone);
            vhItem.txtAndroidVersion = (TextView) convertView
                    .findViewById(com.boha.proximity.library.R.id.AE_androidVersion);
            vhItem.txtNum = (TextView) convertView
                    .findViewById(com.boha.proximity.library.R.id.AE_number);

            convertView.setTag(vhItem);
        } else {
            vhItem = (ViewHolderItem) convertView.getTag();
        }

        final ErrorStoreAndroidDTO p = mList.get(position);
        vhItem.txtNum.setText("" + (position + 1));
        vhItem.txtGroupName.setText(p.getCompanyName());
        vhItem.txtAndroidVersion.setText(p.getAndroidVersion());
        vhItem.txtAppVersion.setText("Version: " + p.getAppVersionName() +"." + p.getAppVersionCode());
        vhItem.txtBrand.setText(p.getBrand().toUpperCase());
        vhItem.txtErrorDate.setText(y.format(new Date(p.getErrorDate())));
        vhItem.txtLogcat.setText(p.getLogCat());
        vhItem.txtStackTrace.setText(p.getStackTrace());
        vhItem.txtPackage.setText(p.getPackageName());
        vhItem.txtPhone.setText(p.getPhoneModel());

        animateView(convertView);
        return (convertView);
    }

    public void animateView(final View view) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f);
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f);
        scaleDownX.setDuration(1000);
        scaleDownY.setDuration(1000);

        final AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleUpX).with(scaleUpY);

        scaleDown.start();

    }

    static final Locale x = Locale.getDefault();
    static final SimpleDateFormat y = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss", x);
}
