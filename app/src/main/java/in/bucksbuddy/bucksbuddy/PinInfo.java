package in.bucksbuddy.bucksbuddy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by shrukul on 21/1/16.
 */
public class PinInfo extends AppCompatActivity {

    EditText id,phone;
    Button btn;
    int setup = -1;
    View parentLayout;

    private final String serverUrl = "http://bucksbuddy.pe.hu/index.php";
    String idstr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_info);

        phone = (EditText)findViewById(R.id.p_phone);
        btn = (Button)findViewById(R.id.btn_setup);
        parentLayout = findViewById(android.R.id.content);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validate()) {
                    Snackbar snackbar = Snackbar.make( parentLayout, "Enter Valid Details.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;
                }

                setup();
            }
        });


    }

    private void setup() {
        String sphone;

        sphone = phone.getText().toString();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("phone",sphone);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    public boolean validate() {
        boolean valid = true;

        String phoneText = phone.getText().toString();

        if (phoneText.length() != 10) {
            phone.setError("Enter a valid Phone Number");
            valid = false;
        } else {
            phone.setError(null);
        }
        return valid;
    }
}
