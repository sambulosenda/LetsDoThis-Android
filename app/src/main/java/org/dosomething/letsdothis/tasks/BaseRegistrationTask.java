package org.dosomething.letsdothis.tasks;
import android.content.Context;
import android.util.Patterns;

import com.parse.ParseInstallation;

import org.dosomething.letsdothis.data.DatabaseHelper;
import org.dosomething.letsdothis.data.User;
import org.dosomething.letsdothis.network.NetworkHelper;
import org.dosomething.letsdothis.network.models.ParseInstallationRequest;
import org.dosomething.letsdothis.utils.AppPrefs;

/**
 * Created by toidiu on 4/16/15.
 */
public abstract class BaseRegistrationTask extends BaseNetworkErrorHandlerTask
{
    private final String mPhoneEmail;
    protected final String mPassword;

    protected BaseRegistrationTask(String email, String password)
    {
        mPhoneEmail = email.isEmpty()
                ? null
                : email;
        mPassword = password;
    }

    protected void loginUser(Context context, User user) throws Throwable
    {
        DatabaseHelper.getInstance(context).getUserDao().createOrUpdate(user);
        AppPrefs.getInstance(context).setCurrentUserId(user.id);
        AppPrefs.getInstance(context).setCurrentDrupalId(user.drupalId);

        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        currentInstallation.put("user_id", "user_" + user.id);
        currentInstallation.saveInBackground();
        String parseInstallation = currentInstallation.getInstallationId();

        NetworkHelper.getNorthstarAPIService()
                     .setParseInstallationId(AppPrefs.getInstance(context).getCurrentUserId(),
                                             new ParseInstallationRequest(parseInstallation));
    }

    /**
     * Using the configured country instead of something provided by the LocationManager. This should
     * be fine in a large majority of cases. If we find we need to get more refined with our location,
     * then we can revisit this.
     *
     * @param context
     * @return String country code
     */
    protected String getCountryCode(Context context) {
        return context.getResources().getConfiguration().locale.getCountry();
    }

    @Override
    protected void run(Context context) throws Throwable {
        if(mPhoneEmail == null) {
            return;
        }

        String country = getCountryCode(context);

        attemptRegistration(context, country);
    }

    protected abstract void attemptRegistration(Context context, String country) throws Throwable;

    protected boolean matchesEmail(String email)
    {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
