package com.boha.monitor.library.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.TaskStatusTypeDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Statics;
import com.boha.monitor.library.util.ThemeChooser;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ProjectMapActivity extends AppCompatActivity
        implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleMap googleMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest locationRequest;
    Location location;
    Context ctx;

    List<Marker> markers = new ArrayList<Marker>();
    static final String LOG = ProjectMapActivity.class.getSimpleName();
    boolean mResolvingError;
    static final long ONE_MINUTE = 1000 * 60;
    static final long FIVE_MINUTES = 1000 * 60 * 5;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";

    ProjectDTO project;
    int type;
    TextView text, txtCount;
    View topLayout;
    List<LocationTrackerDTO> trackList;
    static final Locale loc = Locale.getDefault();
    List<ProjectDTO> projectList;
    public static final int STAFF = 1, MONITOR = 2;
    DisplayMetrics displayMetrics;
    int themeDarkColor, themePrimaryColor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ctx = getApplicationContext();

        ThemeChooser.setTheme(this);
        Resources.Theme theme = getTheme();
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
        themeDarkColor = typedValue.data;
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        themePrimaryColor = typedValue.data;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_map);

        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        type = getIntent().getIntExtra("type", 0);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        text = (TextView) findViewById(R.id.text);
        txtCount = (TextView) findViewById(R.id.count);
        txtCount.setText("0");
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);


        Statics.setRobotoFontBold(ctx, text);

        topLayout = findViewById(R.id.top);

        googleMap = mapFragment.getMap();
        if (googleMap == null) {
            Util.showToast(ctx, "map is not available");
            finish();
            return;
        }
        setGoogleMap();
        if (project != null) {
            txtCount.setText("" + 1);
            Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                    project.getProjectName(),project.getCityName(),
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses48));
            setOneProjectMarker();
        } else {
            Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(),
                    SharedUtil.getCompany(ctx).getCompanyName() ,"Project Locations",
                    ContextCompat.getDrawable(getApplicationContext(), R.drawable.glasses48));
            getCachedData();
        }

    }

    private void getCachedData() {
        switch (type) {
            case STAFF:
                text.setVisibility(View.GONE);
                CacheUtil.getCachedStaffData(ctx, new CacheUtil.CacheUtilListener() {
                    @Override
                    public void onFileDataDeserialized(ResponseDTO response) {
                        if (response.getProjectList() != null) {
                            projectList = response.getProjectList();
                            txtCount.setText("" + projectList.size());
                            setProjectMarkers();
                        }
                    }

                    @Override
                    public void onDataCached() {

                    }

                    @Override
                    public void onError() {

                    }
                });
                break;
            case MONITOR:
                text.setVisibility(View.GONE);
                CacheUtil.getCachedMonitorProjects(ctx, new CacheUtil.CacheUtilListener() {
                    @Override
                    public void onFileDataDeserialized(ResponseDTO response) {
                        if (response.getProjectList() != null) {
                            projectList = response.getProjectList();

                            txtCount.setText("" + projectList.size());
                            setProjectMarkers();
                        }
                    }

                    @Override
                    public void onDataCached() {

                    }

                    @Override
                    public void onError() {

                    }
                });
                break;
        }
    }

    private void temporaryWork(LatLng latLng) {
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        Log.w(LOG, "********* onMapClick");

        RequestDTO w = new RequestDTO(RequestDTO.UPDATE_PROJECT);
        ProjectDTO p = new ProjectDTO();
        p.setProjectID(project.getProjectID());
        p.setLatitude(latLng.latitude);
        p.setLongitude(latLng.longitude);
        p.setLocationConfirmed(true);
        w.setProject(p);

        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                if (response.getStatusCode() == 0)
                    Log.i(LOG, "+++ cool. project location updated");
            }

            @Override
            public void onError(String message) {
                Log.e(LOG, message);
            }

            @Override
            public void onWebSocketClose() {

            }
        });

    }

    Activity activity;
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");
    int index = 0;
    private void setGoogleMap() {
        activity = this;
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);

        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location loc) {
                location = loc;
            }
        });
        //TODO - remove after test
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                project = projectList.get(index);
                index++;
                if (index == projectList.size()) {
                    return;
                }
                final AlertDialog.Builder d = new AlertDialog.Builder(activity);
                d.setTitle("Project Location")
                        .setMessage("Do you want to set location for " + project.getProjectName())
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                temporaryWork(latLng);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
            }
        });
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng latLng = marker.getPosition();
                Location loc = new Location(location);
                loc.setLatitude(latLng.latitude);
                loc.setLongitude(latLng.longitude);
                if (project != null) {

                }
                float mf = location.distanceTo(loc);
                Log.w(LOG, "######### distance, again: " + mf);

                showPopup(latLng.latitude, latLng.longitude,
                        marker.getTitle(), marker.getSnippet());

                return true;
            }
        });


    }

    static final DecimalFormat df = new DecimalFormat("###,##0.00");

    private ProjectTaskStatusDTO getLastStatus(ProjectDTO project) {
        ProjectTaskStatusDTO status = null;
        List<ProjectTaskStatusDTO> ptList = new ArrayList<>();
        for (ProjectTaskDTO task: project.getProjectTaskList()) {
                ptList.addAll(task.getProjectTaskStatusList());
        }
        Collections.sort(ptList);
        if (!ptList.isEmpty()) {
            status = ptList.get(0);
        }

        return status;
    }

    private void setProjectMarkers() {
        googleMap.clear();

        index = 0;
        for (ProjectDTO project : projectList) {
            if (project.getLatitude() == null) continue;
            LatLng pnt = new LatLng(project.getLatitude(), project.getLongitude());
            ProjectTaskStatusDTO status = getLastStatus(project);

            View view = getLayoutInflater().inflate(R.layout.project_name, null);
            TextView name = (TextView)view.findViewById(R.id.name);
            TextView st = (TextView)view.findViewById(R.id.statusColor);
            name.setText(project.getProjectName());
            Bitmap bitmap = null;
            BitmapDescriptor desc = null;
            st.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xblack_oval_small));
            if (status != null) {
                Short color = status.getTaskStatusType().getStatusColor();
                switch (color) {
                    case TaskStatusTypeDTO.STATUS_COLOR_RED:
                        st.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xred_oval_small));
                        break;
                    case TaskStatusTypeDTO.STATUS_COLOR_GREEN:
                        st.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xgreen_oval_small));
                        break;
                    case TaskStatusTypeDTO.STATUS_COLOR_AMBER:
                        st.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xamber_oval_small));
                        break;
                    default:
                        st.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xblack_oval_small));

                        break;
                }
            }

            bitmap = Util.createBitmapFromView(ctx, view, displayMetrics);
            desc = BitmapDescriptorFactory.fromBitmap(bitmap);
            Marker m = googleMap.addMarker(new MarkerOptions()
                    .title(project.getProjectID().toString())
                    .icon(desc)
                    .snippet(project.getProjectName())
                    .position(pnt));
            markers.add(m);
            index++;
        }
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                //ensure that all markers in bounds
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }

                LatLngBounds bounds = builder.build();
                int padding = 60; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                txtCount.setText("" + markers.size());
                googleMap.animateCamera(cu);
            }
        });

    }

    private void setOneProjectMarker() {

        LatLng pnt = new LatLng(project.getLatitude(),
                project.getLongitude());
        View view = getLayoutInflater().inflate(R.layout.project_name, null);
        TextView name = (TextView)view.findViewById(R.id.name);
        TextView st = (TextView)view.findViewById(R.id.statusColor);
        ProjectTaskStatusDTO status = getLastStatus(project);
        name.setText(project.getProjectName());
        st.setBackground(ContextCompat.getDrawable(ctx, R.drawable.xblack_oval_small));
        if (status != null) {
            switch (status.getTaskStatusType().getStatusColor()) {
                case TaskStatusTypeDTO.STATUS_COLOR_AMBER:
                    st.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xamber_oval_small));
                    break;
                case TaskStatusTypeDTO.STATUS_COLOR_GREEN:
                    st.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xgreen_oval_small));
                    break;
                case TaskStatusTypeDTO.STATUS_COLOR_RED:
                    st.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xred_oval_small));
                    break;

            }
        } else {
            st.setBackground(ContextCompat.getDrawable(ctx,R.drawable.xblack_oval_small));
        }
        Bitmap bmBitmap = Util.createBitmapFromView(ctx,view,displayMetrics);
        BitmapDescriptor desc = BitmapDescriptorFactory.fromBitmap(bmBitmap);
        Marker m =
                googleMap.addMarker(new MarkerOptions()
                        .title(project.getProjectID().toString())
                        .icon(desc)
                        .snippet(project.getProjectName())
                        .position(pnt));
        markers.add(m);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pnt, 1.0f));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f));
    }

    List<String> list;

    private void showPopup(final double lat, final double lng, final String id, String snippet) {
        list = new ArrayList<>();
        list.add(getString(R.string.directions));
        list.add(getString(R.string.streetview));
        list.add(getString(R.string.statrpt));

        Util.showPopupBasicWithHeroImage(ctx, this, list, topLayout,
                snippet,
                new Util.UtilPopupListener() {
                    @Override
                    public void onItemSelected(int index) {
                        if (list.get(index).equalsIgnoreCase(getString(R.string.directions))) {
                            startDirectionsMap(lat, lng);
                        }
                        if (list.get(index).equalsIgnoreCase(getString(R.string.streetview))) {
                            getStreetView(lat, lng);
                        }

                        if (list.get(index).equalsIgnoreCase(getString(R.string.statrpt))) {
                            Integer projectID = Integer.parseInt(id);
                            ProjectDTO g = null;
                            for (ProjectDTO x : projectList) {
                                if (x.getProjectID().intValue() == projectID.intValue()) {
                                    g = x;
                                    break;
                                }
                            }
                            if (g != null) {
                                getStatusReport(g);
                            }
                        }
                    }
                });


    }

    private void getStreetView(double latitude, double longitude) {
        StringBuilder sb = new StringBuilder();
        sb.append("google.streetview:cbll=");
        sb.append(latitude).append(",").append(longitude);
        Uri gmmIntentUri = Uri.parse(sb.toString());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void getStatusReport(ProjectDTO project) {

        Intent w = new Intent(this, StatusReportActivity.class);
        w.putExtra("project", project);
        startActivity(w);
    }


    private void startDirectionsMap(double lat, double lng) {
        Log.i(LOG, "startDirectionsMap ..........");
        String url = "http://maps.google.com/maps?saddr="
                + location.getLatitude() + "," + location.getLongitude()
                + "&daddr=" + lat + "," + lng + "&mode=driving";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.monitor_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Log.e(LOG, "####### onLocationChanged");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderEnabled(String provider) {

    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates
     * is called on an already disabled provider, this method is called
     * immediately.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(LOG, "################ onStart .... connect API and location clients ");
        if (!mResolvingError) {  // more about this later
            //mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        Log.w(LOG, "############## onStop stopping google service clients");
        try {
            //mGoogleApiClient.disconnect();
        } catch (Exception e) {
            Log.e(LOG, "Failed to Stop something", e);
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e(LOG, "########### onConnected .... what is in the bundle...?");


    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }


    List<BitmapDescriptor> bmdList = new ArrayList<BitmapDescriptor>();

    private void loadIcons() {
        try {
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_1));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_2));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_3));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_4));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_5));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_6));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_7));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_8));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_9));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_10));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_11));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_12));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_13));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_14));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_15));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_16));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_17));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_18));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_19));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_20));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_21));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_22));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_23));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_24));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_25));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_26));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_27));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_28));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_29));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_30));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_31));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_32));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_33));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_34));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_35));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_36));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_37));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_38));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_39));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_40));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_41));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_42));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_43));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_44));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_45));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_46));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_47));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_48));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_49));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_50));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_51));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_52));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_53));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_54));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_55));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_56));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_57));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_58));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_59));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_60));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_61));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_62));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_63));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_64));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_65));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_66));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_67));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_68));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_69));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_70));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_71));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_72));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_73));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_74));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_75));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_76));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_77));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_78));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_79));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_80));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_81));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_82));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_83));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_84));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_85));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_86));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_87));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_88));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_89));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_90));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_91));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_92));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_93));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_94));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_95));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_96));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_97));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_98));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_99));
            bmdList.add(BitmapDescriptorFactory.fromResource(R.drawable.number_100));
        } catch (Exception e) {
            Log.e(LOG, "Load icons failed", e);
        }


    }

    boolean coordsConfirmed;

    @Override
    public void onBackPressed() {
        Log.e(LOG, "######## onBackPressed, coordsConfirmed: " + coordsConfirmed);

        finish();
    }

    @Override
    public void onPause() {
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        super.onPause();
    }


}
