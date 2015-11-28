package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.activities.PhotoListActivity;
import com.boha.monitor.library.adapters.StatusReportAdapter;
import com.boha.monitor.library.dto.PhotoUploadDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskDTO;
import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.Statics;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.boha.monitor.library.util.Util.showErrorToast;

public class StatusReportFragment extends Fragment implements PageFragment {



    public static StatusReportFragment newInstance(ResponseDTO r) {
        StatusReportFragment fragment = new StatusReportFragment();
        Bundle args = new Bundle();
        args.putSerializable("response", r);
        fragment.setArguments(args);
        return fragment;
    }


    public StatusReportFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    Context ctx;
    ImageView heroImage;
    ProjectDTO project;
    TextView txtTitle, txtCount, txtPhotoCount;
    ListView listView;
    LayoutInflater inflater;
    StatusReportAdapter adapter;
    Button btnStart, btnEnd;
    Date startDate, endDate;
    View view, handle, topView, photoLayout;
    static final Locale locale = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", locale);
    FloatingActionButton fab;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG, "########## onCreateView");
        this.inflater = inflater;
        ctx = getActivity();
        view = inflater.inflate(R.layout.fragment_status_list, container, false);
        setFields();



        return view;
    }


    private void setFields() {
        photoLayout = view.findViewById(R.id.STATLST_photoLayout);
        handle = view.findViewById(R.id.STATLST_handle);
        topView = view.findViewById(R.id.STATLST_topView);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        listView = (ListView) view.findViewById(R.id.STATLST_list);
        heroImage = (ImageView) view.findViewById(R.id.STATLST_heroImage);
        txtTitle = (TextView) view.findViewById(R.id.STATLST_txtTitle);
        txtPhotoCount = (TextView) view.findViewById(R.id.STATLST_txtPhotoCount);
        txtCount = (TextView) view.findViewById(R.id.STATLST_txtCount);
        btnEnd = (Button) view.findViewById(R.id.STATLST_endDate);
        btnStart = (Button) view.findViewById(R.id.STATLST_startDate);

        heroImage.setImageDrawable(Util.getRandomBackgroundImage(ctx));

        Statics.setRobotoFontBold(ctx, btnEnd);
        Statics.setRobotoFontBold(ctx, btnStart);
        Statics.setRobotoFontLight(ctx, txtTitle);
        setDatesx();


        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartDate = false;
                showDateDialog();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartDate = true;
                showDateDialog();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.setAlpha(0.3f);
                fab.setEnabled(false);
                getProjectStatus();
            }
        });
        txtCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent w = new Intent(getActivity(), PhotoListActivity.class);
                w.putExtra("project",project);
                startActivity(w);
            }
        });
        photoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent w = new Intent(getActivity(), PhotoListActivity.class);
                w.putExtra("project",project);
                startActivity(w);
            }
        });

    }

    boolean isStartDate;
    List<ProjectTaskStatusDTO> projectTaskStatusList;
    List<ProjectTaskDTO> projectTaskList;
    List<PhotoUploadDTO> photoUploadList;

    public void setProject(ProjectDTO project) {
        this.project = project;
        getCachedStatus();
    }

    public void getProjectStatus() {

        RequestDTO w = new RequestDTO(RequestDTO.GET_PROJECT_DATA);
        w.setProjectID(project.getProjectID());

        long diff = endDate.getTime() - startDate.getTime();
        long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        w.setNumberOfDays(Integer.parseInt("" + days));
        Log.e(LOG,"## number of days: " + w.getNumberOfDays());
        final Activity act = getActivity();

        mListener.setBusy(true);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.setBusy(false);
                        fab.setAlpha(1.0f);
                        fab.setEnabled(true);
                        if (response.getStatusCode() == 0) {
                            if (!response.getProjectList().isEmpty()) {
                                project = response.getProjectList().get(0);
                                projectTaskList = project.getProjectTaskList();
                                photoUploadList = project.getPhotoUploadList();

                                setList();
                                response.setStartDate(startDate.getTime());
                                response.setEndDate(endDate.getTime());
                                CacheUtil.cacheProjectStatus(ctx, response, project.getProjectID(), new CacheUtil.CacheUtilListener() {
                                    @Override
                                    public void onFileDataDeserialized(ResponseDTO response) {

                                    }

                                    @Override
                                    public void onDataCached() {

                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
                            } else {
                                getProjectStatus();
                            }
                        }
                    }
                });
            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListener.setBusy(false);
                        fab.setAlpha(1.0f);
                        fab.setEnabled(true);
                        showErrorToast(ctx, message);

                    }
                });
            }

            @Override
            public void onWebSocketClose() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        });

    }

    public void getCachedStatus() {

        CacheUtil.getCachedProjectStatus(ctx, project.getProjectID(), new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response != null) {
                    if (!response.getProjectList().isEmpty()) {
                        project = response.getProjectList().get(0);
                        projectTaskList = project.getProjectTaskList();
                        photoUploadList = project.getPhotoUploadList();
                        if (response.getStartDate() != null) {
                            btnStart.setText(sdf.format(response.getStartDate()));
                            btnEnd.setText(sdf.format(response.getEndDate()));
                            startDate = new Date(response.getStartDate());
                            endDate = new Date(response.getEndDate());
                        }
                        setList();
                        animateHeroHeight();
                    }
                }
                getProjectStatus();

            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {

            }
        });
    }

    private void setList() {
        Log.d(LOG, "########## setList");
        projectTaskStatusList = new ArrayList<>();
        int count = 0;
        for (ProjectTaskDTO projectTaskDTO: projectTaskList) {
            projectTaskStatusList.addAll(projectTaskDTO.getProjectTaskStatusList());
            if (projectTaskDTO.getPhotoUploadList() != null) {
                count += projectTaskDTO.getPhotoUploadList().size();
            }
        }
        if (project.getPhotoUploadList() != null) {
            count += project.getPhotoUploadList().size();
        }
        txtPhotoCount.setText("" + count);
        Collections.sort(projectTaskStatusList);
        txtCount.setText("" + projectTaskStatusList.size());
        adapter = new StatusReportAdapter(ctx, R.layout.status_report_card, projectTaskStatusList, false);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });


    }
    DatePickerDialog dpStart;
    int mYear, mMonth, mDay;

    private void showDateDialog() {
        final Calendar calendar = Calendar.getInstance();
        int xYear, xMth, xDay;
        if (mYear == 0) {
            xYear = calendar.get(Calendar.YEAR);
            xMth = calendar.get(Calendar.MONTH);
            xDay = calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            xYear = mYear;
            xMth = mMonth;
            xDay = mDay;
        }
        dpStart = new DatePickerDialog(ctx, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;

                calendar.set(Calendar.YEAR, mYear);
                calendar.set(Calendar.MONTH, mMonth);
                calendar.set(Calendar.DAY_OF_MONTH, mDay);

                if (isStartDate) {
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    startDate = calendar.getTime();
                } else {
                    endDate = calendar.getTime();
                }

                btnStart.setText(sdf.format(startDate));
                btnEnd.setText(sdf.format(endDate));

            }
        }, xYear, xMth, xDay);

        dpStart.show();


    }

    private void setDatesx() {
        Calendar cal = GregorianCalendar.getInstance();
        endDate = cal.getTime();
        cal.roll(Calendar.MONTH, false);
        startDate = cal.getTime();
        btnStart.setText(sdf.format(startDate));
        btnEnd.setText(sdf.format(endDate));

    }

    StatusReportListener mListener;
    @Override
    public void onAttach(Activity activity) {
        if (activity instanceof StatusReportListener) {
            mListener = (StatusReportListener)activity;
        } else {
            throw new ClassCastException("Host " + activity.getLocalClassName() +
            " must implement StatusReportListener");
        }
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }
    static final String LOG = StatusReportFragment.class.getSimpleName();

    @Override
    public void animateHeroHeight() {
        Util.fadeIn(topView);

    }
    String pageTitle;
    @Override
    public void setPageTitle(String title) {
        pageTitle = title;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }
    int primaryColor, darkColor;
    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }

    public interface StatusReportListener {
        void setBusy(boolean busy);
    }

}
