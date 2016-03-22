package in.bucksbuddy.bucksbuddy;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.appspot.bucks_buddy.bucksbuddy.Bucksbuddy;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsBooleanMessage;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsCreditForm;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsGCMForm;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsTransactionForm;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;

/**
 * Created by shrukul on 27/1/16.
 */
// abbreviated tag name
public class RegistrationIntentService extends IntentService {

    SharedPreferences preferences;
    String token;

    // abbreviated tag name
    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences("bucksbuddyPref",0);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Make a call to Instance API

        InstanceID instanceID = InstanceID.getInstance(this);
        String senderId = getResources().getString(R.string.gcm_defaultSenderId);
        try {
            // request token that will be used by the server to send push notifications
            token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
            Log.d(TAG, "GCM Registration Token: " + token);

            // pass along this data
            sendRegistrationToServer(token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
        preferences.edit().putString("token",token).commit();
        UserSessionManager session = new UserSessionManager(getApplicationContext());
        ModelsGCMForm mgf = new ModelsGCMForm();
        mgf.setRegid(token);
        mgf.setSender(session.getProfileInfo().phone);

        BucksBuddyTask obj = new BucksBuddyTask();
        obj.execute(mgf);
    }
}

class BucksBuddyTask extends AsyncTask<ModelsGCMForm, Void, ModelsBooleanMessage> {
    Context context;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ModelsBooleanMessage doInBackground(ModelsGCMForm... mcf) {

        Bucksbuddy.Builder builder = new Bucksbuddy.Builder(
                AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
        Bucksbuddy service = builder.build();

        ModelsBooleanMessage res = null;

        try {
            res = service.registerGCM(mcf[0]).execute();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("gae", "Some error");
        }
        return res;
    }

    @Override
    protected void onPostExecute(ModelsBooleanMessage res) {
        // Do something with the result.
        Boolean result = res.getData();
        String output = "";

        if(result == true){
        }
    }
}
