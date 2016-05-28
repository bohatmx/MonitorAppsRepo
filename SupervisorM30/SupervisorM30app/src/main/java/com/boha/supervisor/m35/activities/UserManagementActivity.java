package com.boha.supervisor.m35.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.boha.supervisor.m35.R;
import com.boha.supervisor.m35.dto.MonitorCompanyDTO;
import com.boha.supervisor.m35.dto.UserDTO;
import com.boha.supervisor.m35.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class UserManagementActivity extends AppCompatActivity {

    static final String TAG = UserManagementActivity.class.getSimpleName();
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private DatabaseReference mDatabase;

    private EditText firstName, lastName, email, password;

    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setFields();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        mDatabase = db.getReference("MonitorDB");
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        checkStatus();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeCompany();
            }
        });
    }

    private void addUser() {
        if (TextUtils.isEmpty(email.getText())) {
            Toast.makeText(this, "Please enter email address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password.getText())) {
            Toast.makeText(this, "Please enter passwords", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "addUser: ******* adding user");
        snackbar = Snackbar.make(email, "Adding user to Firebase...", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        snackbar.dismiss();
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            snackbar = Snackbar.make(email, "ERROR adding  user to Firebase...",
                                    Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                        } else {
                            Log.i(TAG, "onComplete: +++++++++++++ user added to Firebase");
                            FirebaseUser user = task.getResult().getUser();
                            Log.e(TAG, "++++++++++++++ onComplete: Firebase user: " + user.getEmail() + " " + user.getUid());
                        }

                        // ...
                    }
                });
    }

    private void writeCompany() {
        final MonitorCompanyDTO c = new MonitorCompanyDTO();
        c.setCompanyName("TigerM Construction");
        c.setAddress("3033 Gary Player Avenue, Waterloo, Virginia");
        String key = Util.getCompaniesKey(getApplicationContext());
        if (key == null) {
            key = mDatabase.child("monitor-companies").push().getKey();
            Util.setCompaniesKey(getApplicationContext(), key);
        }
        Log.i(TAG, "writeCompany: key: " + key);

        ValueEventListener listener = mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object val = dataSnapshot.getValue();
                Log.d(TAG, "onDataChange: " + dataSnapshot.getValue());
                c.setCompanyID(dataSnapshot.getKey());
                String json = gson.toJson(c);
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("COMPANY", json);
                editor.commit();
                Log.w(TAG, "onDataChange: company saved: " + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: code: " + databaseError.getCode() + " "
                        + " databaseError:" + databaseError.getMessage() + "\n"
                        + databaseError.getDetails());
            }
        });
        Map<String, String> map = new HashMap<>();
        map.put("companyName", c.getCompanyName());
        map.put("address", c.getAddress());

        mDatabase.child("monitor-companies").child(key).push().setValue(c, listener);

    }

    //This is our own user
    static final Gson gson = new Gson();
    SharedPreferences sp;

    private void writeExistingMonitorUsers() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String json = sp.getString("COMPANY", null);
        if (json == null) {
            Log.e(TAG, "writeExistingMonitorUsers: Company not found in cache");
            return;
        }
        MonitorCompanyDTO c = gson.fromJson(json, MonitorCompanyDTO.class);

        UserDTO user1 = new UserDTO();
        user1.setUserID("3xZ28myfUGVjDrpcb9EaGvjBOq62");
        user1.setFirstName("Aubrey St Vincent");
        user1.setLastName("Malabie");
        user1.setEmail("aubrey@mlab.co.za");
        user1.setCompanyID(c.getCompanyID());


        ValueEventListener listener = mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: dataSnapshot: " + dataSnapshot.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: code: " + databaseError.getCode() + " "
                        + " databaseError:" + databaseError.getMessage() + "\n"
                        + databaseError.getDetails());
            }
        });

        mDatabase.child("users").push().setValue(user1, listener);

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
                    UserDTO x = new UserDTO();
                    x.setEmail(user.getEmail());
                    x.setUserID(user.getUid());
                    Log.w(TAG, "****************** - onAuthStateChanged: remember to save this app user somewhere");

                } else {
                    // User is signed out
                    Log.d(TAG, "-----------onAuthStateChanged:signed_out - start sign in");
                    signIn();
                }

            }
        };

        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "+++++++++++++++++++ onStart: FirebaseAuth.getInstance(");
        mAuth = FirebaseAuth.getInstance();


    }

    @Override
    public void onStop() {
        super.onStop();
        Log.w(TAG, "---------------- onStop: ");
        mAuth.removeAuthStateListener(mAuthListener);

    }

    private void signIn() {
        Log.w(TAG, "signIn: ================ Firebase signin");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String email = "aubrey@mlab.co.za";
        String password = "kktiger3";

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i(TAG, "####### signIn: onComplete: " + task.isSuccessful());

                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            Log.i(TAG, "####### onComplete: we cool, name: "
                                    + user.getDisplayName() + " email: " + user.getEmail()
                                    + " uid: " + user.getUid() + " \ntoken: " + user.getToken(true));
                        } else {
                            Log.e(TAG, "------------ sign in FAILED");
                        }
                    }
                });
    }

    private void setFields() {
        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);


    }
}
