package com.boha.monitor.operations;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.boha.monitor.library.dto.CompanyDTO;
import com.boha.monitor.library.dto.CompanyStaffDTO;
import com.boha.monitor.library.dto.GcmDeviceDTO;
import com.boha.monitor.library.dto.transfer.RequestDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.ErrorUtil;
import com.boha.monitor.library.util.GCMUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.acra.ACRA;

import java.util.ArrayList;
import java.util.List;

import static com.boha.monitor.library.util.Util.showErrorToast;


public class RegistrationActivity extends AppCompatActivity {

    Context ctx;
    Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        ctx = getApplicationContext();
        activity = this;

        checkVirgin();
        setFields();
        getEmail();
    }

    private void checkVirgin() {

        CompanyStaffDTO dto = SharedUtil.getCompanyStaff(ctx);
        if (dto != null) {
            Log.i(LOG, "++++++++ Not a virgin anymore ...checking GCM registration....");
            String id = SharedUtil.getRegistrationId(getApplicationContext());
            if (id == null) {
                registerGCMDevice();
            }

            Intent intent = new Intent(ctx, DrawerActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        registerGCMDevice();
    }

    private void registerGCMDevice() {
        boolean ok = checkPlayServices();

        if (ok) {
            Log.e(LOG, "############# Starting Google Cloud Messaging registration");
            GCMUtil.startGCMRegistration(getApplicationContext(), new GCMUtil.GCMUtilListener() {
                @Override
                public void onDeviceRegistered(String id) {
                    Log.e(LOG, "############# GCM - we cool, cool.....: " + id);
                    gcmDevice = new GcmDeviceDTO();
                    gcmDevice.setManufacturer(Build.MANUFACTURER);
                    gcmDevice.setModel(Build.MODEL);
                    gcmDevice.setSerialNumber(Build.SERIAL);
                    gcmDevice.setProduct(Build.PRODUCT);
                    gcmDevice.setAndroidVersion(Build.VERSION.RELEASE);
                    gcmDevice.setRegistrationID(id);

                }

                @Override
                public void onGCMError() {
                    Log.e(LOG, "############# onGCMError --- we got GCM problems");

                }
            });
        }
    }

    public boolean checkPlayServices() {
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

    private void sendRegistration() {
        if (eCompany.getText().toString().isEmpty()) {
            showErrorToast(ctx, ctx.getResources().getString(R.string.enter_comp_name));
            return;
        }
        if (eFirstName.getText().toString().isEmpty()) {
            showErrorToast(ctx, ctx.getResources().getString(R.string.enter_firstname));
            return;
        }
        if (eLastName.getText().toString().isEmpty()) {
            showErrorToast(ctx, ctx.getResources().getString(R.string.enter_lastname));
            return;
        }

        if (ePin.getText().toString().isEmpty()) {
            showErrorToast(ctx, ctx.getResources().getString(R.string.enter_password));
            return;
        }

        if (email == null) {
            showErrorToast(ctx, ctx.getResources().getString(R.string.select_email));
            return;
        }
        CompanyStaffDTO a = new CompanyStaffDTO();

        a.setEmail(email);
        a.setFirstName(eFirstName.getText().toString());
        a.setLastName(eLastName.getText().toString());
        a.setPin(ePin.getText().toString());
        a.setGcmDevice(gcmDevice);

        final CompanyDTO g = new CompanyDTO();
        g.setCompanyName(eCompany.getText().toString());


        RequestDTO r = new RequestDTO();
        r.setRequestType(RequestDTO.REGISTER_COMPANY);
        r.setCompany(g);
        r.setCompanyStaff(a);

        progressBar.setVisibility(View.VISIBLE);
        NetUtil.sendRequest(ctx, r, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (!ErrorUtil.checkServerError(ctx, response)) {
                            return;
                        }

                        SharedUtil.saveCompanyStaff(ctx, response.getCompanyStaff());
                        SharedUtil.saveCompany(ctx, response.getCompany());
                        if (response.getCompany() != null) {
                            //TODO - restore for production
                            try {
                                ACRA.getErrorReporter().putCustomData("companyStaffID", "" + response.getCompanyStaff().getCompanyStaffID());
                            } catch (Exception e) {
                                //ignore
                            }
                        }

                        CacheUtil.cacheData(ctx, response, CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {

                            }

                            @Override
                            public void onDataCached() {
                                Intent intent = new Intent(ctx, DrawerActivity.class);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onError() {

                            }
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

    private void sendSignIn() {

        if (ePin.getText().toString().isEmpty()) {
            showErrorToast(ctx, ctx.getResources().getString(R.string.enter_password));
            return;
        }
        if (email == null) {
            showErrorToast(ctx, ctx.getResources().getString(R.string.select_email));
            return;
        }
        RequestDTO r = new RequestDTO();
        r.setRequestType(RequestDTO.LOGIN);
        r.setEmail(email);
        r.setPin(ePin.getText().toString());
        r.setGcmDevice(gcmDevice);

        progressBar.setVisibility(View.VISIBLE);
        NetUtil.sendRequest(ctx, r, new NetUtil.NetUtilListener() {
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
                        SharedUtil.saveCompanyStaff(ctx, response.getCompanyStaff());
                        Intent intent = new Intent(ctx, DrawerActivity.class);
                        startActivity(intent);

                        CacheUtil.cacheData(ctx, response, CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {

                            }

                            @Override
                            public void onDataCached() {

                            }

                            @Override
                            public void onError() {

                            }
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

    boolean isRegistration;
    GcmDeviceDTO gcmDevice;
    ProgressBar progressBar;


    private void setFields() {
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        spinnerCountry = (Spinner) findViewById(R.id.EP_countrySpinner);
        eFirstName = (EditText) findViewById(R.id.EP_firstName);
        eLastName = (EditText) findViewById(R.id.EP_lastName);
        ePin = (EditText) findViewById(R.id.EP_password);
        txtEmail = (TextView) findViewById(R.id.EP_txtEmail);
        eCompany = (EditText) findViewById(R.id.EP_groupName);
        mainEPLayout = findViewById(R.id.REG_ediLayout);
        mainRegLayout = findViewById(R.id.REG_mainLayout);
        mainEPLayout.setVisibility(View.GONE);
        final TextView txtHdr = (TextView) findViewById(R.id.EP_header);
        btnStartSignIn = (Button) findViewById(R.id.REG_btnExisting);
        btnStartNewGroup = (Button) findViewById(R.id.REG_btnNewGroup);
        txtEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtEmail, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        Util.showPopupBasicWithHeroImage(ctx, activity, emailAccountList,
                                eCompany,
                                ctx.getString(R.string.select_acct), new Util.UtilPopupListener() {
                                    @Override
                                    public void onItemSelected(int index) {
                                        txtEmail.setText(emailAccountList.get(index));
                                        email = emailAccountList.get(index);
                                    }
                                });
                    }
                });
            }
        });
        btnStartNewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.flashOnce(btnStartNewGroup, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        mainRegLayout.setVisibility(View.GONE);
                        mainEPLayout.setVisibility(View.VISIBLE);
                        isRegistration = true;
                        btnSave.setText(ctx.getResources().getString(R.string.register));
                        txtHdr.setText(ctx.getResources().getString(R.string.company_reg));
                    }
                });

            }
        });
        btnStartSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.flashOnce(btnStartSignIn, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        mainRegLayout.setVisibility(View.GONE);
                        mainEPLayout.setVisibility(View.VISIBLE);
                        eCompany.setVisibility(View.GONE);
                        eFirstName.setVisibility(View.GONE);
                        eLastName.setVisibility(View.GONE);
                        spinnerCountry.setVisibility(View.GONE);
                        isRegistration = false;
                        btnSave.setText(ctx.getResources().getString(R.string.sign_in));
                        txtHdr.setText(ctx.getResources().getString(R.string.company_signin));
                    }
                });

            }
        });

        btnSave = (Button) findViewById(R.id.EP_btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.flashOnce(btnSave, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        if (isRegistration) {
                            sendRegistration();
                        } else {
                            sendSignIn();
                        }
                    }
                });

            }
        });
        final Button btnCan = (Button) findViewById(R.id.EP_btnCancel);
        btnCan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.flashOnce(btnCan, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        mainRegLayout.setVisibility(View.VISIBLE);
                        mainEPLayout.setVisibility(View.GONE);
                        eCompany.setVisibility(View.VISIBLE);
                        eFirstName.setVisibility(View.VISIBLE);
                        eLastName.setVisibility(View.VISIBLE);
                        spinnerCountry.setVisibility(View.VISIBLE);
                    }
                });

            }
        });
    }


    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(LOG, "onResume ...nothing to be done");
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        Log.d(LOG, "--- onSaveInstanceState ...");
        super.onSaveInstanceState(b);
    }

    Menu mMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.registration, menu);
        mMenu = menu;


        return true;
    }

    public void setRefreshActionButtonState(final boolean refreshing) {
        if (mMenu != null) {
            final MenuItem refreshItem = mMenu.findItem(R.id.menu_help);
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
    public boolean onOptionsItemSelected( MenuItem item) {

        switch (item.getItemId()) {

//            case com.boha.monitor.library.R.id.menu_help:
//                showToast(ctx, "Under Construction");
//                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void onStop() {

        super.onStop();
    }

    List<String> emailAccountList;
    public void getEmail() {
        AccountManager am = AccountManager.get(getApplicationContext());
        Account[] accts = am.getAccounts();
        if (accts.length == 0) {
            showErrorToast(ctx, "No Accounts found. Please create one and try again");
            finish();
            return;
        }

        emailAccountList = new ArrayList<String>();
        if (accts != null) {
            for (int i = 0; i < accts.length; i++) {
                emailAccountList.add(accts[i].name);
            }
        }
    }

    View mainRegLayout, mainEPLayout;
    Button btnStartSignIn, btnStartNewGroup, btnSave;
    EditText eFirstName, eLastName, ePin, eCompany;
    TextView txtEmail;
    Spinner spinnerCountry;
    static final String LOG = "RegistrationActivity";

    String email;


}
