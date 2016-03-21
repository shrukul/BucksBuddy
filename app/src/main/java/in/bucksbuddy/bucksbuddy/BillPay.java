package in.bucksbuddy.bucksbuddy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.bucks_buddy.bucksbuddy.Bucksbuddy;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsBillPayForm;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsBillShareForm;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsTransactionForm;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by shrukul on 30/12/15.
 */
public class BillPay extends Fragment {


    private static final String TAG = "BillShareActivity";
    private final String serverUrl = "http://bucksbuddy.pe.hu/index.php";

    EditText sender_id,receiver_id,sender_pin,amount,receiver_pin;
    Button transfer;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.bill_pay,container,false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sender_id = (EditText)getView().findViewById(R.id.sender_id);
        receiver_id = (EditText)getView().findViewById(R.id.receiver_id);
        sender_pin = (EditText)getView().findViewById(R.id.sender_pin);
        receiver_pin = (EditText)getView().findViewById(R.id.receiver_pin);
        amount = (EditText)getView().findViewById(R.id.amount);
        transfer = (Button)getView().findViewById(R.id.transfer);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        UserSessionManager session = new UserSessionManager(getActivity().getApplicationContext());
        sender_id.setText(session.getProfileInfo().phone);

        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transfer();
            }
        });
    }

    private void transfer() {

        InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(getActivity().getApplicationContext().INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null : getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        //transfer.setEnabled(false);

        String amt = amount.getText().toString();
        UserSessionManager session = new UserSessionManager(getActivity().getApplicationContext());
        int balance = Integer.parseInt(session.getBalance());
        if(Integer.parseInt(amt) > balance){
            Snackbar snackbar = Snackbar.make(getView(), "Amount Exceeds Balance", Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        } else if(Integer.parseInt(amt) > 2000){
            Snackbar snackbar = Snackbar.make(getView(), "Transaction Limit is 2000, for now...", Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }

        String s_id = sender_id.getText().toString();
        String r_id = receiver_id.getText().toString();
        String s_pin = sender_pin.getText().toString();
        String r_pin = receiver_pin.getText().toString();

        // TODO: Implement your own signup logic here.

        int tranferred = -1;

        ModelsBillPayForm mbpf = new ModelsBillPayForm();
        mbpf.setAmount((long) Integer.parseInt(amt));
        mbpf.setSender(s_id);
        mbpf.setReceiver(r_id);
        mbpf.setSenderPin((long) Integer.parseInt(s_pin));
        mbpf.setReceiverPin((long)Integer.parseInt(r_pin));

        BucksBuddyTask obj = new BucksBuddyTask();
        obj.execute(mbpf);
//        AsyncDataClass asyncRequestObject = new AsyncDataClass();
//        asyncRequestObject.execute(serverUrl, s_id, r_id, s_pin, r_pin, amt);
    }

    private class BucksBuddyTask extends AsyncTask<ModelsBillPayForm, Void, ModelsTransactionForm>{
        Context context;
        final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Transferring Money...");
            progressDialog.show();
        }

        @Override
        protected ModelsTransactionForm doInBackground(ModelsBillPayForm... mbpf) {

            Bucksbuddy.Builder builder = new Bucksbuddy.Builder(
                    AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
            Bucksbuddy service = builder.build();

            ModelsTransactionForm trans = null;

            try {
                trans = service.billPay(mbpf[0]).execute();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("gae","Some error");
            }
            return trans;
        }

        @Override
        protected void onPostExecute(ModelsTransactionForm trans) {
            progressDialog.dismiss();
            // Do something with the result.
            int result = trans.getSuccess().intValue();
            String output = "";

            switch (result){
                case 1:
                    DatabaseHandler db = new DatabaseHandler(getActivity());
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = df.format(c.getTime());

                    db.addContact(new Person(trans.getDisplayName(), formattedDate, R.drawable.profile, ""+trans.getAmount().intValue(), 0));

                    db.close();
                    UserSessionManager session = new UserSessionManager(getActivity().getApplicationContext());
                    session.setBalance("" + trans.getBalance().intValue());

                    output = "Transaction Successful";

                    break;
                case 2:
                    output = "The Pin is Incorrect. Please enter the pin again.";
                    break;
                case 3:
                    output = "You dont have sufficient balance to make this transaction.";
                    break;
                case 4:
                    output = "The Receiver Phone number is incorrect. Pleae enter the phone number again";
                    break;
            }

            Snackbar snackbar = Snackbar.make(getView(), output, Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private class AsyncDataClass extends AsyncTask<String, Void, String> {

        final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute() {
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Transferring Money...");
            progressDialog.show();
        }

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

                nameValuePairs.add(new BasicNameValuePair("sender_id", params[1]));
                nameValuePairs.add(new BasicNameValuePair("receiver_id", params[2]));
                nameValuePairs.add(new BasicNameValuePair("sender_pin", params[3]));
                nameValuePairs.add(new BasicNameValuePair("receiver_pin", params[4]));
                nameValuePairs.add(new BasicNameValuePair("amount", params[5]));

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
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            System.out.println("Resulted Value: " + result);

            if(result.equals("") || result == null){
                Snackbar snackbar = Snackbar.make(getView(), "Server Connection Failed", Snackbar.LENGTH_LONG);
                snackbar.show();
                return;
            }

            int jsonResult = returnParsedJsonObject(result);

            if(jsonResult == 0){
                Snackbar snackbar = Snackbar.make(getView(), "Inavalid ID or pin", Snackbar.LENGTH_LONG);
                snackbar.show();
                return;
            } else if(jsonResult == 1){

                Snackbar snackbar = Snackbar.make(getView(), "Transaction Successful", Snackbar.LENGTH_LONG);
                snackbar.show();

                DatabaseHandler db = new DatabaseHandler(getActivity());
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = df.format(c.getTime());

                try {
                    db.addContact(new Person(new JSONObject(result).getString("name"), formattedDate, R.drawable.profile, amount.getText().toString(),0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                db.close();

                UserSessionManager session = new UserSessionManager(getActivity().getApplicationContext());
                session.setBalance("" + ((Integer.parseInt(session.getBalance())) - Integer.parseInt(amount.getText().toString())));


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

    private int returnParsedJsonObject(String result){

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
}
