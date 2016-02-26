package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.activities.MonApp;
import com.boha.monitor.library.activities.ProjectMapActivity;
import com.boha.monitor.library.adapters.ProjectAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.SimpleDividerItemDecoration;
import com.boha.monitor.library.util.Snappy;
import com.boha.monitor.library.util.Statics;
import com.boha.platform.library.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProjectListFragmentListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class ProjectListFragment extends Fragment implements PageFragment {


    private ProjectListFragmentListener mListener;
    private ResponseDTO mResponse;
    private View view, top;
    private ImageView image;
    private RecyclerView mRecyclerView;
    private AutoCompleteTextView auto;
    private TextView txtProgramme, txtProjectCount;
    MonApp monApp;

    public MonApp getMonApp() {
        return monApp;
    }

    public void setMonApp(MonApp monApp) {
        this.monApp = monApp;
    }
    private static final String LOG = ProjectListFragment.class.getSimpleName();


    public ProjectListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(LOG, "### onSaveInstanceState");
        savedInstanceState.putSerializable("response", mResponse);
        super.onSaveInstanceState(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG, "onCreateView .......");
        view = inflater.inflate(R.layout.fragment_project_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        auto = (AutoCompleteTextView) view.findViewById(R.id.autocomplete_project);
        txtCount = (TextView) view.findViewById(R.id.count);
        Statics.setRobotoFontLight(getActivity(),txtCount);
        top = view.findViewById(R.id.top);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        mRecyclerView.setHasFixedSize(true);

        if (auto != null) {
            hideKeyboard();
        }

        return view;
    }

    ProjectAdapter projectAdapter;
    List<String> projectNameList;
    ProjectDTO selectedProject;
    TextView txtCount;


    @Override
    public void onResume() {
        Log.e(LOG,"------------------ onResume, getting projects ............");
        getProjectList();
        super.onResume();
    }
    public void getProjectList() {
        Log.w(LOG, "..... getProjectList .....from Snappy");

        Snappy.SnappyReadListener listener = new Snappy.SnappyReadListener() {
            @Override
            public void onDataRead(ResponseDTO response) {
                projectList = response.getProjectList();
                Log.e(LOG,"onDataRead: projectList: " + projectList.size());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setList();
                        }
                    });
                }
            }

            @Override
            public void onError(String message) {
                Log.e(LOG,"Failed to get projects: " + message);
            }
        };
        monApp.getSnappyDB();
        Snappy.getProjectList(monApp, listener);
    }

    private void setList() {

        if (projectList == null || projectList.isEmpty()) {
            Log.e(LOG, "--- projectList is NULL");
            return;
        }
        if (getContext() == null) {
            return;
        }
        txtCount.setText("" + projectList.size());
        if (projectList.size() > 2) {
            projectNameList = new ArrayList<>(projectList.size());
            for (ProjectDTO p : projectList) {
                projectNameList.add(p.getProjectName());
            }
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    R.layout.simple_spinner_item, projectNameList);
            auto.setAdapter(adapter);
            auto.setHint("Search Projects");
            auto.setThreshold(1);
            auto.setVisibility(View.VISIBLE);

            auto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    hideKeyboard();
                    int index = 0;
                    String name = adapter.getItem(i);
                    for (ProjectDTO p : projectList) {
                        if (p.getProjectName().equalsIgnoreCase(name)) {
                            mRecyclerView.scrollToPosition(index);
                            auto.setText("");
                            break;
                        }
                        index++;
                    }
                }
            });

        } else {
            auto.setVisibility(View.GONE);
        }

        projectAdapter = new ProjectAdapter(projectList, getActivity(),
                darkColor, new ProjectListFragmentListener() {
            @Override
            public void onCameraRequired(ProjectDTO project) {
                Log.d(LOG, "### onCameraRequired");
                selectedProject = project;
                mListener.onCameraRequired(project);
            }

            @Override
            public void onStatusUpdateRequired(ProjectDTO project) {
                Log.d(LOG, "### onStatusUpdateRequired");
                selectedProject = project;
                mListener.onStatusUpdateRequired(project);
            }

            @Override
            public void onLocationRequired(ProjectDTO project) {
                Log.d(LOG, "### onLocationRequired");
                selectedProject = project;
                mListener.onLocationRequired(project);
            }

            @Override
            public void onDirectionsRequired(ProjectDTO project) {
                Log.d(LOG, "### onDirectionsRequired");
                selectedProject = project;
                mListener.onDirectionsRequired(project);
            }

            @Override
            public void onMessagingRequired(ProjectDTO project) {
                Log.d(LOG, "### onMessagingRequired");
                selectedProject = project;
                mListener.onMessagingRequired(project);
            }

            @Override
            public void onGalleryRequired(ProjectDTO project) {
                Log.d(LOG, "### onGalleryRequired");
                selectedProject = project;
                mListener.onGalleryRequired(project);
            }

            @Override
            public void onStatusReportRequired(ProjectDTO project) {
                Log.d(LOG, "### onStatusReportRequired");
                selectedProject = project;
                mListener.onStatusReportRequired(project);
            }

            @Override
            public void onMapRequired(ProjectDTO project) {
                Log.i(LOG, "### onMapRequired");
                selectedProject = project;
                Intent w = new Intent(getActivity(), ProjectMapActivity.class);
                ResponseDTO responseDTO = new ResponseDTO();
                responseDTO.setProjectList(new ArrayList<ProjectDTO>());
                responseDTO.getProjectList().add(project);
                w.putExtra("projects", responseDTO);
                startActivity(w);
            }
        });
        mRecyclerView.setAdapter(projectAdapter);
        Integer pID = SharedUtil.getLastProjectID(getActivity());


        int index = 0;
        boolean isFound = false;
        mRecyclerView.scrollToPosition(0);
        if (pID != null) {
            for (ProjectDTO x : projectList) {
                if (x.getProjectID().intValue() == pID.intValue()) {
                    isFound = true;
                    break;
                }
                index++;
            }
        }


        if (isFound) {
            if (index + 1  < projectList.size()) {
                mRecyclerView.scrollToPosition(index + 1);
            }
        }


    }


    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(auto.getWindowToken(), 0);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ProjectListFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ProjectListFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        Log.d(LOG, "### onDetach");
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        RefWatcher refWatcher = MonApp.getRefWatcher(getActivity());
//        refWatcher.watch(this);
    }

    @Override
    public void animateHeroHeight() {
    }

    String pageTitle;

    @Override
    public void setPageTitle(String title) {
        pageTitle = title;
    }

    public void setLastProject() {
        Integer id = SharedUtil.getLastProjectID(getActivity());
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
            mRecyclerView.smoothScrollToPosition(index);
        }
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface ProjectListFragmentListener {
        void onCameraRequired(ProjectDTO project);

        void onStatusUpdateRequired(ProjectDTO project);

        void onLocationRequired(ProjectDTO project);

        void onDirectionsRequired(ProjectDTO project);


        void onMessagingRequired(ProjectDTO project);

        void onGalleryRequired(ProjectDTO project);

        void onStatusReportRequired(ProjectDTO project);

        void onMapRequired(ProjectDTO project);
    }

    List<ProjectDTO> projectList;
    int primaryColor, darkColor;

    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }

}
