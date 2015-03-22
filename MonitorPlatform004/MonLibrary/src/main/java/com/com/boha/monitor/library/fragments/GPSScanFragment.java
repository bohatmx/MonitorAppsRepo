package com.com.boha.monitor.library.fragments;

import android.animation.ObjectAnimator;
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
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.dto.ProjectSiteDTO;
import com.com.boha.monitor.library.dto.transfer.RequestDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.services.RequestCache;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.ErrorUtil;
import com.com.boha.monitor.library.util.RequestCacheUtil;
import com.com.boha.monitor.library.util.Statics;
import com.com.boha.monitor.library.util.Util;
import com.com.boha.monitor.library.util.WebSocketUtil;

import java.text.DecimalFormat;


public class GPSScanFragment extends Fragment implements PageFragment {

    @Override
    public void animateHeroHeight() {

    }

    public interface GPSScanFragmentListener {
        public void onStartScanRequested();
        public void onLocationConfirmed(ProjectSiteDTO projectSite);
        public void onEndScanRequested();
        public void onMapRequested(ProjectSiteDTO projectSite);


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
    Button btnScan, btnSave;
    View view;
    SeekBar seekBar;
    boolean isScanning;
    ProjectSiteDTO projectSite;
    ImageView imgLogo, hero;
    Context ctx;
    ObjectAnimator logoAnimator;
    long start, end;
    Chronometer chronometer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w(LOG,"###### onCreateView");

        view = inflater.inflate(R.layout.fragment_gps, container, false);
        ctx = getActivity();
        setFields();

        btnScan.setText(ctx.getString(R.string.stop_scan));
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
        btnSave = (Button) view.findViewById(R.id.GPS_btnSave);
        btnScan = (Button) view.findViewById(R.id.GPS_btnStop);
        seekBar = (SeekBar) view.findViewById(R.id.GPS_seekBar);
        imgLogo = (ImageView) view.findViewById(R.id.GPS_imgLogo);
        hero = (ImageView) view.findViewById(R.id.GPS_hero);
        txtName = (TextView) view.findViewById(R.id.GPS_siteName);
        chronometer = (Chronometer)view.findViewById(R.id.GPS_chrono);
        gpsMessage.setVisibility(View.GONE);

        btnSave.setVisibility(View.GONE);
        Statics.setRobotoFontBold(ctx, txtLat);
        Statics.setRobotoFontBold(ctx, txtLng);

        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(imgLogo,100,new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                       listener.onMapRequested(projectSite);
                    }
                });
            }
        });

        txtAccuracy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtAccuracy,100,new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        if (projectSite.getAccuracy() == null) return;
                        listener.onMapRequested(projectSite);
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
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(btnScan,100,new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        if (isScanning) {
                            listener.onEndScanRequested();
                            isScanning = false;
                            btnScan.setText(ctx.getString(R.string.start_scan));
                            chronometer.stop();
                        } else {
                            listener.onStartScanRequested();
                            isScanning = true;
                            btnScan.setText(ctx.getString(R.string.stop_scan));
                            chronometer.setBase(SystemClock.elapsedRealtime());
                            chronometer.start();
                            Util.collapse(btnSave,300,null);
                        }
                    }
                });

            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(btnSave,100,new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        sendGPSData();
                    }
                });

            }
        });
    }
    private void confirmLocation() {
        RequestDTO w = new RequestDTO(RequestDTO.CONFIRM_LOCATION);
        w.setProjectSiteID(projectSite.getProjectSiteID());
        w.setLatitude(projectSite.getLatitude());
        w.setLongitude(projectSite.getLongitude());
        w.setAccuracy(projectSite.getAccuracy());
        sendRequest(w);

    }

    private void sendRequest(final RequestDTO request) {
        WebSocketUtil.sendRequest(ctx, Statics.COMPANY_ENDPOINT, request, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage(final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.getStatusCode() > 0) {
                            addRequestToCache(request);
                        } else {
                            btnScan.setVisibility(View.GONE);
                            gpsMessage.setVisibility(View.VISIBLE);
                            Util.flashSeveralTimes(gpsMessage,200,3,null);
                        }
                    }
                });

            }

            @Override
            public void onClose() {

            }

            @Override
            public void onError(String message) {
                addRequestToCache(request);
            }
        });
    }
    private void addRequestToCache(RequestDTO request) {
        RequestCacheUtil.addRequest(ctx, request, new CacheUtil.CacheRequestListener() {
            @Override
            public void onDataCached() {
                if (projectSite == null) return;
                projectSite.setLocationConfirmed(1);
                Log.e(LOG, "----onDataCached, onEndScanRequested - please stop scanning");
                listener.onEndScanRequested();
                listener.onLocationConfirmed(projectSite);

            }

            @Override
            public void onRequestCacheReturned(RequestCache cache) {

            }

            @Override
            public void onError() {

            }
        });
    }



    private void sendGPSData() {

        RequestDTO w = new RequestDTO(RequestDTO.UPDATE_PROJECT_SITE);
        final ProjectSiteDTO site = new ProjectSiteDTO();
        site.setProjectSiteID(projectSite.getProjectSiteID());
        site.setLatitude(location.getLatitude());
        site.setLongitude(location.getLongitude());
        site.setAccuracy(location.getAccuracy());

        w.setProjectSite(site);

        WebSocketUtil.sendRequest(ctx, Statics.COMPANY_ENDPOINT, w, new WebSocketUtil.WebSocketListener() {
            @Override
            public void onMessage(final ResponseDTO response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!ErrorUtil.checkServerError(ctx, response)) {
                            return;
                        }
                        listener.onEndScanRequested();
                        site.setLocationConfirmed(1);
                        listener.onLocationConfirmed(site);
                        Log.w(LOG, "++++++++++++ project site location updated");
                    }
                });
            }

            @Override
            public void onClose() {

            }

            @Override
            public void onError(final String message) {
                Log.e(LOG, "---- ERROR websocket - " + message);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showErrorToast(ctx, message);
                    }
                });
            }
        });
    }

    int count;
    public void setLocation(Location location) {
        if (projectSite == null) {
            Log.e(LOG,"#### projectSite is NULL, verboten!");
            return;
        }
        count++;
        txtCount.setText("" + count);
        this.location = location;
        txtLat.setText("" + location.getLatitude());
        txtLng.setText("" + location.getLongitude());
        txtAccuracy.setText("" + location.getAccuracy());

        if (location.getAccuracy() == seekBar.getProgress()
                || location.getAccuracy() < seekBar.getProgress()) {
            isScanning = false;
            chronometer.stop();
            btnScan.setText(ctx.getString(R.string.start_scan));
            projectSite.setLatitude(location.getLatitude());
            projectSite.setLongitude(location.getLongitude());
            projectSite.setAccuracy(location.getAccuracy());
            //
            listener.onEndScanRequested();
            listener.onLocationConfirmed(projectSite);
            confirmLocation();
            return;
        }
        Util.flashSeveralTimes(hero,200,2, null);
    }

    static final DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (GPSScanFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " - Host activity" + activity.getLocalClassName() + " must implement GPSScanFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    Location location;
    static final String LOG = GPSScanFragment.class.getSimpleName();

    public void setProjectSite(ProjectSiteDTO projectSite) {
        this.projectSite = projectSite;
        if (projectSite != null)
            txtName.setText(projectSite.getProjectSiteName());
    }

    public ProjectSiteDTO getProjectSite() {
        return projectSite;
    }
}
