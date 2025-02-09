package org.dosomething.letsdothis.ui.fragments;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.dosomething.letsdothis.BuildConfig;
import org.dosomething.letsdothis.R;
import org.dosomething.letsdothis.data.Campaign;
import org.dosomething.letsdothis.data.DatabaseHelper;
import org.dosomething.letsdothis.tasks.DbGetUserTask;
import org.dosomething.letsdothis.tasks.GetUserCampaignsTask;
import org.dosomething.letsdothis.tasks.GetUserTask;
import org.dosomething.letsdothis.tasks.RbShareDataTask;
import org.dosomething.letsdothis.tasks.ReportbackUploadTask;
import org.dosomething.letsdothis.tasks.UploadAvatarTask;
import org.dosomething.letsdothis.ui.CampaignInviteActivity;
import org.dosomething.letsdothis.ui.GroupActivity;
import org.dosomething.letsdothis.ui.PhotoCropActivity;
import org.dosomething.letsdothis.ui.PublicProfileActivity;
import org.dosomething.letsdothis.ui.ReportBackUploadActivity;
import org.dosomething.letsdothis.ui.adapters.HubAdapter;
import org.dosomething.letsdothis.utils.AppPrefs;

import java.io.File;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.tasks.TaskQueue;
import co.touchlab.android.threading.tasks.utils.TaskQueueHelper;

/**
 * Created by izzyoji :) on 4/15/15.
 */
public class HubFragment extends Fragment implements HubAdapter.HubAdapterClickListener
{

    //~=~=~=~=~=~=~=~=~=~=~=~=Constants
    public static final  String TAG            = HubFragment.class.getSimpleName();
    private static final int    SELECT_PICTURE = 52345;
    public static final  String EXTRA_ID       = "id";

    //~=~=~=~=~=~=~=~=~=~=~=~=Fields
    private HubAdapter       adapter;
    private Uri              imageUri;
    private SetTitleListener titleListener;
    private ReplaceFragmentListener replaceFragmentListener;
    private ProgressBar      progress;

