package com.boha.monitor.firebase.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.firebase.R;
import com.boha.monitor.firebase.data.UserDTO;

import java.util.List;

/**
 * Created by aubreyM on 14/12/17.
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface UserListener {
        void onDeleteRequired(UserDTO user);
    }

    private UserListener mListener;
    private List<UserDTO> users;
    private Context ctx;
    boolean isSelectionList;

    public UserAdapter(List<UserDTO> userList,UserListener listener) {
        this.users = users;
        this.mListener = listener;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final UserViewHolder holder, final int position) {

        final UserDTO p = users.get(position);
        holder.userName.setText(p.getFullName());
        holder.email.setText(p.getEmail());

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDeleteRequired(p);
            }
        });




    }

    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        protected TextView userName, email;
        protected ImageView delete;


        public UserViewHolder(View itemView) {
            super(itemView);
            userName = (TextView) itemView.findViewById(R.id.name);
            email = (TextView) itemView.findViewById(R.id.date);
            delete = (ImageView) itemView.findViewById(R.id.icon);
        }

    }

    static final String LOG = UserAdapter.class.getSimpleName();
}
