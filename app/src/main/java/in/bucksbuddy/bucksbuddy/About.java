package in.bucksbuddy.bucksbuddy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Created by shrukul on 20/1/16.
 */
public class About extends Fragment {

    private List<Person> foundTeam;
    private List<Person> techTeam;
    private List<Person> pubTeam;
    private RecyclerView rvabout1, rvabout2, rvabout3;
    MyLinearLayoutManager llm1, llm2, llm3;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.about, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

/*        llm1 = new MyLinearLayoutManager(getActivity());
        rvabout1 = (RecyclerView) getView().findViewById(R.id.rvabout1);
        rvabout1.setLayoutManager(llm1);
        rvabout1.setHasFixedSize(true);

        llm2 = new MyLinearLayoutManager(getActivity());
        rvabout2 = (RecyclerView) getView().findViewById(R.id.rvabout2);
        rvabout2.setLayoutManager(llm2);
        rvabout2.setHasFixedSize(true);

        llm3 = new MyLinearLayoutManager(getActivity());
        rvabout3 = (RecyclerView) getView().findViewById(R.id.rvabout3);
        rvabout3.setLayoutManager(llm3);
        rvabout3.setHasFixedSize(true);

        AsyncDataClass asyncRequestObject = new AsyncDataClass();
        asyncRequestObject.execute();*/

    }

/*    private void initializeData() {
        foundTeam = new ArrayList<>();
        foundTeam.add(new Person("Srinath Chamarthi", "NITK Surathkal", R.drawable.srinath, ""));
        foundTeam.add(new Person("Pavan Vellampalli", "NITK Surathkal", R.drawable.pavan, ""));

        techTeam = new ArrayList<>();
        techTeam.add(new Person("Shrukul Habib", "NITK Surathkal", R.drawable.shrukul, ""));

        pubTeam = new ArrayList<>();
        pubTeam.add(new Person("Tejaswi Ravu", "NITK Surathkal", R.drawable.tejaswi, ""));
        pubTeam.add(new Person("S Harshavardhan Reddy", "NITK Surathkal", R.drawable.harsh, ""));
    }

    private void initializeAdapter() {
        RVAdapterAbout adapter = new RVAdapterAbout(foundTeam);
        rvabout1.setAdapter(adapter);
        rvabout1.setHasFixedSize(true);
        adapter = new RVAdapterAbout(techTeam);
        rvabout2.setAdapter(adapter);
        rvabout2.setHasFixedSize(true);
        adapter = new RVAdapterAbout(pubTeam);
        rvabout3.setAdapter(adapter);
        rvabout3.setHasFixedSize(true);
    }

    private class AsyncDataClass extends AsyncTask<String, Void, String> {


        @Override

        protected String doInBackground(String... params) {
            initializeData();
            return "";
        }

        @Override

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            initializeAdapter();
        }
    }*/
}
