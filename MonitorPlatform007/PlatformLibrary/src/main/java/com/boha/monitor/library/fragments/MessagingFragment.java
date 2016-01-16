package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.adapters.PopupMonitorListAdapter;
import com.boha.monitor.library.dto.ChatMessageDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MessagingListener} interface
 * to handle interaction events.
 * Use the {@link MessagingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessagingFragment extends Fragment implements PageFragment {

    private MessagingListener mListener;
    private ResponseDTO response;
    private View view, handle;
    private ListView listView;
    private List<MonitorDTO> monitorList;
    private ImageView  iconAdd, hero;
    private TextView txtRecipients, title;
    private Context ctx;
    private int color = R.color.teal_900;

    public static MessagingFragment newInstance(List<MonitorDTO> list) {
        MessagingFragment fragment = new MessagingFragment();
        ResponseDTO w = new ResponseDTO();
        Bundle args = new Bundle();
        w.setMonitorList(list);
        args.putSerializable("trackerDTOList", w);
        fragment.setArguments(args);
        return fragment;
    }

    public MessagingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            response = (ResponseDTO) getArguments().getSerializable("trackerDTOList");
            monitorList = response.getMonitorList();
            Collections.sort(monitorList);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_messaging, container, false);
        ctx = getActivity();
        setFields();
        return view;
    }

    private void setFields() {
        txtRecipients = (TextView) view.findViewById(R.id.MSG_recipients);
        title = (TextView) view.findViewById(R.id.MSG_name);
        iconAdd = (ImageView) view.findViewById(R.id.MSG_iconAdd);
        hero = (ImageView) view.findViewById(R.id.backdrop);
        listView = (ListView) view.findViewById(R.id.MSG_messageList);
        handle = view.findViewById(R.id.handle);
        txtRecipients.setText(getString(R.string.sel_recipients));

        txtRecipients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup();
            }
        });
        iconAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup();
            }
        });

    }

    private List<MonitorDTO> recipientList;
    private HashMap<Integer, MonitorDTO> map = new HashMap<>();
    private void showPopup() {

        final ListPopupWindow pop = new ListPopupWindow(getActivity());
        LayoutInflater inf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inf.inflate(R.layout.hero_image_popup, null);
        TextView txt = (TextView) v.findViewById(R.id.HERO_caption);
        txt.setText("Select/Remove Recipient");
        ImageView img = (ImageView) v.findViewById(R.id.HERO_image);
        img.setImageDrawable(Util.getRandomBackgroundImage(ctx));

        pop.setPromptView(v);
        pop.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);

        pop.setAnchorView(handle);
        pop.setHorizontalOffset(Util.getPopupHorizontalOffset(getActivity()));
        pop.setModal(true);
        pop.setWidth(Util.getPopupWidth(getActivity()));

        pop.setAdapter(new PopupMonitorListAdapter(ctx,
                R.layout.xxmonitor_spinner_item,
                darkColor,
                monitorList));

        pop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pop.dismiss();
                if (monitorList.get(position).isSelected() == Boolean.TRUE) {
                    monitorList.get(position).setSelected(false);
                    map.remove(monitorList.get(position).getMonitorID());
                } else {
                    monitorList.get(position).setSelected(true);
                    map.put(monitorList.get(position).getMonitorID(), monitorList.get(position));
                }


                StringBuilder v = new StringBuilder();
                Set<Integer> set = map.keySet();
                for (Integer i: set) {
                    v.append(map.get(i).getFullName()).append(", '");
                }
                String rec = v.toString();
                if (rec.isEmpty()) {
                    txtRecipients.setText(getString(R.string.sel_recipients));
                } else {
                    txtRecipients.setText(v.toString());
                }

            }
        });
        try {
            pop.show();
        } catch (Exception e) {
            Log.e("MessagingFragment", "-- popup failed, probably nullpointer", e);
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MessagingListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MessagingListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        RefWatcher refWatcher = MonApp.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }
    @Override
    public void animateHeroHeight() {
        hero.setImageDrawable(Util.getRandomBackgroundImage(getActivity()));
        Util.expand(hero, 1000, null);
    }

    String pageTitle;

    @Override
    public void setPageTitle(String title) {
        pageTitle = title;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    int primaryColor, darkColor;
    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface MessagingListener {
        void onMessageSelected(ChatMessageDTO message);
    }



}
