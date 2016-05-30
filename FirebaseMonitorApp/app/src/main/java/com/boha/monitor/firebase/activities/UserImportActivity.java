package com.boha.monitor.firebase.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.boha.monitor.firebase.R;
import com.boha.monitor.firebase.dto.MonitorCompanyDTO;
import com.boha.monitor.firebase.dto.UserDTO;
import com.boha.monitor.firebase.util.DataUtil;
import com.boha.monitor.firebase.util.ImportUtil;
import com.boha.monitor.firebase.util.Util;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserImportActivity extends AppCompatActivity {

    static final String TAG = UserImportActivity.class.getSimpleName();

    private FirebaseDatabase db;
    private FirebaseAnalytics analytics;
    private Snackbar snackbar;
    private TextView txtTitle, txtCount;
    private Spinner fileSpinner;
    private Button btnImport;
    private ImageView image;
    private RecyclerView recyclerView;
    private List<UserDTO> users = new ArrayList<>();
    private List<File> files = new ArrayList<File>();
    private MonitorCompanyDTO company;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_users);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        company = (MonitorCompanyDTO) getIntent().getSerializableExtra("company");
        db = FirebaseDatabase.getInstance();
        analytics = FirebaseAnalytics.getInstance(getApplicationContext());
        setFields();

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent m = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(m);
            }
        });
        new Mtask().execute();
    }


    private void setFields() {
        fileSpinner = (Spinner) findViewById(R.id.IMP_fileSpinner);
        btnImport = (Button) findViewById(R.id.IMP_btnImport);
        txtTitle = (TextView) findViewById(R.id.IMP_title);
        txtCount = (TextView) findViewById(R.id.IMP_count);
        recyclerView = (RecyclerView) findViewById(R.id.IMP_list);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setHasFixedSize(true);

        txtTitle.setText(company.getCompanyName());

        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });


