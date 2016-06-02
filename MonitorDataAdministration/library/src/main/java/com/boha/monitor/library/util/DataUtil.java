package com.boha.monitor.library.util;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.boha.monitor.library.data.CityDTO;
import com.boha.monitor.library.data.MonitorCompanyDTO;
import com.boha.monitor.library.data.MonitorDTO;
import com.boha.monitor.library.data.MunicipalityDTO;
import com.boha.monitor.library.data.ProjectDTO;
import com.boha.monitor.library.data.ProjectTaskDTO;
import com.boha.monitor.library.data.ProjectTaskStatusDTO;
import com.boha.monitor.library.data.ProvinceDTO;
import com.boha.monitor.library.data.StaffDTO;
import com.boha.monitor.library.data.UserDTO;
import com.boha.monitor.library.data.UserProjectDTO;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
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

    public interface UserProfileListener {
        void onProfileUpdated();

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
            USER_PHOTOS = "userphotos",
            PROJECT_PHOTOS = "projectPhotos",
            PROVINCES = "provinces",
            MUNICIPALITIES = "municipalities",
            CITIES = "cities",
            COMPANY_PROJECTS = "companyProjects";

    private static FirebaseDatabase db;
    private static FirebaseAuth.AuthStateListener mAuthListener;
    private static FirebaseAuth mAuth;
    private static FirebaseAnalytics analytics;

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

    public static void addFCMToken(Context ctx) {

        Log.w(TAG, "+++++++++++++++++++ sendToken: send FCM token and update user record");
        final UserDTO user = SharedUtil.getUser(ctx);
        if (user == null) {
            return;
        }
        final String token = SharedUtil.getFCMtoken(ctx);
        if (token == null) {
            return;
        }
        if (db == null)
            db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference(DataUtil.MONITOR_DB)
                .child(DataUtil.USERS);

        final Query query = ref.orderByChild("userID").equalTo(user.getUserID());

        valueEventListener =  new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DatabaseReference ref = dataSnapshot.getRef().child(user.getUserID());
                ref.child("fcmToken").setValue(token);
                Log.w(TAG, "onDataChange: token has been written to user" );
                query.removeEventListener(valueEventListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query.addValueEventListener(valueEventListener);
        Log.e(TAG, "sendToken: token sent to database " + token);
    }

    private static ValueEventListener valueEventListener;

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

    public static void addMonitor(final MonitorDTO monitor, final String projectName, final DataAddedListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();


        final DatabaseReference monitors = db.getReference(MONITOR_DB)
                .child(COMPANIES)
                .child(monitor.getCompanyID())
                .child(PROJECTS)
                .child(monitor.getProjectID())
                .child(MONITORS);

        monitors.push().setValue(monitor, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.i(TAG, "onComplete: monitor added to FB, " + monitor.getFullName() +
                            " key: " + databaseReference.getKey());
                    //set monitorID
                    DatabaseReference monitorIDref = databaseReference.child("monitorID");
                    monitorIDref.setValue(databaseReference.getKey());

                    //add project to user
                    DatabaseReference userRef = db.getReference(MONITOR_DB)
                            .child(USERS)
                            .child(monitor.getUserID()).child("userProjects");
                    UserProjectDTO up = new UserProjectDTO();
                    up.setProjectID(monitor.getProjectID());
                    up.setProjectName(projectName);
                    up.setDateAssigned(new Date().getTime());
                    up.setDateUpdated(new Date().getTime());
                    userRef.push().setValue(up);

                    final DatabaseReference projectRef = databaseReference.getParent().getParent();

                    projectRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            monitors.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    projectRef.child("monitorCount").setValue(dataSnapshot.getChildrenCount());
                                    Log.d(TAG, "onDataChange: monitorCount updated: " + dataSnapshot.getChildrenCount());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    listener.onResponse(databaseReference.getKey());
                } else {
                    listener.onError("Unable to add monitor");
                }
            }
        });
    }

    public static void createUser(final Context ctx, final UserDTO user,
                                  final DataAddedListener listener) {

        if (mAuth == null)
            mAuth = FirebaseAuth.getInstance();
        Task<AuthResult> authResultTask = mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword());
        authResultTask.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                FirebaseUser fbUser = authResult.getUser();
                Log.i(TAG, "onSuccess: user added to Monitor Platform: " + fbUser.getEmail() + " "
                        + fbUser.getUid());
                user.setUid(fbUser.getUid());
                //add user to MPS
                addUser(ctx, user, new DataAddedListener() {
                    @Override
                    public void onResponse(String key) {
                        listener.onResponse(key);
                        updateUserProfile(user, null);
                    }

                    @Override
                    public void onError(String message) {
                        listener.onError(message);
                    }
                });


            }
        });
        authResultTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report(e);
                Log.e(TAG, "onFailure: ", e);
                listener.onError("Unable to create user");
            }
        });

    }

    static FirebaseAuth.AuthStateListener authStateListener;

    public static void updateUserProfile(final UserDTO user, final UserProfileListener listener) {
        if (mAuth == null)
            mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(user.getEmail(), user.getPassword());
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser fbUser = firebaseAuth.getCurrentUser();
                //update user profile set display name + photo
                UserProfileChangeRequest.Builder b;
                if (user.getUri() == null) {
                    b = new UserProfileChangeRequest.Builder()
                            .setDisplayName(user.getFullName());
                } else {
                    Uri uri = Uri.parse(user.getUri());
                    b = new UserProfileChangeRequest.Builder()
                            .setDisplayName(user.getFullName()).setPhotoUri(uri);
                }
                Task<Void> task = fbUser.updateProfile(b.build());
                task.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        FirebaseCrash.report(e);
                        Log.e(TAG, "--------- onFailure: unable to update profile", e);
                        if (listener != null)
                            listener.onError(e.getMessage());
                    }
                });

                task.addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "++++++++++ onSuccess: user display name updated");

                        mAuth.removeAuthStateListener(authStateListener);
                        if (listener != null)
                            listener.onProfileUpdated();

                    }
                });
            }
        };

        mAuth.addAuthStateListener(authStateListener);

    }

    private static void addUser(final Context ctx, final UserDTO user, final DataAddedListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();

        final DatabaseReference users = db.getReference(MONITOR_DB)
                .child(USERS);

        users.push().setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Log.i(TAG, "********** onComplete: user added to MPS, " + user.getFirstName() +
                            " key: " + databaseReference.getKey());
                    //set userID
                    DatabaseReference userID = users.child(databaseReference.getKey())
                            .child("userID");
                    userID.setValue(databaseReference.getKey());

                    if (analytics == null) {
                        analytics = FirebaseAnalytics.getInstance(ctx);
                    }
                    Bundle b = new Bundle();
                    b.putString("userAdded", "MPS");
                    analytics.logEvent("userEvent", b);
                    listener.onResponse(databaseReference.getKey());
                } else {
                    listener.onError("Unable to add user");
                    FirebaseCrash.log("Unable to add authenticated user to MPS platform: " + user.getEmail());
                }
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
                    listener.onResponse(databaseReference.getKey());
                } else {
                    listener.onError("Unable to add project to database");
                }
            }
        });
    }
    public static void addProjectLocation(final ProjectDTO project,
                                          final DataAddedListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        final DatabaseReference projectRef = db.getReference(MONITOR_DB)
                .child(COMPANIES)
                .child(project.getCompanyID())
                .child(PROJECTS)
                .child(project.getProjectID());

        projectRef.child("latitude").setValue(project.getLatitude());
        projectRef.child("longitude").setValue(project.getLongitude());
        projectRef.child("locationConfirmed").setValue(true, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    listener.onResponse("done");
                    Log.w(TAG, "Yeahhh!! -- onComplete: location has been updated" );
                    StorageUtil.addProjectToGeofire(project, new StorageUtil.StorageListener() {
                        @Override
                        public void onUploaded(String key) {
                            Log.e(TAG, "onUploaded: Project location uploaded to Geofire" );
                        }

                        @Override
                        public void onError(String message) {
                            Log.e(TAG, "onError: failed geofire upload: " + message );
                        }
                    });
                } else {
                    listener.onError("Unable to update project location");
                }
            }
        });


    }

    public static void addProvince(final ProvinceDTO proj, final DataAddedListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        final DatabaseReference projects = db.getReference(MONITOR_DB)
                .child(PROVINCES);

        projects.push().setValue(proj, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.i(TAG, "onComplete: returned from adding province: " + proj.getProvinceName() +
                        " key: " + databaseReference.getKey());
                if (databaseError == null) {
                    proj.setProvinceID(databaseReference.getKey());
                    DatabaseReference provinceID = db.getReference(MONITOR_DB)
                            .child(PROVINCES)
                            .child(databaseReference.getKey())
                            .child("provinceID");
                    provinceID.setValue(databaseReference.getKey());
                    listener.onResponse(databaseReference.getKey());
                } else {
                    listener.onError("Unable to add project to database");
                }
            }
        });
    }

    public static void addMunicipality(final MunicipalityDTO mun, final DataAddedListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        final DatabaseReference muniRef = db.getReference(MONITOR_DB)
                .child(PROVINCES)
                .child(mun.getProvinceID())
                .child(MUNICIPALITIES);


        muniRef.push().setValue(mun, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.i(TAG, "onComplete: returned from adding muni: " + mun.getMunicipalityName() +
                        " key: " + databaseReference.getKey());
                if (databaseError == null) {
                    mun.setMunicipalityID(databaseReference.getKey());
                    DatabaseReference mID = muniRef
                            .child(databaseReference.getKey())
                            .child("municipalityID");
                    mID.setValue(databaseReference.getKey());
                    listener.onResponse(databaseReference.getKey());
                } else {
                    listener.onError("Unable to add muni to database");
                }
            }
        });
    }

    public static void addCity(final CityDTO city, final DataAddedListener listener) {
        if (db == null)
            db = FirebaseDatabase.getInstance();
        final DatabaseReference citiesRef = db.getReference(MONITOR_DB)
                .child(PROVINCES)
                .child(city.getProvinceID())
                .child(MUNICIPALITIES)
                .child(city.getMunicipalityID())
                .child(CITIES);


        citiesRef.push().setValue(city, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.i(TAG, "onComplete: returned from adding city: " + city.getCityName() +
                        " key: " + databaseReference.getKey());
                if (databaseError == null) {
                    city.setCityID(databaseReference.getKey());
                    DatabaseReference mID = citiesRef
                            .child(databaseReference.getKey())
                            .child("cityID");
                    mID.setValue(databaseReference.getKey());
                    listener.onResponse(databaseReference.getKey());
                } else {
                    listener.onError("Unable to add muni to database");
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
                Log.i(TAG, "onComplete: returned from adding project: key: " + databaseReference.getKey());
                if (databaseError == null) {
                    co.setCompanyID(databaseReference.getKey());
                    DatabaseReference x = db.getReference(MONITOR_DB)
                            .child("companies")
                            .child(databaseReference.getKey()).child("companyID");
                    x.setValue(databaseReference.getKey());

                    listener.onResponse(databaseReference.getKey());
                } else {
                    listener.onError("Unable to add project to database");
                }
            }
        });
    }

}
