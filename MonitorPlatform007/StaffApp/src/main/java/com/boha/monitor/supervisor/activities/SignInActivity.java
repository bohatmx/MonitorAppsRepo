package com.boha.monitor.supervisor.activities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.activities.ThemeSelectorActivity;
import com.boha.monitor.library.dto.GcmDeviceDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.util.GCMUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.supervisor.R;
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
    boolean gcmOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        ctx = getApplicationContext();
        activity = this;
        banner = (ImageView) findViewById(R.id.SI_banner);
        setFields();
        banner.setImageDrawable(Util.getRandomBackgroundImage(ctx));

        MonApp app = (MonApp) getApplication();
        app.getSnappyDB();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG, "#################### onResume");
       // checkPermissions();
        checkVirgin();
    }
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_ACCESS_FINE_LOCATION);

            }

        }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.GET_ACCOUNTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        MY_PERMISSIONS_GET_ACCOUNTS);

            }
        } else {
            getEmail();
        }
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);

            }
        }
    }
    static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 83;
    static final int MY_PERMISSIONS_GET_ACCOUNTS = 85;
    static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 87;
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.w(LOG,"ACCESS_FINE_LOCATION permission granted");

                } else {
                    Log.e(LOG,"ACCESS_FINE_LOCATION permission denied");

                }
                return;
            }
            case MY_PERMISSIONS_GET_ACCOUNTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.w(LOG,"GET_ACCOUNTS permission granted");
                    getEmail();

                } else {
                    Log.e(LOG,"GET_ACCOUNTS permission denied");

                }
                return;
            }
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.w(LOG,"WRITE_EXTERNAL_STORAGE permission granted");
                    registerGCMDevice();

                } else {
                    Log.e(LOG,"WRITE_EXTERNAL_STORAGE permission denied");

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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
                gcmOnly = true;
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

    private void buildDevice() {
        gcmDevice = new GcmDeviceDTO();
        gcmDevice.setManufacturer(Build.MANUFACTURER);
        gcmDevice.setModel(Build.MODEL);
        gcmDevice.setSerialNumber(Build.SERIAL);
        gcmDevice.setAndroidVersion(Build.VERSION.RELEASE);
        gcmDevice.setProduct(Build.PRODUCT);
        gcmDevice.setApp(ctx.getPackageName());

    }
    /**
     * Register device to Google Cloud Messaging
     */
    public void registerGCMDevice() {
        buildDevice();
        Snackbar.make(btnSave, "Just a second, checking services ...", Snackbar.LENGTH_LONG)
                .setAction("CLOSE", null)
                .show();
        boolean ok = checkPlayServices();
        if (ok) {
            Log.e(LOG, "############# Starting Google Cloud Messaging registration");
            GCMUtil.startGCMRegistration(getApplicationContext(), new GCMUtil.GCMUtilListener() {
                @Override
                public void onDeviceRegistered(String id) {
                    Log.i(LOG, "############# GCM - we cool, GcmDeviceDTO waiting to be sent with signin .....: " + id);
                    gcmDevice.setRegistrationID(id);
                    SharedUtil.saveGCMDevice(ctx, gcmDevice);
                    if (gcmOnly) {
                        RequestDTO w = new RequestDTO(RequestDTO.UPDATE_STAFF_DEVICE);
                        StaffDTO staff = SharedUtil.getCompanyStaff(ctx);
                        gcmDevice.setStaffID(staff.getStaffID());
                        w.setGcmDevice(gcmDevice);
                        w.setZipResponse(false);
                        //update staff device on server
                        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
                            @Override
                            public void onResponse(ResponseDTO response) {
                                gcmOnly = false;
                                if (response.getGcmDeviceList() != null) {
                                    if (!response.getGcmDeviceList().isEmpty()) {
                                        SharedUtil.saveGCMDevice(ctx,response.getGcmDeviceList().get(0));
                                    }
                                }
                            }

                            @Override
                            public void onError(String message) {

                            }

                        });

                    }


                }

                @Override
                public void onGCMError() {
                    Log.e(LOG, "############# onGCMError --- we got GCM problems");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setBusyIndicator(false);
                        }
                    });


                }
            });
        }
    }
    Snackbar bar;
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
        gcmDevice = SharedUtil.getGCMDevice(ctx);
        if (gcmDevice == null) {
            buildDevice();
        }
        if (gcmDevice.getRegistrationID() != null) {
            r.setGcmDevice(gcmDevice);
        }


        Snackbar.make(btnSave,"Signing in, data is being prepared " +
                "and may take a couple of minutes.", Snackbar.LENGTH_INDEFINITE).show();

        setBusyIndicator(true);
        NetUtil.sendRequest(ctx, r, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBusyIndicator(false);
                        if (response.getStatusCode() > 0) {
                            btnSave.setEnabled(true);
                            showErrorToast(ctx, response.getMessage());
                            return;
                        }

                        SharedUtil.saveCompany(ctx, response.getCompany());
                        SharedUtil.saveCompanyStaff(ctx, response.getStaff());
                        if (response.getGcmDeviceList() != null) {
                            if (!response.getGcmDeviceList().isEmpty()) {
                                Log.d(LOG, "... about to save GcmDeviceDTO to SharedUtil, id: " + response.getGcmDeviceList().get(0).getGcmDeviceID());
                                SharedUtil.saveGCMDevice(ctx, response.getGcmDeviceList().get(0));
                            }
                        }
                        if (!response.getPhotoUploadList().isEmpty()) {
                            SharedUtil.savePhoto(ctx, response.getPhotoUploadList().get(0));
                        }
                        try {
                            ACRA.getErrorReporter().putCustomData("staffID", ""
                                    + response.getStaff().getStaffID());
                        } catch (Exception e) {//ignore}
                        }
                        startSnappy(response);
                        Intent m = new Intent(ctx, ThemeSelectorActivity.class);
                        startActivity(m);

                    }
                });
            }

            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setBusyIndicator(false);
                        showErrorToast(ctx, message);
                        btnSave.setEnabled(true);
                    }
                });
            }


        });


    }
    private void startSnappy(final ResponseDTO response) {

        Log.e(LOG, "################### START cachingService for: status types, projects, staff and monitors......");

        final MonApp ctx = (MonApp) getApplication();
        Snappy.cacheProjects(ctx, response, new Snappy.SnappyWriteListener() {
            @Override
            public void onDataWritten() {
                Snappy.cacheLookups(ctx, response, new Snappy.SnappyWriteListener() {
                    @Override
                    public void onDataWritten() {
                        Log.e(LOG, "Yeaaaah!! Overall Data cached to SnappyDB");
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }

            @Override
            public void onError(String message) {

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
        txtApp.setText("Monitor Supervisor");
        btnSave.setText("Sign In");
        btnSave.setEnabled(true);

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
        Log.d(LOG,"getEmail accounts...............");
//        if (ContextCompat.checkSelfPermission(this,Manifest.permission.GET_ACCOUNTS)
//                != PackageManager.PERMISSION_GRANTED) {
//            checkPermissions();
//            return;
//        }
        Log.d(LOG,"... getting AccountManager");
        AccountManager am = AccountManager.get(getApplicationContext());
        Account[] accts = am.getAccounts();

        if (accts != null) {
            tarList.add(ctx.getResources().getString(R.string.select_email));
            for (int i = 0; i < accts.length; i++) {
                tarList.add(accts[i].name);

            }
        }

    }

    ArrayList<String> tarList = new ArrayList<String>();
    Menu mMenu;

    public void setBusyIndicator(final boolean refreshing) {
        if (mMenu != null) {
            final MenuItem refreshItem = mMenu.findItem(R.id.signin_action_help);
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
        super.onPause();
    }

}
