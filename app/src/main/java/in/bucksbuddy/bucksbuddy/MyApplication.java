package in.bucksbuddy.bucksbuddy;

import android.app.Application;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by shrukul on 12/1/16.
 */
public class MyApplication extends Application {

    public static GoogleSignInAccount myAccount;

    public void setAccount(GoogleSignInResult result) {
        this.myAccount = result.getSignInAccount();
    }

    public GoogleSignInAccount getAccount() {
        return this.myAccount;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }
}

