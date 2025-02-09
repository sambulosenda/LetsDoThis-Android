package org.dosomething.letsdothis.ui.fragments;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.dosomething.letsdothis.BuildConfig;
import org.dosomething.letsdothis.R;
import org.dosomething.letsdothis.data.Campaign;
import org.dosomething.letsdothis.data.DatabaseHelper;
import org.dosomething.letsdothis.data.InterestGroup;
import org.dosomething.letsdothis.data.ReportBack;
import org.dosomething.letsdothis.tasks.BaseReportBackListTask;
import org.dosomething.letsdothis.tasks.CampaignSignUpTask;
import org.dosomething.letsdothis.tasks.DbInterestGroupCampaignListTask;
import org.dosomething.letsdothis.tasks.GetUserCampaignsTask;
import org.dosomething.letsdothis.tasks.InterestReportBackListTask;
import org.dosomething.letsdothis.tasks.UpdateInterestGroupCampaignTask;
import org.dosomething.letsdothis.tasks.UpdateInterestGroupCampaignTask.IdQuery;
import org.dosomething.letsdothis.ui.CampaignDetailsActivity;
import org.dosomething.letsdothis.ui.ReportBackDetailsActivity;
import org.dosomething.letsdothis.ui.adapters.CampaignAdapter;
import org.dosomething.letsdothis.ui.views.ActionGridSpacingDecoration;
import org.dosomething.letsdothis.utils.AppPrefs;

import java.util.ArrayList;
import java.util.List;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.TaskQueue;
import co.touchlab.android.threading.tasks.utils.TaskQueueHelper;

/**
 * Created by izzyoji :) on 4/14/15.
 */
public class CampaignFragment extends Fragment implements CampaignAdapter.CampaignAdapterClickListener
{
    //~=~=~=~=~=~=~=~=~=~=~=~=Constants
    public static final String KEY_POSITION = "pagerPosition";
    public static final int    FIRST_PAGE   = 1;

    //~=~=~=~=~=~=~=~=~=~=~=~=Views
    private RecyclerView recyclerView;

    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    private CampaignAdapter adapter;

    // Position of this fragment in the pager
    private int             mPagerPosition;
    private int             currentPage;
    private int             totalPages;
    private String          currentRbQueryStatus;
    private ArrayList<Integer> campaignIds = new ArrayList<>();
    private ProgressBar mProgress;

