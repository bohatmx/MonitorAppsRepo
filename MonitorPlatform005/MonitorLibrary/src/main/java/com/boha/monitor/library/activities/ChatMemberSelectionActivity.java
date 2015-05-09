package com.boha.monitor.library.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.ChatDTO;
import com.boha.monitor.library.dto.ChatMessageDTO;
import com.boha.monitor.library.dto.CompanyStaffDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.fragments.ChatMemberSelectionFragment;
import com.boha.monitor.library.util.CacheUtil;



import java.util.List;

public class ChatMemberSelectionActivity extends AppCompatActivity {

    ChatMemberSelectionFragment chatMemberSelectionFragment;
    ChatMessageDTO chatMessage;
    List<CompanyStaffDTO> companyStaffList;
    Context ctx;
    ChatDTO chat;
    ProjectDTO project;
    static final String LOG = ChatMemberSelectionActivity.class.getSimpleName();
    boolean isStartedFromNotification;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(LOG, "### onCreate");
        setContentView(R.layout.activity_chat);
        ctx = getApplicationContext();
        chat = (ChatDTO)getIntent().getSerializableExtra("chat");
        project = (ProjectDTO)getIntent().getSerializableExtra("project");

        chatMemberSelectionFragment = (ChatMemberSelectionFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
        chatMemberSelectionFragment.setProject(project);
        chatMemberSelectionFragment.setChat(chat);

        getCachedCompanyData();

    }


    public void refresh() {

    }
    private void getCachedCompanyData() {
        CacheUtil.getCachedData(ctx, CacheUtil.CACHE_DATA,new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized( ResponseDTO response) {
                if (response.getCompany() != null) {
                    companyStaffList = response.getCompany().getCompanyStaffList();
                    chatMemberSelectionFragment.setCompanyStaffList(companyStaffList);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
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

    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
    }


}
