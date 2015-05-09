package com.com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.activities.ChatMemberSelectionActivity;
import com.com.boha.monitor.library.activities.ChatMessageListActivity;
import com.com.boha.monitor.library.adapters.ChatListAdapter;
import com.com.boha.monitor.library.dto.ChatDTO;
import com.com.boha.monitor.library.dto.CompanyStaffDTO;
import com.com.boha.monitor.library.dto.ProjectDTO;
import com.com.boha.monitor.library.dto.transfer.RequestDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.NetUtil;
import com.com.boha.monitor.library.util.SharedUtil;
import com.com.boha.monitor.library.util.Util;

import java.util.ArrayList;
import java.util.List;


public class NewChatFragment extends Fragment implements PageFragment {

    TextView title, txtProject;
    EditText txtChatName;
    TextView txtColor1, txtColor2, txtColor3, txtColor4, txtColor5, txtColor6;
    View editorView, topView, fab;
    Context ctx;
    View view;
    ChannelFragmentListener mListener;
    int selectedColor;
    List<ProjectDTO> projectList;
    List<String> projectStringList;
    List<ChatDTO> chatList;
    ProjectDTO project;
    ImageView iconClose;
    ChatDTO chat;
    ListView listView;
    ProgressBar progressBar;
    ChatListAdapter chatListAdapter;

    static final String LOG = NewChatFragment.class.getSimpleName();

