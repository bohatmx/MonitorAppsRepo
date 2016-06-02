package com.boha.monitor.firebase.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.boha.monitor.firebase.R;
import com.boha.monitor.library.data.MonitorCompanyDTO;
import com.boha.monitor.library.data.UserDTO;
import com.boha.monitor.library.util.DataUtil;
import com.boha.monitor.library.util.ListUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    EditText email, password;
    Button btnSend;
    ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private Snackbar bar;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        db.setPersistenceEnabled(true);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        setFields();
        checkStatus();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void checkStatus() {
        Log.i(TAG, "checkStatus: ---------- check Firebase user log in");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.i(TAG, "++++++++++++++ onAuthStateChanged:signed_in:" + user.getUid()
                            + " " + user.getEmail());
                    startMain();
                } else {
                    // User is signed out
                    Log.e(TAG, "-----------onAuthStateChanged:signed_out - start sign in");
                    signIn();
                }

            }
        };

        mAuth.addAuthStateListener(mAuthListener);

    }

//    private void checkUser(String uid) {
//
//        DatabaseReference ref = db.getReference(DataUtil.MONITOR_DB)
//                .child(DataUtil.USERS);
//        Query query = ref.orderByChild("uid").equalTo(uid);
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.getChildrenCount() > 0) {
//                    for (DataSnapshot m: dataSnapshot.getChildren()) {
//                        UserDTO user = (UserDTO) m.getValue();
//                        SharedUtil.setUser(user, getApplicationContext());
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//                UserDTO m = new UserDTO();
//            }
//        });
//
//    }

    private void startMain() {
        Intent m = new Intent(getApplicationContext(), CompanyActivity.class);
        startActivity(m);
    }

    private void setFields() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        btnSend = (Button) findViewById(R.id.btnSignIn);
        progressBar.setVisibility(View.GONE);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void errorBar(String message) {
        bar = Snackbar.make(email, message, Snackbar.LENGTH_INDEFINITE);
        bar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.amber_400));
        bar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bar.dismiss();
            }
        });
        bar.show();
    }

    private void signIn() {
        if (TextUtils.isEmpty(email.getText())) {
            errorBar("Please enter email address");
            return;
        }
        if (TextUtils.isEmpty(password.getText())) {
            errorBar("Please enter password");
            return;
        }
        Log.w(TAG, "signIn: ================ Firebase signin");
        final String e = email.getText().toString();
        final String p = password.getText().toString();

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(e, p)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i(TAG, "####### signIn: onComplete: " + task.isSuccessful());
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            Log.i(TAG, "####### onComplete: we cool, name: "
                                    + user.getDisplayName() + " email: " + user.getEmail()
                                    + " uid: " + user.getUid() + " \ntoken: " + user.getToken(true));
                            SharedUtil.setCreds(e, p, getApplicationContext());
                            getUser(user.getUid());

                            startMain();
                        } else {
                            Log.e(TAG, "------------ sign in FAILED");
                            errorBar("Sorry! MPS Sign in has failed. Please try again a bit later");
                        }
                    }
                });
    }

    private void getUser(String uid) {
        ListUtil.getUser(uid, new ListUtil.UserListener() {
            @Override
            public void onResponse(List<UserDTO> users) {
                if (!users.isEmpty()) {
                    SharedUtil.setUser(users.get(0),getApplicationContext());
                    DataUtil.addFCMToken(getApplicationContext());

                    ListUtil.getCompany(users.get(0).getCompanyID(),
                            new ListUtil.CompaniesListener() {
                        @Override
                        public void onResponse(List<MonitorCompanyDTO> list) {
                            if (!list.isEmpty()) {
                                SharedUtil.setCompany(list.get(0),getApplicationContext());
                            }
                        }

                        @Override
                        public void onError(String message) {

                        }
                    });
                }
            }

            @Override
            public void onError(String message) {

            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.w(TAG, "---------------- onStop: ");
        mAuth.removeAuthStateListener(mAuthListener);

    }

    static final String TAG = SignInActivity.class.getSimpleName();

    static final int REQUEST_RESOLVE_ERROR = 111, DIALOG_ERROR = 287;
    boolean mResolvingError;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                googleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt("DIALOERROR", errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }
    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt("DIALOERROR");
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!googleApiClient.isConnecting() &&
                        !googleApiClient.isConnected()) {
                    googleApiClient.connect();
                }
            }
        }
    }
    private boolean checkGooglePlayServices(){
        int checkGooglePlayServices = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getApplicationContext());
        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
		/*
		* Google Play Services is missing or update is required
		*  return code could be
		* SUCCESS,
		* SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED,
		* SERVICE_DISABLED, SERVICE_INVALID.
		*/
            GooglePlayServicesUtil.getErrorDialog(checkGooglePlayServices,
                    this, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();

            return false;
        }

        return true;
    }

    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 6;
}

