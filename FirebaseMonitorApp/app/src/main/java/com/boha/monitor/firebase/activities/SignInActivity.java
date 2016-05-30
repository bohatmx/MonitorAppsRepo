package com.boha.monitor.firebase.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.boha.monitor.firebase.R;
import com.boha.monitor.firebase.util.SharedUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity {

    EditText email, password;
    Button btnSend;
    ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private Snackbar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

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
    private void startMain() {
        Intent m = new Intent(getApplicationContext(), CompanyActivity.class);
        startActivity(m);
    }
    private void setFields() {
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);
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
        bar = Snackbar.make(email,message,Snackbar.LENGTH_INDEFINITE);
        bar.setActionTextColor(ContextCompat.getColor(getApplicationContext(),R.color.amber_400));
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
                            SharedUtil.setCreds(e,p,getApplicationContext());
                            startMain();
                        } else {
                            Log.e(TAG, "------------ sign in FAILED");
                            errorBar("Sorry! MPS Sign in has failed. Please try again a bit later");
                        }
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.w(TAG, "---------------- onStop: ");
        mAuth.removeAuthStateListener(mAuthListener);

    }
    static final String TAG = SignInActivity.class.getSimpleName();
}
