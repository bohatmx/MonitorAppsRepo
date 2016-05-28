package com.boha.monitor.firebase.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.boha.monitor.firebase.dto.KeyName;
import com.boha.monitor.firebase.dto.MonitorCompanyDTO;
import com.boha.monitor.firebase.dto.MonitorDTO;
import com.boha.monitor.firebase.dto.PhotoUploadDTO;
import com.boha.monitor.firebase.dto.ProjectDTO;
import com.boha.monitor.firebase.dto.ProjectTaskDTO;
import com.boha.monitor.firebase.dto.ProjectTaskStatusDTO;
import com.boha.monitor.firebase.dto.StaffDTO;
import com.boha.monitor.firebase.dto.UserDTO;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aubreymalabie on 5/22/16.
 */

public class DataUtil {

    public interface CompaniesListener {
        void onResponse(List<MonitorCompanyDTO> list);

        void onError(String message);
    }

    public interface ProjectsListener {
        void onResponse(List<ProjectDTO> list);

        void onError(String message);
    }

    public interface DataAddedListener {
        void onResponse(String key);

        void onError(String message);
    }

    static final String TAG = DataUtil.class.getSimpleName();

    public static String MONITOR_DB = "MonitorDB",
            COMPANIES = "companies",
            PROJECTS = "projects",
            USERS = "users",
            MONITORS = "monitors",
            STAFF = "staff",
            TASKS = "tasks",
            TASK_STATUS = "taskStatus",
            PHOTOS = "photos",
            COMPANY_PROJECTS = "companyProjects";

    private static FirebaseDatabase db;
    private static FirebaseAuth.AuthStateListener mAuthListener;
    private static FirebaseAuth mAuth;

