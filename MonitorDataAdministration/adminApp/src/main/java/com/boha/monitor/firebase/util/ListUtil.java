package com.boha.monitor.firebase.util;

import android.util.Log;

import com.boha.monitor.firebase.data.KeyName;
import com.boha.monitor.firebase.data.MonitorCompanyDTO;
import com.boha.monitor.firebase.data.MonitorDTO;
import com.boha.monitor.firebase.data.ProjectDTO;
import com.boha.monitor.firebase.data.StaffDTO;
import com.boha.monitor.firebase.data.UserDTO;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aubreymalabie on 5/22/16.
 */

public class ListUtil {

    public interface CompaniesListener {
        void onResponse(List<MonitorCompanyDTO> list);
        void onError(String message);
    }
    public interface CompanyProjectsListener {
        void onResponse(List<KeyName> list);
        void onError(String message);
    }
    public interface ProjectDataListener {
        void onResponse(ProjectDTO project);
        void onError(String message);
    }
    public interface MonitorListener {
        void onResponse(List<MonitorDTO> monitors);
        void onError(String message);
    }
    public interface UserListener {
        void onResponse(List<UserDTO> users);
        void onError(String message);
    }

    public interface StaffListener {
        void onResponse(List<StaffDTO> staffList);
        void onError(String message);
    }
    static final String TAG = ListUtil.class.getSimpleName();


    static FirebaseDatabase db;
    public static void getUsers(String companyID, final UserListener listener) {
        if (db == null)
         db = FirebaseDatabase.getInstance();
        DatabaseReference companyUsers = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES)
                .child(companyID)
                .child(DataUtil.USERS);

        companyUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: " + dataSnapshot.getValue());
                List<UserDTO> list = new ArrayList<UserDTO>();
                for (DataSnapshot shot: dataSnapshot.getChildren()) {
                    UserDTO u =  shot.getValue(UserDTO.class);
                    list.add(u);
                    Log.d(TAG, "onDataChange: " + u.getFirstName() + " " + u.getLastName());
                }
                listener.onResponse(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: databaseError: " + databaseError );
                listener.onError("Unable to get users");
            }
        });

    }
    public static void getCompany(String companyID, final CompaniesListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference companiesRef = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES);
        Query q = companiesRef.orderByChild("companyID").equalTo(companyID);

        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: " + dataSnapshot.getValue());
                List<MonitorCompanyDTO> list = new ArrayList<>();
                for (DataSnapshot shot: dataSnapshot.getChildren()) {
                    MonitorCompanyDTO u =  shot.getValue(MonitorCompanyDTO.class);
                    list.add(u);
                    Log.d(TAG, "onDataChange: " + u.getCompanyName());
                }
                listener.onResponse(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: databaseError: " + databaseError );
                listener.onError("Unable to get users");
            }
        });

    }
    public static void getUser(String uID, final UserListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference companyUsers = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.USERS);
        Query q = companyUsers.orderByChild("uid").equalTo(uID);

        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: " + dataSnapshot.getValue());
                List<UserDTO> list = new ArrayList<UserDTO>();
                for (DataSnapshot shot: dataSnapshot.getChildren()) {
                    UserDTO u =  shot.getValue(UserDTO.class);
                    list.add(u);
                    Log.d(TAG, "onDataChange: " + u.getFirstName() + " " + u.getLastName());
                }
                listener.onResponse(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: databaseError: " + databaseError );
                listener.onError("Unable to get users");
            }
        });

    }
    public static  void getProjectStaff(String companyID, String projectID, final StaffListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference staff = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES)
                .child(companyID)
                .child(DataUtil.PROJECTS)
                .child(projectID)
                .child(DataUtil.STAFF);


        staff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot);
                List<StaffDTO> list = new ArrayList<StaffDTO>();
                for (DataSnapshot shot: dataSnapshot.getChildren() ) {
                    HashMap<String, String> obj = (HashMap<String, String>) shot.getValue();
                    StaffDTO c  = shot.getValue(StaffDTO.class);
                    list.add(c);
                    Log.i(TAG, "onDataChange: staff: " + c.getFirstName());
                }
                Log.d(TAG, "onDataChange: staff found: " + list.size());
                listener.onResponse(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: databaseError: " + databaseError );
                listener.onError("Unable to get staff");
            }
        });

    }
    public static  void getProjectPhotos(String companyID, String projectID, final StaffListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference photos = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES)
                .child(companyID)
                .child(DataUtil.PROJECTS)
                .child(projectID)
                .child(DataUtil.PHOTOS);


        photos.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot);
                List<StaffDTO> list = new ArrayList<StaffDTO>();
                for (DataSnapshot shot: dataSnapshot.getChildren() ) {
                    HashMap<String, String> obj = (HashMap<String, String>) shot.getValue();
                    StaffDTO c  = shot.getValue(StaffDTO.class);
                    list.add(c);
                    Log.i(TAG, "onDataChange: staff: " + c.getFirstName());
                }
                Log.d(TAG, "onDataChange: staff found: " + list.size());
                listener.onResponse(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: databaseError: " + databaseError );
                listener.onError("Unable to get staff");
            }
        });

    }
    public static  void getProjectTasks(String companyID, String projectID, final StaffListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference tasks = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES)
                .child(companyID)
                .child(DataUtil.PROJECTS)
                .child(projectID)
                .child(DataUtil.TASKS);


        tasks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot);
                List<StaffDTO> list = new ArrayList<StaffDTO>();
                for (DataSnapshot shot: dataSnapshot.getChildren() ) {
                    HashMap<String, String> obj = (HashMap<String, String>) shot.getValue();
                    StaffDTO c  = shot.getValue(StaffDTO.class);
                    list.add(c);
                    Log.i(TAG, "onDataChange: staff: " + c.getFirstName());
                }
                Log.d(TAG, "onDataChange: staff found: " + list.size());
                listener.onResponse(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: databaseError: " + databaseError );
                listener.onError("Unable to get staff");
            }
        });

    }
    public static  void getProjectMonitors(String companyID, String projectID, final MonitorListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference monitors = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES)
                .child(companyID)
                .child(DataUtil.PROJECTS)
                .child(projectID)
                .child(DataUtil.MONITORS);


        monitors.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot);
                List<MonitorDTO> list = new ArrayList<MonitorDTO>();
                for (DataSnapshot shot: dataSnapshot.getChildren() ) {
                    HashMap<String, String> obj = (HashMap<String, String>) shot.getValue();
                    MonitorDTO c  = shot.getValue(MonitorDTO.class);
                    list.add(c);
                    Log.i(TAG, "onDataChange: monitor: " + c.getFullName());
                }
                Log.d(TAG, "onDataChange: monitors found: " + list.size());
                listener.onResponse(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: databaseError: " + databaseError );
                listener.onError("Unable to get monitors");
            }
        });

    }
    public static  void getProjectDetail(String companyID, String projectID, final ProjectDataListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference companyProjects = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES)
                .child(companyID)
                .child(DataUtil.COMPANY_PROJECTS)
                .child(projectID);

        final List<ProjectDTO> list = new ArrayList<ProjectDTO>();

        companyProjects.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot);
