package com.boha.monitor.library.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boha.monitor.library.R;
import com.boha.monitor.library.adapters.BeneficiaryAdapter;
import com.boha.monitor.library.adapters.PopupListAdapter;
import com.boha.monitor.library.dto.BeneficiaryDTO;
import com.boha.monitor.library.dto.ProjectDTO;
import com.boha.monitor.library.dto.ProjectSiteDTO;
import com.boha.monitor.library.dto.transfer.ResponseDTO;
import com.boha.monitor.library.util.CacheUtil;
import com.boha.monitor.library.util.Statics;
import com.boha.monitor.library.util.Util;




import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated
public class BeneficiaryListFragment extends Fragment implements PageFragment {


    private BeneficiaryListListener mListener;
    private AbsListView mListView;

    public BeneficiaryListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    Context ctx;
    View view, topView, hero;
    ImageView topImage;
    TextView txtCount, txtTitle, txtProjectName;
    ProgressBar progressBar;
    static final String LOG = BeneficiaryListFragment.class.getSimpleName();

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        Log.d(LOG, "### onCreateView");
        view = inflater.inflate(R.layout.fragment_beneficiary_list, container, false);
        ctx = getActivity();
        setFields();

        if (savedInstanceState != null) {
            Log.e(LOG, "## onCreateView, savedInstanceState not = null");
            ResponseDTO r = (ResponseDTO) savedInstanceState.getSerializable("projectList");
            projectList = r.getProjectList();

            if (!projectList.isEmpty()) {
                txtProjectName.setText(projectList.get(0).getProjectName());
                beneficiaryList = projectList.get(0).getBeneficiaryList();
                return view;
            } else {
                txtProjectName.setVisibility(View.GONE);
            }
        }
        Bundle args = getArguments();
        if (args != null) {
            ResponseDTO r = (ResponseDTO) args.getSerializable("projectList");
            projectList = r.getProjectList();
            if (!projectList.isEmpty()) {
                project = projectList.get(0);
                txtProjectName.setText(project.getProjectName());
                buildBeneficiaryList();
                setList();
            }else {
                txtProjectName.setVisibility(View.GONE);
            }
        }



        return view;
    }
