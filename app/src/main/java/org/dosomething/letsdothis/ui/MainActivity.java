package org.dosomething.letsdothis.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.dosomething.letsdothis.R;
import org.dosomething.letsdothis.ui.fragments.ActionsFragment;
import org.dosomething.letsdothis.ui.fragments.InvitesFragment;
import org.dosomething.letsdothis.ui.fragments.NotificationsFragment;


import static org.dosomething.letsdothis.ui.fragments.HubFragment.TAG;
import static org.dosomething.letsdothis.ui.fragments.HubFragment.newInstance;


public class MainActivity extends BaseActivity implements NotificationsFragment.SetTitleListener
{
    private View     actions;
    private View     hub;
    private View     invites;
    private View     notifications;
    private TextView title;

    public static Intent getLaunchIntent(Context context)
    {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, ActionsFragment.newInstance(), ActionsFragment.TAG)
                    .commit();
        }

        initToolbar();
        initDrawer();
    }

    private void initToolbar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        title = (TextView) findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                                                                        R.string.invite_code_opt,
                                                                        R.string.account);
        drawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
    }

    //TODO the drawer should have a list view, then we won't need all this logic
    private void initDrawer()
    {
        final View drawer = findViewById(R.id.drawer);
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        actions = findViewById(R.id.actions);
        actions.setSelected(true);
        actions.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(! actions.isSelected())
                {
                    replaceCurrentFragment(ActionsFragment.newInstance(), ActionsFragment.TAG);
                    drawerLayout.closeDrawer(drawer);
                }
            }
        });

        hub = findViewById(R.id.hub);
        hub.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(! hub.isSelected())
                {
                    replaceCurrentFragment(newInstance(false), TAG);
                    drawerLayout.closeDrawer(drawer);
                }
            }
        });

        invites = findViewById(R.id.invites);
        invites.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(! invites.isSelected())
                {
                    replaceCurrentFragment(InvitesFragment.newInstance(), InvitesFragment.TAG);
                    drawerLayout.closeDrawer(drawer);
                }
            }
        });

        notifications = findViewById(R.id.notifications);
        notifications.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(! notifications.isSelected())
                {
                    replaceCurrentFragment(NotificationsFragment.newInstance(),
                                           NotificationsFragment.TAG);
                    drawerLayout.closeDrawer(drawer);
                }
            }
        });

        View setting = findViewById(R.id.setting);
        setting.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(SettingsActivity.getLaunchIntent(MainActivity.this));
                drawerLayout.closeDrawer(drawer);
            }
        });
    }

    private void replaceCurrentFragment(Fragment fragment, String tag)
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, tag)
                .commit();
        getSupportFragmentManager().executePendingTransactions();
        updateNavBar();
    }

    private void updateNavBar()
    {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        String currentFragTag = currentFragment.getTag();

        actions.setSelected(TextUtils.equals(ActionsFragment.TAG, currentFragTag));
        notifications.setSelected(TextUtils.equals(NotificationsFragment.TAG, currentFragTag));
        hub.setSelected(TextUtils.equals(TAG, currentFragTag));
        invites.setSelected(TextUtils.equals(InvitesFragment.TAG, currentFragTag));
    }

    @Override
    public void setTitle(String title)
    {
        this.title.setText(title);
    }


}
