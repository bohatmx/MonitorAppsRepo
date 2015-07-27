package com.boha.monitor.library.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.dto.ChatDTO;
import com.boha.monitor.library.dto.ChatMessageDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.fragments.ChatMessageListFragment;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.platform.library.R;


public class ChatMessageListActivity extends AppCompatActivity {

    ChatMessageListFragment chatMessageListFragment;
    ProjectDTO project;
    ChatDTO chat;
    ChatMessageDTO chatMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message_list);
        project = (ProjectDTO)getIntent().getSerializableExtra("project");
        chatMessage = (ChatMessageDTO)getIntent().getSerializableExtra("message");
        chat = (ChatDTO)getIntent().getSerializableExtra("chat");

        chatMessageListFragment = (ChatMessageListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);

        if (chatMessage == null) {
            chatMessageListFragment.setChat(chat);
            chatMessageListFragment.setProject(project);
            return;
        }
        CacheUtil.getCachedData(getApplicationContext(), CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getCompany() != null) {
//                    for (ProjectDTO f : response.getCompany().getProjectList()) {
//                        if (chatMessage.getProjectID().intValue() == f.getProjectID().intValue()) {
//                            project = f;
//                            chatMessageListFragment.setChatID(chatMessage.getChatID());
//                            chatMessageListFragment.setProject(project);
//                            break;
//                        }
//                    }
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

    public void refreshMessages() {
        chatMessageListFragment.refreshMessages();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_chat_message_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
