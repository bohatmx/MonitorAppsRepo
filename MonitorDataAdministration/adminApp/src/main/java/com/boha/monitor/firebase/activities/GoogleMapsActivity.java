package com.boha.monitor.firebase.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.boha.monitor.firebase.R;
import com.boha.monitor.firebase.data.ProjectDTO;
import com.boha.monitor.firebase.util.DataUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.Date;

public class GoogleMapsActivity extends FragmentActivity
        implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    Location mLocation;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    boolean mRequestingLocationUpdates;
    ProjectDTO project;
    ProgressBar progressBar;
    Snackbar bar;
    Button tryAgain, confirmLocation;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);
        setFields();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        project = (ProjectDTO) getIntent().getSerializableExtra("project");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setFields() {
        tryAgain = (Button) findViewById(R.id.btnRepeat);
        confirmLocation = (Button) findViewById(R.id.btnConfirm);
        tryAgain.setVisibility(View.GONE);
        confirmLocation.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        confirmLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addConfirm();
            }
        });
        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationUpdates();
            }
        });
    }

    private void addConfirm() {
        project.setLatitude(mLocation.getLatitude());
        project.setLongitude(mLocation.getLongitude());

        DataUtil.addProjectLocation(project, new DataUtil.DataAddedListener() {
                    @Override
                    public void onResponse(String key) {
                        bar = Snackbar.make(tryAgain, "Project Location has been confirmed",
                                Snackbar.LENGTH_INDEFINITE);
                        bar.setActionTextColor(ContextCompat.getColor(
                                getApplicationContext(), R.color.green_200));
                        bar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bar.dismiss();
                                finish();
                            }
                        });
                        bar.show();
                    }

                    @Override
                    public void onError(String message) {
                        bar = Snackbar.make(tryAgain, message, Snackbar.LENGTH_INDEFINITE);
                        bar.setActionTextColor(ContextCompat.getColor(
                                getApplicationContext(), R.color.red_500));
                        bar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bar.dismiss();
                            }
                        });
                        bar.show();
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.w(TAG, "######### onStart");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "######### onStop");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.gps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG,
                "+++  onConnected() -  requestLocationUpdates ...");
        mLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLocation != null) {
            Log.w(TAG, "## requesting location updates ....lastLocation: "
                    + mLocation.getLatitude() + " "
                    + mLocation.getLongitude() + " acc: "
                    + mLocation.getAccuracy());
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(1000);

        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    public void startLocationUpdates() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }

        Log.w(TAG, "###### startLocationUpdates: " + new Date().toString());
        if (mGoogleApiClient.isConnected()) {
            mRequestingLocationUpdates = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();

                } else {
                    throw new UnsupportedOperationException();
                }
                return;
            }
        }
    }

    protected void stopLocationUpdates() {
        Log.w(TAG, "###### stopLocationUpdates - " + new Date().toString());
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            progressBar.setVisibility(View.GONE);
        }
    }


    static final float ACCURACY = 10;

    @Override
    public void onLocationChanged(Location loc) {
        Log.d(TAG, "## onLocationChanged accuracy = " + loc.getAccuracy()
                + " - " + new Date().toString());
        mLocation = loc;
        if (loc.getAccuracy() > ACCURACY) {
            return;
        }
        stopLocationUpdates();
        putMarker();
        tryAgain.setVisibility(View.VISIBLE);
        confirmLocation.setVisibility(View.VISIBLE);

    }

    private void putMarker() {
        IconGenerator gen = new IconGenerator(getApplicationContext());
        gen.setColor(ContextCompat.getColor(getApplicationContext(), R.color.green_700));
        gen.setTextAppearance(R.style.iconGenText);
        Bitmap bm = gen.makeIcon(project.getProjectName());
        BitmapDescriptor desc = BitmapDescriptorFactory.fromBitmap(bm);

        final LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        Marker m =
                mMap.addMarker(new MarkerOptions()
                        .title(project.getProjectName())
                        .icon(desc)
                        .position(latLng));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 1.0f));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    static final int MAP_REQUESTED = 9007;
    static final String TAG = GoogleMapsActivity.class.getSimpleName();

}
