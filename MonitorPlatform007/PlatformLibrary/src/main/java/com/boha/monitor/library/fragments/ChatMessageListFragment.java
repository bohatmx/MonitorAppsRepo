package com.boha.monitor.library.fragments;

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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.dto.ChatDTO;
import com.boha.monitor.library.dto.ChatMessageDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.adapters.ChatMessageListAdapter;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageListFragment extends Fragment implements PageFragment {

    static final String LOG = ChatMessageListFragment.class.getSimpleName();

    int state;
    ListView msgList;
    ChatMessageListAdapter chatMessageListAdapter;
    Context ctx;
    View fabSend, topView, messageLayout;
    TextView txtProject, txtChat;
    ProjectDTO project;
    ProgressBar progressBar;
    StaffDTO staff;
    List<ChatMessageDTO> chatMessageList = new ArrayList<>();


    public static ChatMessageListFragment newInstance(ResponseDTO response, int state) {
        ChatMessageListFragment fragment = new ChatMessageListFragment();
        Bundle args = new Bundle();
        args.putSerializable("response", response);
        args.putInt("state", state);
        fragment.setArguments(args);
        return fragment;
    }

    public ChatMessageListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(LOG, "### onCreate");

    }

    View view;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(LOG, "### onCreateView");
        ctx = getActivity();
        view = inflater.inflate(R.layout.fragment_chatmessage_list, container, false);
        setFields();
        staff = SharedUtil.getCompanyStaff(ctx);

        return view;
    }

    private void setFields() {
        msgList = (ListView) view.findViewById(R.id.CML_messageList);
        editMessage = (EditText) view.findViewById(R.id.CML_text);
        fabSend = view.findViewById(R.id.FAB_SEND);
        topView = view.findViewById(R.id.CML_top);
        txtProject = (TextView) view.findViewById(R.id.CML_projectName);
        txtChat = (TextView) view.findViewById(R.id.CML_chatName);
        messageLayout = view.findViewById(R.id.CML_messageLayout);
        progressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
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
        txtChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtChat, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        showChatPopup();
                    }
                });
            }
        });
    }
    private void showChatPopup() {

    }
    private void getCachedChat() {
        Log.w(LOG,"### starting getCachedChat");
        CacheUtil.getCachedProjectChats(ctx, project.getProjectID(), new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getChatMessageList() != null) {
                    Log.i(LOG, "++ cached messages found: " + response.getChatMessageList().size());
                    chatMessageList = response.getChatMessageList();
                    setMessageListView();
                }
                getRemoteMessages();
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

    private void getRemoteMessages() {
        Log.w(LOG,"** starting getRemoteMessages ...");
        RequestDTO w = new RequestDTO(RequestDTO.GET_MESSAGES_BY_PROJECT);
        w.setProjectID(project.getProjectID());

        progressBar.setVisibility(View.VISIBLE);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (response.getStatusCode() == 0) {
                            Log.i(LOG, "++ returned messages: " + response.getChatMessageList().size());
                            chatMessageList = response.getChatMessageList();
                            setMessageListView();
                        }
                    }
                });
            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Util.showErrorToast(ctx, message);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }

    public void refreshMessages() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getRemoteMessages();
            }
        });

    }
    public void setChat( ChatDTO chat) {
        this.chat = chat;
        this.chatID = chat.getChatID();
    }

    public void setChatID(Integer chatID) {
        this.chatID = chatID;
    }

    public void setProject( ProjectDTO project) {
        Log.i(LOG,"++ setProject: " + project.getProjectName());
        this.project = project;
        txtProject.setText(project.getProjectName());
        //
        getRemoteMessages();

    }

    private void setMessageListView() {

        if (chatMessageList == null) {
            chatMessageList = new ArrayList<>();
        }
        Log.w(LOG, "## setMessageListView, chatMessageList: " + chatMessageList.size());
        chatMessageListAdapter = new ChatMessageListAdapter(ctx, R.layout.message_item, chatMessageList);
        msgList.setAdapter(chatMessageListAdapter);



    }

    EditText editMessage;
    ChatDTO chat;
    Integer chatID;

    private void sendMessage() {
        Log.w(LOG, "$$$ sendMessage ....");
        if (editMessage.getText().toString().isEmpty()) {
            Util.showErrorToast(ctx, "Enter message");
            return;
        }
        hideKeyboard();
        RequestDTO w = new RequestDTO(RequestDTO.SEND_CHAT_MESSAGE);
        ChatMessageDTO xm = new ChatMessageDTO();
        StaffDTO zz = SharedUtil.getCompanyStaff(ctx);
        xm.setMessage(editMessage.getText().toString());
        xm.setChat(chat);
        xm.setStaff(staff);

        w.setChatMessage(xm);
        progressBar.setVisibility(View.VISIBLE);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                Log.i(LOG, "++ returned from sendMessage");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        editMessage.setText("");
                        Log.i(LOG, "** status message: " + response.getMessage());

                    }
                });

            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Util.showErrorToast(ctx, message);
                    }
                });
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
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void animateHeroHeight() {

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
    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editMessage.getWindowToken(), 0);
    }
    int primaryColor, darkColor;
    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }
}
