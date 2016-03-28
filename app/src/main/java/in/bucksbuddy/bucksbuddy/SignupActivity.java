package in.bucksbuddy.bucksbuddy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.bucks_buddy.bucksbuddy.Bucksbuddy;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsBillShareForm;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsBooleanMessage;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsProfileForm;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsUserForm;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class SignupActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "SignupActivity";
    private final String serverUrl = "http://bucksbuddy.pe.hu/index.php";
    String PROJECT_NUMBER = "744421378343";
    private String sname, semail, suri, sphone, spin;

    UserSessionManager session;
    View parentLayout;

    private int signedUp = -1;
    private SignInButton googleSignInButton;

    private GoogleSignInOptions gso;

    //google api client
    private GoogleApiClient mGoogleApiClient;

    EditText nameText;
    EditText emailText;
    Button signupButton;
    TextView loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        nameText = (EditText) findViewById(R.id.input_name);
        emailText = (EditText) findViewById(R.id.input_email);
        signupButton = (Button) findViewById(R.id.btn_signup);
        loginLink = (TextView) findViewById(R.id.link_login);
        parentLayout = findViewById(android.R.id.content);

        //Initializing google signin option
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        //Initializing signinbutton
        googleSignInButton = (SignInButton) findViewById(R.id.google_sign_in);
        googleSignInButton.setSize(SignInButton.SIZE_WIDE);
        googleSignInButton.setScopes(gso.getScopeArray());

        //Initializing google api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //Setting onclick listener to signing button
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, 100);
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validate()) {
                    onSignupFailed("Enter the right details");
                    return;
                }

