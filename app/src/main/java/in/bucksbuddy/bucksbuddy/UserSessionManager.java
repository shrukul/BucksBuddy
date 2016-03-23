package in.bucksbuddy.bucksbuddy;

/**
 * Created by shrukul on 31/12/15.
 */

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UserSessionManager {

    // Shared Preferences reference
    SharedPreferences pref;

    // Editor reference for Shared preferences
    Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREFER_NAME = "bucksbuddyPref";

    // All Shared Preferences Keys
    private static final String IS_USER_LOGIN = "IsUserLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "name";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    public static final String KEY_PHONE = "phone";

    public static final String KEY_URI = "uri";

    public static final String KEY_BAL = "bal";

    // Constructor
    public UserSessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //Create login session
    public void createUserLoginSession(String name, String email, String phone, String uri) {
        // Storing login value as TRUE
        editor.putBoolean(IS_USER_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NAME, name);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        editor.putString(KEY_PHONE, phone);

        editor.putString(KEY_URI, uri);

        // commit changes
        editor.commit();

        System.out.println(pref.getBoolean(IS_USER_LOGIN, false));
    }

    public void setBalance(String balance) {
        editor.putString(KEY_BAL, balance);
        editor.commit();
    }

    public String getBalance() {
        return pref.getString(KEY_BAL, "0");
    }

    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else do anything
     */
    public boolean checkLogin() {
        // Check login status
        if (!this.isUserLoggedIn()) {

            System.out.println("here A1");

            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);

            // Closing all the Activities from stack
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);

            return true;
        }
        System.out.println("here A2");
        return false;
    }


    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {

        //Use hashmap to store user credentials
        HashMap<String, String> user = new HashMap<String, String>();

        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     */
    public void logoutUser() {

        // Clearing all user data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, LoginActivity.class);

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }


    // Check for login
    public boolean isUserLoggedIn() {
        System.out.println("here A3");
        System.out.println("Here - " + pref.getBoolean(IS_USER_LOGIN, false));
        return pref.getBoolean(IS_USER_LOGIN, false);
    }

    //Create login session
    public ProfileData getProfileInfo() {

        ProfileData pd = new ProfileData();

        // Storing name in pref
        pd.name = pref.getString(KEY_NAME, "");

        // Storing email in pref
        pd.email = pref.getString(KEY_EMAIL, "");

        pd.phone = pref.getString(KEY_PHONE, "");

        pd.url = pref.getString(KEY_URI, "");

        return pd;
    }

    public String check() {
        return pref.getString(KEY_EMAIL, "");
    }
}

