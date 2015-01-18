package com.com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.com.boha.monitor.library.activities.ClaimAndInvoicePagerActivity;
import com.com.boha.monitor.library.activities.MonitorMapActivity;
import com.com.boha.monitor.library.activities.PictureActivity;
import com.com.boha.monitor.library.activities.ProjectSitePagerActivity;
import com.com.boha.monitor.library.adapters.PopupListAdapter;
import com.com.boha.monitor.library.adapters.ProjectAdapter;
import com.com.boha.monitor.library.dto.CompanyDTO;
import com.com.boha.monitor.library.dto.ProjectDTO;
import com.com.boha.monitor.library.dto.ProjectSiteDTO;
import com.com.boha.monitor.library.dto.ProjectSiteTaskDTO;
import com.com.boha.monitor.library.dto.transfer.PhotoUploadDTO;
import com.com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.com.boha.monitor.library.util.CacheUtil;
import com.com.boha.monitor.library.util.SharedUtil;
import com.com.boha.monitor.library.util.Util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Manages a list of projects. Provides facility to select a project
 * and ask the host activity to take responsibility for listing the
 * sites that belong in the project.
 * Hosted by: ProjectPagerActivity, OperationsPagerActivity - these activities
 * must implement the ProjectListListener interface.
 * <p/>
 * Entry points: onCreaView
 */
public class ProjectListFragment extends Fragment implements PageFragment {


    private ListView mListView;
    private TextView txtProjectCount, txtStatusCount, txtLabel;

    static final String LOG = ProjectListFragment.class.getSimpleName();
    ProjectDTO project;
    int statusCount;
    TextView txtName;


    public static ProjectListFragment newInstance(ResponseDTO r) {
        ProjectListFragment fragment = new ProjectListFragment();
        Bundle args = new Bundle();
        args.putSerializable("response", r);
        args.putInt("type", ProjectListFragment.PROJECT_TYPE);
        fragment.setArguments(args);
        return fragment;
    }

    public ProjectListFragment() {
    }

    Context ctx;
    View topView;
    LayoutInflater inflater;
    View view;
    ImageView imgLogo, imgSearch1, imgSearch2;
    EditText editSearch;
    public static final int PROJECT_TYPE = 1, OPERATIONS_TYPE = 2;
    int type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w(LOG, "######### onCreateView...");
        view = inflater.inflate(R.layout.fragment_project_list, container, false);
        this.inflater = inflater;
        ctx = getActivity();
        setFields();
        Bundle b = getArguments();
        if (b != null) {
            ResponseDTO r = (ResponseDTO) b.getSerializable("response");
            type = b.getInt("type", PROJECT_TYPE);
            if (r.getCompany() != null) {
                projectList = r.getCompany().getProjectList();
                statusCountInPeriod = r.getStatusCountInPeriod();
            }
        }
        setTotals();
        setList();

        Date lastReminder = SharedUtil.getReminderTime(ctx);
        Date now = new Date();
        long delta = now.getTime() - lastReminder.getTime();
        if (delta > (ONE_DAY)) {
            Util.pretendFlash(txtLabel, 300, 5, new Util.UtilAnimationListener() {
                @Override
                public void onAnimationEnded() {
                    SharedUtil.saveReminderTime(ctx, new Date());
                    Util.showPagerToast(ctx, ctx.getString(R.string.swipe_for_more), ctx.getResources().getDrawable(R.drawable.arrow_right));
                }
            });
        }

