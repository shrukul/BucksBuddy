package in.bucksbuddy.bucksbuddy;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.appspot.bucks_buddy.bucksbuddy.Bucksbuddy;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsGoogleLoginForm;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsLoginForm;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsProfileForm;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    UserSessionManager session;
    View parentLayout;

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private int loggedIn = -1;
    private String phone = "", name = "", uri = "", email = "";

    private SignInButton googleSignInButton;
    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApiClient;

    EditText phoneText, pinText;
    Button loginButton;
    TextView signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new UserSessionManager(getApplicationContext());
        initVars();

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

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
    }

    private void initVars() {
        phoneText = (EditText) findViewById(R.id.input_phone);
        pinText = (EditText) findViewById(R.id.input_pin);
        loginButton = (Button) findViewById(R.id.btn_login);
        signupLink = (TextView) findViewById(R.id.link_signup);
        parentLayout = findViewById(android.R.id.content);
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed(-1);
            return;
        }

        InputMethodManager inputManager = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        final String phone = phoneText.getText().toString();
        final String pin = pinText.getText().toString();
        loggedIn = -1;

        ModelsLoginForm mlf = new ModelsLoginForm();
        mlf.setPhoneNumber(phone);
        mlf.setPin(Long.valueOf(pin));

        BucksBuddyTask obj = new BucksBuddyTask();
        obj.execute(mlf);
    }

    private class BucksBuddyTask extends AsyncTask<ModelsLoginForm, Void, ModelsProfileForm> {
        Context context;
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Logging in...");
            progressDialog.show();
        }

        @Override
        protected ModelsProfileForm doInBackground(ModelsLoginForm... mlf) {

            Bucksbuddy.Builder builder = new Bucksbuddy.Builder(
                    AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
            Bucksbuddy service = builder.build();

            ModelsProfileForm profile = null;

            try {
                profile = service.loginUser(mlf[0]).execute();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("gae", "Some error");
            }
            System.out.println(profile);
            return profile;
        }

        @Override
        protected void onPostExecute(ModelsProfileForm profile) {
            progressDialog.dismiss();
            if(profile!=null && profile.getSuccess().intValue() == 1) {
                phone = profile.getPhoneNumber();
                name = profile.getDisplayName();
                uri = profile.getUri();
                email = profile.getMainEmail();
                UserSessionManager session = new UserSessionManager(getApplicationContext());
                session.setBalance("" + profile.getBalance());
                System.out.println("got id = " + phone);
                onLoginSuccess();
                // Do something with the result.
            }else{
                onLoginFailed(profile.getSuccess().intValue());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        } else if (requestCode == 100) {
            System.out.println("result 100");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //Calling a new function to handle signin
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {

        System.out.println("Handling Part");

        if (result.isSuccess()) {
            ((MyApplication) getApplicationContext()).setAccount(result);

            String email = result.getSignInAccount().getEmail();
            String uri = "null";
            try {
                uri = result.getSignInAccount().getPhotoUrl().toString();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            ModelsGoogleLoginForm mglf = new ModelsGoogleLoginForm();
            mglf.setMainEmail(email);
            mglf.setUri(uri);

            GoogleSignInTask asyncRequestObject = new GoogleSignInTask();
//            System.out.println("values are " + serverUrl + ":" + uri + ":" + uri);
            asyncRequestObject.execute(mglf);
        }
    }

    private class GoogleSignInTask extends AsyncTask<ModelsGoogleLoginForm, Void, ModelsProfileForm>{
        Context context;
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Signing In...");
            progressDialog.show();
        }

        @Override
        protected ModelsProfileForm doInBackground(ModelsGoogleLoginForm... mglf) {

            Bucksbuddy.Builder builder = new Bucksbuddy.Builder(
                    AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
            Bucksbuddy service = builder.build();

            ModelsProfileForm prof = null;

            try {
                prof = service.googleLoginUser(mglf[0]).execute();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("gae","Some error");
            }
            return prof;
        }

        @Override
        protected void onPostExecute(ModelsProfileForm profile) {
            progressDialog.dismiss();
            // Do something with the result.

            if(profile!=null && profile.getSuccess().intValue() == 1) {
                phone = profile.getPhoneNumber();
                name = profile.getDisplayName();
                uri = profile.getUri();
                email = profile.getMainEmail();
                UserSessionManager session = new UserSessionManager(getApplicationContext());
                session.setBalance("" + profile.getBalance());
                System.out.println("got id = " + phone);
            } else {
                onLoginFailed(profile.getSuccess().intValue());
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        loginButton.setEnabled(true);

        session.createUserLoginSession(name,
                email, phone, uri);

        RegGCM();

        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);

        finish();
    }

    private void RegGCM() {

        // Start IntentService to register this application with GCM.
        Intent intent2 = new Intent(this, RegistrationIntentService.class);
        startService(intent2);
    }

    public void onLoginFailed(int resCode) {
        String text = "Something went Wrong. Try again...";
        if(resCode == 0)
            text = "The User does not exist. Check the phone number again...";
        else if (resCode == 2)
            text = "Invalid Phone Number / Pin";
        if(resCode != -1) {// Validation failed
            Snackbar snackbar = Snackbar.make(parentLayout, text, Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String phone = phoneText.getText().toString();
        String pin = pinText.getText().toString();

        if (phone.isEmpty() || !Patterns.PHONE.matcher(phone).matches()) {
            phoneText.setError("enter a valid phone address");
            valid = false;
        } else {
            phoneText.setError(null);
        }

        if (pin.isEmpty() || pin.length() != 4) {
            pinText.setError("Pin must be 4 characters long");
            valid = false;
        } else {
            pinText.setError(null);
        }

        return valid;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
