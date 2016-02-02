package com.boha.monitor.library.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.NetUtil;
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
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Displays a map showing the location(s) passed to the activity.
 * Tapping on the location icons on the maps pops up a list of possible actions
 *
 * @see LocationTrackerDTO
 */
public class MonitorMapActivity extends AppCompatActivity
        implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleMap googleMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest locationRequest;
    Location location;
    Context ctx;

    List<Marker> markers = new ArrayList<Marker>();
    static final String LOG = MonitorMapActivity.class.getSimpleName();
    boolean mResolvingError;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";

    int index;
    TextView text, txtCount, txtTime;
    View topLayout;
    List<LocationTrackerDTO> trackList;
    LocationTrackerDTO track;
    CircleImageView image;
    int themeDarkColor, themePrimaryColor;
    static final Locale loc = Locale.getDefault();

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
        setContentView(R.layout.activity_monitor_map);

        track = (LocationTrackerDTO) getIntent().getSerializableExtra("track");
        ResponseDTO w = (ResponseDTO) getIntent().getSerializableExtra("response");
        if (w != null) {
            trackList = w.getLocationTrackerList();
        }


        index = getIntent().getIntExtra("index", 0);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        text = (TextView) findViewById(R.id.text);
        image = (CircleImageView) findViewById(R.id.statusColor);
        txtCount = (TextView) findViewById(R.id.count);
        txtTime = (TextView) findViewById(R.id.time);
        txtCount.setText("0");
        txtTime.setVisibility(View.GONE);

        Statics.setRobotoFontBold(ctx, text);

        topLayout = findViewById(R.id.top);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);
        googleMap = mapFragment.getMap();
        if (googleMap == null) {
            Util.showToast(ctx, "map is not available");
            finish();
            return;
        }
        setGoogleMap();
        if (track != null) {
            getPersonPhotos();
            setPersonMarker();
            String name = "";
            if (track.getStaffName() != null) {
                name = track.getStaffName();
            }
            if (track.getMonitorName() != null) {
                name = track.getMonitorName();
            }
            final String dispName = name;
            Util.setCustomActionBar(ctx, getSupportActionBar(), dispName, "Location",
                    ContextCompat.getDrawable(ctx, R.drawable.glasses48));

            txtTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopup(track.getLatitude(), track.getLongitude(), dispName);
                }
            });
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopup(track.getLatitude(), track.getLongitude(), dispName);
                }
            });
        }
        if (trackList != null && !trackList.isEmpty()) {
            image.setVisibility(View.GONE);
            setTitle("Locations - " + trackList.get(0).getMonitorName());
            setLocationMarkers();
        }
    }

    Activity activity;
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy HH:mm");

    /**
     * Prepare the map for display
     */
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

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng latLng = marker.getPosition();
                Location loc = new Location(location);
                loc.setLatitude(latLng.latitude);
                loc.setLongitude(latLng.longitude);

                float mf = location.distanceTo(loc);
                Log.w(LOG, "######### distance, again: " + mf);

                showPopup(latLng.latitude, latLng.longitude,
                        marker.getTitle());

                return true;
            }
        });


    }

    static final SimpleDateFormat fTime = new SimpleDateFormat("HH:mm");
    static final SimpleDateFormat fDate = new SimpleDateFormat("dd MMMM yyyy");
    DisplayMetrics displayMetrics;


    /**
     * Set location markers on the Google map
     */
    private void setLocationMarkers() {
        googleMap.clear();
        int index = 0, count = 0;

        for (LocationTrackerDTO track : trackList) {
            if (track.getLatitude() == null) continue;
            doMarker(track);
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

    private void setPersonMarker() {

        LatLng pnt = new LatLng(track.getLatitude(), track.getLongitude());
        doMarker(track);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pnt, 1.0f));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(13.0f));
    }

    private void doMarker(LocationTrackerDTO track) {
        LatLng pnt = new LatLng(track.getLatitude(), track.getLongitude());
        View view = getLayoutInflater().inflate(R.layout.location_track_item, null);
        TextView date = (TextView) view.findViewById(R.id.LTI_date);
        TextView time = (TextView) view.findViewById(R.id.LTI_time);
        TextView name = (TextView) view.findViewById(R.id.LTI_name);
        TextView model = (TextView) view.findViewById(R.id.LTI_model);
        TextView manuf = (TextView) view.findViewById(R.id.LTI_device);
        View deviceLayout = view.findViewById(R.id.LTI_bottom2);
        if (track.getGcmDevice() != null) {
            deviceLayout.setVisibility(View.VISIBLE);
            manuf.setText(track.getGcmDevice().getManufacturer());
            model.setText(track.getGcmDevice().getModel());
        } else {
            deviceLayout.setVisibility(View.GONE);
        }
        if (track.getStaffName() != null) {
            name.setText(track.getStaffName());
        }
        if (track.getMonitorName() != null) {
            name.setText(track.getMonitorName());
        }
        if (track.getStaffName() != null) {
            name.setText(track.getMonitorName());
        }
        date.setText(fDate.format(new Date(track.getDateTracked())));
        time.setText(fTime.format(new Date(track.getDateTracked())));

        Bitmap bmBitmap = Util.createBitmapFromView(ctx, view, displayMetrics);
        BitmapDescriptor desc = BitmapDescriptorFactory.fromBitmap(bmBitmap);
        Marker m =
                googleMap.addMarker(new MarkerOptions()
                        .title(name.getText().toString())
                        .icon(desc)
                        .snippet(name.getText().toString())
                        .position(pnt));
        markers.add(m);
    }

    List<String> list;

    private void showPopup(final double lat, final double lng, String title) {
        list = new ArrayList<>();
        list.add("Directions");
        list.add("Street View");
        list.add("Status Report");

        Util.showPopupBasicWithHeroImage(ctx, this, list, topLayout,
                title,
                new Util.UtilPopupListener() {
                    @Override
                    public void onItemSelected(int index) {
                        if (list.get(index).equalsIgnoreCase("Directions")) {
                            startDirectionsMap(lat, lng);
                        }
                        if (list.get(index).equalsIgnoreCase("Street View")) {
                            if (track != null) {
                                getStreetView(track.getLatitude(), track.getLongitude());
                            }
                        }

                        if (list.get(index).equalsIgnoreCase("Status Report")) {
                            getStatusReport();
                        }
                    }
                });


    }

    boolean isStatusReport, isGallery;

    private void getStreetView(double latitude, double longitude) {
        StringBuilder sb = new StringBuilder();
        sb.append("google.streetview:cbll=");
        sb.append(latitude).append(",").append(longitude);
        Uri gmmIntentUri = Uri.parse(sb.toString());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    private void getStatusReport() {

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
            mGoogleApiClient.disconnect();
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

    private void getPersonPhotos() {

        RequestDTO w = new RequestDTO();
        if (track.getMonitorID() != null) {
            w.setRequestType(RequestDTO.GET_MONITOR_PHOTOS);
            w.setMonitorID(track.getMonitorID());
            Log.d(LOG, ".............getPersonPhotos for Monitor");
        }
        if (track.getStaffID() != null) {
            w.setRequestType(RequestDTO.GET_STAFF_PHOTOS);
            w.setStaffID(track.getStaffID());
            Log.d(LOG, ".............getPersonPhotos for Staff");
        }


        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(ResponseDTO response) {
                if (response.getStatusCode() == 0) {
                    if (!response.getPhotoUploadList().isEmpty()) {
                        String url = response.getPhotoUploadList().get(0).getUri();
                        Picasso.with(ctx).load(url).fit().into(image);
                    }
                }
            }

            @Override
            public void onError(String message) {
                Log.e("MonitorMapActivity", message);
            }

            @Override
            public void onWebSocketClose() {

            }
        });
    }

}
