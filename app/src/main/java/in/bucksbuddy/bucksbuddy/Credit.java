package in.bucksbuddy.bucksbuddy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.appspot.bucks_buddy.bucksbuddy.Bucksbuddy;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsBillPayForm;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsCreditForm;
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
 * Created by shrukul on 20/1/16.
 */
public class Credit extends Fragment {

    EditText input_credit;
    Button btn_credit;
    private final String serverUrl = "http://bucksbuddy.pe.hu/index.php";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.credit, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        input_credit = (EditText) getView().findViewById(R.id.input_credit);
        btn_credit = (Button) getView().findViewById(R.id.btn_credit);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btn_credit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().getApplicationContext().INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus()) ? null : getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                credit();
            }
        });
    }

    private void credit() {

        //transfer.setEnabled(false);

        String amt = input_credit.getText().toString();

        // TODO: Implement your own signup logic here.

        UserSessionManager session = new UserSessionManager(getActivity().getApplicationContext());
        String s_id = session.getProfileInfo().phone;

        ModelsCreditForm mcf = new ModelsCreditForm();
        mcf.setAmount((long) Integer.parseInt(amt));
        mcf.setSender(s_id);

        BucksBuddyTask obj = new BucksBuddyTask();
        obj.execute(mcf);

//        AsyncDataClass asyncRequestObject = new AsyncDataClass();
//        asyncRequestObject.execute(serverUrl, s_id, amt);
    }

    private class BucksBuddyTask extends AsyncTask<ModelsCreditForm, Void, ModelsTransactionForm>{
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
        protected ModelsTransactionForm doInBackground(ModelsCreditForm... mcf) {

            Bucksbuddy.Builder builder = new Bucksbuddy.Builder(
                    AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
            Bucksbuddy service = builder.build();

            ModelsTransactionForm trans = null;

            try {
                trans = service.credit(mcf[0]).execute();
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
                case 0:
                    output = "The ID is incorrect.";
                    break;
                case 1:

                    UserSessionManager session = new UserSessionManager(getActivity().getApplicationContext());
                    session.setBalance("" + trans.getBalance().intValue());

                    output = "Transaction Successful";
                    Snackbar snackbar = Snackbar.make(getView(), output, Snackbar.LENGTH_LONG);
                    snackbar.show();
                    Intent it = new Intent(getActivity(),TransferSuccess.class);
                    it.putExtra("msg", "Successfully credited ₹ " + input_credit.getText() + " to your wallet");
                    startActivity(it);
                    input_credit.setText("");
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
            super.onPreExecute();
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Crediting Money...");
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
                nameValuePairs.add(new BasicNameValuePair("amount", params[2]));

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

            if (result.equals("") || result == null) {
                Snackbar snackbar = Snackbar.make(getView(), "Server Connection Failed", Snackbar.LENGTH_LONG);
                snackbar.show();
                return;
            }

            int jsonResult = returnParsedJsonObject(result);

            if (jsonResult == 0) {
                Snackbar snackbar = Snackbar.make(getView(), "Transaction Failed", Snackbar.LENGTH_LONG);
                snackbar.show();
                return;
            } else if (jsonResult == 1) {
                Snackbar snackbar = Snackbar.make(getView(), "Transaction Successful. Your account will be credited in a minute...", Snackbar.LENGTH_LONG);
                snackbar.show();

                Intent it = new Intent(getActivity(),TransferSuccess.class);
                it.putExtra("msg", "Successfully credited ₹ " + input_credit.getText() + " to your wallet");
                startActivity(it);
                input_credit.setText("");
//                UserSessionManager session = new UserSessionManager(getActivity().getApplicationContext());
//                session.setBalance("" + ((Integer.parseInt(session.getBalance())) + Integer.parseInt(input_credit.getText().toString())));
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
}
