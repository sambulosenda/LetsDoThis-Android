package org.dosomething.letsdothis.ui.adapters;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.dosomething.letsdothis.R;
import org.dosomething.letsdothis.data.Campaign;
import org.dosomething.letsdothis.data.ReportBack;
import org.dosomething.letsdothis.data.User;
import org.dosomething.letsdothis.ui.views.ReportBackImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by izzyoji :) on 5/6/15.
 */
public class GroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    //~=~=~=~=~=~=~=~=~=~=~=~=Constants
    public static final int VIEW_TYPE_FRIEND         = 0;
    public static final int VIEW_TYPE_REPORT_BACK    = 1;
    public static final int VIEW_TYPE_CAMPAIGN       = 2;
    public static final int VIEW_TYPE_ACTION_BUTTONS = 3;
    public static final int VIEW_TYPE_REWARD         = 4;
    private final ActionButtons actionButtons = new ActionButtons();

    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    public List<Object> dataSet = new ArrayList<>();
    private Campaign                  currentCampaign;
    private GroupAdapterClickListener groupAdapterClickListener;

    public GroupAdapter(Campaign campaign, GroupAdapterClickListener groupAdapterClickListener)
    {
        currentCampaign = campaign;

        this.groupAdapterClickListener = groupAdapterClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        switch(viewType)
        {
            case VIEW_TYPE_FRIEND:
                View friendLayout = LayoutInflater.from(parent.getContext())
                                                  .inflate(R.layout.item_friend, parent, false);
                return new FriendViewHolder((ImageView) friendLayout);
            case VIEW_TYPE_REPORT_BACK:
                View reportBackLayout = LayoutInflater.from(parent.getContext())
                                                      .inflate(R.layout.item_report_back_square,
                                                               parent, false);
                return new ReportBackViewHolder((ReportBackImageView) reportBackLayout);
            case VIEW_TYPE_ACTION_BUTTONS:
                View primaryButton = LayoutInflater.from(parent.getContext())
                                                   .inflate(R.layout.item_action_buttons, parent, false);
                return new ActionButtonsViewHolder(primaryButton);
            case VIEW_TYPE_REWARD:
                View rewardLayout = LayoutInflater.from(parent.getContext())
                                               .inflate(R.layout.item_reward, parent,
                                                        false);
                return new RewardViewHolder((TextView) rewardLayout);
            default:
                View campaignLayout = LayoutInflater.from(parent.getContext())
                                                 .inflate(R.layout.item_group_campaign, parent, false);
                return new CampaignViewHolder(campaignLayout);

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position)
    {

        if(getItemViewType(position) == VIEW_TYPE_CAMPAIGN)
        {
            final Campaign campaign = (Campaign) dataSet.get(position);
            CampaignViewHolder expandedCampaignViewHolder = (CampaignViewHolder) holder;
            expandedCampaignViewHolder.title.setText(campaign.title);
            expandedCampaignViewHolder.callToAction.setText(campaign.callToAction);

            Context context = expandedCampaignViewHolder.imageView.getContext();
            int height = context.getResources().getDimensionPixelSize(R.dimen.campaign_height);
            Picasso.with(context).load(campaign.imagePath).resize(0, height)
                   .into(expandedCampaignViewHolder.imageView);

        }
        else if(getItemViewType(position) == VIEW_TYPE_REPORT_BACK)
        {
            final ReportBack reportBack = (ReportBack) dataSet.get(position);
            ReportBackViewHolder reportBackViewHolder = (ReportBackViewHolder) holder;

            Picasso.with(reportBackViewHolder.root.getContext()).load(reportBack.getImagePath())
                   .into(reportBackViewHolder.root);

            reportBackViewHolder.root.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    groupAdapterClickListener.onReportBackClicked(reportBack.id);
                }
            });
        }
        else if(getItemViewType(position) == VIEW_TYPE_ACTION_BUTTONS)
        {
            ActionButtonsViewHolder actionButtonsViewHolder = (ActionButtonsViewHolder) holder;

            actionButtonsViewHolder.invite.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    groupAdapterClickListener.onInviteClicked(currentCampaign.title);
                }
            });

            switch(currentCampaign.showShare)
            {
                case UPLOADING:
                    actionButtonsViewHolder.proveShare.setText(R.string.uploading);
                    actionButtonsViewHolder.proveShare.setOnClickListener(null);
                    break;
                case SHARE:
                    actionButtonsViewHolder.proveShare.setText(R.string.share_photo);
                    actionButtonsViewHolder.proveShare.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            groupAdapterClickListener.onShareClicked(currentCampaign);
                        }
                    });
                    break;
                case SHOW_OFF:
                    actionButtonsViewHolder.proveShare.setText(R.string.show_off);
                    actionButtonsViewHolder.proveShare.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            groupAdapterClickListener.onShowOffClicked(currentCampaign);
                        }
                    });
                    break;
            }

        }
        else if(getItemViewType(position) == VIEW_TYPE_FRIEND)
        {
            final User user = (User) dataSet.get(position);
            FriendViewHolder friendViewHolder = (FriendViewHolder) holder;

            Picasso.with(friendViewHolder.itemView.getContext()).load(user.avatarPath)
                    .placeholder(R.drawable.ic_action_user)
                   .into((ImageView) friendViewHolder.itemView);

            friendViewHolder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    groupAdapterClickListener.onFriendClicked(user.id);
                }
            });
        }

    }

    @Override
    public int getItemViewType(int position)
    {
        Object currentObject = dataSet.get(position);
        if(currentObject instanceof Campaign)
        {
            return VIEW_TYPE_CAMPAIGN;
        }
        else if(currentObject instanceof Reward)
        {
            return VIEW_TYPE_REWARD;
        }
        else if(currentObject instanceof ReportBack)
        {
            return VIEW_TYPE_REPORT_BACK;
        }
        else if(currentObject instanceof ActionButtons)
        {
            return VIEW_TYPE_ACTION_BUTTONS;
        }
        else
        {
            return VIEW_TYPE_FRIEND;
        }
    }

    @Override
    public int getItemCount()
    {
        return dataSet.size();
    }


    public int getStartPositionOfReportBacks()
    {
        return dataSet.indexOf(actionButtons);
    }

    public void updateCampaign(Campaign campaign)
    {
        if(currentCampaign == null)
        {
            currentCampaign = campaign;
            dataSet.add(campaign);
            dataSet.add(new Reward());
            notifyItemRangeInserted(0, 1);

            dataSet.addAll(campaign.group);
        }
        else
        {
            currentCampaign = campaign;
            dataSet.set(0, currentCampaign);
            notifyItemChanged(0);
            dataSet.addAll(2, campaign.group);
        }
    }

    public Campaign getCampaign()
    {
        return currentCampaign;
    }

    public void addUsers(List<User> users)
    {
        dataSet.addAll(users);
        dataSet.add(actionButtons);
        notifyDataSetChanged();
    }

    public void addReportBacks(List<ReportBack> reportBacks)
    {
        dataSet.addAll(reportBacks);
        notifyDataSetChanged();
    }

    private class Reward
    {
    }

    private class ActionButtons
    {
    }

    private static class ActionButtonsViewHolder extends RecyclerView.ViewHolder
    {
        protected Button proveShare;
        protected Button invite;

        public ActionButtonsViewHolder(View view)
        {
            super(view);
            proveShare = (Button) view.findViewById(R.id.prove_share);
            invite = (Button) view.findViewById(R.id.invite);

        }
    }

    private static class RewardViewHolder extends RecyclerView.ViewHolder
    {
        public RewardViewHolder(TextView view)
        {
            super(view);
        }
    }

    public interface GroupAdapterClickListener
    {
        void onReportBackClicked(int reportBackId);

        void onInviteClicked(String title);

        void onFriendClicked(String id);

        void onShareClicked(Campaign campaign);

        void onShowOffClicked(Campaign campaign);
    }
}
