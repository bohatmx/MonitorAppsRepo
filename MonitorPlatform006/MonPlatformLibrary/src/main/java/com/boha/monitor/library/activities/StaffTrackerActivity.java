package com.boha.monitor.library.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.dto.CompanyStaffDTO;
import com.boha.monitor.library.dto.LocationTrackerDTO;
import com.boha.monitor.library.dto.transfer.RequestDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.library.util.WebCheck;
import com.boha.monitor.library.util.WebCheckResult;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class StaffTrackerActivity extends AppCompatActivity {

    AutoCompleteTextView autoCompleteTextView;
    GoogleMap googleMap;
    DisplayMetrics displayMetrics;
    Context ctx;
    Location location;
    Marker marker;
    List<LocationTrackerDTO> locationTrackerList;
    LocationTrackerDTO locationTracker;
    List<CompanyStaffDTO> companyStaffList;
    AutoCompleteTextView autoCompleteTxt;
    View fab, topView;
    TextView title;
    ProgressBar progressBar;
    CompanyStaffDTO companyStaff;
    ImageView fabIcon, staffImage;
    boolean isStaffTracks;

    static final String LOG = StaffTrackerActivity.class.getSimpleName();
    static final Locale lox = Locale.getDefault();
    static final SimpleDateFormat sdate = new SimpleDateFormat("dd/MM/yyyy", lox);
    static final SimpleDateFormat stime = new SimpleDateFormat("HH:mm", lox);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_tracker);
        setFields();

        ResponseDTO r = (ResponseDTO) getIntent().getSerializableExtra("staffList");
        companyStaffList = r.getCompany().getCompanyStaffList();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.TRK_map);
        googleMap = mapFragment.getMap();
        if (googleMap == null) {
            Util.showToast(ctx, "Map is not available");
            finish();
            return;
        }
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay()
                .getMetrics(displayMetrics);

        getTrackerData();
        setGoogleMap();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(fab, 200, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        Util.fadeOut(staffImage, 500, new Util.UtilAnimationListener() {
                            @Override
                            public void onAnimationEnded() {
                                staffImage.setVisibility(View.GONE);
                            }
                        });
                        isStaffTracks = false;
                        title.setText(ctx.getString(R.string.team_trks));
                        getTrackerData();
                    }
                });
            }
        });
    }

    private void setFields() {
        fab = findViewById(R.id.FAB);
        fabIcon = (ImageView) findViewById(R.id.FAB_icon);
        staffImage = (ImageView) findViewById(R.id.TRK_staffImage);
        staffImage.setVisibility(View.GONE);
        topView = findViewById(R.id.TRK_top);
        title = (TextView) findViewById(R.id.TRK_title);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        autoCompleteTxt = (AutoCompleteTextView) findViewById(R.id.TRK_search);
        ctx = getApplicationContext();
        progressBar.setVisibility(View.GONE);
    }


    private void setGoogleMap() {
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);
        location = googleMap.getMyLocation();

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker m) {
                marker = m;
                if (locationTrackerList != null) {
                    for (LocationTrackerDTO loc : locationTrackerList) {
                        if (loc.getLocationTrackerID().intValue() == Integer.parseInt(marker.getTitle())) {
                            locationTracker = loc;
                            break;
                        }
                    }
                }
                showPopup();
                return true;
            }
        });

    }


    private void showPopup() {
        final List<String> list = new ArrayList<>();
        if (!isStaffTracks) {
            list.add(getString(R.string.show_tracks));
        }
        list.add(getString(R.string.get_dir));


        Util.showPopupStaffImage(ctx, this, list,
                locationTracker,
                title,
                isStaffTracks, new Util.UtilPopupListener() {
                    @Override
                    public void onItemSelected(int index) {

                        if (list.get(index).equalsIgnoreCase(
                                getString(R.string.show_tracks))) {
                            isStaffTracks = true;
                            title.setText(locationTracker.getStaffName());
                            String url = Util.getStaffImageURL(ctx, locationTracker.getCompanyStaffID());
                            ImageLoader.getInstance().displayImage(url, staffImage, new ImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String s, View view) {

                                }

                                @Override
                                public void onLoadingFailed(String s, View view, FailReason failReason) {
                                    staffImage.setVisibility(View.GONE);
                                }

                                @Override
                                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                                    staffImage.setVisibility(View.VISIBLE);
                                    Util.fadeIn(staffImage, 500);
                                }

                                @Override
                                public void onLoadingCancelled(String s, View view) {

                                }
                            });
                            List<LocationTrackerDTO> tList = new ArrayList<>();
                            int cnt = 0;
                            for (LocationTrackerDTO x : locationTrackerList) {
                                if (x.getCompanyStaffID() == locationTracker.getCompanyStaffID()) {
                                    tList.add(x);
                                    cnt++;
                                    if (cnt > MAX_TRACKER_EVENTS) {
                                        break;
                                    }
                                }
                            }
                            if (!tList.isEmpty()) {
                                setStaffTracks(tList);
                            }
                        }
                        if (list.get(index).equalsIgnoreCase(
                                getString(R.string.get_dir))) {
                            startDirectionsMap(locationTracker.getLatitude(),
                                    locationTracker.getLongitude());
                        }

                    }
                });

    }

    static final int MAX_TRACKER_EVENTS = 24;
    private void getCachedTrackerData() {
        CacheUtil.getCachedTrackerData(ctx, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                locationTrackerList = response.getLocationTrackerList();


                getTrackerData();
            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

    private void getTrackerData() {
        WebCheckResult wcr = WebCheck.checkNetworkAvailability(ctx, true);
        if (!wcr.isMobileConnected() && !wcr.isWifiConnected()) {
            getCachedTrackerData();
            return;
        }
        RequestDTO w = new RequestDTO(RequestDTO.GET_LOCATION_TRACK_BY_COMPANY_IN_PERIOD);
        w.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());

        progressBar.setVisibility(View.VISIBLE);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        if (response.getStatusCode() == 0) {
                            locationTrackerList = response.getLocationTrackerList();
                            //todo cache in different place :)
                            HashMap<Integer, LocationTrackerDTO> map = new HashMap<Integer, LocationTrackerDTO>();
                            for (LocationTrackerDTO dto : locationTrackerList) {
                                if (!map.containsKey(dto.getCompanyStaffID())) {
                                    map.put(dto.getCompanyStaffID(), dto);
                                }
                            }
                            List<LocationTrackerDTO> list = new ArrayList<LocationTrackerDTO>(map.values());
                            if (!list.isEmpty()) {
                                setMarkers(list);
                            }
                        }
                    }
                });

            }

            @Override
            public void onError(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showErrorToast(ctx, message);
                    }
                });
            }

            @Override
            public void onWebSocketClose() {

            }
        });

    }

    private void setMarkers( List<LocationTrackerDTO> list) {
        if (markers == null) {
            markers = new ArrayList<>();
        }
        Collections.reverse(list);
        googleMap.clear();
        LatLng pnt = null;
        View v = getLayoutInflater().inflate(R.layout.dot_black, null);
        TextView dotRed = (TextView) v.findViewById(R.id.DOT_textRed);
        TextView dotBlack = (TextView) v.findViewById(R.id.DOT_textBlack);
        TextView dotDate = (TextView) v.findViewById(R.id.DOT_date);
        TextView dotTime = (TextView) v.findViewById(R.id.DOT_time);
        int count = 1;
        for (LocationTrackerDTO dto : list) {
            pnt = new LatLng(dto.getLatitude(), dto.getLongitude());
            dotRed.setText("" + count);
            dotBlack.setText("" + count);
            dotDate.setText(sdate.format(dto.getDateTracked()));
            dotTime.setText(stime.format(dto.getDateTracked()));
            if (count == 1) {
                dotRed.setVisibility(View.VISIBLE);
                dotBlack.setVisibility(View.GONE);
            } else {
                dotRed.setVisibility(View.GONE);
                dotBlack.setVisibility(View.VISIBLE);
            }
            Bitmap bm = Util.createBitmapFromView(ctx, v, displayMetrics);
            BitmapDescriptor desc = BitmapDescriptorFactory.fromBitmap(bm);
            Marker m =
                    googleMap.addMarker(new MarkerOptions()
                            .title(""+dto.getLocationTrackerID().intValue())
                            .icon(desc)
                            .snippet(sdf.format(dto.getDateTracked()))
                            .position(pnt));
            markers.add(m);
            count++;
            Log.i(LOG, "## marker added: " + dto.getStaffName());
        }
        if (list.size() == 1) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pnt, 1.0f));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f));
            return;

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
                googleMap.animateCamera(cu);
            }
        });

    }
    private void setStaffTracks( List<LocationTrackerDTO> list) {
        if (markers == null) {
            markers = new ArrayList<>();
        }
        Collections.reverse(list);
        googleMap.clear();
        LatLng pnt = null;
        BitmapDescriptor desc = BitmapDescriptorFactory.fromResource(R.drawable.dot_red);
        int count = 1;
        for (LocationTrackerDTO dto : list) {
            pnt = new LatLng(dto.getLatitude(), dto.getLongitude());
            Marker m =
                    googleMap.addMarker(new MarkerOptions()
                            .title("" + dto.getLocationTrackerID().intValue())
                            .icon(desc)
                            .snippet(sdf.format(dto.getDateTracked()))
                            .position(pnt));
            markers.add(m);
            count++;
            Log.i(LOG, "## marker added: " + dto.getStaffName());
        }
        if (list.size() == 1) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pnt, 1.0f));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(12.0f));
            return;

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
                googleMap.animateCamera(cu);
            }
        });

    }

    private void startDirectionsMap(double lat, double lng) {
        location = googleMap.getMyLocation();
        Log.i(LOG, "startDirectionsMap ..........");
        String url = "http://maps.google.com/maps?saddr="
                + location.getLatitude() + "," + location.getLongitude()
                + "&daddr=" + lat + "," + lng + "&mode=driving";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    List<Marker> markers;
    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm", loc);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_staff_tracker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