    public static CampaignFragment newInstance(int position)
    {
        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, position);
        CampaignFragment campaignFragment = new CampaignFragment();
        campaignFragment.setArguments(args);
        return campaignFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.activity_fragment_recycler, container, false);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        EventBusExt.getDefault().unregister(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mPagerPosition = getArguments().getInt(KEY_POSITION);
        mProgress = (ProgressBar) getView().findViewById(R.id.progress);
        mProgress.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(R.color.cerulean_1),
                                PorterDuff.Mode.SRC_IN);

        recyclerView = (RecyclerView) getView().findViewById(R.id.recycler);
        adapter = new CampaignAdapter(this, getResources());
        recyclerView.setAdapter(adapter);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.addItemDecoration(new ActionGridSpacingDecoration());
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup()
        {
            @Override
            public int getSpanSize(int position)
            {
                switch(adapter.getItemViewType(position))
                {
                    case CampaignAdapter.VIEW_TYPE_REPORT_BACK:
                        return 1;
                    default:
                        return 2;
                }
            }
        });

        recyclerView.setLayoutManager(layoutManager);

        EventBusExt.getDefault().register(this);
        dbCampaignRefresh();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(adapter != null)
        {
            adapter.notifyItemRangeChanged(0, 3);
        }
    }

    @Override
    public void onCampaignClicked(int campaignId, boolean alreadySignedUp)
    {
        if (!alreadySignedUp) {
            TaskQueue.loadQueueDefault(getActivity()).execute(new CampaignSignUpTask(campaignId));
        }
        else {
            startActivity(CampaignDetailsActivity.getLaunchIntent(getActivity(), campaignId));
        }

        refreshProgressBar();
    }

    @Override
    public void onCampaignExpanded(int position)
    {
        recyclerView.smoothScrollToPosition(position);
    }

    @Override
    public void onReportBackClicked(int reportBackId, int campaignId)
    {
        startActivity(ReportBackDetailsActivity.getLaunchIntent(getActivity(), reportBackId, campaignId));
    }

    @Override
    public void onScrolledToBottom() {
        boolean getMoreData = false;
        if (currentRbQueryStatus == BaseReportBackListTask.STATUS_PROMOTED && totalPages > 0) {
            getMoreData = true;

            if (currentPage >= totalPages) {
                currentPage = 0;
                currentRbQueryStatus = BaseReportBackListTask.STATUS_APPROVED;
            }
        }
        else if (currentRbQueryStatus == BaseReportBackListTask.STATUS_APPROVED && currentPage < totalPages) {
            getMoreData = true;
        }

        if (getMoreData) {
            InterestReportBackListTask task = new InterestReportBackListTask(mPagerPosition, StringUtils
                    .join(campaignIds, ","), currentPage + 1, currentRbQueryStatus);
            getCampaignQueue().execute(task);
        }
    }

    @Override
    public void onNetworkCampaignRefresh()
    {
        adapter.clear();
        currentPage = 0;
        totalPages = 0;
        for(InterestGroup i : InterestGroup.values())
        {
            getCampaignQueue()
                    .execute(new UpdateInterestGroupCampaignTask(i.id));
        }

        refreshProgressBar();
    }

    private TaskQueue getCampaignQueue()
    {
        return TaskQueue.loadQueue(getActivity(), "campaignQueue");
    }


    public void dbCampaignRefresh()
    {
        adapter.clear();
        currentRbQueryStatus = BaseReportBackListTask.STATUS_PROMOTED;
        currentPage = 0;
        totalPages = 0;
        DatabaseHelper.defaultDatabaseQueue(getActivity())
                .execute(new DbInterestGroupCampaignListTask(findGroupId()));
        refreshProgressBar();
    }

    private int findGroupId()
    {
        return InterestGroup.values()[mPagerPosition].id;
    }

    /**
     * Show or hide progress bar based on status of tasks.
     */
    private void refreshProgressBar() {
        if (mProgress != null) {
            boolean showProgress = false;

            // Check if there is a query for interest groups in progress
            IdQuery queueQuery = new IdQuery(findGroupId());
            getCampaignQueue().query(queueQuery);

            if (queueQuery.found) {
                showProgress = true;
            }

            // Check if there is a signup in progress
            TaskQueue defaultQueue = TaskQueue.loadQueueDefault(getActivity());
            if (TaskQueueHelper.hasTasksOfType(defaultQueue, CampaignSignUpTask.class)) {
                showProgress = true;
            }

            if (showProgress) {
                mProgress.setVisibility(View.VISIBLE);
            }
            else {
                mProgress.setVisibility(View.GONE);
            }
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(InterestReportBackListTask task)
    {
        if (task.pagerPosition != mPagerPosition) {
            return;
        }

        if (task.error == null && currentPage < task.page) {
            totalPages = task.totalPages;
            currentPage = task.page;
            List<ReportBack> reportBacks = task.reportBacks;
            adapter.addAll(reportBacks);
        }
        else if (task.error != null && task.status == BaseReportBackListTask.STATUS_PROMOTED) {
            // A search for promoted reportbacks yielded nothing. Search again for approved photos.
            String campaigns = StringUtils.join(campaignIds, ",");
            getCampaignQueue().execute(new InterestReportBackListTask(mPagerPosition, campaigns,
                    FIRST_PAGE, BaseReportBackListTask.STATUS_APPROVED));
            currentPage = 0;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DbInterestGroupCampaignListTask task)
    {
        if(findGroupId() == task.interestGroupId)
        {
            // @TODO seems like campaigns won't get updated on app if they're changed on the server?
            if(task.campList.isEmpty())
            {
                getCampaignQueue()
                        .execute(new UpdateInterestGroupCampaignTask(task.interestGroupId));
            }
            else
            {
                refreshProgressBar();
                adapter.setCampaigns(task.campList);

                campaignIds.clear();
                for(Campaign campaign : task.campList)
                {
                    campaignIds.add(campaign.id);
                }

                // On initial query of reportback photos, set page to 1 and status to "promoted"
                String campaigns = StringUtils.join(campaignIds, ",");
                getCampaignQueue().execute(new InterestReportBackListTask(mPagerPosition, campaigns,
                        FIRST_PAGE, BaseReportBackListTask.STATUS_PROMOTED));

                // Now that we have campaigns, get user info to find out what they've participated in
                String userId = AppPrefs.getInstance(getActivity()).getCurrentUserId();
                TaskQueue.loadQueueDefault(getActivity()).execute(new GetUserCampaignsTask(userId));
            }
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(UpdateInterestGroupCampaignTask task)
    {
        getCampaignQueue().execute(new DbInterestGroupCampaignListTask(task.interestGroupId));
    }

    /**
     * On receiving user campaign data, determine if the user's has signed up for any of the
     * displayed campaigns.
     *
     * @TODO A potential optimization would be to only query for campaigns and user activity once
     *   overall, instead of once per CampaignFragment.
     *
     * @param task Result of getting current user's campaign activity
     */
    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(GetUserCampaignsTask task) {
        List<Campaign> allCampaigns = new ArrayList<>();
        if (task.currentCampaignList != null) {
            allCampaigns.addAll(task.currentCampaignList);
        }

        if (task.pastCampaignList != null) {
            allCampaigns.addAll(task.pastCampaignList);
        }

        for (Campaign campaign : allCampaigns) {
            int match = campaignIds.indexOf(campaign.id);
            if (match >= 0) {
                adapter.userSignedUpForCampaign(campaign.id);
            }
        }
    }

    /**
     * If signup task succeeds, open campaign detail screen.
     *
     * @param task
     */
    public void onEventMainThread(CampaignSignUpTask task) {
        if (task.getPagerPosition() == mPagerPosition) {
            refreshProgressBar();

            if (task != null && !task.hasError()) {
                adapter.userSignedUpForCampaign(task.getCampaignId());
                startActivity(CampaignDetailsActivity.getLaunchIntent(getActivity(), task.getCampaignId()));
            }
        }
    }

}
