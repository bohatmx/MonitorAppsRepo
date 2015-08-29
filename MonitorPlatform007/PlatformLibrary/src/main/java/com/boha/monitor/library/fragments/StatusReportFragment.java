package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.adapters.PopupListAdapter;
import com.boha.monitor.library.adapters.StatusReportAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectTaskStatusDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Statics;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    TextView txtCount, txtSiteName;
    ImageView heroImage;
    ProjectDTO projectSite;
    ProjectDTO project;
    TextView txtTitle;
    Button btnProject;
    ListView listView;
    LayoutInflater inflater;
    StatusReportAdapter adapter;
    Button btnStart, btnEnd;
    Date startDate, endDate;
    View view, handle, topView;
    static final Locale locale = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", locale);
    List<ProjectDTO> projectList;
    ListPopupWindow popupWindow;
    ProgressBar progressBar;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG, "########## onCreateView");
        this.inflater = inflater;
        ctx = getActivity();
        view = inflater.inflate(R.layout.fragment_status_list, container, false);
        setFields();
        Bundle b = getArguments();
        if (b != null) {
            ResponseDTO r = (ResponseDTO) b.getSerializable("response");
            if (r.getCompany() == null) {
                return view;
            }
//            projectList = r.getCompany().getProjectList();
            Integer projectID = SharedUtil.getLastProjectID(ctx);
            if (projectID != null) {
                for (ProjectDTO dto : projectList) {
                    if (dto.getProjectID().intValue() == projectID.intValue()) {
                        project = dto;
                        btnProject.setText(project.getProjectName());
                        getCachedStatus();
                        break;
                    }
                }
            } else {
                if (projectList != null && !projectList.isEmpty()) {
                    project = projectList.get(0);
                    btnProject.setText(project.getProjectName());
                    getCachedStatus();
                }
            }
        }

        return view;
    }

    private void showPopup() {
        List<String> list = new ArrayList<>();
        for (ProjectDTO p : projectList) {
            list.add(p.getProjectName());
        }
        View v = Util.getHeroView(ctx, ctx.getString(R.string.select_proj));
        popupWindow = new ListPopupWindow(ctx);
        popupWindow.setPromptView(v);
        popupWindow.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);
        popupWindow.setAnchorView(handle);
        popupWindow.setHorizontalOffset(Util.getPopupHorizontalOffset(getActivity()));
        popupWindow.setWidth(Util.getPopupWidth(getActivity()));
        popupWindow.setModal(true);
        popupWindow.setAdapter(new PopupListAdapter(ctx, R.layout.xxsimple_spinner_item, list, false));
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                project = projectList.get(position);
                SharedUtil.saveLastProjectID(ctx, project.getProjectID());
                btnProject.setText(project.getProjectName());
                getProjectStatus();
                popupWindow.dismiss();
            }
        });
        popupWindow.show();
    }

    private void setFields() {
        handle = view.findViewById(R.id.STATLST_handle);
        topView = view.findViewById(R.id.STATLST_topView);
        progressBar = (ProgressBar) view.findViewById(R.id.STATLST_progress);
        progressBar.setVisibility(View.GONE);
        listView = (ListView) view.findViewById(R.id.STATLST_list);
        heroImage = (ImageView) view.findViewById(R.id.STATLST_heroImage);
        btnProject = (Button) view.findViewById(R.id.STATLST_btnProject);
        txtCount = (TextView) view.findViewById(R.id.STATLST_txtCount);
        txtTitle = (TextView) view.findViewById(R.id.STATLST_txtTitle);
        btnEnd = (Button) view.findViewById(R.id.STATLST_endDate);
        btnStart = (Button) view.findViewById(R.id.STATLST_startDate);
        txtSiteName = (TextView) view.findViewById(R.id.STATLST_txtTitle);

        heroImage.setImageDrawable(Util.getRandomHeroImage(ctx));

        Statics.setRobotoFontLight(ctx, btnProject);
        Statics.setRobotoFontBold(ctx, btnEnd);
        Statics.setRobotoFontBold(ctx, btnStart);
//        Statics.setRobotoFontLight(ctx, txtTitle);
        setDates();


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


        txtCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtCount, 200, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        getProjectStatus();
                        txtCount.setAlpha(1.0f);
                    }
                });

            }
        });
        btnProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(btnProject, 200, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        showPopup();
                    }
                });
            }
        });

    }

    boolean isStartDate;
    List<ProjectTaskStatusDTO> projectSiteTaskStatusList;

    public void getProjectStatus() {

        if (project == null) {
            Log.e(LOG,"--- project is NULL - getProjectStatus()");
            return;
        }
        final long start = System.currentTimeMillis();
        RequestDTO w = new RequestDTO(RequestDTO.GET_PROJECT_STATUS_IN_PERIOD);
        w.setProjectID(project.getProjectID());
        w.setEndDate(endDate.getTime());
        w.setStartDate(startDate.getTime());
        final Activity act = getActivity();

        progressBar.setVisibility(View.VISIBLE);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);

                        projectSiteTaskStatusList = response.getProjectTaskStatusList();
                        long end = System.currentTimeMillis();
                        Log.e(LOG, "######### project status call, took: " + Util.getElapsed(start, end) + " seconds" +
                                " - statusCount: " + projectSiteTaskStatusList.size());


                        txtCount.setText("" + projectSiteTaskStatusList.size());

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
                    }
                });
            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        showErrorToast(ctx, message);

                    }
                });
            }

            @Override
            public void onWebSocketClose() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });

    }

    public void getCachedStatus() {

        if (project == null) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        CacheUtil.getCachedProjectStatus(ctx, project.getProjectID(), new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized( ResponseDTO response) {
                progressBar.setVisibility(View.GONE);
                if (response != null) {
                    projectSiteTaskStatusList = response.getProjectTaskStatusList();
                    if (response.getStartDate() != null) {
                        btnStart.setText(sdf.format(response.getStartDate()));
                        btnEnd.setText(sdf.format(response.getEndDate()));
                        startDate = new Date(response.getStartDate());
                        endDate = new Date(response.getEndDate());
                    }
                    txtCount.setText("" + projectSiteTaskStatusList.size());
                    setList();
                    animateHeroHeight();
                }
                setDates();

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

        txtCount.setText("" + projectSiteTaskStatusList.size());
        adapter = new StatusReportAdapter(ctx, R.layout.status_report_card, projectSiteTaskStatusList, false);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                findProjectSite(projectSiteTaskStatusList.get(position));
            }
        });
