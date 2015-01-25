package com.com.boha.monitor.library.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.dto.ProjectSiteDTO;
import com.com.boha.monitor.library.dto.TaskStatusDTO;
import com.com.boha.monitor.library.util.Statics;
import com.com.boha.monitor.library.util.WebCheckResult;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProjectSiteAdapter extends ArrayAdapter<ProjectSiteDTO> implements SiteAdapterInterface {

    private final LayoutInflater mInflater;
    private final int mLayoutRes;
    private List<ProjectSiteDTO> mList;
    private Context ctx;
    private WebCheckResult wcr;

    public interface ProjectSiteListener {
        public void onProjectSiteClicked(ProjectSiteDTO site, int index);
    }

    ProjectSiteListener listener;

    public ProjectSiteAdapter(Context context, int textViewResourceId,
                              List<ProjectSiteDTO> list,
                              WebCheckResult wcr,
                              ProjectSiteListener listener) {
        super(context, textViewResourceId, list);
        this.mLayoutRes = textViewResourceId;
        mList = list;
        this.wcr = wcr;
        this.listener = listener;
        ctx = context;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    View view;


    static class ViewHolderItem {
        TextView txtName, txtLastStatus, txtTaskName;
        TextView txtPictureCount, txtStatusCount;
        TextView txtNumber, txtDate, txtBen, txtAccuracy;
        ImageView imgHero, imgConfirmed;
        View statLayout1, statLayout2, imageScroller;
        LinearLayout imageLayout;
//        ImageView img1, img2, img3, img4, img5;
//        TextView num1, num2, num3, num4, num5;
//        TextView date1, date2, date3, date4, date5;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolderItem item;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutRes, null);
            item = new ViewHolderItem();
//            item.img1 = (ImageView) convertView.findViewById(R.id.SITE_scrollImage1);
//            item.img2 = (ImageView) convertView.findViewById(R.id.SITE_scrollImage2);
//            item.img3 = (ImageView) convertView.findViewById(R.id.SITE_scrollImage3);
//            item.img4 = (ImageView) convertView.findViewById(R.id.SITE_scrollImage4);
//            item.img5 = (ImageView) convertView.findViewById(R.id.SITE_scrollImage5);
//
//
//            item.num1 = (TextView) convertView.findViewById(R.id.SITE_number1);
//            item.num2 = (TextView) convertView.findViewById(R.id.SITE_number2);
//            item.num3 = (TextView) convertView.findViewById(R.id.SITE_number3);
//            item.num4 = (TextView) convertView.findViewById(R.id.SITE_number4);
//            item.num5 = (TextView) convertView.findViewById(R.id.SITE_number5);
//
//
//
//            item.date1 = (TextView) convertView.findViewById(R.id.SITE_date1);
//            item.date2 = (TextView) convertView.findViewById(R.id.SITE_date2);
//            item.date3 = (TextView) convertView.findViewById(R.id.SITE_date3);
//            item.date4 = (TextView) convertView.findViewById(R.id.SITE_date4);
//            item.date5 = (TextView) convertView.findViewById(R.id.SITE_date5);
            item.imageLayout = (LinearLayout) convertView
                    .findViewById(R.id.SITE_imageLayout);
            item.imageScroller = convertView
                    .findViewById(R.id.SITE_imageScroll);
            item.txtName = (TextView) convertView
                    .findViewById(R.id.SITE_txtName);
            item.txtNumber = (TextView) convertView
                    .findViewById(R.id.SITE_image);
            item.txtBen = (TextView) convertView
                    .findViewById(R.id.SITE_txtBeneficiary);
            item.txtPictureCount = (TextView) convertView
                    .findViewById(R.id.SITE_txtPictureCount);
            item.txtTaskName = (TextView) convertView
                    .findViewById(R.id.SITE_lastTask);
            item.statLayout1 = convertView.findViewById(R.id.SITE_bottom);
            item.statLayout2 = convertView.findViewById(R.id.SITE_layoutStatus);
            item.txtStatusCount = (TextView) convertView
                    .findViewById(R.id.SITE_txtStatusCount);
            item.txtDate = (TextView) convertView
                    .findViewById(R.id.SITE_lastStatusDate);
            item.txtLastStatus = (TextView) convertView
                    .findViewById(R.id.SITE_lastStatus);
            item.txtAccuracy = (TextView) convertView
                    .findViewById(R.id.SITE_accuracy);

            item.imgHero = (ImageView) convertView.findViewById(R.id.SITE_heroImage);
            item.imgConfirmed = (ImageView) convertView.findViewById(R.id.SITE_confirmed);
            convertView.setTag(item);
        } else {
            item = (ViewHolderItem) convertView.getTag();
        }

        final ProjectSiteDTO p = mList.get(position);
        item.txtName.setText(ctx.getString(R.string.site) + ": " + p.getProjectSiteName());
        item.txtNumber.setText("" + (position + 1));

        if (p.getAccuracy() == null) {
            item.txtAccuracy.setVisibility(View.GONE);
        } else {
            item.txtAccuracy.setVisibility(View.VISIBLE);
            item.txtAccuracy.setText(df.format(p.getAccuracy()));
        }
        if (p.getStatusCount() != null)
            item.txtStatusCount.setText("" + p.getStatusCount());
        else
            item.txtStatusCount.setText("0");

        if (p.getBeneficiary() != null) {
            item.txtBen.setText(p.getBeneficiary().getFullName());
            item.txtBen.setVisibility(View.VISIBLE);
        } else {
            item.txtBen.setVisibility(View.GONE);
        }
        if (p.getLocationConfirmed() == null) {
            item.imgConfirmed.setVisibility(View.GONE);
        } else {
            item.imgConfirmed.setVisibility(View.VISIBLE);
        }
        if (p.getPhotoUploadList() == null) {
            item.txtPictureCount.setText("0");
        } else {
            item.txtPictureCount.setText(df.format(p.getPhotoUploadList().size()));
        }
        Statics.setRobotoFontLight(ctx, item.txtName);
        //manage scroll view images
        hideScrollImages(item);
        int index = 0;
        boolean b1 = false, b2 = false, b3 = false, b4 = false, b5 = false;

//        if (p.getPhotoUploadList() != null
//                && !p.getPhotoUploadList().isEmpty()) {
//            item.imageScroller.setVisibility(View.VISIBLE);
//            item.imgHero.setVisibility(View.GONE);
//            for (final PhotoUploadDTO d : p.getPhotoUploadList()) {
//                final String uri = Statics.IMAGE_URL + d.getUri();
//                switch (index) {
//                    case 0:
//                        setImage(d, item.img1);
//                        item.date1.setText(sdf.format(d.getDateTaken()));
//                        item.img1.setVisibility(View.VISIBLE);
//                        item.num1.setText("1");
//                        item.num1.setVisibility(View.VISIBLE);
//                        item.date1.setVisibility(View.VISIBLE);
//
//                        b1 = true;
//                        break;
//                    case 1:
//                        setImage(d, item.img2);
//                        item.date2.setText(sdf.format(d.getDateTaken()));
//
//                        item.img2.setVisibility(View.VISIBLE);
//                        item.num2.setText("2");
//                        item.num2.setVisibility(View.VISIBLE);
//
//                        item.date2.setVisibility(View.VISIBLE);
//                        b2 = true;
//                        break;
//                    case 2:
//                        setImage(d, item.img3);
//                        item.date3.setText(sdf.format(d.getDateTaken()));
//
//                        item.img3.setVisibility(View.VISIBLE);
//                        item.num3.setText("3");
//                        item.num3.setVisibility(View.VISIBLE);
//                        item.date3.setVisibility(View.VISIBLE);
//                        b3 = true;
//                        break;
//                    case 3:
//                        setImage(d, item.img4);
//                        item.date4.setText(sdf.format(d.getDateTaken()));
//                        item.img4.setVisibility(View.VISIBLE);
//                        item.num4.setText("4");
//                        item.num4.setVisibility(View.VISIBLE);
//                        item.date4.setVisibility(View.VISIBLE);
//                        b4 = true;
//                        break;
//                    case 4:
//                        ImageLoader.getInstance().displayImage(uri, item.img5);
//                        item.date5.setText(sdf.format(d.getDateTaken()));
//                        item.img5.setVisibility(View.VISIBLE);
//                        item.num5.setText("5");
//                        item.num5.setVisibility(View.VISIBLE);
//                        item.date5.setVisibility(View.VISIBLE);
//                        b5 = true;
//                        break;
//
//                }
//                index++;
//                if (index == 5) {
//                    break;
//                }
//            }
//        }

//        if (!b1) {
//            item.img1.setVisibility(View.GONE);
//            item.date1.setVisibility(View.GONE);
//        }
//        if (!b2) {
//            item.img2.setVisibility(View.GONE);
//            item.date2.setVisibility(View.GONE);
//        }
//        if (!b3) {
//            item.img3.setVisibility(View.GONE);
//            item.date3.setVisibility(View.GONE);
//        }
//        if (!b4) {
//            item.img4.setVisibility(View.GONE);
//            item.date4.setVisibility(View.GONE);
//        }
//        if (!b5) {
//            item.img5.setVisibility(View.GONE);
//            item.date5.setVisibility(View.GONE);
//        }


//        if (p.getPhotoUploadList() != null ) {
//            if (p.getPhotoUploadList().size() == 1) {
//                hideScrollImages(item);
//                item.imageScroller.setVisibility(View.GONE);
//                item.imgHero.setVisibility(View.VISIBLE);
//                final String uri = Statics.IMAGE_URL + p.getPhotoUploadList().get(0).getUri();
//                ImageLoader.getInstance().displayImage(uri, item.imgHero);
//
//            }
//        }
//        if (p.getPhotoUploadList() != null && p.getPhotoUploadList().isEmpty()) {
//            hideScrollImages(item);
//            item.imageScroller.setVisibility(View.GONE);
//            item.imgHero.setVisibility(View.GONE);
//        }

        if (p.getLastStatus() != null) {
            item.txtLastStatus.setText(p.getLastStatus().getTaskStatus().getTaskStatusName());
            item.txtTaskName.setText(p.getLastStatus().getTask().getTaskName());
            if (p.getLastStatus().getStatusDate() == null) {
                item.txtDate.setText("Date not available");
            } else {
                item.txtDate.setText(sdf.format(p.getLastStatus().getStatusDate()));
            }

            item.statLayout1.setVisibility(View.VISIBLE);
            item.statLayout2.setVisibility(View.VISIBLE);
            switch (p.getLastStatus().getTaskStatus().getStatusColor()) {
                case TaskStatusDTO.STATUS_COLOR_GREEN:
                    item.txtStatusCount.setBackground(ctx.getResources().getDrawable(R.drawable.xgreen_oval));
                    item.txtPictureCount.setBackground(ctx.getResources().getDrawable(R.drawable.xgreen_oval));
                    break;
                case TaskStatusDTO.STATUS_COLOR_YELLOW:
                    item.txtStatusCount.setBackground(ctx.getResources().getDrawable(R.drawable.xorange_oval));
                    item.txtPictureCount.setBackground(ctx.getResources().getDrawable(R.drawable.xorange_oval));
                    break;
                case TaskStatusDTO.STATUS_COLOR_RED:
                    item.txtStatusCount.setBackground(ctx.getResources().getDrawable(R.drawable.xred_oval));
                    item.txtPictureCount.setBackground(ctx.getResources().getDrawable(R.drawable.xred_oval));
                    break;
                default:
                    item.txtStatusCount.setBackground(ctx.getResources().getDrawable(R.drawable.xgrey_oval));
                    item.txtPictureCount.setBackground(ctx.getResources().getDrawable(R.drawable.xgrey_oval));
                    break;
            }

        } else {
            item.statLayout1.setVisibility(View.GONE);
            item.statLayout2.setVisibility(View.GONE);
            item.txtStatusCount.setBackground(ctx.getResources().getDrawable(R.drawable.xgrey_oval));
            item.txtPictureCount.setBackground(ctx.getResources().getDrawable(R.drawable.xgrey_oval));
        }

//        if (p.getProjectSiteTaskList() == null) {
//            item.txtPictureCount.setText("0");
//        } else {
//            item.txtPictureCount.setText("" + p.getProjectSiteTaskList().size());
//        }

        if (p.getLastStatus() != null) {
            if (p.getLastStatus().getStatusDate() == null) {
                item.txtDate.setText("Date not available");
            } else {
                item.txtDate.setText(sdf.format(p.getLastStatus().getStatusDate()));
                item.txtDate.setVisibility(View.VISIBLE);
            }
        }

        item.imageScroller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProjectSiteClicked(p, position);
            }
        });
        item.imageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProjectSiteClicked(p, position);
            }
        });
        item.txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProjectSiteClicked(p, position);
            }
        });
        item.txtStatusCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProjectSiteClicked(p, position);
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onProjectSiteClicked(p, position);
            }
        });
        Statics.setRobotoFontLight(ctx, item.txtNumber);
        Statics.setRobotoFontBold(ctx, item.txtDate);
        Statics.setRobotoFontLight(ctx, item.txtName);

        return (convertView);
    }


    private void hideScrollImages(ViewHolderItem item) {
//        item.img1.setVisibility(View.GONE);
//        item.img2.setVisibility(View.GONE);
//        item.img3.setVisibility(View.GONE);
//        item.img4.setVisibility(View.GONE);
//        item.img5.setVisibility(View.GONE);
//
//        item.num1.setVisibility(View.GONE);
//        item.num2.setVisibility(View.GONE);
//        item.num3.setVisibility(View.GONE);
//        item.num4.setVisibility(View.GONE);
//        item.num5.setVisibility(View.GONE);

    }

    static final Locale x = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm", x);
    static final DecimalFormat df = new DecimalFormat("###,###,###,###,###");
}