        return view;
    }

    public void updateStatusCount(int count) {
        Log.w(LOG, "### incrementing status count by " + count);
        if (count == 0) return;
        if (statusCountInPeriod == null) {
            statusCountInPeriod = count;
        } else {
            statusCountInPeriod += count;
        }
        if (project.getStatusCount() == null) {
            project.setStatusCount(count);
        } else {
            project.setStatusCount(project.getStatusCount() + count);
        }
        setList();

        CacheUtil.getCachedData(ctx, CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized(ResponseDTO response) {
                if (response != null) {
                    response.getCompany().setProjectList(projectList);
                    response.setStatusCountInPeriod(statusCountInPeriod);
                    //write data back
                    CacheUtil.cacheData(ctx, response, CacheUtil.CACHE_DATA, null);
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

    public void refreshData(ResponseDTO resp) {
        Log.e(LOG, "##### refreshing data, projectList from host, count: " + resp.getStatusCountInPeriod());
        projectList = resp.getCompany().getProjectList();
        statusCountInPeriod = resp.getStatusCountInPeriod();
        setTotals();
        setList();
    }

    static final long ONE_DAY = 1000 * 60 * 60 * 60 * 24, TWO_HOUR = 1000 * 60 * 60 * 2;

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
    }

    private void search() {
        if (editSearch.getText().toString().isEmpty()) {
            return;
        }
        hideKeyboard();
        int index = 0;
        boolean isFound = false;
        for (ProjectDTO site : projectList) {
            if (site.getProjectName().contains(editSearch.getText().toString())) {
                isFound = true;
                break;
            }
            index++;
        }
        if (isFound) {
            mListView.setSelection(index);
            SharedUtil.saveLastProjectID(ctx,projectList.get(index).getProjectID());
        }

    }
    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    Integer statusCountInPeriod;
    ImageView heroImage;
    static final DecimalFormat df = new DecimalFormat("###,###,###,###");

    int lastIndex;
    public void refreshData(List<ProjectDTO> list) {
        projectList = list;
        setTotals();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            mListView.setSelection(lastIndex);
        } else {
            setList();
        }
        setLastProject();
    }

    private void setLastProject() {
        Integer id = SharedUtil.getLastProjectID(ctx);
        boolean isFound = false;
        int index = 0;
        if (id.intValue() > 0) {
            for (ProjectDTO p: projectList) {
                if (p.getProjectID().intValue() == id.intValue()) {
                    isFound = true;
                    break;
                }
                index++;
            }
        }
        if (isFound) {
            mListView.setSelection(index);
        }
    }
    private void setList() {
        adapter = new ProjectAdapter(ctx, R.layout.project_item, projectList);
        mListView.setAdapter(adapter);

        if (statusCountInPeriod != null) {
            txtStatusCount.setText(df.format(statusCountInPeriod));
        } else {
            txtStatusCount.setText("0");
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                project = projectList.get(position);
                lastIndex = position;
                SharedUtil.saveLastProjectID(ctx,project.getProjectID());
                if (project.getProjectSiteList() == null || project.getProjectSiteList().isEmpty()) {
                    Util.showErrorToast(ctx, "Project has no sites defined. Please add the sites.");
                    return;
                }
                showPopup();


            }
        });
        mListView.setSelection(lastIndex);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener(){
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                final ListView lw = mListView;

                if(scrollState == 0)
                    //Log.i(LOG, "scrolling stopped...");

                if (view.getId() == lw.getId()) {
                    final int currentFirstVisibleItem = lw.getFirstVisiblePosition();
                    if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                        mIsScrollingUp = false;
                        Log.i(LOG, "scrolling down...");
                        //Util.collapse(topView,500,null);
                    } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                        mIsScrollingUp = true;
                        Log.i(LOG, "scrolling up...");
                        //Util.expand(topView,500,null);
                    }

                    mLastFirstVisibleItem = currentFirstVisibleItem;
                }
            }
        });
    }

    boolean mIsScrollingUp;
    int mLastFirstVisibleItem;
    ListPopupWindow actionsWindow;
    List<String> list;

    private void showPopup() {

        list = new ArrayList<>();
        list.add(ctx.getString(com.boha.monitor.library.R.string.site_list));
        list.add(ctx.getString(com.boha.monitor.library.R.string.claims_invoices));
        list.add(ctx.getString(com.boha.monitor.library.R.string.project_map));
        list.add(ctx.getString(com.boha.monitor.library.R.string.take_picture));

        View v = getActivity().getLayoutInflater().inflate(com.boha.monitor.library.R.layout.hero_image, null);
        TextView cap = (TextView) v.findViewById(com.boha.monitor.library.R.id.HERO_caption);
        cap.setText(ctx.getString(com.boha.monitor.library.R.string.select_action));
        ImageView img = (ImageView) v.findViewById(com.boha.monitor.library.R.id.HERO_image);
        img.setImageDrawable(Util.getRandomHeroImage(ctx));

        actionsWindow = new ListPopupWindow(getActivity());
        actionsWindow.setPromptView(v);
        actionsWindow.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);
        actionsWindow.setAdapter(new PopupListAdapter(ctx,
                com.boha.monitor.library.R.layout.xxsimple_spinner_item, list, false));
        actionsWindow.setAnchorView(txtProjectCount);
        actionsWindow.setWidth(Util.getPopupWidth(getActivity()));
        actionsWindow.setHorizontalOffset(Util.getPopupHorizontalOffset(getActivity()));
        actionsWindow.setModal(true);
        try {
            actionsWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0:
                            Log.e(LOG, "Yeaaah! position: " + position);
                            Intent i = new Intent(ctx, ProjectSitePagerActivity.class);
                            CompanyDTO company = new CompanyDTO();
                            company.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());
                            i.putExtra("project", project);
                            i.putExtra("company", company);
                            i.putExtra("type", SiteTaskAndStatusAssignmentFragment.PROJECT_MANAGER);
                            startActivity(i);
                            break;
                        case 1:
                            Log.e(LOG, "Yeaaah! position: " + position);
                            Intent i2 = new Intent(ctx, ClaimAndInvoicePagerActivity.class);
                            i2.putExtra("project", project);
                            startActivity(i2);
                            break;
                        case 2:
                            Log.e(LOG, "Yeaaah! position: " + position);
                            Intent i3 = new Intent(ctx, MonitorMapActivity.class);
                            i3.putExtra("project", project);
                            startActivity(i3);
                            break;
                        case 3:
                            Log.e(LOG, "Yeaaah! position: " + position);
                            Intent i4 = new Intent(ctx, PictureActivity.class);
                            i4.putExtra("project", project);
                            i4.putExtra("type", PhotoUploadDTO.PROJECT_IMAGE);
                            startActivity(i4);
                            break;
                    }
                    actionsWindow.dismiss();
                }
            });
            actionsWindow.show();
        } catch (IllegalStateException e) {
            actionsWindow.dismiss();
        }
    }


    private void setFields() {
        //set fields
        topView = view.findViewById(R.id.topTop);
        imgLogo = (ImageView) view.findViewById(R.id.PROJ_LIST_img);
        txtStatusCount = (TextView) view.findViewById(R.id.HERO_P_statusCount);
        heroImage = (ImageView) view.findViewById(R.id.HERO_P_image);
        imgSearch1 = (ImageView) view.findViewById(R.id.SLT_imgSearch1);
        imgSearch2 = (ImageView) view.findViewById(R.id.SLT_imgSearch2);
        editSearch = (EditText) view.findViewById(R.id.SLT_editSearch);
        txtProjectCount = (TextView) view.findViewById(R.id.PROJ_LIST_projectCount);
        mListView = (ListView) view.findViewById(R.id.PROJ_LIST_list);
        //random hero
        heroImage.setImageDrawable(Util.getRandomHeroImage(ctx));


        imgSearch2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
        Util.expand(heroImage, 1000, null);
    }

    public void setTotals() {

        try {
            if (projectList == null) {
                txtProjectCount.setText("0");
            } else {
                txtProjectCount.setText("" + projectList.size());
            }
        } catch (Exception e) {
            Log.e(LOG, "--- ran aground ...", e);
            return;
        }
        statusCount = 0;
        for (ProjectDTO p : projectList) {
            for (ProjectSiteDTO ps : p.getProjectSiteList()) {
                for (ProjectSiteTaskDTO pst : ps.getProjectSiteTaskList()) {
                    statusCount += pst.getProjectSiteTaskStatusList().size();
                }
            }
        }

        txtStatusCount.setText("" + df.format(statusCount));
        animateCounts();


    }

    @Override
    public void animateCounts() {

        Util.animateRotationY(txtProjectCount, 500);
        //Util.animateScaleX(topView, 500);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void addProject(ProjectDTO project) {
        if (projectList == null) {
            projectList = new ArrayList<>();
        }
        projectList.add(0, project);
        //Collections.sort(engineerList);
        adapter.notifyDataSetChanged();
        txtProjectCount.setText("" + projectList.size());
        try {
            Thread.sleep(1000);
            Util.animateRotationY(txtProjectCount, 500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the taskStatusList is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }


    private List<ProjectDTO> projectList;
    private ProjectAdapter adapter;

}