//        listView.setOnScrollListener(new AbsListView.OnScrollListener(){
//            int mLastFirstVisibleItem = 0;
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                if (view.getId() == listView.getId()) {
//                    final int currentFirstVisibleItem = listView.getFirstVisiblePosition();
//
//                    if (currentFirstVisibleItem > mLastFirstVisibleItem) {
//                        //Util.collapse(topView,300, null);
//                        topView.setVisibility(View.GONE);
//                    } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
//                        //Util.expand(topView,300,null);
//                        topView.setVisibility(View.VISIBLE);
//                    }
//
//                    mLastFirstVisibleItem = currentFirstVisibleItem;
//                }
//            }
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                final ListView lw = listView;
//            }
//        });

    }
    private void findProjectSite( final ProjectTaskStatusDTO taskStatus) {
        final Integer projectID = project.getProjectID();
        if (projectID > 0) {
            CacheUtil.getCachedProjectData(ctx,projectID,new CacheUtil.CacheUtilListener() {
                @Override
                public void onFileDataDeserialized( ResponseDTO response) {
                    if (response.getProjectList() != null && !response.getProjectList().isEmpty()) {
                        for (ProjectDTO x: response.getProjectList()) {
                            if (x.getProjectID().intValue() == projectID.intValue()) {
                                project = x;
//                                if (project.getProjectSiteList() != null && !project.getProjectSiteList().isEmpty()) {
//                                    for (ProjectSiteDTO z: project.getProjectSiteList()) {
//                                        if (z.getProjectSiteID().intValue() == taskStatus.getProjectSiteID().intValue()) {
//                                            projectSite = z;
//                                            if (projectSite.getPhotoUploadList() != null && !projectSite.getPhotoUploadList().isEmpty()) {
//                                                Intent intent = new Intent(ctx, SitePictureGridActivity.class);
//                                                intent.putExtra("projectSite", projectSite);
//                                                startActivity(intent);
//                                            } else {
//                                                Log.w(LOG,"--- no pictures found for this site: " + projectSite.getProjectSiteName());
//                                                Util.showToast(ctx,"No pictures found for site " + projectSite.getProjectSiteName());
//                                            }
//                                            break;
//                                        }
//                                    }
//                                }
                            }
                        }
                        return;
                    }
                }

                @Override
                public void onDataCached() {

                }

                @Override
                public void onError() {

                }
            });
        }
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
                setDates();
                Util.flashSeveralTimes(txtCount, 200, 2, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        getProjectStatus();
                    }
                });
            }
        }, xYear, xMth, xDay);

        dpStart.show();
//
//        dpStart.setYearRange(2013, calendar.get(Calendar.YEAR));
//        Bundle args = new Bundle();
//        args.putInt("year", mYear);
//        args.putInt("month", mMonth);
//        args.putInt("day", mDay);
//
//        dpStart.setArguments(args);
//        dpStart.show(getFragmentManager(), "diagx");


    }

    private void setDates() {
        DateTime now = new DateTime();
        DateTime then = now.minusDays(30);

        if (startDate == null) {
            then = then.withHourOfDay(0);
            then = then.withMinuteOfHour(0);
            then = then.withSecondOfMinute(0);
            startDate = then.toDate();
        }
        if (endDate == null) {
            now = now.withHourOfDay(23);
            now = now.withMinuteOfHour(59);
            now = now.withSecondOfMinute(59);

            endDate = now.toDate();
        }
        btnStart.setText(sdf.format(startDate));
        btnEnd.setText(sdf.format(endDate));

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    @Override public void onDestroy() {
        super.onDestroy();
//        RefWatcher refWatcher = MonApp.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }

    TaskDTO task;
    static final String LOG = StatusReportFragment.class.getSimpleName();

    @Override
    public void animateHeroHeight() {
        Util.fadeIn(topView);
        Util.rotateViewWithDelay(getActivity(),
                txtCount,500,1000, new Util.UtilAnimationListener() {
            @Override
            public void onAnimationEnded() {
                Util.flashOnce(btnProject,300,null);
            }
        });
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

}