    public static HubFragment newInstance(String id)
    {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ID, id);
        HubFragment fragment = new HubFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_recycler, container, false);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        titleListener = (SetTitleListener) getActivity();

        if (activity instanceof ReplaceFragmentListener) {
            replaceFragmentListener = (ReplaceFragmentListener) activity;
        }

        refreshUserCampaign();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        EventBusExt.getDefault().unregister(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if(! EventBusExt.getDefault().isRegistered(this))
        {
            EventBusExt.getDefault().register(this);
        }

        titleListener.setTitle("Hub");

        String publicId = getArguments().getString(EXTRA_ID, null);
        if(publicId != null)
        {
            TaskQueue.loadQueueDefault(getActivity()).execute(new GetUserTask(publicId));
        }
        else
        {
            DatabaseHelper.defaultDatabaseQueue(getActivity()).execute(
                    new DbGetUserTask(AppPrefs.getInstance(getActivity()).getCurrentUserId()));
        }

        if(TaskQueueHelper.hasTasksOfType(ReportbackUploadTask.getQueue(getActivity()),
                                          ReportbackUploadTask.class))
        {
            adapter.processingUpload();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        progress = (ProgressBar) getView().findViewById(R.id.progress);
        progress.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(R.color.cerulean_1),
                                PorterDuff.Mode.SRC_IN);
        refreshProgressBar();

        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.recycler);
        String publicId = getArguments().getString(EXTRA_ID, null);
        adapter = new HubAdapter(getActivity(), this, publicId != null);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void friendClicked(String friendId)
    {
        startActivity(PublicProfileActivity.getLaunchIntent(getActivity(), friendId));
    }

    @Override
    public void groupClicked(int groupId)
    {
        startActivity(GroupActivity.getLaunchIntent(getActivity(), groupId));
    }

    @Override
    public void onShareClicked(Campaign campaign)
    {
        TaskQueue.loadQueueDefault(getActivity()).execute(new RbShareDataTask(campaign));
    }

    @Override
    public void onProveClicked(Campaign campaign)
    {
        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                                       android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File externalFile = new File(Environment.getExternalStorageDirectory(), "DoSomething");
        externalFile.mkdirs();
        File file = new File(externalFile, "reportBack" + System.currentTimeMillis() + ".jpg");
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        imageUri = Uri.parse(file.getAbsolutePath());
        if(BuildConfig.DEBUG)
        {
            Log.d("photo location", imageUri.toString());
        }

        String pickTitle = getString(R.string.select_picture);
        Intent chooserIntent = Intent.createChooser(takePhotoIntent, pickTitle);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, SELECT_PICTURE);
    }

    @Override
    public void onInviteClicked(String title, int signupGroup)
    {
        startActivity(CampaignInviteActivity
                              .getLaunchIntent(getActivity(), title, signupGroup));
    }

    /**
     * When Action button is clicked in an empty Hub, go to the Actions screen.
     *
     * Implements HubAdapter.HubAdapterClickListener
     */
    @Override
    public void onActionsButtonClicked() {
        replaceFragmentListener.replaceWithActionsFragment();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == Activity.RESULT_OK)
        {
            if(requestCode == SELECT_PICTURE)
            {
                final boolean isCamera;
                if(data == null && data.getData() == null)
                {
                    isCamera = true;
                }
                else
                {
                    final String action = data.getAction();
                    if(action == null)
                    {
                        isCamera = false;
                    }
                    else
                    {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Uri selectedImageUri;
                if(isCamera)
                {
                    selectedImageUri = imageUri;
                }
                else
                {
                    selectedImageUri = data.getData();
                }

                startActivityForResult(PhotoCropActivity.getResultIntent(getActivity(),
                                                                         selectedImageUri
                                                                                 .toString(),
                                                                         getString(
                                                                                 R.string.share_photo),
                                                                         null),
                                       PhotoCropActivity.RESULT_CODE);
            }
            else if(requestCode == PhotoCropActivity.RESULT_CODE)
            {
                String filePath = data.getStringExtra(PhotoCropActivity.RESULT_FILE_PATH);
                Campaign clickedCampaign = adapter.getClickedCampaign();
                String format = String
                        .format(getString(R.string.reportback_upload_hint), clickedCampaign.noun,
                                clickedCampaign.verb);
                startActivity(ReportBackUploadActivity.getLaunchIntent(getActivity(), filePath,
                                                                       clickedCampaign.title,
                                                                       clickedCampaign.id, format));
            }
        }
    }


    private void refreshUserCampaign() {
        String userId = getArguments().getString(EXTRA_ID, null);
        if (userId == null) {
            userId = AppPrefs.getInstance(getActivity()).getCurrentUserId();
        }

        TaskQueue.loadQueueDefault(getActivity()).execute(new GetUserCampaignsTask(userId));
        refreshProgressBar();
    }

    private void refreshProgressBar()
    {
        boolean b = TaskQueueHelper.hasTasksOfType(TaskQueue.loadQueueDefault(getActivity()),
                                                   GetUserCampaignsTask.class);
        if(b)
        {
            if(progress != null)
            {
                progress.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            if(progress != null)
            {
                progress.setVisibility(View.GONE);
            }
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(GetUserCampaignsTask task) {
        refreshProgressBar();
        adapter.setCurrentCampaign(task.currentCampaignList);

        if (task.pastCampaignList != null && !task.pastCampaignList.isEmpty()) {
            adapter.addPastCampaign(task.pastCampaignList);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(RbShareDataTask task)
    {
        if(task.file != null && task.file.exists())
        {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/*");
            Uri uri = Uri.fromFile(task.file);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(share);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ReportbackUploadTask task)
    {
        refreshUserCampaign();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(GetUserTask task)
    {
        adapter.addUser(task.user);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DbGetUserTask task)
    {
        adapter.addUser(task.user);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(UploadAvatarTask task)
    {
        adapter.addUser(task.user);
    }

}
