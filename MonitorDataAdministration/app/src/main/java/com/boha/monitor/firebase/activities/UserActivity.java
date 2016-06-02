package com.boha.monitor.firebase.activities;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.boha.monitor.firebase.R;
import com.boha.monitor.library.data.MonitorCompanyDTO;
import com.boha.monitor.library.data.UserDTO;
import com.boha.monitor.library.util.DataUtil;
import com.boha.monitor.library.util.ListUtil;
import com.boha.monitor.library.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    String mCurrentPhotoPath;
    static final int REQUEST_VIDEO_CAPTURE = 162, CAPTURE_IMAGE = 766;
    File photoFile;
    ImageView image;
    FloatingActionButton fab;
    Uri fileUri;
    Snackbar snackbar;
    File currentThumbFile, currentFullFile;
    Uri thumbUri, fullUri;
    Bitmap bitmapForScreen;
    EditText eFirstName, eLastName, eEMail;
    Button btnSave;
    Spinner spinner;
    CheckBox checkBox;

    List<MonitorCompanyDTO> companies;
    MonitorCompanyDTO company;


    static final String TAG = UserActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        setFields();

    }
    private void getCompanies() {
        ListUtil.getCompanies(new ListUtil.CompaniesListener() {
            @Override
            public void onResponse(List<MonitorCompanyDTO> list) {
                companies = list;
                setSpinner();
            }

            @Override
            public void onError(String message) {
                snackbar = Snackbar.make(btnSave,message, Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.amber_700));
                snackbar.setAction("Cool", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
        });
    }
    private void setSpinner() {
        List<String> list = new ArrayList<>();
        for (MonitorCompanyDTO m: companies) {
            list.add(m.getCompanyName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getApplicationContext(), R.layout.simple_list_item,list);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Select Organization");
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                company = companies.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void setFields() {
        eFirstName = (EditText)findViewById(R.id.firstName);
        eLastName = (EditText)findViewById(R.id.lastName);
        eEMail = (EditText)findViewById(R.id.email);
        image = (ImageView) findViewById(R.id.image);
        spinner = (Spinner) findViewById(R.id.spinner);
        checkBox = (CheckBox) findViewById(R.id.chkBox);
        btnSave = (Button) findViewById(R.id.btnSave);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser();
            }
        });
        btnSave.setEnabled(false);
    }
    private void addUser() {

        if (TextUtils.isEmpty(eFirstName.getText())) {

            return;
        }
        if (TextUtils.isEmpty(eLastName.getText())) {

            return;
        }
        if (TextUtils.isEmpty(eEMail.getText())) {

            return;
        }

        if (company == null) {

            return;
        }

        UserDTO u = new UserDTO();
        u.setFirstName(eFirstName.getText().toString());
        u.setLastName(eLastName.getText().toString());
        u.setEmail(eEMail.getText().toString());
        u.setPassword(Util.getOneTimePassword());

        if (checkBox.isChecked()) {
            u.setCompanyID(company.getCompanyID());
        }
        DataUtil.createUser(getApplicationContext(),u, new DataUtil.DataAddedListener() {
            @Override
            public void onResponse(String key) {
                userID = key;
                snackbar = Snackbar.make(btnSave,"User has been created on MPS", Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green_200));
                snackbar.setAction("Cool", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });
            }

            @Override
            public void onError(String message) {
                snackbar = Snackbar.make(btnSave,message, Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red_500));
                snackbar.setAction("Cool", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });
            }
        });

    }
    String userID;



}
