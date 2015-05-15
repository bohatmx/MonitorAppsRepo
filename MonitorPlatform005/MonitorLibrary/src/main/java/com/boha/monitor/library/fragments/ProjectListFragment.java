package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.internal.util.Predicate;
import com.boha.monitor.library.R;
import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.activities.MonitorMapActivity;
import com.boha.monitor.library.activities.PictureActivity;
import com.boha.monitor.library.adapters.ProjectAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.dto.transfer.PhotoUploadDTO;
import com.boha.monitor.library.dto.transfer.RequestDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.Util;
import com.squareup.leakcanary.RefWatcher;

import java.text.DecimalFormat;
import java.util.ArrayList;
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

    public interface ProjectListFragmentListener {
        public void onSiteListRequested(ProjectDTO project);

        public void onStatusReportRequested();
    }

    private ListView mListView;
    private TextView txtStatusCount, txtLabel;

    static final String LOG = ProjectListFragment.class.getSimpleName();
    ProjectDTO project;
    int statusCount;
    TextView txtName;



    public static ProjectListFragment newInstance(ResponseDTO r, int type) {
        ProjectListFragment fragment = new ProjectListFragment();
        Bundle args = new Bundle();
        args.putSerializable("response", r);
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    public ProjectListFragment() {
    }

    Context ctx;
    View topView;
    LayoutInflater inflater;
    View view, searchLayout, fab, editorLayout, headerLayout, handle;
    ImageView imgSearch1, imgSearch2, fabIcon;
    EditText editSearch, editName, editDesc;
    Button btnSave, btnCloseProject;
    TextView txtCount;
    public static final int PROJECT_TYPE = 1, OPERATIONS_TYPE = 2;
    int type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w(LOG, "######### onCreateView...");
        view = inflater.inflate(R.layout.fragment_project_list, container, false);
        this.inflater = inflater;
        ctx = getActivity();

        Bundle b = getArguments();
        if (b != null) {
            ResponseDTO r = (ResponseDTO) b.getSerializable("response");
            type = b.getInt("type", PROJECT_TYPE);
            if (r.getCompany() != null) {
                projectList = r.getCompany().getProjectList();
                statusCountInPeriod = r.getStatusCountInPeriod();
            }
        }
        setFields();
        setTotals();
        if (statusCountInPeriod != null) {
            txtStatusCount.setText(df.format(statusCountInPeriod));
        } else {
            txtStatusCount.setText("0");
        }
        setList();


        return view;
    }

    private void doThis() {

        Predicate<ProjectDTO> predicate = new Predicate<ProjectDTO>() {
            @Override
            public boolean apply(ProjectDTO projectDTO) {
                if (projectDTO.getCompleteFlag() == null || projectDTO.getCompleteFlag() > 0) {
                    return true;
                }
                return false;
            }
        };
        for (ProjectDTO dto: projectList) {
            if (predicate.apply(dto)) {
                Log.e(LOG, "#### project is active: " + dto.getProjectName());
            }
        }
    }
    public void updateStatusCount(int count) {
        Log.w(LOG, "### incrementing status count by " + count);
        statusCountInPeriod = count;
        txtStatusCount.setText(df.format(statusCountInPeriod));


        CacheUtil.getCachedData(ctx, CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized( ResponseDTO response) {
                if (response != null) {
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

    static final long ONE_HOUR = 1000 * 60 * 60,
            TWO_HOUR = ONE_HOUR * 2;

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
            SharedUtil.saveLastProjectID(ctx, projectList.get(index).getProjectID());
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
        changeHeroImage();
    }

    Integer statusCountInPeriod;
    ImageView heroImage;
    static final DecimalFormat df = new DecimalFormat("###,###,###,###");

    int lastIndex;

    public void changeHeroImage() {
        heroImage.setImageDrawable(Util.getRandomHeroImage(ctx));
    }


    public void setLastProject() {
        Integer id = SharedUtil.getLastProjectID(ctx);
        boolean isFound = false;
        int index = 0;
        if (id.intValue() > 0) {
            for (ProjectDTO p : projectList) {
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
        txtCount.setText("" + projectList.size());
        adapter = new ProjectAdapter(ctx, R.layout.project_item, projectList);
        mListView.setAdapter(adapter);
        if (projectList.size() > 5) {
            searchLayout.setVisibility(View.VISIBLE);
        } else {
            searchLayout.setVisibility(View.GONE);
        }


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                project = projectList.get(position);
                lastIndex = position;
                SharedUtil.saveLastProjectID(ctx, project.getProjectID());
                if (project.getProjectSiteList() == null || project.getProjectSiteList().isEmpty()) {
                    Util.showErrorToast(ctx, "Project has no sites defined. Please add the sites.");
                    return;
                }
                showPopup();


            }
        });
        setLastProject();
    }

    List<String> list;
    boolean popupIsOpen, isUpdate;
    Activity activity;


    private void showPopup() {
        activity = getActivity();
        list = new ArrayList<>();
        list.add(ctx.getString(R.string.site_list));
        list.add(ctx.getString(R.string.project_map));
        list.add(ctx.getString(R.string.take_picture));
        if (type == OPERATIONS_TYPE) {
            list.add(ctx.getString(R.string.edit_project));

        }

        Util.showPopupBasicWithHeroImage(ctx, activity, list,
                handle, project.getProjectName(), new Util.UtilPopupListener() {
                    @Override
                    public void onItemSelected(int index) {
                        switch (index) {
                            case 0:
                                mListener.onSiteListRequested(project);
                                break;

                            case 1:
                                if (!locationAvailable()) {
                                    Util.showToast(ctx, ctx.getString(R.string.no_location_found));
                                    return;
                                }
                                Intent i3 = new Intent(ctx, MonitorMapActivity.class);
                                i3.putExtra("project", project);
                                startActivity(i3);
                                break;
                            case 2:
                                Intent i4 = new Intent(ctx, PictureActivity.class);
                                i4.putExtra("project", project);
                                i4.putExtra("type", PhotoUploadDTO.PROJECT_IMAGE);
                                startActivity(i4);
                                break;
                            case 3:
                                editName.setText(project.getProjectName());
                                editDesc.setText(project.getDescription());
                                btnCloseProject.setVisibility(View.VISIBLE);
                                isUpdate = true;
                                openEditor();

                                break;
                        }
                    }
                });
        popupIsOpen = true;

    }

    private boolean locationAvailable() {

        for (ProjectSiteDTO site : project.getProjectSiteList()) {
            if (site.getLocationConfirmed() != null || site.getLatitude() != null) {
                return true;
            }
        }
        return false;
    }

    private void setFields() {
        handle = view.findViewById(R.id.PROJ_LIST_handle);
        editorLayout = view.findViewById(R.id.PROJ_LIST_editor);
        headerLayout = view.findViewById(R.id.PROJ_LIST_layoutx);
        editorLayout.setVisibility(View.GONE);
        btnSave = (Button) view.findViewById(R.id.PROJ_LIST_btnSave);
        btnCloseProject = (Button) view.findViewById(R.id.PROJ_LIST_btnCloseProject);
        editName = (EditText) view.findViewById(R.id.PROJ_LIST_editName);
        editDesc = (EditText) view.findViewById(R.id.PROJ_LIST_editDesc);
        txtCount = (TextView) view.findViewById(R.id.PROJ_LIST_count);
        topView = view.findViewById(R.id.topTop);
        fab = view.findViewById(R.id.FAB);
        fabIcon = (ImageView) view.findViewById(R.id.FAB_icon);
        searchLayout = view.findViewById(R.id.SLT_searchLayout);
        txtStatusCount = (TextView) view.findViewById(R.id.HERO_P_statusCount);
        heroImage = (ImageView) view.findViewById(R.id.HERO_P_image);
        imgSearch1 = (ImageView) view.findViewById(R.id.SLT_imgSearch1);
        imgSearch2 = (ImageView) view.findViewById(R.id.SLT_imgSearch);
        editSearch = (EditText) view.findViewById(R.id.SLT_editSearch);
        mListView = (ListView) view.findViewById(R.id.PROJ_LIST_list);
        //random hero
        heroImage.setImageDrawable(Util.getRandomHeroImage(ctx));
        if (type == PROJECT_TYPE) {
            fab.setVisibility(View.GONE);
        }

        txtStatusCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtStatusCount, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        mListener.onStatusReportRequested();
                    }
                });
            }
        });
        imgSearch2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(fab, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        if (editorLayout.getVisibility() == View.GONE) {
                            isUpdate = false;
                            openEditor();
                        } else {
                            Util.collapse(editorLayout, 500, null);
                            mListView.setVisibility(View.VISIBLE);
                            headerLayout.setVisibility(View.VISIBLE);
                            fabIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_action_new));
                        }
                    }
                });
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(btnSave, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        sendData();
                    }
                });
            }
        });
        btnCloseProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(btnCloseProject, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        AlertDialog.Builder f = new AlertDialog.Builder(getActivity());
                        f.setTitle("Confirmation")
                                .setMessage("Do you really want to close this project?")
                                .setPositiveButton(ctx.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Util.showToast(ctx, ctx.getString(R.string.under_cons));
                                        closeEditor();
                                    }
                                })
                                .setNegativeButton(ctx.getString(R.string.no), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        closeEditor();
                                    }
                                })
                                .show();
                    }
                });
            }
        });
    }

    private void openEditor() {
        btnCloseProject.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        Util.expand(editorLayout, 500, null);
        headerLayout.setVisibility(View.GONE);
        fabIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_action_overflow));
    }

    private void closeEditor() {
        Util.collapse(editorLayout, 500, null);
        mListView.setVisibility(View.VISIBLE);
        headerLayout.setVisibility(View.VISIBLE);
        fabIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_action_new));
    }

    public void sendData() {

        if (editName.getText().toString().isEmpty()) {
            Util.showErrorToast(ctx, ctx.getString(R.string.enter_project));
            return;
        }
        if (editDesc.getText().toString().isEmpty()) {
            Util.showErrorToast(ctx, ctx.getString(R.string.enter_project_desc));
            return;
        }
        RequestDTO req = new RequestDTO(RequestDTO.REGISTER_PROJECT);
        ProjectDTO dto = new ProjectDTO();
        dto.setProjectName(editName.getText().toString());
        dto.setCompanyID(SharedUtil.getCompany(ctx).getCompanyID());
        dto.setDescription(editDesc.getText().toString());
        req.setProject(dto);

        NetUtil.sendRequest(ctx, req, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse( final ResponseDTO response) {
                if (response.getStatusCode() == 0) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addProject(response.getProjectList().get(0));
                            Util.collapse(editorLayout, 500, null);
                            headerLayout.setVisibility(View.VISIBLE);
                            mListView.setVisibility(View.VISIBLE);
                            fabIcon.setImageDrawable(ctx.getResources()
                                    .getDrawable(R.drawable.ic_action_new));
                            CacheUtil.getCachedData(ctx, CacheUtil.CACHE_DATA,
                                    new CacheUtil.CacheUtilListener() {
                                        @Override
                                        public void onFileDataDeserialized( ResponseDTO r) {
                                            if (r != null) {
                                                r.getProjectList().add(response.getProjectList().get(0));
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
                    });

                }
            }

            @Override
            public void onError(final String message) {
                getActivity().runOnUiThread(new Runnable() {
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

    public void setTotals() {


        statusCount = 0;
        for (ProjectDTO p : projectList) {
            statusCount += p.getStatusCount();
        }

        txtStatusCount.setText("" + df.format(statusCount));
        animateHeroHeight();


    }

    @Override
    public void animateHeroHeight() {
        Util.fadeIn(topView);
        Util.rotateViewWithDelay(getActivity(),
                fab, 500, 1000, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        Util.flashOnce(txtStatusCount, 300, null);
                    }
                });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ProjectListFragmentListener) {
            mListener = (ProjectListFragmentListener) activity;
        } else {
            throw new ClassCastException("Host "
                    + activity.getLocalClassName() + " must implement ProjectListFragmentListener");
        }

    }


    ProjectListFragmentListener mListener;

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MonApp.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }


    public void addProject(ProjectDTO project) {
        if (projectList == null) {
            projectList = new ArrayList<>();
        }
        projectList.add(0, project);
        adapter.notifyDataSetChanged();
        txtCount.setText("" + projectList.size());

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
