package org.dosomething.letsdothis.tasks;
import android.content.Context;

import org.dosomething.letsdothis.network.DataHelper;
import org.dosomething.letsdothis.network.NorthstarAPI;

import co.touchlab.android.threading.eventbus.EventBusExt;
import retrofit.client.Response;

/**
 * Created by toidiu on 4/15/15.
 */
public class SignupTask extends BaseRegistrationTask
{

    public SignupTask(String email, String phone, String password)
    {
        super(email, phone, password);
    }

    @Override
    protected void attemptRegistration(Context context) throws Throwable
    {
        Response response = null;
        if(email != null)
        {
            //            String regInfo= "{email: test@touchlab.co, password: test}";
            String regInfo = "{email: " + email + ", password: " + password + "}";
            response = DataHelper.makeRequestAdapter().create(NorthstarAPI.class)
                    .registerWithEmail(regInfo);
            DataHelper.debugOut(response);
        }
        else if(phone != null)
        {
            String regInfo = "{mobile: " + phone + ", password: " + password + "}";
            response = DataHelper.makeRequestAdapter().create(NorthstarAPI.class)
                    .registerWithMobile(regInfo);
        }
        if(response != null)
        {
            //FIXME this is not solid and will be handled when we have response objects
            success = true;
        }
    }


    @Override
    protected void onComplete(Context context)
    {
        EventBusExt.getDefault().post(this);
        super.onComplete(context);
    }
}