    public static void getUsers(final DataAddedListener listener) {
        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(MONITOR_DB).child(USERS);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onDataChange: " + dataSnapshot.getValue());
                List<UserDTO> list = new ArrayList<UserDTO>();
                for (DataSnapshot shot : dataSnapshot.getChildren()) {
                    UserDTO u = shot.getValue(UserDTO.class);
                    list.add(u);
                    Log.d(TAG, "onDataChange: " + u.getFirstName() + " " + u.getLastName());
                }
                listener.onResponse("Users found: " + list.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static void addProjectTaskStatus(final ProjectTaskStatusDTO status, final DataAddedListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference projectStatuses = db.getReference(MONITOR_DB)
                .child(COMPANIES)
                .child(status.getCompanyID())
                .child(PROJECTS)
                .child(status.getProjectID())
                .child(TASKS)
                .child(status.getProjectTaskID())
                .child(TASK_STATUS);
        projectStatuses.push().setValue(status, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.i(TAG, "onComplete: status added to FB, " + status.getTaskName() +
                            " key: " + databaseReference.getKey());
                    listener.onResponse(databaseReference.getKey());
                } else {
                    listener.onError("Unable to add status");
                }
            }
        });

    }


    public static void addProjectTask(final ProjectTaskDTO task, final DataAddedListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference projectTasks = db.getReference(MONITOR_DB)
                .child(COMPANIES)
                .child(task.getCompanyID())
                .child(PROJECTS)
                .child(task.getProjectID())
                .child(TASKS);
        projectTasks.push().setValue(task, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.i(TAG, "onComplete: projectTask added to FB, " + task.getTaskName() +
                            " key: " + databaseReference.getKey());
                    listener.onResponse(databaseReference.getKey());
                } else {
                    listener.onError("Unable to add projectTask");
                }
            }
        });
    }

    public static void addStaff(final StaffDTO staff, final DataAddedListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference staffers = db.getReference(MONITOR_DB)
                .child(COMPANIES)
                .child(staff.getCompanyID())
                .child(PROJECTS)
                .child(staff.getProjectID())
                .child(STAFF);
        staffers.push().setValue(staff, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.i(TAG, "onComplete: staff added to FB, " + staff.getFirstName() +
                            " key: " + databaseReference.getKey());
                    listener.onResponse(databaseReference.getKey());
                } else {
                    listener.onError("Unable to add staff");
                }
            }
        });
    }

    public static void addMonitor(final MonitorDTO monitor, final DataAddedListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference monitors = db.getReference(MONITOR_DB)
                .child(COMPANIES)
                .child(monitor.getCompanyID())
                .child(PROJECTS)
                .child(monitor.getProjectID())
                .child(MONITORS);
        monitors.push().setValue(monitor, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.i(TAG, "onComplete: monitor added to FB, " + monitor.getFirstName() +
                            " key: " + databaseReference.getKey());
                    listener.onResponse(databaseReference.getKey());
                } else {
                    listener.onError("Unable to add monitor");
                }
            }
        });
    }

    public static void createUser(final UserDTO user,
                                  final DataAddedListener listener) {

        if (mAuth == null)
            mAuth = FirebaseAuth.getInstance();
        Task<AuthResult> authResultTask = mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword());
        authResultTask.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                FirebaseUser fbUser = authResult.getUser();
                Log.i(TAG, "onSuccess: user added to Monitor Platform: " + fbUser.getEmail());
                //add user to company
                if (user.getCompanyID() != null) {
                    addUser(user, new DataAddedListener() {
                        @Override
                        public void onResponse(String key) {
                            listener.onResponse(key);
                        }

                        @Override
                        public void onError(String message) {
                            listener.onError(message);
                        }
                    });
                }

                //update user profile set display name + photo
                UserProfileChangeRequest.Builder b = new UserProfileChangeRequest.Builder()
                        .setDisplayName(user.getFullName());
                Task<Void> task = fbUser.updateProfile(b.build());
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        FirebaseCrash.report(e);
                        Log.e(TAG, "onFailure: unable to update profile",e);
                    }
                });
                task.addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: user display name updated");
                    }
                });

            }
        });
        authResultTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report(e);
                Log.e(TAG, "onFailure: ",e );
                listener.onError("Unable to create user");
            }
        });

    }

    private static void addUser(final UserDTO user, final DataAddedListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference users = db.getReference(MONITOR_DB)
                .child(COMPANIES)
                .child(user.getCompanyID())
                .child(USERS);
        users.push().setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.i(TAG, "onComplete: user added to FB, " + user.getFirstName() +
                            " key: " + databaseReference.getKey());
                    listener.onResponse(databaseReference.getKey());
                } else {
                    listener.onError("Unable to add user");
                }
            }
        });
    }

    public static void addProjectPhoto(final PhotoUploadDTO photo, final DataAddedListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference photos = db.getReference(MONITOR_DB)
                .child(COMPANIES)
                .child(photo.getCompanyID())
                .child(PROJECTS)
                .child(photo.getProjectID())
                .child(PHOTOS);
        photos.push().setValue(photo, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.i(TAG, "onComplete: photo added: key: " + databaseReference.getKey());
            }
        });
    }

    public static void addProject(final ProjectDTO proj, final DataAddedListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        final DatabaseReference projects = db.getReference(MONITOR_DB)
                .child(COMPANIES)
                .child(proj.getCompanyID())
                .child(PROJECTS);
        projects.push().setValue(proj, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.i(TAG, "onComplete: returned from adding project: " + proj.getProjectName() +
                        " key: " + databaseReference.getKey());
                if (databaseError == null) {
                    proj.setProjectID(databaseReference.getKey());
                    DatabaseReference projectID = db.getReference(MONITOR_DB)
                            .child(COMPANIES)
                            .child(proj.getCompanyID())
                            .child(PROJECTS)
                            .child(databaseReference.getKey())
                            .child("projectID");
                    projectID.setValue(databaseReference.getKey());

                    //add to companyProjects - add project id and name to list
                    DatabaseReference companyProjects = db.getReference(MONITOR_DB)
                            .child(COMPANIES)
                            .child(proj.getCompanyID())
                            .child(COMPANY_PROJECTS);
                    KeyName kn = new KeyName();
                    kn.setKey(databaseReference.getKey());
                    kn.setName(proj.getProjectName());

                    companyProjects.push().setValue(kn, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            Log.i(TAG, "onComplete: returned from adding project to company, "
                                    + proj.getProjectName() + ": key: "
                                    + databaseReference.getKey());

                        }
                    });

                    listener.onResponse(databaseReference.getKey());
                } else {
                    listener.onError("Unable to add project to database");
                }
            }
        });
    }

    public static void addCompany(final MonitorCompanyDTO co, final DataAddedListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference companies = db.getReference(MONITOR_DB).child("companies");
        companies.push().setValue(co, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.i(TAG, "onComplete: returned from adding company: key: " + databaseReference.getKey());
                if (databaseError == null) {
                    co.setCompanyID(databaseReference.getKey());
                    DatabaseReference x = db.getReference(MONITOR_DB)
                            .child("companies")
                            .child(databaseReference.getKey()).child("companyID");
                    x.setValue(databaseReference.getKey());

                    listener.onResponse(databaseReference.getKey());
                } else {
                    listener.onError("Unable to add company to database");
                }
            }
        });
    }

}
