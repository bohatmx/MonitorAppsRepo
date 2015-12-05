package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.RequestList;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.RequestCacheUtil;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.text.DecimalFormat;

/**
 * GPSScanFragment manages the process of establishing the
 * GPS coordinates for a project
 */
public class GPSScanFragment extends Fragment implements PageFragment {

    @Override
    public void animateHeroHeight() {

    }

    @Override
    public void setPageTitle(String title) {

    }

    @Override
    public String getPageTitle() {
        return null;
    }

    public interface GPSScanFragmentListener {
         void onStartScanRequested();
         void onLocationConfirmed(ProjectDTO projectSite);
         void onEndScanRequested();
         void onMapRequested(ProjectDTO projectSite);
        void setBusy(boolean busy);


    }


    private GPSScanFragmentListener listener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GPSScanFragment.
     */

    public static GPSScanFragment newInstance() {
        GPSScanFragment fragment = new GPSScanFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public GPSScanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    TextView desiredAccuracy, txtLat, txtCount,
            gpsMessage, txtLng, txtAccuracy, txtName;
    View view;
    SeekBar seekBar;
    boolean isScanning;
    ProjectDTO project;
    ImageView imgLogo, hero;
    Context ctx;
    Chronometer chronometer;
    Button btnScan;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w(LOG,"###### onCreateView");
        view = inflater.inflate(R.layout.fragment_gps, container, false);
        ctx = getActivity();
        setFields();



        return view;
    }
    private void setFields() {
        desiredAccuracy = (TextView) view.findViewById(R.id.GPS_desiredAccuracy);
        txtAccuracy = (TextView) view.findViewById(R.id.GPS_accuracy);
        txtLat = (TextView) view.findViewById(R.id.GPS_latitude);
        txtLng = (TextView) view.findViewById(R.id.GPS_longitude);
        txtCount = (TextView) view.findViewById(R.id.GPS_count);
        gpsMessage = (TextView) view.findViewById(R.id.GPS_message);
        seekBar = (SeekBar) view.findViewById(R.id.GPS_seekBar);
        imgLogo = (ImageView) view.findViewById(R.id.GPS_imgLogo);
        hero = (ImageView) view.findViewById(R.id.GPS_hero);
        txtName = (TextView) view.findViewById(R.id.GPS_siteName);
        btnScan = (Button) view.findViewById(R.id.GPS_btnStop);
        chronometer = (Chronometer)view.findViewById(R.id.GPS_chrono);
        gpsMessage.setVisibility(View.GONE);
        btnScan.setText(ctx.getString(R.string.start_gps));

        hero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.flashOnce(hero, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.start();
                        listener.onStartScanRequested();
                        btnScan.setText(ctx.getString(R.string.stop_gps));
                    }
                });
            }
        });
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(imgLogo, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        if (btnScan.getText().toString().contains(getString(R.string.start_gps))) {
                            chronometer.setBase(SystemClock.elapsedRealtime());
                            chronometer.start();
                            listener.onStartScanRequested();
                            btnScan.setText(ctx.getString(R.string.stop_gps));
                        } else {
                            chronometer.stop();
                            listener.onEndScanRequested();
                            btnScan.setText(ctx.getString(R.string.start_gps));
                        }
                    }
                });
            }
        });

        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(imgLogo, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        listener.onMapRequested(project);
                    }
                });
            }
        });

        txtAccuracy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtAccuracy, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        if (project.getAccuracy() == null) return;
                        listener.onMapRequested(project);
                    }
                });

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                desiredAccuracy.setText("" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar.setProgress(5);

    }

    /**
     * Confirm the location of the project and send the coordinates
     * to update the project on the backend
     */
    private void confirmLocation() {

        AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
        d.setTitle(ctx.getString(R.string.confirm_loc))
                .setMessage(ctx.getString(R.string.confirm_loc_of)
                        + project.getProjectName() + "\n" + project.getCityName())
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sendGPSCoordinates();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    /**
     * Update the location coordinates of a project
     * on the backend server
     */
    private void sendGPSCoordinates() {
        final RequestDTO w = new RequestDTO(RequestDTO.CONFIRM_LOCATION);
        w.setProjectID(project.getProjectID());
        w.setLatitude(project.getLatitude());
        w.setLongitude(project.getLongitude());
        w.setAccuracy(project.getAccuracy());

        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                listener.onLocationConfirmed(project);
            }

            @Override
            public void onError(String message) {
                addRequestToCache(w);
            }

            @Override
            public void onWebSocketClose() {

            }
        });

    }

    private void addRequestToCache(RequestDTO request) {
        RequestCacheUtil.addRequest(ctx, request, new RequestCacheUtil.RequestCacheListener() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onRequestAdded() {
                if (project == null) return;
                project.setLocationConfirmed(true);
                Log.e(LOG, "----onDataCached, onEndScanRequested - please stop scanning");
                listener.onEndScanRequested();
                listener.onLocationConfirmed(project);
            }

            @Override
            public void onRequestsRetrieved(RequestList requestList) {

            }
        });

    }

    int count;

    /**
     * Receive location from GoogleApiClient and update project
     * @see com.boha.monitor.library.activities.GPSActivity
     * @param location
     */
    public void setLocation( Location location) {
        count++;
        txtCount.setText("" + count);
        this.location = location;
        txtLat.setText("" + location.getLatitude());
        txtLng.setText("" + location.getLongitude());
        txtAccuracy.setText("" + location.getAccuracy());

        if (location.getAccuracy() <= seekBar.getProgress()) {
            isScanning = false;
            chronometer.stop();
            project.setLatitude(location.getLatitude());
            project.setLongitude(location.getLongitude());
            project.setAccuracy(location.getAccuracy());
            //
            listener.onEndScanRequested();
            Util.flashSeveralTimes(hero, 300, 2, new Util.UtilAnimationListener() {
                @Override
                public void onAnimationEnded() {
                    confirmLocation();
                }
            });
        }

    }


    static final DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
    @Override
    public void onAttach( Activity activity) {
        super.onAttach(activity);
        try {
            listener = (GPSScanFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " - Host activity" + activity.getLocalClassName()
                    + " must implement GPSScanFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        RefWatcher refWatcher = MonApp.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }
    Location location;
    static final String LOG = GPSScanFragment.class.getSimpleName();

    public void setProject(ProjectDTO project) {
        this.project = project;
        if (project != null)
            txtName.setText(project.getProjectName());

    }

    int primaryColor, darkColor;
    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }
}
