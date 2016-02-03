package com.boha.monitor.setup.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.boha.monitor.library.dto.ProgrammeDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.RequestDTO;
import com.boha.monitor.library.dto.ResponseDTO;
import com.boha.monitor.library.dto.SubTaskDTO;
import com.boha.monitor.library.dto.TaskDTO;
import com.boha.monitor.library.dto.TaskTypeDTO;
import com.boha.monitor.library.fragments.PageFragment;
import com.boha.monitor.library.util.ImportUtil;
import com.boha.monitor.library.util.NetUtil;
import com.boha.monitor.library.util.Statics;
import com.boha.monitor.library.util.Util;
import com.boha.monitor.library.util.bean.ImportException;
import com.boha.monitor.setup.R;
import com.boha.monitor.setup.adapters.ProjectImportAdapter;
import com.boha.monitor.setup.adapters.TaskImportAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


public class FileImportFragment extends Fragment implements PageFragment {

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

    public interface ImportListener {
        void onTasksImported(List<TaskTypeDTO> taskTypeList);

        void onProjectsImported(List<ProjectDTO> projectList);

        void onError(String message);

        void setBusy(boolean busy);
    }

    ImportListener listener;
    View view;
    Context ctx;
    TextView txtTitle, txtCount, txtLabel;
    Spinner fileSpinner;
    Button btnImport;
    ListView list;
    List<TaskTypeDTO> taskTypeList;
    List<ProjectDTO> projectList;
    List<File> files = new ArrayList<File>();
    ImageView image;

    public ListView getList() {
        return list;
    }

    int index = 0, pageCnt = 0, totalPages = 0;


    @Override
    public void onAttach(Activity a) {
        if (a instanceof ImportListener) {
            listener = (ImportListener) a;
        } else {
            throw new UnsupportedOperationException("Host activity " +
                    a.getLocalClassName() + " must implement ImportListener");
        }
        super.onAttach(a);
    }