//        setSpinner();
//        Log.w(TAG, "Import files found: " + files.size());
    }

    private void showDialog() {

        AlertDialog.Builder dg = new AlertDialog.Builder(this);
        StringBuilder sb = new StringBuilder();
        sb.append("Do you want to import these users?\n\n");
        for (UserDTO u : users) {
            sb.append(u.getFullName()).append(" - ").append(u.getEmail()).append("\n");
        }
        dg.setTitle(company.getCompanyName())
                .setMessage(sb.toString())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        index = 0;
                        importUsers();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    int index;

    private void importUsers() {
        if (index < users.size()) {
            importOneUser(users.get(index));
            return;
        }
        Log.i(TAG, "importUsers: Users imported: " + users.size());
        snackbar = Snackbar.make(fileSpinner, "" + users.size() + " users have been created on MPS",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green_200));
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();

    }

    private void importOneUser(final UserDTO user) {
        user.setCompanyID(company.getCompanyID());
        user.setPassword(Util.getOneTimePassword());
        Log.w(TAG, "importOneUser: starting creatUser for: " + user.getFullName());
        DataUtil.createUser(getApplicationContext(), user, new DataUtil.DataAddedListener() {
            @Override
            public void onResponse(String key) {
                Log.e(TAG, "++++++++++++++++ onResponse: User created on MPS:"
                        + user.getFullName() + " key: " + key);
                index++;
                importUsers();
            }

            @Override
            public void onError(String message) {
                snackbar = Snackbar.make(fileSpinner, message,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red_500));
                snackbar.setAction("OK", new View.OnClickListener() {
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

        List<String> list = new ArrayList<String>();
        list.add("Select import file");
        for (File p : files) {
            list.add(p.getName() + " - " + sdf.format(new Date(p.lastModified())));
        }
        ArrayAdapter a = new ArrayAdapter(getApplicationContext(), R.layout.simple_list_item, list);
        fileSpinner.setAdapter(a);
        fileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                if (index == 0) {
                    return;
                }
                try {
                    parseFile(files.get(index - 1));
                } catch (IOException e) {
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void parseFile(File file) throws IOException {
        users = new ArrayList<UserDTO>();

        BufferedReader brReadMe = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8"));
        String strLine = brReadMe.readLine();
        while (strLine != null) {
            UserDTO dto = null;
            try {
                dto = parseLine(strLine);
                users.add(dto);
                Log.e(TAG, "####### User added to list from import file: " + dto.getFullName());
                strLine = brReadMe.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        brReadMe.close();

        showDialog();
    }

    public static UserDTO parseLine(String line) throws Exception {
        Pattern patt = Pattern.compile(";");

        if (line.indexOf(",") > -1) {
            patt = Pattern.compile(",");
        }
        String[] result = patt.split(line);
        UserDTO dto = new UserDTO();
        try {
            if (result[0] != null) {
                dto.setFirstName(result[0]);
            }

            if (result[1] != null) {
                dto.setLastName(result[1]);
            }
            try {
                if (result[2] != null) {
                    dto.setEmail(result[2]);
                }


            } catch (Exception e) {
                // ignore
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //ignore
        }
        // validate
        if (dto.getFirstName() == null || dto.getLastName() == null) {
            throw new Exception();
        }
        Log.e(TAG,
                "Found user: " + dto.getFirstName() + " "
                        + dto.getLastName() + " to import into MPS");
        return dto;
    }

    SharedPreferences sp;



    FirebaseRecyclerAdapter<UserDTO, UserViewHolder> adapter;

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "+++++++++++++++++++ onStart: FirebaseAuth.getInstance(");
        if (company == null) {
            return;
        }
        final DatabaseReference userRef = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES).child(company.getCompanyID()).child(DataUtil.USERS);

        adapter =
                new FirebaseRecyclerAdapter<UserDTO, UserViewHolder>(
                        UserDTO.class,
                        R.layout.user_item,
                        UserViewHolder.class,
                        userRef
                ) {
                    @Override
                    protected void populateViewHolder(UserViewHolder h, final UserDTO model, int position) {
                        Log.d(TAG, "####### populateViewHolder: " + model.getFullName() + " " + model.getCompanyID());
                        h.userName.setText(model.getFullName());
                        h.email.setText(model.getEmail());
                        h.camera.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                camera(model);
                            }
                        });
                        txtCount.setText("" + adapter.getItemCount());
                        if (model.getPhotoList() == null
                                || model.getPhotoList().isEmpty()) {

                        } else {
                            String url = model.getPhotoList().get(
                                    model.getPhotoList().size() - 1).getUrl();
                            Picasso.with(getApplicationContext())
                                    .load(Uri.parse(url)).into(h.image);
                        }


                    }
                };

        recyclerView.setAdapter(adapter);

    }

    private void camera(UserDTO co) {
        Log.d(TAG, "addMonitors requested: " + co.getFullName());
        Intent m = new Intent(getApplicationContext(), PictureActivity.class);
        m.putExtra("user", co);
        startActivity(m);

    }

    private void assignProjects(UserDTO user) {
        Log.d(TAG, "assignProject to: " + user.getFullName());
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", loc);

    @Override
    public void onStop() {
        super.onStop();
        Log.w(TAG, "---------------- onStop: ");

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        protected TextView userName, email;
        protected ImageView camera, assignProject;
        protected CircleImageView image;


        public UserViewHolder(View itemView) {
            super(itemView);
            userName = (TextView) itemView.findViewById(R.id.name);
            email = (TextView) itemView.findViewById(R.id.date);
            image = (CircleImageView) itemView.findViewById(R.id.image);
            camera = (ImageView) itemView.findViewById(R.id.takePicture);
            assignProject = (ImageView) itemView.findViewById(R.id.assignProject);
        }

    }

    class Mtask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            files.clear();
            files = ImportUtil.getImportFiles();
            files.addAll(ImportUtil.getImportFilesOnSD());

            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (files.isEmpty()) {
                snackbar = Snackbar.make(btnImport, "No import files found", Snackbar.LENGTH_INDEFINITE);
                snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red_500));
                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        finish();
                        return;
                    }
                });
                snackbar.show();
            } else {
                setSpinner();
            }
        }
    }
}
