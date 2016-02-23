package com.boha.monitor.library.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.boha.platform.library.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProjectAssignmentActivityFragment extends Fragment {

    public ProjectAssignmentActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_project_assignment, container, false);
    }
}
