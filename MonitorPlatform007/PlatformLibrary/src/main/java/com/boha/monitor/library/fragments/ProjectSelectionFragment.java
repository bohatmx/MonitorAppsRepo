package com.boha.monitor.library.fragments;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.boha.monitor.library.adapters.SelectionAdapter;
import com.boha.monitor.library.dto.MonitorDTO;
import com.boha.monitor.library.dto.MonitorProjectDTO;
import com.boha.monitor.library.dto.PersonProject;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.StaffDTO;
import com.boha.monitor.library.dto.StaffProjectDTO;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.OKHttpException;
import com.boha.monitor.library.util.OKUtil;
import com.boha.monitor.library.util.SharedUtil;
import com.boha.monitor.library.util.SimpleDividerItemDecoration;
import com.boha.monitor.library.util.Util;
import com.boha.platform.library.R;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.StreamHandler;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class ProjectSelectionFragment extends Fragment {

    List<ProjectDTO> projectList;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProjectSelectionFragment() {
    }


    List<MonitorProjectDTO> monitorProjectList;
    List<StaffProjectDTO> staffProjectList;
    RecyclerView recycler;
    SelectionAdapter adapter;
    TextView txtName, txtCount, sortByName, sortBySel;
    Button btnDone;
    AutoCompleteTextView auto;
    View view;
    int numberSelected;

    MonitorDTO monitor;
    StaffDTO staff;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_project_select_list, container, false);
        recycler = (RecyclerView) view.findViewById(R.id.recycler);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(llm);
        recycler.setHasFixedSize(true);
        recycler.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        setFields();


        return view;
    }

    public void setMonitor(MonitorDTO monitor) {
        this.monitor = monitor;
        if (view == null) return;
        txtName.setText(monitor.getFullName());
        getCompanyProjects();
    }

    public void setStaff(StaffDTO staff) {
        this.staff = staff;
        if (view == null) return;
        txtName.setText(staff.getFullName());
        getCompanyProjects();
    }

    private void getCompanyProjects() {
        RequestDTO w = new RequestDTO();
        if (monitor != null) {
            w.setRequestType(RequestDTO.GET_PROJECTS_FOR_MONITOR_ASSIGNMENTS);
            w.setCompanyID(SharedUtil.getCompany(getActivity()).getCompanyID());
            w.setMonitorID(monitor.getMonitorID());
        }
        if (staff != null) {
            w.setRequestType(RequestDTO.GET_PROJECTS_FOR_STAFF_ASSIGNMENTS);
            w.setCompanyID(SharedUtil.getCompany(getActivity()).getCompanyID());
            w.setStaffID(staff.getStaffID());
        }

        NetUtil.sendRequest(getActivity(), w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {
                projectList = response.getProjectList();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (monitor != null) {
                                monitorProjectList = response.getMonitorProjectList();
                                for (MonitorProjectDTO sp : monitorProjectList) {
                                    for (ProjectDTO proj : projectList) {
                                        if (proj.getProjectID().intValue() == sp.getProjectID().intValue()) {
                                            proj.setSelected(Boolean.TRUE);
                                            break;
                                        }
                                    }
                                }
                                setList();
                            }
                            if (staff != null) {
                                staffProjectList = response.getStaffProjectList();
                                for (StaffProjectDTO sp : staffProjectList) {
                                    for (ProjectDTO proj : projectList) {
                                        if (proj.getProjectID().intValue() == sp.getProjectID().intValue()) {
                                            proj.setSelected(Boolean.TRUE);
                                            break;
                                        }
                                    }
                                }
                                setList();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(final String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Util.showErrorToast(getActivity(), message);
                        }
                    });
                }
            }
        });

    }

    private void setList() {
        setTotalSelected();
        if (projectList.size() > 10) {
            projectNameList = new ArrayList<>(projectList.size());
            for (ProjectDTO p : projectList) {
                projectNameList.add(p.getProjectName());
            }
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_item, projectNameList);
            auto.setAdapter(adapter);
            auto.setHint("Search Projects");
            auto.setThreshold(2);
            auto.setVisibility(View.VISIBLE);

            auto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    hideKeyboard();
                    int index = 0;
                    String name = adapter.getItem(i);
                    for (ProjectDTO p : projectList) {
                        if (p.getProjectName().equalsIgnoreCase(name)) {
                            recycler.scrollToPosition(index);
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

        adapter = new SelectionAdapter(projectList, 0,
                new SelectionAdapter.ProjectSelectionListener() {
                    @Override
                    public void onProjectAdded(ProjectDTO project) {
                        setTotalSelected();
                    }

                    @Override
                    public void onProjectRemoved(ProjectDTO project) {
                        setTotalSelected();
                    }
                });
        recycler.setAdapter(adapter);
    }

    private void setTotalSelected() {
        numberSelected = 0;
        for (ProjectDTO p : projectList) {
            if (p.getSelected() != null) {
                if (p.getSelected() == Boolean.TRUE) {
                    numberSelected++;
                }
            }
        }
        animateCounter(0, numberSelected);

    }

    public void animateCounter(int initialValue, int finalValue) {

        ValueAnimator valueAnimator = ValueAnimator.ofInt((int) initialValue, (int) finalValue);
        valueAnimator.setDuration(1000);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                txtCount.setText(valueAnimator.getAnimatedValue().toString());

            }
        });
        valueAnimator.start();

    }

    List<String> projectNameList;

    private void setFields() {
        recycler = (RecyclerView) view.findViewById(R.id.recycler);
        txtCount = (TextView) view.findViewById(R.id.count);
        txtName = (TextView) view.findViewById(R.id.name);
        auto = (AutoCompleteTextView) view.findViewById(R.id.auto);
        btnDone = (Button) view.findViewById(R.id.btnDone);
        txtCount.setText("0");
        txtName.setText("");
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendProjectAssignments();
            }
        });
    }

    private void sendProjectAssignments() {
        if (numberSelected == 0) {
            Util.showErrorToast(getActivity(), "Please make some project assignment(s)");
            return;
        }
        RequestDTO w = new RequestDTO(RequestDTO.ADD_MONITOR_PROJECTS);
        if (staff != null) {
            w.setRequestType(RequestDTO.ADD_STAFF_PROJECTS);
            w.setStaffProjectList(new ArrayList<StaffProjectDTO>());
            for (ProjectDTO p : projectList) {
                if (p.getSelected() != null) {
                    if (p.getSelected() == Boolean.TRUE) {
                        StaffProjectDTO sp = new StaffProjectDTO();
                        sp.setStaffID(staff.getStaffID());
                        sp.setProjectID(p.getProjectID());
                        sp.setActiveFlag(Boolean.TRUE);
                        sp.setProjectName(p.getProjectName());
                        w.getStaffProjectList().add(sp);
                    }
                }
            }
        }
        if (monitor != null) {
            w.setMonitorProjectList(new ArrayList<MonitorProjectDTO>());
            for (ProjectDTO p : projectList) {
                if (p.getSelected() != null) {
                    if (p.getSelected() == Boolean.TRUE) {
                        MonitorProjectDTO sp = new MonitorProjectDTO();
                        sp.setMonitorID(monitor.getMonitorID());
                        sp.setProjectID(p.getProjectID());
                        sp.setActiveFlag(Boolean.TRUE);
                        w.getMonitorProjectList().add(sp);
                    }
                }
            }
        }


        OKUtil util = new OKUtil();
        try {
            util.sendPOSTRequest(getActivity(), w, new OKUtil.OKListener() {
                @Override
                public void onResponse(final ResponseDTO response) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (monitor != null) {
                                    monitor.setMonitorProjectList(response.getMonitorProjectList());
                                    mListener.onSelectionCompleteForMonitor(response.getMonitorProjectList());
                                }
                                if (staff != null) {
                                    staff.setStaffProjectList(response.getStaffProjectList());
                                    mListener.onSelectionCompleteForStaff(response.getStaffProjectList());
                                }

                            }
                        });
                    }
                }

                @Override
                public void onError(final String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Util.showErrorToast(getActivity(), message);
                            }
                        });
                    }
                }
            });
        } catch (final OKHttpException e) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Util.showErrorToast(getActivity(), e.getMessage());
                    }
                });
            }
        }
    }

    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(auto.getWindowToken(), 0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SelectionListener) {
            mListener = (SelectionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SelectionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    SelectionListener mListener;

    public interface SelectionListener {
        void onSelectionCompleteForStaff(List<StaffProjectDTO> list);

        void onSelectionCompleteForMonitor(List<MonitorProjectDTO> list);
    }

}
