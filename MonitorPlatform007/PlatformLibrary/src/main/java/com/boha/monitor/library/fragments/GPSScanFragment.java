package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        public void onStartScanRequested();
        public void onLocationConfirmed(ProjectDTO projectSite);
        public void onEndScanRequested();
        public void onMapRequested(ProjectDTO projectSite);


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

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w(LOG,"###### onCreateView");
        view = inflater.inflate(R.layout.fragment_gps, container, false);
        ctx = getActivity();
        setFields();

        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

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
        chronometer = (Chronometer)view.findViewById(R.id.GPS_chrono);
        gpsMessage.setVisibility(View.GONE);


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

    }
    private void confirmLocation() {
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
