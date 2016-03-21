package in.bucksbuddy.bucksbuddy;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
public class ContentFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

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
        View v = inflater.inflate(R.layout.content_fragment,container,false);
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
        amt = (TextView)getView().findViewById(R.id.account_wallet_amount);
        swipeRefreshLayout = (SwipeRefreshLayout)getView().findViewById(R.id.swipe_refresh_layout);
        spinner = (MaterialProgressBar)getView().findViewById(R.id.loading_spinner);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        spinner.setVisibility(View.VISIBLE);
        llm = new LinearLayoutManager(getActivity());
        rv=(RecyclerView)getView().findViewById(R.id.rv);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);
        session = new UserSessionManager(getActivity().getApplicationContext());
        UserSessionManager session = new UserSessionManager(getActivity().getApplicationContext());
        amt.setText("₹ " + session.getBalance());
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(
                Color.GREEN, Color.RED, Color.BLUE, Color.YELLOW);

//        initializeData();
//        initializeAdapter();

        UserSessionManager sessionManager = new UserSessionManager(getActivity().getApplicationContext());
        initRecyclerView obj = new initRecyclerView();
        if(sessionManager.isUserLoggedIn())
            obj.execute();
    }

    private void initializeData() throws ParseException {
        DatabaseHandler db = new DatabaseHandler(getActivity());
//        persons = new ArrayList<>();
        persons = db.getAllContacts();
        db.close();
//        persons.add(new Person("Emma Wilson", "23 years old", R.drawable.profile));
//        persons.add(new Person("Lavery Maiss", "25 years old", R.drawable.profile));
//        persons.add(new Person("Lillie Watts", "35 years old", R.drawable.profile));
    }

    private void initializeAdapter(){
        RVAdapter adapter = new RVAdapter(persons);
        rv.setAdapter(adapter);
    }

    @Override
    public void onRefresh() {
        AsyncFetchBal asyncRequestObject = new AsyncFetchBal();
        asyncRequestObject.execute(serverUrl, "getBalnce", session.getProfileInfo().phone);;
    }

    private class initRecyclerView extends AsyncTask<String, Void, String>{

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

    private class AsyncFetchBal extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // showing refresh animation before making http call
            swipeRefreshLayout.setRefreshing(true);
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

                nameValuePairs.add(new BasicNameValuePair("action", params[1]));
                nameValuePairs.add(new BasicNameValuePair("id", params[2]));

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

            // stopping swipe refresh
            swipeRefreshLayout.setRefreshing(false);

            System.out.println("Resulted Value: " + result);

            if (result.equals("") || result == null) {
                Snackbar snackbar = Snackbar.make(getView(), "Server Connection Failed", Snackbar.LENGTH_LONG);
                snackbar.show();
                return;
            }

            int jsonResult = returnParsedJsonObject(result);

            if (jsonResult == 0) {
                Snackbar snackbar = Snackbar.make(getView(), "Something Went Wrong. Please try again later...", Snackbar.LENGTH_LONG);
                snackbar.show();
                return;
            } else if (jsonResult == 1) {
                try {
                    session.setBalance("" + ((Integer.parseInt(new JSONObject(result).getString("balance")))));
                    amt.setText("₹ " + session.getBalance());
                    Snackbar snackbar = Snackbar.make(getView(), "Balance Updated.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_refresh){
            onRefresh();
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }
}
