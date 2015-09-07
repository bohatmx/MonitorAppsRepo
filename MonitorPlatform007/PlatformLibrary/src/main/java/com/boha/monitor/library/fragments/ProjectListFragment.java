package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.monitor.library.activities.MonitorMapActivity;
import com.boha.monitor.library.adapters.ProjectAdapter;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.platform.library.R;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProjectListFragmentListener} interface
 * to handle interaction events.
 * Use the {@link ProjectListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProjectListFragment extends Fragment implements PageFragment {


    private ProjectListFragmentListener mListener;
    private ResponseDTO mResponse;
    private View view;
    private ImageView image;
    private RecyclerView mRecyclerView;
    private TextView txtProgramme, txtProjectCount;

    private static final String LOG = ProjectListFragment.class.getSimpleName();

    public static ProjectListFragment newInstance(ResponseDTO response) {
        ProjectListFragment fragment = new ProjectListFragment();
        Bundle args = new Bundle();
        args.putSerializable("response", response);
        fragment.setArguments(args);
        return fragment;
    }

    public ProjectListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mResponse = (ResponseDTO) getArguments().getSerializable("response");
            projectList = mResponse.getProjectList();
        }
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
        view = inflater.inflate(R.layout.fragment_project_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setHasFixedSize(true);

        setList();
        return view;
    }

    ProjectAdapter projectAdapter;
    int index;

    private void setList() {

        if (projectList == null || projectList.isEmpty()) {
            Log.e(LOG,"--- projectList is NULL");
            return;
        }
        Integer pID = SharedUtil.getLastProjectID(getActivity());

        int index = 0;
        boolean isFound = false;
        if (pID != null) {
            for (ProjectDTO x : projectList) {
                if (x.getProjectID().intValue() == pID.intValue()) {
                    isFound = true;
                    break;
                }
                index++;
            }
        }
        projectAdapter = new ProjectAdapter(projectList, getActivity(),
                darkColor, new ProjectListFragmentListener() {
            @Override
            public void onCameraRequired(ProjectDTO project) {
                Log.d(LOG, "### onCameraRequired");
                mListener.onCameraRequired(project);
            }

            @Override
            public void onStatusUpdateRequired(ProjectDTO project) {
                Log.d(LOG, "### onStatusUpdateRequired");
                mListener.onStatusUpdateRequired(project);
            }

            @Override
            public void onLocationRequired(ProjectDTO project) {
                Log.d(LOG, "### onLocationRequired");
                mListener.onLocationRequired(project);
            }

            @Override
            public void onDirectionsRequired(ProjectDTO project) {
                Log.d(LOG, "### onDirectionsRequired");
                mListener.onDirectionsRequired(project);
            }

            @Override
            public void onMessagingRequired(ProjectDTO project) {
                Log.d(LOG, "### onMessagingRequired");
                mListener.onMessagingRequired(project);
            }

            @Override
            public void onGalleryRequired(ProjectDTO project) {
                Log.d(LOG, "### onGalleryRequired");
                mListener.onGalleryRequired(project);
            }

            @Override
            public void onStatusReportRequired(ProjectDTO project) {
                Log.d(LOG, "### onStatusReportRequired");
                mListener.onStatusReportRequired(project);
            }

            @Override
            public void onMapRequired(ProjectDTO project) {
                Log.i(LOG, "### onMapRequired");
                Intent w = new Intent(getActivity(), MonitorMapActivity.class);
                w.putExtra("project", project);
                startActivity(w);
            }
        });
        mRecyclerView.setAdapter(projectAdapter);

        if (index == 0) return;
        if (isFound) {
            if (index + 1 < projectList.size()) {
                mRecyclerView.scrollToPosition(index + 1);
            } else {
                mRecyclerView.scrollToPosition(index);
            }
        }


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
        Log.d(LOG, "### onDetach");
        mListener = null;
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
            for (ProjectDTO p : mResponse.getProjectList()) {
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
