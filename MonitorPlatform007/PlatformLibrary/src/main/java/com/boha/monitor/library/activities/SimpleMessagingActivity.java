package com.boha.monitor.library.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;

import com.boha.monitor.library.dto.SimpleMessageDTO;
import com.boha.monitor.library.fragments.SimpleMessageFragment;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

public class SimpleMessagingActivity extends AppCompatActivity implements SimpleMessageFragment.SimpleMessageFragmentListener{

    SimpleMessageFragment simpleMessageFragment;
    SimpleMessageDTO simpleMessage;
    int themeDarkColor, themePrimaryColor;

    static final String LOG = "SimpleMessagingActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeChooser.setTheme(this);
        Resources.Theme theme = getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        themeDarkColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        themePrimaryColor = typedValue.data;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        simpleMessageFragment = (SimpleMessageFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
        simpleMessage = (SimpleMessageDTO)getIntent().getSerializableExtra("simpleMessage");
        if (simpleMessage != null) {
            Log.e(LOG,"GCM msg received: " + simpleMessage.getMessage());
        }
        Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),"Monitor Messaging",
                ContextCompat.getDrawable(getApplicationContext(),R.drawable.glasses48));
    }

    @Override
    public void setBusy(boolean busy) {

    }
}
