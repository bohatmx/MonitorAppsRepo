package com.boha.monitor.staffapp.activities;

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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.dto.GcmDeviceDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.GCMUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.staffapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.acra.ACRA;

import java.util.ArrayList;

import static com.boha.monitor.library.util.Util.showErrorToast;
import static com.boha.monitor.library.util.Util.showToast;

/**
 * This class manages the Staff sign-in process. Staff have to be
 * pre-registered and must exist on the backend database.
 * The process includes registering the device to Google Cloud Messaging as well
 * as collecting the user's email address and supplied PIN.
 */
public class SignInActivity extends AppCompatActivity {

    TextView txtApp, txtEmail, label;
    EditText ePin, editEmail;
    Button btnSave;
    Context ctx;
    String email;
    ImageView banner;

    GcmDeviceDTO gcmDevice;
    static final String LOG = SignInActivity.class.getSimpleName();
    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        ctx = getApplicationContext();
        activity = this;
        banner = (ImageView) findViewById(R.id.SI_banner);
        setFields();
        banner.setImageDrawable(Util.getRandomBackgroundImage(ctx));

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG, "#################### onResume");
        getEmail();
        checkVirgin();
    }

    /**
     * Check whether this user is already signed in. If the user is
     * signed in, the method passes control to
     * @see StaffMainActivity
     */
    private void checkVirgin() {

        StaffDTO dto = SharedUtil.getCompanyStaff(ctx);
        if (dto != null) {
            Log.i(LOG, "++++++++ Not a virgin anymore ...checking GCM registration: " + dto.getFullName());
            String id = SharedUtil.getRegistrationId(getApplicationContext());
            if (id == null) {
                registerGCMDevice();
            }

            Intent intent = new Intent(ctx, StaffMainActivity.class);
            startActivity(intent);
            //
            finish();
            return;
        }
        Log.i(LOG, "checkVirgin: waiting for sign in ...");
        registerGCMDevice();
    }

    /**
     * Register device to Google Cloud Messaging
     */
    public void registerGCMDevice() {

        Snackbar.make(btnSave, "Just a second, checking services ...", Snackbar.LENGTH_LONG)
                .setAction("CLOSE", null)
                .show();
        boolean ok = checkPlayServices();
        setRefreshActionButtonState(true);
        if (ok) {
            Log.e(LOG, "############# Starting Google Cloud Messaging registration");
            GCMUtil.startGCMRegistration(getApplicationContext(), new GCMUtil.GCMUtilListener() {
                @Override
                public void onDeviceRegistered(String id) {
                    Log.i(LOG, "############# GCM - we cool, GcmDeviceDTO waiting to be sent with signin .....: " + id);
                    setRefreshActionButtonState(false);
                    gcmDevice = new GcmDeviceDTO();
                    gcmDevice.setManufacturer(Build.MANUFACTURER);
                    gcmDevice.setModel(Build.MODEL);
                    gcmDevice.setSerialNumber(Build.SERIAL);
                    gcmDevice.setAndroidVersion(Build.VERSION.RELEASE);
                    gcmDevice.setRegistrationID(id);
                    gcmDevice.setProduct(Build.PRODUCT);
                    gcmDevice.setApp(ctx.getPackageName());
                    btnSave.setEnabled(true);


                }

                @Override
                public void onGCMError() {
                    Log.e(LOG, "############# onGCMError --- we got GCM problems");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setRefreshActionButtonState(false);
                        }
                    });


                }
            });
        }
    }

    /**
     * Send staff email and PIN to the backend.
     * The device details are sent with the sign in  request
     * @see GcmDeviceDTO
     * On successful return, cache the data on the device
     */
    public void sendSignIn() {
        if (ePin.getText().toString().isEmpty()) {
            showErrorToast(ctx, "Enter PIN");
            return;
        }
        if (email == null) {
            if (editEmail.getText().toString().isEmpty()) {
                showErrorToast(ctx, getString(R.string.select_account));
                return;
            } else {
                email = editEmail.getText().toString();
            }
        }
        RequestDTO r = new RequestDTO();
        r.setRequestType(RequestDTO.LOGIN_STAFF);
        r.setEmail(email);
        r.setPin(ePin.getText().toString());
        r.setGcmDevice(gcmDevice);

        setRefreshActionButtonState(true);
        NetUtil.sendRequest(ctx, r, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setRefreshActionButtonState(false);
                        if (response.getStatusCode() > 0) {
                            btnSave.setEnabled(true);
                            showErrorToast(ctx, response.getMessage());
                            return;
                        }

                        SharedUtil.saveCompany(ctx, response.getCompany());
                        SharedUtil.saveCompanyStaff(ctx, response.getStaff());
                        SharedUtil.saveGCMDevice(ctx, response.getGcmDeviceList().get(0));
                        if (!response.getPhotoUploadList().isEmpty()) {
                            SharedUtil.savePhoto(ctx, response.getPhotoUploadList().get(0));
                        }
                        try {
                            ACRA.getErrorReporter().putCustomData("monitorID", ""
                                    + response.getMonitorList().get(0).getMonitorID());
                        } catch (Exception e) {//ignore}
                        }
                        CacheUtil.cacheStaffData(ctx, response, new CacheUtil.CacheUtilListener() {
                            @Override
                            public void onFileDataDeserialized(ResponseDTO response) {
                            }

                            @Override
                            public void onDataCached() {
                                Intent intent = new Intent(ctx, StaffMainActivity.class);
                                intent.putExtra("justSignedIn", true);
                                startActivity(intent);
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
                        setRefreshActionButtonState(false);
                        showErrorToast(ctx, message);
                        btnSave.setEnabled(true);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        });


    }

    public void setFields() {
        ePin = (EditText) findViewById(R.id.SI_pin);
        editEmail = (EditText) findViewById(R.id.SI_editEmail);
        txtEmail = (TextView) findViewById(R.id.SI_txtEmail);
        label = (TextView) findViewById(R.id.SI_welcome);
        txtApp = (TextView) findViewById(R.id.SI_app);
        btnSave = (Button) findViewById(R.id.btnRed);
        txtApp.setText("Company Staff");
        btnSave.setText("Sign In");
        btnSave.setEnabled(false);

        editEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                btnSave.setEnabled(true);
                return true;
            }
        });

        txtEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtEmail, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        Util.showPopupBasicWithHeroImage(ctx, activity, tarList,
                                label, getString(R.string.select_email),
                                new Util.UtilPopupListener() {
                                    @Override
                                    public void onItemSelected(int index) {
                                        if (index == 0) {
                                            email = null;
                                        } else {
                                            email = tarList.get(index);
                                            txtEmail.setText(email);
                                            btnSave.setEnabled(true);
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
                        btnSave.setEnabled(false);
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
            try {
                if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms")));
                    return false;
                } else {
                    Log.i(LOG, "This device is not supported.");
                    throw new UnsupportedOperationException("GooglePlayServicesUtil resultCode: " + resultCode);
                }
            } catch (Exception e) {
                Log.e(LOG,"GooglePlayServices may not be available, maybe on emulator");
                return false;
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
            showErrorToast(ctx, "No email accounts found");
            //finish();
            return;
        }
        if (accts != null) {
            tarList.add(ctx.getResources().getString(R.string.select_email));
            for (int i = 0; i < accts.length; i++) {
                tarList.add(accts[i].name);

            }
        }

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
