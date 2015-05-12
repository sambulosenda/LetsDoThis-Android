package org.dosomething.letsdothis.tasks;
import android.content.Context;
import android.util.Patterns;

import org.dosomething.letsdothis.data.DatabaseHelper;
import org.dosomething.letsdothis.data.User;
import org.dosomething.letsdothis.utils.AppPrefs;

import co.touchlab.android.threading.eventbus.EventBusExt;

/**
 * Created by toidiu on 4/16/15.
 */
public abstract class BaseRegistrationTask extends BaseNetworkErrorHandlerTask
{
    protected final String phoneEmail;
    protected final String password;

    protected BaseRegistrationTask(String phoneEmail, String password)
    {
        this.phoneEmail = phoneEmail.isEmpty()
                ? null
                : phoneEmail;
        this.password = password;
    }

    protected void loginUser(Context context, User user) throws Throwable
    {
        DatabaseHelper.getInstance(context).getUserDao().createOrUpdate(user);
        AppPrefs.getInstance(context).setCurrentUserId(user.id);
    }

    @Override
    protected void run(Context context) throws Throwable
    {
        if(phoneEmail == null)
        {
            return;
        }

        attemptRegistration(context);
    }

    protected abstract void attemptRegistration(Context context) throws Throwable;

    protected boolean matchesEmail(String phoneEmail)
    {
        return Patterns.EMAIL_ADDRESS.matcher(phoneEmail).matches();
    }

}
