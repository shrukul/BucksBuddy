package in.bucksbuddy.bucksbuddy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appspot.bucks_buddy.bucksbuddy.Bucksbuddy;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsCreditForm;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsGetBalanceForm;
import com.appspot.bucks_buddy.bucksbuddy.model.ModelsStringMessage;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 04-06-2015.
 */
public class ContentFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private final String serverUrl = "http://bucksbuddy.pe.hu/index.php";

    private List<Person> persons;
    private RecyclerView rv;
    LinearLayoutManager llm;
    TextView amt;
    private SwipeRefreshLayout swipeRefreshLayout;
    UserSessionManager session;
    private MaterialProgressBar spinner;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_fragment, container, false);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_content, menu);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        amt = (TextView) getView().findViewById(R.id.account_wallet_amount);
        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);
        spinner = (MaterialProgressBar) getView().findViewById(R.id.loading_spinner);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        spinner.setVisibility(View.VISIBLE);
        llm = new LinearLayoutManager(getActivity());
        rv = (RecyclerView) getView().findViewById(R.id.rv);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        session = new UserSessionManager(getActivity().getApplicationContext());
        UserSessionManager session = new UserSessionManager(getActivity().getApplicationContext());
        amt.setText("₹ " + session.getBalance());
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(
                Color.GREEN, Color.RED, Color.BLUE, Color.YELLOW);

        UserSessionManager sessionManager = new UserSessionManager(getActivity().getApplicationContext());
        initRecyclerView obj = new initRecyclerView();
        if (sessionManager.isUserLoggedIn())
            obj.execute();
    }

    @Override
    public void onRefresh() {

        ModelsGetBalanceForm mgbf = new ModelsGetBalanceForm();
        mgbf.setPhoneNumber(session.getProfileInfo().phone);
        BucksBuddyTask obj = new BucksBuddyTask();
        obj.execute(mgbf);
    }

    private class initRecyclerView extends AsyncTask<String, Void, String> {

        RVAdapter adapter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            DatabaseHandler db = new DatabaseHandler(getActivity());

            try {
                persons = db.getAllContacts();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            db.close();

            adapter = new RVAdapter(persons);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            spinner.setVisibility(View.GONE);
            rv.setAdapter(adapter);
        }
    }

    private class BucksBuddyTask extends AsyncTask<ModelsGetBalanceForm, Void, ModelsStringMessage> {
        Context context;
        final ProgressDialog progressDialog = new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // showing refresh animation before making http call
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected ModelsStringMessage doInBackground(ModelsGetBalanceForm... mgbf) {

            Bucksbuddy.Builder builder = new Bucksbuddy.Builder(
                    AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
            Bucksbuddy service = builder.build();

            ModelsStringMessage bal = null;

            try {
                bal = service.getBalance(mgbf[0]).execute();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("gae", "Some error");
            }
            return bal;
        }

        @Override
        protected void onPostExecute(ModelsStringMessage res) {

            // stopping swipe refresh
            swipeRefreshLayout.setRefreshing(false);

            // Do something with the result.
            if (res != null) {
                session.setBalance(res.getData().toString());
                amt.setText("₹ " + session.getBalance());
                Snackbar snackbar = Snackbar.make(getView(), "Balance Updated.", Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                Snackbar snackbar = Snackbar.make(getView(), "Something Went Wrong. Please try again later...", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            onRefresh();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }
}
