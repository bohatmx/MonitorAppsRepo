package com.com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.adapters.ChatListAdapter;
import com.com.boha.monitor.library.adapters.ChatStaffAdapter;
import com.com.boha.monitor.library.dto.ChatDTO;
import com.com.boha.monitor.library.dto.ChatMemberDTO;
import com.com.boha.monitor.library.dto.ChatMessageDTO;
import com.com.boha.monitor.library.dto.CompanyStaffDTO;
import com.com.boha.monitor.library.dto.transfer.RequestDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.NetUtil;
import com.com.boha.monitor.library.util.SharedUtil;
import com.com.boha.monitor.library.util.Util;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements PageFragment {

    List<CompanyStaffDTO> companyStaffList;
    static final String LOG = ChatFragment.class.getSimpleName();
    public static final int
            STARTING_CHAT = 1,
            RESPONDING_TO_MESSAGE = 2;
    int state;
    ListView staffList, msgList;
    ChatListAdapter chatListAdapter;
    ChatStaffAdapter chatStaffAdapter;
    Context ctx;
    View fabSend, fabChat, topView, header, staffLayout, messageLayout;
    ImageView fabIcon;
    List<ChatMessageDTO> chatMessageList = new ArrayList<>();

    public static ChatFragment newInstance(ResponseDTO response, int state) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putSerializable("response", response);
        args.putInt("state", state);
        fragment.setArguments(args);
        return fragment;
    }

    public ChatFragment() {
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
            state = getArguments().getInt("state", STARTING_CHAT);
        }
    }

    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(LOG, "### onCreateView");
        ctx = getActivity();
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        msgList = (ListView) view.findViewById(R.id.CHAT_messageList);
        staffList = (ListView) view.findViewById(R.id.CHAT_staffList);
        editMessage = (EditText) view.findViewById(R.id.CHAT_text);
        fabSend = view.findViewById(R.id.FAB_SEND);
        fabChat = view.findViewById(R.id.FAB_CHAT);
        topView = view.findViewById(R.id.CHAT_top);
        header = view.findViewById(R.id.CHAT_label1);
        staffLayout = view.findViewById(R.id.CHAT_staffLayout);
        messageLayout = view.findViewById(R.id.CHAT_messageLayout);


        getCachedChat();


        return view;
    }

    private void getCachedChat() {
        CacheUtil.getCachedData(ctx, CacheUtil.CACHE_CHAT, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getChatMessageList() != null) {
                    chatMessageList = response.getChatMessageList();

                }
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

//    public void setChatMessage() {
//
//        Log.w(LOG, "$$ setChatMessage");
//        state = RESPONDING_TO_MESSAGE;
//        CacheUtil.getCachedData(ctx, CacheUtil.CACHE_CHAT, new CacheUtil.CacheUtilListener() {
//            @Override
//            public void onFileDataDeserialized(ResponseDTO response) {
//                if (response.getChatMessageList() != null) {
//                    chatMessageList = response.getChatMessageList();
//                }
//                staffList.setVisibility(View.GONE);
//                msgList.setVisibility(View.VISIBLE);
//                fabSend.setVisibility(View.VISIBLE);
//                fabChat.setVisibility(View.GONE);
//                fabSend.setAlpha(1.0f);
//                fabSend.setEnabled(true);
//                fabSend.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Util.flashOnce(fabSend, 300, new Util.UtilAnimationListener() {
//                            @Override
//                            public void onAnimationEnded() {
//                                sendMessage();
//                            }
//                        });
//                    }
//                });
//                setMessageListView(false);
//            }
//
//            @Override
//            public void onDataCached() {
//
//            }
//
//            @Override
//            public void onError() {
//
//            }
//        });
//
//
//    }

    public void setChatMessageList(List<ChatMessageDTO> chatMessageList) {
        this.chatMessageList = chatMessageList;
        Log.e(LOG, "### setChatMessageList, should we refresh? ....");
        if (messageLayout.getVisibility() == View.VISIBLE) {
            staffLayout.setVisibility(View.GONE);
            fabChat.setVisibility(View.GONE);
            messageLayout.setVisibility(View.VISIBLE);
            fabSend.setVisibility(View.VISIBLE);
            setMessageListView(false);
        }

    }

    public void setChatID(Integer chatID) {
        this.chatID = chatID;
    }

    public void setCompanyStaffList(final List<CompanyStaffDTO> companyStaffList) {
        this.companyStaffList = companyStaffList;
        this.state = STARTING_CHAT;

        messageLayout.setVisibility(View.GONE);
        staffLayout.setVisibility(View.VISIBLE);
        fabSend.setVisibility(View.GONE);
        fabChat.setVisibility(View.VISIBLE);

        fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(fabChat, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        for (CompanyStaffDTO g : companyStaffList) {
                            if (g.getSelected() == null) {
                                g.setSelected(Boolean.FALSE);
                            }

                        }
                        messageLayout.setVisibility(View.VISIBLE);
                        staffLayout.setVisibility(View.GONE);
                        fabSend.setVisibility(View.VISIBLE);
                        fabChat.setVisibility(View.GONE);
                        setMessageListView(true);
                    }
                });

            }
        });
        setStaffListView();

    }

    int lastIndex;

    private void setStaffListView() {

        chatStaffAdapter = new ChatStaffAdapter(ctx, R.layout.chat_staff_item, companyStaffList, new ChatStaffAdapter.ChatStaffAdapterListener() {
            @Override
            public void onStaffSelected(CompanyStaffDTO companyStaff, int index) {
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
                for (CompanyStaffDTO dto : companyStaffList) {
                    if (setOn) {
                        dto.setSelected(true);
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

    boolean setOn = true;
    List<ChatMemberDTO> chatMemberList = new ArrayList<>();

    private void setMessageListView(boolean isStarting) {

        if (chatMessageList == null) {
            chatMessageList = new ArrayList<>();
        }
        chatListAdapter = new ChatListAdapter(ctx, R.layout.message_item, chatMessageList);
        msgList.setAdapter(chatListAdapter);

        CompanyStaffDTO cs = SharedUtil.getCompanyStaff(ctx);
        CompanyStaffDTO zx = new CompanyStaffDTO();
        zx.setCompanyStaffID(cs.getCompanyStaffID());
        zx.setFirstName(cs.getFirstName());
        zx.setLastName(cs.getLastName());
        if (isStarting) {
            if (chatMemberList.isEmpty()) {
                ChatMemberDTO z = new ChatMemberDTO();
                z.setCompanyStaff(zx);
                z.setChatOwner(zx);
                chatMemberList.add(z);
                for (CompanyStaffDTO x : companyStaffList) {
                    if (x.getSelected()) {
                        ChatMemberDTO za = new ChatMemberDTO();
                        CompanyStaffDTO xx = new CompanyStaffDTO();
                        xx.setCompanyStaffID(x.getCompanyStaffID());
                        xx.setFirstName(x.getFirstName());
                        xx.setLastName(x.getLastName());
                        za.setCompanyStaff(xx);
                        za.setChatOwner(zx);
                        if (xx.getCompanyStaffID().intValue() == cs.getCompanyStaffID().intValue()) {
                            continue;
                        }
                        chatMemberList.add(za);
                    }
                }
                inviteStaffToChat();
            }
        }

        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Util.flashOnce(fabSend, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        sendMessage();
                    }
                });
            }
        });
    }

    EditText editMessage;
    boolean chatMembersInvited;
    ChatDTO chat;
    Integer chatID;

    private void sendMessage() {
        Log.w(LOG, "$$$ sendMessage");
        if (editMessage.getText().toString().isEmpty()) {
            Util.showErrorToast(ctx, "Enter message");
            return;
        }
        hideKeyboard();
        RequestDTO w = new RequestDTO(RequestDTO.SEND_CHAT_MESSAGE);
        ChatMessageDTO xm = new ChatMessageDTO();
        CompanyStaffDTO zz = SharedUtil.getCompanyStaff(ctx);
        xm.setMessage(editMessage.getText().toString());
        if (chat != null) {
            xm.setChatID(chat.getChatID());
        } else {
            if (chatID != null) {
                xm.setChatID(chatID);
            } else {
                throw new UnsupportedOperationException("Chat and ChatID is NULL");
            }
        }

        xm.setStaffName(zz.getFullName());
        xm.setCompanyStaffID(zz.getCompanyStaffID());

        w.setChatMessage(xm);

        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                Log.e(LOG, "## GCM Success: " + response.getGcmSuccess() + " Failure: " + response.getGcmFailure());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        editMessage.setText("");
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

    private void inviteStaffToChat() {

        ChatDTO x = new ChatDTO();
        x.setMessage("Whazzup?");
        x.setChatMemberList(chatMemberList);
        CompanyStaffDTO s = new CompanyStaffDTO();
        s.setFirstName(SharedUtil.getCompanyStaff(ctx).getFirstName());
        s.setLastName(SharedUtil.getCompanyStaff(ctx).getLastName());
        s.setCompanyStaffID(SharedUtil.getCompanyStaff(ctx).getCompanyStaffID());
        x.setCompanyStaff(s);

        RequestDTO w = new RequestDTO(RequestDTO.ADD_CHAT);
        w.setChat(x);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                Log.e(LOG, "$$$ chatMembersInvited, status: " + response.getStatusCode());
                if (response.getStatusCode() > 0) {
                    chatMembersInvited = false;
                } else {
                    chatMembersInvited = true;
                    chat = response.getChat();
                    if (chat == null) {
                        Log.e(LOG, "we has a small problem, chat is null");
                    } else {
                        Log.i(LOG, "we cool with chat...");
                    }
                }

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

    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editMessage.getWindowToken(), 0);
    }
}
