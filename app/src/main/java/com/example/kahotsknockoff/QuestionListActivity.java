package com.example.kahotsknockoff;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.kahotsknockoff.GSON.Question;
import com.example.kahotsknockoff.adapter.questionAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuestionListActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Question> questions = new ArrayList<>();

    // Flag for checking if it is paused
    private boolean isPaused = false;
    private boolean isSettingActivity = false;

    //set the user properties
    private final FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();;
    private GoogleApiClient mGoogleClient;
    private FirebaseUser mFirebaseUser;

    // http client
    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    // this is deprecated but the UI is nice so I am using it
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);
        if(!isNetworkConnected()){
            android.app.AlertDialog ad = new android.app.AlertDialog.Builder(this).create();
            ad.setTitle("No Internet connection");
            ad.setMessage("Please connect to the internet?");

            ad.setButton(DialogInterface.BUTTON_POSITIVE,"Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    isSettingActivity = true;
                    dialog.cancel();
                }
            });
            ad.show();
        }
        //init all the user settings
        mGoogleClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        //Initialization of Firebase auth variables
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (FirebaseAuth.getInstance() == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        //set up the action bar
        getSupportActionBar().setTitle(getSupportActionBar().getTitle()+" "+mFirebaseUser.getEmail());

        //set up the recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.question_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new questionAdapter(questions,QuestionListActivity.this,this);
        mRecyclerView.setAdapter(mAdapter);

        new GetRequest().execute();
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isPaused && isSettingActivity){
            isSettingActivity = false;
            isPaused = false;
            if (!isNetworkConnected()) {
                android.app.AlertDialog ad = new android.app.AlertDialog.Builder(this).create();
                ad.setTitle("No Internet connection");
                ad.setMessage("Please connect to the internet?");

                ad.setButton(DialogInterface.BUTTON_POSITIVE, "Open Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        isSettingActivity = true;
                        dialog.cancel();
                    }
                });
                ad.show();
            }

            //Initialization of Firebase auth variables
            mFirebaseUser = mFirebaseAuth.getCurrentUser();
            if (FirebaseAuth.getInstance() == null) {
                // Not signed in, launch the Sign In activity
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
            //set up the recycler view
            new GetRequest().execute();
        }
    }

    //init and show progressDialog
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }
    //this is to close it
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
            getMenuInflater().inflate(R.menu.actionmenu,menu);
            return true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // woops
    }
    private void openLogoutDialog(){
        AlertDialog ad = new AlertDialog.Builder(QuestionListActivity.this).create();
        ad.setTitle("Warning");
        ad.setMessage("Do you want to log out");
        ad.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //this button does nothing
            }
        });
        ad.setButton(DialogInterface.BUTTON_POSITIVE, "Log out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgressDialog();
                //this is to sign out the user from the FirebaseAuth which is stored in the phone
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if(status.isSuccess()){
                            hideProgressDialog();
                            Intent intent = new Intent(QuestionListActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            hideProgressDialog();
                            Snackbar.make(findViewById(android.R.id.content), "Unable to sign out", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
        ad.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case(R.id.action_logout):
                openLogoutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //this is to do a asynchronous calling for checking if user is in our database
    private class GetRequest extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... params) {
            Request request  = new Request.Builder()
                    .url("https://clicker-190408160713.azurewebsites.net/clicker/questions")
                    .build();
            try{
                Response response = client.newCall(request).execute();
                return response.body().string();
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s != null){
                questions.addAll(Arrays.asList(new Gson().fromJson(s,Question[].class)));
                mAdapter.notifyDataSetChanged();
            }else{
                Snackbar.make(findViewById(android.R.id.content), "An Error has occurred with the server", Snackbar.LENGTH_LONG).show();
            }

        }
    }

}
