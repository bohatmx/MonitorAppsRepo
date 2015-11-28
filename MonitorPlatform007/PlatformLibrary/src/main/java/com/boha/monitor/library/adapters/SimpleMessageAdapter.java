package com.boha.monitor.library.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.SimpleMessageDTO;
import com.boha.platform.library.R;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by aubreyM on 14/12/17.
 */
public class SimpleMessageAdapter extends RecyclerView.Adapter<SimpleMessageAdapter.MessageViewHolder> {

    public interface SimpleMessageListener {
        void onResponseRequested(SimpleMessageDTO message, int position);
    }

    private SimpleMessageListener mListener;
    private List<SimpleMessageDTO> simpleMessageList;
    private Context ctx;
    int darkColor;
    

    public SimpleMessageAdapter(List<SimpleMessageDTO> simpleMessageList, int darkColor,
                                Context context, SimpleMessageListener listener) {
        this.simpleMessageList = simpleMessageList;
        this.ctx = context;
        this.mListener = listener;
        this.darkColor = darkColor;
    }


    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_message_item, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder mh, final int position) {

        final SimpleMessageDTO p = simpleMessageList.get(position);
//        if (p.getUrl() != null) {
//            Picasso.with(ctx).load(p.getUrl()).fit().into(mh.image);
//            mh.image.setAlpha(1.0f);
//        } else {
//            mh.image.setImageDrawable(ContextCompat.getDrawable(ctx,R.drawable.boy));
//            mh.image.setAlpha(0.3f);
//        }
        if (p.getStaffName() != null) {
            mh.txtFromName.setText(p.getStaffName());
        }
        if (p.getMonitorName() != null) {
            mh.txtFromName.setText(p.getMonitorName());
        }

        if (p.getMessageDate() != null) {
            mh.txtDate.setText(sdf.format(new Date(p.getMessageDate().longValue())));
        }
        mh.txtMessage.setText(p.getMessage());
        mh.iconRespond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onResponseRequested(p,position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return simpleMessageList == null ? 0 : simpleMessageList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMMM yyyy HH:mm", loc);

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        protected CircleImageView image;
        protected TextView txtFromName, txtDate, txtMessage;
        protected ImageView iconRespond;


        public MessageViewHolder(View itemView) {
            super(itemView);
            image = (CircleImageView) itemView.findViewById(R.id.SMI_fromImage);
            txtFromName = (TextView) itemView.findViewById(R.id.SMI_from);
            txtDate = (TextView) itemView.findViewById(R.id.SMI_fromTime);
            txtMessage = (TextView) itemView.findViewById(R.id.SMI_fromMsg);
            iconRespond = (ImageView) itemView.findViewById(R.id.SMI_iconRespond);
        }

    }

    static final String LOG = SimpleMessageAdapter.class.getSimpleName();
}
