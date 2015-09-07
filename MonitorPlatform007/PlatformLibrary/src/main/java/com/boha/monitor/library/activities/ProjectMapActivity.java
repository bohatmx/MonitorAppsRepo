package com.boha.monitor.library.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import de.hdodenhof.circleimageview.CircleImageView;

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
    CircleImageView image;
    static final Locale loc = Locale.getDefault();
    List<ProjectDTO> projectList;
    public static final int STAFF = 1, MONITOR = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w(LOG, "#### onCreate");
        super.onCreate(savedInstanceState);
        ctx = getApplicationContext();
        setContentView(R.layout.activity_monitor_map);

        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        type = getIntent().getIntExtra("type", 0);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        text = (TextView) findViewById(R.id.text);

        image = (CircleImageView) findViewById(R.id.image);
        image.setVisibility(View.GONE);
        txtCount = (TextView) findViewById(R.id.count);
        txtCount.setText("0");

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
            setTitle(project.getProjectName());
            getSupportActionBar().setSubtitle("Project Location");
            setOneProjectMarker();
        } else {
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
                            setTitle(SharedUtil.getCompany(ctx).getCompanyName());
                            getSupportActionBar().setSubtitle("Project Locations");
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
                            setTitle(SharedUtil.getCompany(ctx).getCompanyName());
                            getSupportActionBar().setSubtitle("Project Locations");
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

    private void setProjectMarkers() {
        googleMap.clear();
        LatLng point = null;
        int index = 0, count = 0;
        ProjectTaskStatusDTO status = null;
        List<ProjectTaskStatusDTO> list = new ArrayList<>();
        for (ProjectDTO x : projectList) {
            for (ProjectTaskDTO y : x.getProjectTaskList()) {
                list.addAll(y.getProjectTaskStatusList());
            }
        }
        Collections.sort(list);
        if (!list.isEmpty()) {
            status = list.get(0);
        }

        Drawable drawable = null;
        index = 0;
        for (ProjectDTO site : projectList) {
            if (site.getLatitude() == null) continue;
            LatLng pnt = new LatLng(site.getLatitude(), site.getLongitude());
            point = pnt;
            drawable = Util.getMapIcon(ctx, index, site);
            Bitmap bitmap = null;
            BitmapDescriptor desc = null;
            Short color = null;
            if (status != null) {
                color = status.getTaskStatusType().getStatusColor();
                switch (color) {
                    case TaskStatusTypeDTO.STATUS_COLOR_RED:
                        drawable.setColorFilter(ContextCompat.getColor(ctx,R.color.red_800), PorterDuff.Mode.SRC_IN);
                        bitmap = Util.drawableToBitmap(drawable);
                        desc = BitmapDescriptorFactory.fromBitmap(bitmap);
                        break;
                    case TaskStatusTypeDTO.STATUS_COLOR_GREEN:
                        drawable.setColorFilter(ContextCompat.getColor(ctx,R.color.green_700), PorterDuff.Mode.SRC_IN);
                        bitmap = Util.drawableToBitmap(drawable);
                        desc = BitmapDescriptorFactory.fromBitmap(bitmap);
                        break;
                    case TaskStatusTypeDTO.STATUS_COLOR_AMBER:
                        drawable.setColorFilter(ContextCompat.getColor(ctx,R.color.amber_700), PorterDuff.Mode.SRC_IN);
                        bitmap = Util.drawableToBitmap(drawable);
                        desc = BitmapDescriptorFactory.fromBitmap(bitmap);
                        break;
                    default:
                        drawable.setColorFilter(ContextCompat.getColor(ctx,R.color.black), PorterDuff.Mode.SRC_IN);
                        bitmap = Util.drawableToBitmap(drawable);
                        desc = BitmapDescriptorFactory.fromBitmap(bitmap);
                        break;

                }

            } else {
                drawable.setColorFilter(ContextCompat.getColor(ctx,R.color.blue_400), PorterDuff.Mode.SRC_IN);
                bitmap = Util.drawableToBitmap(drawable);
                desc = BitmapDescriptorFactory.fromBitmap(bitmap);
                break;
            }
            Marker m = googleMap.addMarker(new MarkerOptions()
                    .title(site.getProjectID().toString())
                    .icon(desc)
                    .snippet(site.getProjectName())
                    .position(pnt));
            markers.add(m);
            index++;
            count++;
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
        if (projectList.get(0).getLatitude() == null) {
            return;
        }
        LatLng pnt = new LatLng(projectList.get(0).getLatitude(),
                projectList.get(0).getLongitude());
        BitmapDescriptor desc = BitmapDescriptorFactory.fromResource(R.drawable.number_1);
        Marker m =
                googleMap.addMarker(new MarkerOptions()
                        .title(projectList.get(0).getProjectID().toString())
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