    public static NewChatFragment newInstance() {
        NewChatFragment fragment = new NewChatFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public NewChatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_new_chat, container, false);
        ctx = getActivity();
        setFields();
        projectID = SharedUtil.getLastProjectID(ctx);
        getCachedProjectList();
        return view;
    }
    Integer projectID = 0;
    private void getCachedChatList() {

        if (project != null) {
            projectID = project.getProjectID();
        }
        CacheUtil.getCachedProjectChats(ctx,projectID,new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getChatList() != null) {
                    chatList = response.getChatList();
                    setList();
                }
                getRemoteChatList();
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }
    private void getRemoteChatList() {
        if (project == null) {
            CacheUtil.getCachedData(ctx,CacheUtil.CACHE_DATA,new CacheUtil.CacheUtilListener() {
                @Override
                public void onFileDataDeserialized(ResponseDTO response) {
                    if (response.getCompany() != null) {
                        projectList = response.getCompany().getProjectList();
                        if (!projectList.isEmpty()) {
                            project = projectList.get(0);
                            getRemoteChatList();
                        }
                    }
                }

                @Override
                public void onDataCached() {

                }

                @Override
                public void onError() {

                }
            });
        } else {
            RequestDTO w = new RequestDTO(RequestDTO.GET_CHATS_BY_PROJECT);
            w.setProjectID(project.getProjectID());

            NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
                @Override
                public void onResponse(final ResponseDTO response) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.getStatusCode() == 0) {
                                chatList = response.getChatList();
                                setList();
                            }
                        }
                    });

                }

                @Override
                public void onError(final String message) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Util.showErrorToast(ctx, message);
                        }
                    });
                }

                @Override
                public void onWebSocketClose() {

                }
            });
        }
    }
    private void setList() {
        chatListAdapter = new ChatListAdapter(ctx,R.layout.chat_item,chatList);
        listView.setAdapter(chatListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chat = chatList.get(position);
                Intent s;
                if (chat.getChatMemberList().isEmpty()) {
                     s = new Intent(ctx, ChatMemberSelectionActivity.class);
                } else {
                     s = new Intent(ctx, ChatMessageListActivity.class);
                }
                s.putExtra("chat", chat);
                s.putExtra("project",project);
                startActivity(s);
            }
        });
    }
    public void setFields() {
        listView = (ListView)view.findViewById(R.id.NEWCHAT_list);
        txtChatName = (EditText) view.findViewById(R.id.NEWCHAT_chatName);
        txtProject = (TextView) view.findViewById(R.id.NEWCHAT_projectName);
        title = (TextView) view.findViewById(R.id.NEWCHAT_title);
        editorView = view.findViewById(R.id.NEWCHAT_editor);
        progressBar = (ProgressBar)view.findViewById(R.id.progressBar);
        iconClose = (ImageView)view.findViewById(R.id.COLORS_close);
        fab = view.findViewById(R.id.FAB);
        editorView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        txtColor1 = (TextView) view.findViewById(R.id.COLORS_1);
        txtColor2 = (TextView) view.findViewById(R.id.COLORS_2);
        txtColor3 = (TextView) view.findViewById(R.id.COLORS_3);
        txtColor4 = (TextView) view.findViewById(R.id.COLORS_4);
        txtColor5 = (TextView) view.findViewById(R.id.COLORS_5);
        txtColor6 = (TextView) view.findViewById(R.id.COLORS_6);

        iconClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(iconClose,300,new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        Util.collapse(editorView,500,null);
                    }
                });

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(fab, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        sendChatData();
                    }
                });
            }
        });
        txtProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtProject, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        showProjectPopup();
                    }
                });
            }
        });
        txtColor1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtColor1, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        setColorSize(1);
                        selectedColor = 1;

                    }
                });
            }
        });
        txtColor2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtColor2, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        setColorSize(2);
                        selectedColor = 2;

                    }
                });
            }
        });
        txtColor3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtColor3, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        setColorSize(3);
                        selectedColor = 3;

                    }
                });
            }
        });
        txtColor4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtColor4, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        setColorSize(4);
                        selectedColor = 4;

                    }
                });
            }
        });
        txtColor5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtColor5, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        setColorSize(5);
                        selectedColor = 5;

                    }
                });
            }
        });
        txtColor6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtColor1, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        setColorSize(6);
                        selectedColor = 6;

                    }
                });
            }
        });
        setColorSize(0);
    }


    static final int SMALL_HEIGHT = 64, SMALL_WIDTH = 64, LARGE_HEIGHT = 96, LARGE_WIDTH = 96;

    private void showProjectPopup() {

        Util.showPopupBasicWithHeroImage(ctx, getActivity(), projectStringList, title, "Select Project", new Util.UtilPopupListener() {
            @Override
            public void onItemSelected(int index) {
                project = projectList.get(index);
                txtProject.setText(project.getProjectName());
                Util.expand(editorView, 500, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        animateColors();
                    }
                });
            }
        });
    }

    private void sendChatData() {
        if (txtChatName.getText().toString().isEmpty()) {
            Util.showErrorToast(ctx, "Please enter Channel name");
            return;
        }
        if (project == null) {
            Util.showErrorToast(ctx,ctx.getString(R.string.select_project));
            return;
        }
        if (selectedColor == 0) {
            Util.showErrorToast(ctx,"Please select a colour");
            return;
        }
        RequestDTO w = new RequestDTO(RequestDTO.ADD_CHAT);
        chat = new ChatDTO();
        CompanyStaffDTO cs = SharedUtil.getCompanyStaff(ctx);
        CompanyStaffDTO csOut = new CompanyStaffDTO();
        csOut.setCompanyStaffID(cs.getCompanyStaffID());
        csOut.setFirstName(cs.getFirstName());
        csOut.setLastName(cs.getLastName());
        chat.setCompanyStaff(csOut);
        chat.setChatName(txtChatName.getText().toString());
        chat.setProjectID(project.getProjectID());
        chat.setAvatarNumber(selectedColor);

        w.setChat(chat);

        progressBar.setVisibility(View.VISIBLE);
        NetUtil.sendRequest(ctx,w,new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (response.getStatusCode() == 0) {
                            chat = response.getChat();
                            chatList = response.getChatList();
                            setList();
                            CacheUtil.cacheProjectChats(ctx, response, project.getProjectID(), null);
                            Intent s = new Intent(ctx, ChatMemberSelectionActivity.class);
                            s.putExtra("chat", chat);
                            s.putExtra("project",project);
                            startActivity(s);

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
                        Util.showErrorToast(ctx,message);
                    }
                });

            }

            @Override
            public void onWebSocketClose() {

            }
        });

    }

    private void getCachedProjectList() {
        CacheUtil.getCachedData(ctx, CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getCompany() != null) {
                    projectList = response.getCompany().getProjectList();
                    projectStringList = new ArrayList<String>();
                    for (ProjectDTO s : projectList) {
                        projectStringList.add(s.getProjectName());
                        if (projectID != null) {
                            if (projectID.intValue() == s.getProjectID()) {
                                project = s;
                                txtProject.setText(project.getProjectName());
                                Util.flashOnce(txtProject,300,null);
                            }
                        }
                    }
                }

                getCachedChatList();
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ChannelFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ChannelFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void animateHeroHeight() {

    }
    static final int DURATION = 50;

    private void setColorSize(int index) {
        LinearLayout.LayoutParams x1 = (LinearLayout.LayoutParams) txtColor1.getLayoutParams();
        x1.height = SMALL_HEIGHT;
        x1.width = SMALL_WIDTH;
        txtColor1.setLayoutParams(x1);

        LinearLayout.LayoutParams x2 = (LinearLayout.LayoutParams) txtColor2.getLayoutParams();
        x2.height = SMALL_HEIGHT;
        x2.width = SMALL_WIDTH;
        txtColor2.setLayoutParams(x2);

        LinearLayout.LayoutParams x3 = (LinearLayout.LayoutParams) txtColor3.getLayoutParams();
        x3.height = SMALL_HEIGHT;
        x3.width = SMALL_WIDTH;
        txtColor3.setLayoutParams(x3);

        LinearLayout.LayoutParams x4 = (LinearLayout.LayoutParams) txtColor4.getLayoutParams();
        x4.height = SMALL_HEIGHT;
        x4.width = SMALL_WIDTH;
        txtColor4.setLayoutParams(x4);

        LinearLayout.LayoutParams x5 = (LinearLayout.LayoutParams) txtColor5.getLayoutParams();
        x5.height = SMALL_HEIGHT;
        x5.width = SMALL_WIDTH;
        txtColor5.setLayoutParams(x5);

        LinearLayout.LayoutParams x6 = (LinearLayout.LayoutParams) txtColor6.getLayoutParams();
        x6.height = SMALL_HEIGHT;
        x6.width = SMALL_WIDTH;
        txtColor6.setLayoutParams(x6);


        switch (index) {
            case 1:
                LinearLayout.LayoutParams p1 = (LinearLayout.LayoutParams) txtColor1.getLayoutParams();
                p1.height = LARGE_HEIGHT;
                p1.width = LARGE_WIDTH;
                txtColor1.setLayoutParams(p1);
                break;
            case 2:
                LinearLayout.LayoutParams p2 = (LinearLayout.LayoutParams) txtColor2.getLayoutParams();
                p2.height = LARGE_HEIGHT;
                p2.width = LARGE_WIDTH;
                txtColor2.setLayoutParams(p2);
                break;
            case 3:
                LinearLayout.LayoutParams p3 = (LinearLayout.LayoutParams) txtColor3.getLayoutParams();
                p3.height = LARGE_HEIGHT;
                p3.width = LARGE_WIDTH;
                txtColor3.setLayoutParams(p3);
                break;
            case 4:
                LinearLayout.LayoutParams p4 = (LinearLayout.LayoutParams) txtColor4.getLayoutParams();
                p4.height = LARGE_HEIGHT;
                p4.width = LARGE_WIDTH;
                txtColor4.setLayoutParams(p4);
                break;
            case 5:
                LinearLayout.LayoutParams p5 = (LinearLayout.LayoutParams) txtColor5.getLayoutParams();
                p5.height = LARGE_HEIGHT;
                p5.width = LARGE_WIDTH;
                txtColor5.setLayoutParams(p5);
                break;
            case 6:
                LinearLayout.LayoutParams p6 = (LinearLayout.LayoutParams) txtColor6.getLayoutParams();
                p6.height = LARGE_HEIGHT;
                p6.width = LARGE_WIDTH;
                txtColor6.setLayoutParams(p6);
                break;
        }
        animateColors();
    }

    public void animateColors() {
        Util.flashOnce(txtColor1,DURATION,new Util.UtilAnimationListener() {
            @Override
            public void onAnimationEnded() {
                Util.flashOnce(txtColor2,DURATION,new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        Util.flashOnce(txtColor3,DURATION,new Util.UtilAnimationListener() {
                            @Override
                            public void onAnimationEnded() {
                                Util.flashOnce(txtColor4,DURATION,new Util.UtilAnimationListener() {
                                    @Override
                                    public void onAnimationEnded() {
                                        Util.flashOnce(txtColor5, DURATION, new Util.UtilAnimationListener() {
                                            @Override
                                            public void onAnimationEnded() {
                                                Util.flashOnce(txtColor6,DURATION,null);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public interface ChannelFragmentListener {
        public void onChannelSelected(ChatDTO chat);
    }

}