//                RegGCM();

                sname = nameText.getText().toString();
                semail = emailText.getText().toString();
                suri = "null";

                getAddInfo();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }

    public void signup(String name, String email, String phone, String uri, String pin) {
        Log.d(TAG, "Signup");

//        RegGCM();

//        signupButton.setEnabled(false);

        // TODO: Implement your own signup logic here.

        signedUp = -1;

//        AsyncDataClass asyncRequestObject = new AsyncDataClass();
//        asyncRequestObject.execute(serverUrl, name, email, id, uri, phone, sregid);

        System.out.println("Reached signup");

        ModelsUserForm muf = new ModelsUserForm();
        muf.setDisplayName(name);
        muf.setMainEmail(email);
        muf.setPhoneNumber(phone);
        muf.setUri(uri);
        muf.setPin(Long.valueOf(pin));

        BucksBuddyTask obj = new BucksBuddyTask();
        obj.execute(muf);
    }

    private class BucksBuddyTask extends AsyncTask<ModelsUserForm, Void, ModelsProfileForm> {
        Context context;
        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Signing Up...");
            progressDialog.show();
        }

        protected ModelsProfileForm doInBackground(ModelsUserForm... muf) {

            Bucksbuddy.Builder builder = new Bucksbuddy.Builder(
                    AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
            Bucksbuddy service = builder.build();

            ModelsProfileForm mpf = null;

            try {
                mpf = service.registerUser(muf[0]).execute();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Some error");
            }
            return mpf;
        }

        protected void onPostExecute(ModelsProfileForm prof) {
            progressDialog.dismiss();
            // Do something with the result.
            if (prof != null && prof.getSuccess().intValue() == 1) {
                sname = prof.getDisplayName();
                semail = prof.getMainEmail();
                suri = prof.getUri();
                sphone = prof.getPhoneNumber();
                onSignupSuccess();
            } else if (prof.getSuccess().intValue() == 0) {
                onSignupFailed("An account with the same Phone Number already exists. Try Logging in instead.");
            } else {
                onSignupFailed("Oops. Something went wrong... Try again!");
            }
        }
    }

    private void RegGCM() {
/*        GCMClientManager pushClientManager = new GCMClientManager(this, PROJECT_NUMBER);
        pushClientManager.registerIfNeeded(new GCMClientManager.RegistrationCompletedHandler() {
            @Override
            public void onSuccess(String registrationId, boolean isNewRegistration) {

                Log.d("Registration id", registrationId);
                sregid = registrationId;
            }

            @Override
            public void onFailure(String ex) {
                super.onFailure(ex);
            }
        });*/

        // Start IntentService to register this application with GCM.
        Intent intent2 = new Intent(this, RegistrationIntentService.class);
        startService(intent2);
    }


    public void onSignupSuccess() {

        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        session = new UserSessionManager(getApplicationContext());
        session.createUserLoginSession(sname,
                semail, sphone, suri);
        UserSessionManager session = new UserSessionManager(getApplicationContext());
        session.setBalance("0");

        RegGCM();

        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void onSignupFailed(String text) {
        Snackbar snackbar = Snackbar.make(parentLayout, text, Snackbar.LENGTH_LONG);
        snackbar.show();
        signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();
        String email = emailText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            nameText.setError("at least 3 characters");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }
        return valid;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void getAddInfo() {
        Intent i = new Intent(this, PinInfo.class);
        startActivityForResult(i, 1);
    }

    private class AsyncDataClass extends AsyncTask<String, Void, String> {

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);

        @Override
        protected String doInBackground(String... params) {
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
            HttpConnectionParams.setSoTimeout(httpParameters, 5000);
            HttpClient httpClient = new DefaultHttpClient(httpParameters);
            HttpPost httpPost = new HttpPost(params[0]);

            String jsonResult = "";

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

                nameValuePairs.add(new BasicNameValuePair("name", params[1]));
                nameValuePairs.add(new BasicNameValuePair("email", params[2]));
                nameValuePairs.add(new BasicNameValuePair("id", params[3]));
                nameValuePairs.add(new BasicNameValuePair("uri", params[4]));
                nameValuePairs.add(new BasicNameValuePair("phone", params[5]));
                nameValuePairs.add(new BasicNameValuePair("regid", params[6]));

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpClient.execute(httpPost);

                jsonResult = inputStreamToString(response.getEntity().getContent()).toString();

                System.out.println("Returned Json object " + jsonResult.toString());

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonResult;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Creating Account...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            System.out.println("Resulted Value: " + result);

            if (result.equals("") || result == null) {
                Snackbar snackbar = Snackbar.make(parentLayout, "Server Connection Failed", Snackbar.LENGTH_LONG);
                snackbar.show();
                signedUp = 0;
                onSignupFailed("");
                return;
            }

            int jsonResult = returnParsedJsonObject(result);

            if (jsonResult == 0) {
                Snackbar snackbar = Snackbar.make(parentLayout, "The Email already exists. Login Intead.", Snackbar.LENGTH_LONG);
                snackbar.show();
                signedUp = 1;
                onSignupFailed("");
                return;
            } else if (jsonResult == 1) {
                signedUp = 2;
                onSignupSuccess();
            }
        }

        private StringBuilder inputStreamToString(InputStream is) {
            String rLine = "";
            StringBuilder answer = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            try {
                while ((rLine = br.readLine()) != null) {
                    answer.append(rLine);
                }
            } catch (IOException e) {

// TODO Auto-generated catch block
                e.printStackTrace();
            }
            return answer;
        }
    }

    private int returnParsedJsonObject(String result) {

        JSONObject resultObject = null;

        int returnedResult = 0;

        try {

            resultObject = new JSONObject(result);

            returnedResult = resultObject.getInt("success");

        } catch (JSONException e) {

            e.printStackTrace();

        }

        return returnedResult;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("on Activity");
        //If signin
        if (requestCode == 100) {
            System.out.println("result 100");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //Calling a new function to handle signin
            handleSignInResult(result);
        } else if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            SharedPreferences preferences = getSharedPreferences("bucksbuddyPref", 0);
            sphone = data.getStringExtra("phone");
            spin = data.getStringExtra("pin");
            signup(sname, semail, sphone, suri, spin);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {

        System.out.println("Handling Part");

        if (result.isSuccess()) {
            ((MyApplication) getApplicationContext()).setAccount(result);

            String name = result.getSignInAccount().getDisplayName();
            String email = result.getSignInAccount().getEmail();
            String uri = "null";
            try {
                uri = result.getSignInAccount().getPhotoUrl().toString();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            sname = name;
            semail = email;
            suri = uri;

            getAddInfo();
//            RegGCM();
        } else {
            System.out.println("GoogleSignUpFailed");
            System.out.println(result.getStatus());
        }
    }

}