package com.com.boha.monitor.library.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.dto.ChatMessageDTO;
import com.com.boha.monitor.library.dto.CompanyStaffDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.fragments.ChatFragment;
import com.com.boha.monitor.library.util.CacheUtil;

import java.util.List;

public class ChatActivity extends ActionBarActivity {

    ChatFragment chatFragment;
    ChatMessageDTO chatMessage;
    List<CompanyStaffDTO> companyStaffList;
    Context ctx;
    static final String LOG = ChatActivity.class.getSimpleName();
    boolean isStartedFromNotification;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(LOG, "### onCreate");
        setContentView(R.layout.activity_chat);
        ctx = getApplicationContext();

        Log.d(LOG,getIntent().toString());

        chatFragment = (ChatFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
        chatMessage = (ChatMessageDTO)getIntent().getSerializableExtra("message");
        isStartedFromNotification = getIntent().getBooleanExtra("isStartedFromNotification", false);

        if (chatMessage != null) {
            Log.w(LOG,"### a chatMessage from gcm: " + chatMessage.getMessage());
            chatFragment.setChatID(chatMessage.getChatID());
        }

        if (isStartedFromNotification) {
            refresh();
        } else {
            getCachedCompanyData();
        }
    }


    public void refresh() {
        CacheUtil.getCachedData(ctx,CacheUtil.CACHE_CHAT,new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                chatFragment.setChatMessageList(response.getChatMessageList());
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }
    private void getCachedCompanyData() {
        CacheUtil.getCachedData(ctx, CacheUtil.CACHE_DATA,new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response.getCompany() != null) {
                    companyStaffList = response.getCompany().getCompanyStaffList();
                    chatFragment.setCompanyStaffList(companyStaffList);
                    CacheUtil.getCachedData(ctx,CacheUtil.CACHE_CHAT,new CacheUtil.CacheUtilListener() {
                        @Override
                        public void onFileDataDeserialized(ResponseDTO response) {
                            chatFragment.setChatMessageList(response.getChatMessageList());
                        }

                        @Override
                        public void onDataCached() {

                        }

                        @Override
                        public void onError() {

                        }
                    });
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
    public boolean onOptionsItemSelected(MenuItem item) {
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
        overridePendingTransition(com.boha.monitor.library.R.anim.slide_in_left, com.boha.monitor.library.R.anim.slide_out_right);
        super.onPause();
    }


}