private void buildBeneficiaryList() {
    beneficiaryList = new ArrayList<>();
    for (ProjectSiteDTO s: project.getProjectSiteList()) {
        if (s.getBeneficiary() != null) {
            beneficiaryList.add(s.getBeneficiary());
        } else {
            Log.w(LOG, "## Beneficiary is null. ignored. site: " + s.getProjectSiteName());
        }
    }

}
    private void setFields() {
        topView = view.findViewById(R.id.BC_top);
        hero = view.findViewById(R.id.BC_hero);
        topImage = (ImageView) view.findViewById(R.id.BC_hero);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        mListView = (AbsListView) view.findViewById(R.id.BC_list);
        txtCount = (TextView) view.findViewById(R.id.BC_count);
        txtTitle = (TextView) view.findViewById(R.id.BC_title);
        txtProjectName = (TextView) view.findViewById(R.id.BC_projectName);
        Statics.setRobotoFontLight(ctx, txtTitle);
        topImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashSeveralTimes(txtCount, 100, 1, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        if (project != null) {
                            mListener.onBeneficiaryImportRequested(project);
                        } else {
                            if (!projectList.isEmpty())
                                mListener.onBeneficiaryImportRequested(projectList.get(0));
                        }
                    }
                });
            }
        });
        txtProjectName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashSeveralTimes(txtProjectName, 100, 1, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        showProjectPopup();
                    }
                });

            }
        });
        txtCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(txtCount, 100, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        if (project != null) {
                            mListener.onBeneficiaryImportRequested(project);
                        } else {
                            if (!projectList.isEmpty())
                                //
                                mListener.onBeneficiaryImportRequested(projectList.get(0));
                        }
                    }
                });

            }
        });

    }
    public void expandTopView() {
       // Util.expand(topView, 500, null);
    }
    private void showProjectPopup() {

        final ListPopupWindow pop = new ListPopupWindow(ctx);
        List<String> sList = new ArrayList<>();
        for (ProjectDTO d: projectList) {
            sList.add(d.getProjectName());
        }
        View pv = getActivity().getLayoutInflater().inflate(R.layout.hero_image, null);
        ImageView img = (ImageView) pv.findViewById(R.id.HERO_image);
        TextView cap = (TextView) pv.findViewById(R.id.HERO_caption);
        Statics.setRobotoFontLight(ctx,cap);
        cap.setText("Select Project");
        img.setImageDrawable(Util.getRandomHeroImage(ctx));
        pop.setAnchorView(hero);
        pop.setPromptView(pv);
        pop.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);
        pop.setAdapter(new PopupListAdapter(ctx, R.layout.xxsimple_spinner_item, sList, false));
        pop.setModal(true);
        pop.setWidth(Util.getPopupWidth(getActivity()));
        pop.setHorizontalOffset(Util.getPopupHorizontalOffset(getActivity()));

        pop.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                project = projectList.get(position);
                getBeneficiaryList(project.getProjectID());
                txtProjectName.setText(project.getProjectName());
                pop.dismiss();
            }
        });
        pop.show();

    }

    private void setCounts() {
        for (ProjectDTO p: projectList) {
            if (p.getBeneficiaryList() != null) {
                p.setBeneficiaryCount(p.getBeneficiaryList().size());
            } else {
                p.setBeneficiaryCount(0);
            }
        }
    }
    @Override
    public void onSaveInstanceState( Bundle state) {
        Log.e(LOG, "####### onSaveInstanceState");
        ResponseDTO r = new ResponseDTO();
        r.setProjectList(projectList);
        state.putSerializable("projectList", r);
        super.onSaveInstanceState(state);
    }
    public void setProjectList(final List<ProjectDTO> list) {
        this.projectList = list;
        if (projectList == null) {
            CacheUtil.getCachedData(ctx,CacheUtil.CACHE_DATA, new CacheUtil.CacheUtilListener() {
                @Override
                public void onFileDataDeserialized( ResponseDTO response) {
                    if (response.getCompany() != null) {
                        projectList = response.getCompany().getProjectList();
                    }
                }

                @Override
                public void onDataCached() {

                }

                @Override
                public void onError() {

                }
            });
            return;
        }
        if (!projectList.isEmpty()) {
            project = projectList.get(0);
            buildBeneficiaryList();
            if (mListView != null) {
                setList();
            }
        }
    }
    public void setProject(ProjectDTO project) {
        this.project = project;
        buildBeneficiaryList();
        if (mListView != null) {
            setList();
        }
    }

    PopupWindow benPopupWindow;
    private void setList() {
        if (beneficiaryList == null) beneficiaryList = new ArrayList<>();
        txtCount.setText("" + beneficiaryList.size());
        adapter = new BeneficiaryAdapter(ctx, R.layout.beneficiary_card,
                beneficiaryList, new BeneficiaryAdapter.BeneficiaryAdapterListener() {
            @Override
            public void onBeneficiaryEditRequested(BeneficiaryDTO client) {

            }
        });
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                beneficiary = beneficiaryList.get(position);
                //mListener.onBeneficiaryClicked(beneficiary);
                View benView = getActivity().getLayoutInflater().inflate(R.layout.beneficiary_item,null);
                TextView name = (TextView) benView.findViewById(R.id.BEN_txtName);
                TextView num = (TextView) benView.findViewById(R.id.BEN_txtNum);
                TextView idnum = (TextView) benView.findViewById(R.id.BEN_idnumber);
                TextView status = (TextView) benView.findViewById(R.id.BEN_status);
                TextView subsidy = (TextView) benView.findViewById(R.id.BEN_subsidy);
                TextView site = (TextView) benView.findViewById(R.id.BEN_siteNumber);
                Button btn = (Button)benView.findViewById(R.id.BEN_btnClose);
                btn.setVisibility(View.VISIBLE);


                name.setText(beneficiary.getFullName());
                num.setText("" + (position + 1));
                num.setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.xgreen_oval));
                idnum.setText(beneficiary.getiDNumber());
                status.setText(beneficiary.getStatus());
                site.setText(beneficiary.getSiteNumber());
                subsidy.setText(df.format(beneficiary.getAmountAuthorized()));
                benView.setBackgroundColor(ctx.getResources().getColor(R.color.white));

                if (benPopupWindow == null)
                benPopupWindow = new PopupWindow(benView,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                else {
                    benPopupWindow.dismiss();
                }
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        benPopupWindow.dismiss();
                        benPopupWindow = null;
                    }
                });
                benPopupWindow.showAsDropDown(txtTitle, 0, 0);
            }
        });

    }

    static final DecimalFormat df = new DecimalFormat("###,###,###,###,###,###,###,###.00");
    @Override
    public void onAttach( Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (BeneficiaryListListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Host " + activity.getLocalClassName()
                    + " must implement BeneficiaryListListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    @Override
    public void animateHeroHeight() {
        Util.animateRotationY(txtCount, 500);

    }

    public void addBeneficiary(BeneficiaryDTO beneficiary) {
        if (beneficiaryList == null) {
            beneficiaryList = new ArrayList<>();
        }
        beneficiaryList.add(beneficiary);
        Collections.sort(beneficiaryList);
        adapter.notifyDataSetChanged();
        txtCount.setText("" + beneficiaryList.size());
        try {
            Thread.sleep(1000);
            Util.animateRotationY(txtCount, 500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow logoAnimator interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface BeneficiaryListListener {
        public void onBeneficiaryClicked(BeneficiaryDTO beneficiary);

        public void onBeneficiaryImportRequested(ProjectDTO project);

        public void onBeneficiaryEditRequested(BeneficiaryDTO beneficiary);
    }

    public void refreshBeneficiaryList( ProjectDTO p) {
        Log.e(LOG,"## refreshBeneficiaryList. size: " + p.getBeneficiaryList().size());
        if (project != null) {
            project.setBeneficiaryList(p.getBeneficiaryList());
            beneficiaryList = project.getBeneficiaryList();
        } else {
            projectList.get(0).setBeneficiaryList(p.getBeneficiaryList());
            beneficiaryList = projectList.get(0).getBeneficiaryList();
        }

        setList();

    }

    BeneficiaryDTO beneficiary;
    List<BeneficiaryDTO> beneficiaryList;
    BeneficiaryAdapter adapter;
    List<ProjectDTO> projectList;
    ProjectDTO project;
    boolean reminderHasBeenShown;

    private void getBeneficiaryList(final Integer projectID) {
        progressBar.setVisibility(View.VISIBLE);
        CacheUtil.getCachedProjectData(ctx, projectID, new CacheUtil.CacheUtilListener() {
            @Override
            public void onFileDataDeserialized( ResponseDTO response) {
                Log.w("BeneficiaryListFragment", "onFileDataDeserialized!");
                if (response != null) {
                    if (!response.getProjectList().isEmpty()) {
                        beneficiaryList = response.getProjectList().get(0).getBeneficiaryList();
                        setList();
                        progressBar.setVisibility(View.GONE);
                    }
                }
                Util.refreshProjectData(getActivity(), ctx, projectID, new Util.ProjectDataRefreshListener() {
                    @Override
                    public void onDataRefreshed( ProjectDTO project) {

                        beneficiaryList = project.getBeneficiaryList();
                        setList();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("BeneficiaryListFragment", "Error getting refresh data!");
                        progressBar.setVisibility(View.GONE);
                        Util.showErrorToast(ctx, message);
                    }
                });

            }

            @Override
            public void onDataCached() {

            }

            @Override
            public void onError() {
                Log.e("BeneficiaryListFragment", "Error with cache...refreshing from server");
                Util.refreshProjectData(getActivity(), ctx, projectID, new Util.ProjectDataRefreshListener() {
                    @Override
                    public void onDataRefreshed( ProjectDTO project) {
                        beneficiaryList = project.getBeneficiaryList();
                        setList();
                    }

                    @Override
                    public void onError(String message) {
                        Log.e("BeneficiaryListFragment", message);
                        Util.showErrorToast(ctx,message);
                    }
                });

            }
        });
    }


}
