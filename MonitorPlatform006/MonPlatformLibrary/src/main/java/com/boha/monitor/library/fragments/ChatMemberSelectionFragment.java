package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.activities.ChatMessageListActivity;
import com.boha.monitor.library.adapters.ChatStaffAdapter;
import com.boha.monitor.library.dto.ChatDTO;
import com.boha.monitor.library.dto.ChatMemberDTO;
import com.boha.monitor.library.dto.CompanyStaffDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.transfer.RequestDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatMemberSelectionFragment extends Fragment implements PageFragment {

    List<CompanyStaffDTO> companyStaffList;
    static final String LOG = ChatMemberSelectionFragment.class.getSimpleName();

    ProjectDTO project;
    TextView txtProject, txtChat;
    ListView staffList;

    ChatStaffAdapter chatStaffAdapter;
    Context ctx;
    View fabChat, topView, header, staffLayout;
    View view;
    boolean setOn = true;

    List<ChatMemberDTO> chatMemberList = new ArrayList<>();
    ChatDTO chat;
    int lastIndex;


    public static ChatMemberSelectionFragment newInstance(ResponseDTO response, int state) {
        ChatMemberSelectionFragment fragment = new ChatMemberSelectionFragment();
        Bundle args = new Bundle();
        args.putSerializable("response", response);
        args.putInt("state", state);
        fragment.setArguments(args);
        return fragment;
    }

    public ChatMemberSelectionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(LOG, "### onCreate");
        if (getArguments() != null) {
            ResponseDTO r = (ResponseDTO) getArguments().getSerializable("response");
            if (r != null) {
                companyStaffList = r.getCompanyStaffList();
            }
        }
    }



    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(LOG, "### onCreateView");
        ctx = getActivity();
        view = inflater.inflate(R.layout.fragment_chatmem_selection, container, false);
        staffList = (ListView) view.findViewById(R.id.CHATMEM_staffList);
        txtProject = (TextView)view.findViewById(R.id.CHATMEM_projectName);
        txtChat = (TextView)view.findViewById(R.id.CHATMEM_chatName);

        fabChat = view.findViewById(R.id.FAB_CHAT);
        topView = view.findViewById(R.id.CHATMEM_top);
        header = view.findViewById(R.id.CHATMEM_label1);
        staffLayout = view.findViewById(R.id.CHATMEM_staffLayout);
        fabChat.setAlpha(0.3f);
        fabChat.setEnabled(false);
        fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(fabChat, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        sendMemberSelections();
                    }
                });
            }
        });


        return view;
    }

    public void setProject( ProjectDTO project) {
        this.project = project;
        txtProject.setText(project.getProjectName());
    }

    public void setChat( ChatDTO chat) {
        this.chat = chat;
        txtChat.setText(chat.getChatName());
    }

    public void setCompanyStaffList(final List<CompanyStaffDTO> companyStaffList) {
        this.companyStaffList = companyStaffList;
        setStaffListView();

    }

    private void setStaffListView() {

        chatStaffAdapter = new ChatStaffAdapter(ctx, R.layout.chat_staff_item, companyStaffList, new ChatStaffAdapter.ChatStaffAdapterListener() {
            @Override
            public void onStaffSelected( CompanyStaffDTO companyStaff, int index) {
                Log.w(LOG, "## staffList clicked: " + companyStaff.getFullName());
                lastIndex = index;
                fabChat.setAlpha(1.0f);
                fabChat.setEnabled(true);
                if (companyStaffList.get(index).getSelected() == null) {
                    companyStaffList.get(index).setSelected(Boolean.FALSE);
                }
                companyStaffList.get(index).setSelected(!companyStaffList.get(index).getSelected());
                setStaffListView();
            }
        });
        staffList.setAdapter(chatStaffAdapter);
        staffList.setSelection(lastIndex);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(header, 300, null);
                chatMemberList.clear();
                for (CompanyStaffDTO dto : companyStaffList) {
                    if (setOn) {
                        dto.setSelected(true);
                        addChatMember(dto);
                    } else {
                        dto.setSelected(false);
                    }

                }
                if (setOn) {
                    fabChat.setAlpha(1.0f);
                    fabChat.setEnabled(true);
                } else {
                    fabChat.setAlpha(0.2f);
                    fabChat.setEnabled(false);
                }
                setOn = !setOn;
                setStaffListView();
            }
        });

    }

    private void addChatMember( CompanyStaffDTO s) {
        ChatMemberDTO cm = new ChatMemberDTO();
        CompanyStaffDTO o = SharedUtil.getCompanyStaff(ctx);
        CompanyStaffDTO owner = new CompanyStaffDTO();
        owner.setCompanyStaffID(o.getCompanyStaffID());
        owner.setFirstName(o.getFirstName());
        owner.setLastName(o.getLastName());
        cm.setChatOwner(owner);

        CompanyStaffDTO member = new CompanyStaffDTO();
        member.setCompanyStaffID(s.getCompanyStaffID());
        member.setFirstName(s.getFirstName());
        member.setLastName(s.getLastName());
        cm.setChatOwner(member);
        cm.setCompanyStaff(member);

        cm.setChatID(chat.getChatID());
        cm.setDateJoined(new Date().getTime());
        chatMemberList.add(cm);
    }
    private void sendMemberSelections() {
        Log.w(LOG, "$$$ sendMemberSelections");
        chatMemberList.clear();
        CompanyStaffDTO me = SharedUtil.getCompanyStaff(ctx);
        for (CompanyStaffDTO x: companyStaffList) {
            if (me.getCompanyStaffID().intValue() == x.getCompanyStaffID().intValue()) {
                addChatMember(x);
            } else {
                if (x.getSelected() == Boolean.TRUE) {
                    addChatMember(x);
                }
            }
        }
        if (chatMemberList.isEmpty() || chatMemberList.size() == 1) {
            Util.showToast(ctx, ctx.getString(R.string.select_msg_receipients));
            return;
        }
        Log.e(LOG,"## sending " + chatMemberList.size() + " chat members");
        RequestDTO w = new RequestDTO(RequestDTO.ADD_CHAT_MEMBERS);
        w.setChatMemberList(chatMemberList);

        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showToast(ctx, "Channel members have been recorded. Send a message!");
                        Intent x = new Intent(ctx, ChatMessageListActivity.class);
                        x.putExtra("project", project);
                        x.putExtra("chat", chat);
                        startActivity(x);
                    }
                });

            }

            @Override
            public void onError(String message) {

            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void animateHeroHeight() {

    }

    @Override
    public void setPageTitle(String title) {

    }

    @Override
    public String getPageTitle() {
        return null;
    }

}