    public FileImportFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_import_tasks, container, false);
        ctx = getActivity();
        setFields();
        files = ImportUtil.getImportFilesOnSD();
        files.addAll(ImportUtil.getImportFiles());

        setSpinner();
        Log.w(LOG, "++++++++ Import files found: " + files.size());
        return view;
    }

    ProgrammeDTO programme;
    int importType;
    public static final int IMPORT_TASKS = 1, IMPORT_PROJECTS = 2;


    public void setImportType(int importType) {
        this.importType = importType;

        switch (importType) {
            case IMPORT_PROJECTS:
                txtTitle.setText("Projects");
                break;
            case IMPORT_TASKS:
                txtTitle.setText("Task Types");
                break;
        }
    }

    public void setProgramme(ProgrammeDTO programme) {
        this.programme = programme;
    }

    private void setFields() {
        fileSpinner = (Spinner) view.findViewById(R.id.IMP_fileSpinner);
        btnImport = (Button) view.findViewById(R.id.IMP_btnImport);
        txtCount = (TextView) view.findViewById(R.id.IMP_count);
        txtTitle = (TextView) view.findViewById(R.id.IMP_countLabel);
        image = (ImageView) view.findViewById(R.id.IMP_image);
        list = (ListView) view.findViewById(R.id.IMP_list);

        Statics.setRobotoFontLight(ctx, txtTitle);


        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int rem = 0;
                switch (importType) {
                    case IMPORT_TASKS:
                        if (taskTypeList == null || taskTypeList.isEmpty()) {
                            Util.showErrorToast(ctx, "Tasks Import Not Found");
                            return;
                        }
                        totalPages = taskTypeList.size() / PAGE_SIZE;
                        rem = taskTypeList.size() % PAGE_SIZE;
                        break;
                    case IMPORT_PROJECTS:
                        if (projectList == null || projectList.isEmpty()) {
                            Util.showErrorToast(ctx, "Projects Import Not Found");
                            return;
                        }
                        totalPages = projectList.size() / PAGE_SIZE;
                        rem = projectList.size() % PAGE_SIZE;
                        break;
                }


                if (rem > 0) {
                    totalPages++;
                }
                sendData();
            }
        });


    }

    private void parseProjectsFile(File file) throws IOException, ImportException {
        listener.setBusy(true);
        projectList = new ArrayList<>();

        BufferedReader brReadMe = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8"));
        String strLine = brReadMe.readLine();
        while (strLine != null) {
            if (isEmptyLine(strLine)) {
                strLine = brReadMe.readLine();
                continue;
            }
            if (strLine.contains("CITYID")) {
                strLine = brReadMe.readLine();
                continue;
            }

            ProjectDTO project = parseProject(strLine);
            if (project != null) {
                projectList.add(parseProject(strLine));
            }
            strLine = brReadMe.readLine();
        }

        brReadMe.close();
        setProjectList();
        listener.setBusy(false);
        Log.i(LOG, "####### Project list has been imported into app, found: " + projectList.size());
    }

    private ProjectDTO parseProject(String line) throws ImportException {
        Pattern patt = Pattern.compile(";");
        String[] result = patt.split(line);
        ProjectDTO dto = new ProjectDTO();
        try {
            dto.setCityID(Integer.parseInt(result[0]));
            dto.setProjectName(result[1]);
            dto.setProgrammeID(programme.getProgrammeID());
        } catch (Exception e) {
            Log.e(LOG, "---- ERROR parseProject failed", e);
            return null;
        }

        Log.d(LOG,
                "Found project: " + dto.getProjectName() + " to import");
        return dto;
    }

    private void parseTasksFile(File file) throws IOException, ImportException {
        listener.setBusy(true);
        taskTypeList = new ArrayList<>();

        String currentSection = "NO SECTION";
        BufferedReader brReadMe = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), "UTF-8"));
        String strLine = brReadMe.readLine();
        while (strLine != null) {
            Log.d(LOG,"--------- starting at top of while ....");
            if (isEmptyLine(strLine)) {
                strLine = brReadMe.readLine();
                continue;
            }
            if (isSection(strLine)) {
                currentSection = getSection(strLine);
                strLine = brReadMe.readLine();
                continue;
            }

            if (isTaskType(strLine)) {
                TaskTypeDTO taskType = parseTaskType(strLine, currentSection);
                if (taskType != null) {
                    taskTypeList.add(taskType);
                    Log.w(LOG,"## taskTypde added to list: " + taskType.getTaskTypeName() + " list: " + taskTypeList.size() + " section: " + currentSection);
                }
                strLine = brReadMe.readLine();
                continue;
            }
            if (isTask(strLine)) {
                int index = taskTypeList.size() - 1;
                parseTask(taskTypeList.get(index), strLine);
                strLine = brReadMe.readLine();
                continue;
            }
            if (isSubTask(strLine)) {
                int index = taskTypeList.size() - 1;
                int index2 = taskTypeList.get(index).getTaskList().size() - 1;
                TaskDTO task = taskTypeList.get(index).getTaskList().get(index2);
                parseSubTask(task, strLine);
                strLine = brReadMe.readLine();
                continue;
            }
            strLine = brReadMe.readLine();
        }

        Log.d(LOG,"################# I DID GET HERE .....");
        brReadMe.close();
        setTaskList();
        listener.setBusy(false);
        Log.i(LOG, "####### TaskType list has been imported into app, found: " + taskTypeList.size());
    }

    private String getSection(String line) throws ImportException {
        Pattern patt = Pattern.compile(";");
        String[] result = patt.split(line);
        try {
            String column1 = result[0];
            String column2 = result[1];
            String column3 = result[2];
            if (column1.isEmpty() && column2.isEmpty()) {
                if (column3.contains("SECTION")) {
                    return column3;
                }
            }

        } catch (Exception e) {
            Log.e(LOG, "---- ERROR parse failed", e);
        }
        return null;
    }

    private boolean isSection(String line) throws ImportException {
        Pattern patt = Pattern.compile(";");
        String[] result = patt.split(line);
        try {
            String column1 = result[0];
            String column2 = result[1];
            String column3 = result[2];
            if (column1.isEmpty() && column2.isEmpty()) {
                if (column3.contains("SECTION")) {
                    return true;
                }
            }

        } catch (Exception e) {
            Log.e(LOG, "---- ERROR parse failed", e);
        }
        return false;
    }

    private boolean isTaskType(String line) throws ImportException {
        Pattern patt = Pattern.compile(";");
        String[] result = patt.split(line);
        try {
            String column1 = result[0];
            if (!column1.isEmpty()) {
                return true;
            }

        } catch (Exception e) {
            Log.e(LOG, "---- ERROR parse failed", e);
        }
        return false;
    }

    private boolean isTask(String line) throws ImportException {
        Pattern patt = Pattern.compile(";");
        boolean OK = false;
        String[] result = patt.split(line);
        try {
            String column1 = result[0];
            String column2 = result[1];
            if (column1.isEmpty()) {
                if (!column2.isEmpty()) {
                    OK = true;
                }
            }

        } catch (Exception e) {
            Log.e(LOG, "---- ERROR parse failed", e);
        }
        return OK;
    }

    private boolean isSubTask(String line) throws ImportException {
        Pattern patt = Pattern.compile(";");
        boolean OK = false;
        String[] result = patt.split(line);
        String column1 = result[0];
        String column2 = result[1];
        String column3 = result[2];
        try {
            if (column1.isEmpty()) {
                if (column2.isEmpty()) {
                    if (!column3.isEmpty()) {
                        OK = true;
                    }
                }
            }

        } catch (Exception e) {
            Log.e(LOG, "---- ERROR parse failed", e);
        }
        return OK;
    }

    private boolean isEmptyLine(String line) throws ImportException {
        Pattern patt = Pattern.compile(";");
        boolean OK = false;
        String[] result = patt.split(line);
        try {
            if (result.length == 0) {
                OK = true;
            }

        } catch (Exception e) {
            Log.e(LOG, "---- ERROR parse failed", e);
        }
        return OK;
    }

    private TaskTypeDTO parseTaskType(String line, String section) throws ImportException {
        Pattern patt = Pattern.compile(";");
        String[] result = patt.split(line);
        TaskTypeDTO dto = new TaskTypeDTO();
        try {
            dto.setTaskTypeName(result[2].toUpperCase() + " (Bill No. " + result[0] + ")");
            dto.setSectionName(section);
            dto.setTaskTypeNumber(Integer.parseInt(result[3]));
            dto.setProgrammeID(programme.getProgrammeID());
        } catch (Exception e) {
            Log.e(LOG, "---- ERROR parse failed", e);
            return null;
        }

        Log.d(LOG,
                "Found taskType: " + dto.getTaskTypeName() + " to import");
        return dto;
    }

    private TaskDTO parseTask(TaskTypeDTO taskType, String line) throws ImportException {
        Pattern patt = Pattern.compile(";");

        String[] result = patt.split(line);
        TaskDTO dto = new TaskDTO();
        if (taskType.getTaskList() == null) {
            taskType.setTaskList(new ArrayList<TaskDTO>());
        }
        try {
            dto.setTaskName(result[2] + " (" + result[1] + ")");
            dto.setTaskNumber(Integer.parseInt(result[4]));
            taskType.getTaskList().add(dto);

        } catch (Exception e) {
            Log.e(LOG, "---- ERROR parse failed: " + taskType.getTaskTypeName() + " --> " + dto.getTaskName(), e);
            return null;
        }

        Log.e(LOG,
                "add task: " + dto.getTaskName() + " to " + taskType.getTaskTypeName());
        return dto;
    }

    private SubTaskDTO parseSubTask(TaskDTO task, String line) throws ImportException {
        Pattern patt = Pattern.compile(";");

        String[] result = patt.split(line);
        SubTaskDTO dto = new SubTaskDTO();
        if (task.getSubTaskList() == null) {
            task.setSubTaskList(new ArrayList<SubTaskDTO>());
        }
        try {
            dto.setSubTaskName(result[2]);
            task.getSubTaskList().add(dto);

        } catch (Exception e) {
            Log.e(LOG, "---- ERROR parse failed", e);
            return null;
        }

        Log.i(LOG,
                "add Subtask: " + dto.getSubTaskName() + " to " + task.getTaskName());
        return dto;
    }

    static final int PAGE_SIZE = 1;
    int getIndex;

    private void sendData() {
        btnImport.setEnabled(false);
        listener.setBusy(true);
        switch (importType) {
            case IMPORT_TASKS:
                controlTaskData();
                break;
            case IMPORT_PROJECTS:
                controlProjectData();
                break;
        }


    }

    private void controlProjectData() {
        if (index < projectList.size()) {
            sendProjectData(projectList.get(index));
        } else {
            Log.i(LOG, "### projects import completed, projectList: " + projectList.size());
            Snackbar.make(fileSpinner, "Project data import completed OK", Snackbar.LENGTH_LONG).show();
            listener.onProjectsImported(importedResponse.getProjectList());
        }
    }

    private void controlTaskData() {
        if (index < taskTypeList.size()) {
            sendTaskData(taskTypeList.get(index));
        } else {
            Log.i(LOG, "### task import completed, taskTypeList: " + taskTypeList.size());
            Snackbar.make(fileSpinner, "Task data import completed OK", Snackbar.LENGTH_LONG).show();
            listener.onTasksImported(importedResponse.getTaskTypeList());
        }
    }

    private ResponseDTO importedResponse;

    private void sendProjectData(ProjectDTO project) {
        Log.e(LOG, "### sendProjectData: projectName: " + project.getProjectName() );
        RequestDTO w = new RequestDTO();
        w.setRequestType(RequestDTO.IMPORT_PROJECT_INFO);
        w.setProject(project);

        listener.setBusy(true);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.setBusy(false);
                        if (response.getStatusCode() == 0) {
                            index++;
                            importedResponse = response;
                            controlProjectData();
                        } else {
                            Util.showErrorToast(ctx, response.getMessage());
                            btnImport.setEnabled(true);
                        }
                    }
                });
            }

            @Override
            public void onError(final String message) {
                Log.e(LOG, "---- ERROR websocket - " + message);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnImport.setEnabled(true);
                        listener.setBusy(false);
                        Util.showErrorToast(ctx, message);
                    }
                });
            }

        });


    }

    private void sendTaskData(TaskTypeDTO taskType) {
        Log.e(LOG, "### sendTaskData" + taskType.getTaskTypeName());
        RequestDTO w = new RequestDTO();
        w.setRequestType(RequestDTO.IMPORT_TASK_INFO);
        w.setTaskType(taskType);

        listener.setBusy(true);
        NetUtil.sendRequest(ctx, w, new NetUtil.NetUtilListener() {
            @Override
            public void onResponse(final ResponseDTO response) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.setBusy(false);
                        if (response.getStatusCode() == 0) {
                            index++;
                            importedResponse = response;
                            controlTaskData();
                        } else {
                            Util.showErrorToast(ctx, response.getMessage());
                            btnImport.setEnabled(true);
                        }
                    }
                });
            }

            @Override
            public void onError(final String message) {
                Log.e(LOG, "---- ERROR websocket - " + message);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnImport.setEnabled(true);
                        listener.setBusy(false);
                        Util.showErrorToast(ctx, message);
                    }
                });
            }

          
        });


    }

    private void setTaskList() {
        taskAdapter = new TaskImportAdapter(ctx, R.layout.task_type_item_card, taskTypeList);
        list.setAdapter(taskAdapter);
        txtCount.setText("" + taskTypeList.size());

    }

    private void setProjectList() {
        projectImportAdapter = new ProjectImportAdapter(ctx, R.layout.project_import_item_card, projectList);
        list.setAdapter(projectImportAdapter);
        txtCount.setText("" + projectList.size());

    }

    TaskImportAdapter taskAdapter;
    ProjectImportAdapter projectImportAdapter;

    static final String LOG = FileImportFragment.class.getSimpleName();
    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", loc);
    static final DecimalFormat df = new DecimalFormat("###,###,###,###,###,###,###,###,###.00");

    File selectedFile;

    private void setSpinner() {

        List<String> list = new ArrayList<>();
        list.add("Select File");
        for (File p : files) {
            list.add(p.getName() + " - " + sdf.format(new Date(p.lastModified())));
        }
        ArrayAdapter a = new ArrayAdapter(ctx, android.R.layout.simple_spinner_item, list);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fileSpinner.setAdapter(a);
        fileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    selectedFile = null;
                    return;
                }
                try {
                    switch (importType) {
                        case IMPORT_TASKS:
                            parseTasksFile(files.get(i - 1));
                            break;
                        case IMPORT_PROJECTS:
                            parseProjectsFile(files.get(i - 1));
                            break;
                    }

                } catch (IOException e) {
                    Util.showErrorToast(ctx, "Import failed: " + e.getMessage());
                } catch (ImportException e) {
                    Util.showErrorToast(ctx, "Import failed: " + e.getMessage());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    int primaryColor, darkColor;
    @Override
    public void setThemeColors(int primaryColor, int darkColor) {
        this.primaryColor = primaryColor;
        this.darkColor = darkColor;
    }
}