//                for (DataSnapshot shot: dataSnapshot.getChildren() ) {
//                    HashMap<String, String> obj = (HashMap<String, String>) shot.getValue();
//                    ProjectDTO c  = shot.getValue(ProjectDTO.class);
//                    list.add(c);
//                    Log.i(TAG, "onDataChange: project listed: " + c.getProjectName());
//                }
//                listener.onResponse(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: databaseError: " + databaseError );
                listener.onError("Unable to get project details");
            }
        });

    }

    public static  void getCompanyProjects(String companyID, final CompanyProjectsListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference companyProjects = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.COMPANIES)
                .child(companyID)
                .child(DataUtil.COMPANY_PROJECTS);

        final List<ProjectDTO> list = new ArrayList<ProjectDTO>();

        companyProjects.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot);
//                for (DataSnapshot shot: dataSnapshot.getChildren() ) {
//                    HashMap<String, String> obj = (HashMap<String, String>) shot.getValue();
//                    ProjectDTO c  = shot.getValue(ProjectDTO.class);
//                    list.add(c);
//                    Log.i(TAG, "onDataChange: project listed: " + c.getProjectName());
//                }
//                listener.onResponse(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static void getCompanies(final CompaniesListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference companies = db.getReference(DataUtil.MONITOR_DB).child("companies");
        companies.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: we have data!!!!: records:  "
                        + dataSnapshot.getChildrenCount());
                List<MonitorCompanyDTO> list = new ArrayList<>();
                for (DataSnapshot shot: dataSnapshot.getChildren()) {
                    HashMap<String, String> obj = (HashMap<String, String>) shot.getValue();
                    MonitorCompanyDTO c  = shot.getValue(MonitorCompanyDTO.class);
                    list.add(c);
                    Log.w(TAG, "++++++++++++ onDataChange: we have a project: " + c.getCompanyName());
                }

                listener.onResponse(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled: databaseError: " + databaseError );
                listener.onError(databaseError.getMessage());
            }
        });
    }
}
