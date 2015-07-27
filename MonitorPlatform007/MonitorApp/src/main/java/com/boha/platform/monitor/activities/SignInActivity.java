package com.boha.platform.monitor.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.boha.monitor.library.dto.GcmDeviceDTO;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.GCMUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.platform.monitor.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.acra.ACRA;

import java.util.ArrayList;

import static com.boha.monitor.library.util.Util.showErrorToast;
import static com.boha.monitor.library.util.Util.showToast;

public class SignInActivity extends AppCompatActivity {

    Spinner spinnerEmail;
    TextView txtApp, txtEmail;
    EditText ePin;
    Button btnSave;
    Context ctx;
    String email;
    ImageView banner;
    ProgressBar progressBar;

    GcmDeviceDTO gcmDevice;
    static final String LOG = SignInActivity.class.getSimpleName();
    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        ctx = getApplicationContext();
        activity = this;
        banner = (ImageView)findViewById(R.id.SI_banner);
        setFields();
        getEmail();
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG,"#################### onResume");
        checkVirgin();
    }
    private void checkVirgin() {

        MonitorDTO dto = SharedUtil.getMonitor(ctx);
        if (dto != null) {
            Log.i(LOG, "++++++++ Not a virgin anymore ...checking GCM registration....");
            String id = SharedUtil.getRegistrationId(getApplicationContext());
            if (id == null) {
                registerGCMDevice();
            }

            Intent intent = new Intent(ctx, MainDrawerActivity.class);
            startActivity(intent);
            //
            finish();
            return;
        }
        registerGCMDevice();
    }

    private void registerGCMDevice() {
        boolean ok = checkPlayServices();
        Snackbar.make(btnSave, "Just a second, checking services ...",Snackbar.LENGTH_LONG)
                .setAction("CLOSE", null)
                .show();
        if (ok) {
            Log.e(LOG, "############# Starting Google Cloud Messaging registration");
            GCMUtil.startGCMRegistration(getApplicationContext(), new GCMUtil.GCMUtilListener() {
                @Override
                public void onDeviceRegistered(String id) {
                    Log.i(LOG, "############# GCM - we cool, GcmDeviceDTO waiting to be sent with signin .....: " + id);
                    gcmDevice = new GcmDeviceDTO();
                    gcmDevice.setManufacturer(Build.MANUFACTURER);
                    gcmDevice.setModel(Build.MODEL);
                    gcmDevice.setSerialNumber(Build.SERIAL);
//                    gcmDevice.setProduct(Build.PRODUCT);
                    gcmDevice.setAndroidVersion(Build.VERSION.RELEASE);
                    gcmDevice.setRegistrationID(id);
                    btnSave.setEnabled(true);


                }

                @Override
                public void onGCMError() {
                    Log.e(LOG, "############# onGCMError --- we got GCM problems");

                }
            });
        }
    }
    private void sendSignIn() {
        if (ePin.getText().toString().isEmpty()) {
            showErrorToast(ctx, "Enter PIN");
            return;
        }
        if (email == null) {
            showErrorToast(ctx, getString(R.string.select_account));
            return;
        }
        RequestDTO r = new RequestDTO();
        r.setRequestType(RequestDTO.LOGIN_MONITOR);
        r.setEmail(email);
        r.setPin(ePin.getText().toString());
        r.setGcmDevice(gcmDevice);

        progressBar.setVisibility(View.VISIBLE);
        NetUtil.sendRequest(ctx,r,new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (response.getStatusCode() > 0) {
                            showErrorToast(ctx, response.getMessage());
                            return;
                        }

                        SharedUtil.saveCompany(ctx, response.getCompany());
                        SharedUtil.saveMonitor(ctx, response.getMonitorList().get(0));
                        Intent intent = new Intent(ctx, MainDrawerActivity.class);
                        startActivity(intent);

                        try {
                            ACRA.getErrorReporter().putCustomData("monitorID", ""
                                    + response.getMonitorList().get(0).getMonitorID());
                        } catch (Exception e) {//ignore}
                        }
                        CacheUtil.cacheData(ctx, response, CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {}

                            @Override
                            public void onDataCached() {}

                            @Override
                            public void onError() {}
                        });
                    }
                });
            }

            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        showErrorToast(ctx, message);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });



    }
    private void setFields() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ePin = (EditText) findViewById(R.id.SI_pin);
        txtEmail = (TextView) findViewById(R.id.SI_txtEmail);
        txtApp = (TextView)findViewById(R.id.SI_app);
        btnSave = (Button)findViewById(R.id.btnRed);
        txtApp.setText(R.string.monitor);
        btnSave.setText("Sign In");
        btnSave.setEnabled(false);
        btnSave.setTextColor(getResources().getColor(R.color.white));

        txtEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent w = new Intent(getApplicationContext(), TestActivity.class);
//                startActivity(w);
                Util.flashOnce(txtEmail,300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        Util.showPopupBasicWithHeroImage(ctx, activity, tarList,
                                banner, getString(R.string.select_email),
                                new Util.UtilPopupListener() {
                            @Override
                            public void onItemSelected(int index) {
                                if (index == 0) {
                                    email = null;
                                } else {
                                    email = tarList.get(index);
                                    txtEmail.setText(email);
                                }
                            }
                        });
                    }
                });
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.flashOnce(btnSave, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        sendSignIn();
                    }
                });
            }
        });
    }


    private boolean checkPlayServices() {
        Log.w(LOG, "checking GooglePlayServices .................");
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(ctx);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                // GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                //         PLAY_SERVICES_RESOLUTION_REQUEST).show();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms")));
                return false;
            } else {
                Log.i(LOG, "This device is not supported.");
                throw new UnsupportedOperationException("GooglePlayServicesUtil resultCode: " + resultCode);
            }
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.signin, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_help) {
            showToast(ctx, getString(R.string.under_cons));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void getEmail() {
        AccountManager am = AccountManager.get(getApplicationContext());
        Account[] accts = am.getAccounts();
        if (accts.length == 0) {
            showErrorToast(ctx, getString(R.string.no_accounts));
            finish();
            return;
        }
        if (accts != null) {
            tarList.add(ctx.getResources().getString(R.string.select_email));
            for (int i = 0; i < accts.length; i++) {
                tarList.add(accts[i].name);

            }
            //setSpinner();
        }

    }
    private void setSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx,R.layout.xxsimple_spinner_item, tarList);
        spinnerEmail.setAdapter(adapter);
        spinnerEmail.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    email = null;
                    return;
                }
                email = tarList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    ArrayList<String> tarList = new ArrayList<String>();
    Menu mMenu;
    public void setRefreshActionButtonState(final boolean refreshing) {
        if (mMenu != null) {
            final MenuItem refreshItem = mMenu.findItem(R.id.action_help);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.action_bar_progess);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }
    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
    }

}
