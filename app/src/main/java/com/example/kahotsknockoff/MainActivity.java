package com.example.kahotsknockoff;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kahotsknockoff.GSON.PostAnswer;
import com.example.kahotsknockoff.GSON.Question;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    private ProgressDialog mProgressDialog;
    private Question currQuestion;

    //Create instance of firebase/google variables
    private FirebaseAuth mFirebaseAuth;
    private GoogleApiClient mGoogleClient;
    private FirebaseUser mFirebaseUser;
    // this is the http rest caller
    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currQuestion = (Question)getIntent().getSerializableExtra("question");

        // get all the required ui elements
        Button btnA = (Button) findViewById(R.id.button_a);
        Button btnB = (Button) findViewById(R.id.button_b);
        Button btnC = (Button) findViewById(R.id.button_c);
        Button btnD = (Button) findViewById(R.id.button_d);
        TextView tvQuestion = (TextView) findViewById(R.id.question_text);

        // set Up the UI properly
        tvQuestion.setText(currQuestion.getId()+") "+currQuestion.getQuestionStr());
        btnA.setText(btnA.getText()+currQuestion.getA());
        btnB.setText(btnB.getText()+currQuestion.getB());
        btnC.setText(btnC.getText()+currQuestion.getC());
        btnD.setText(btnD.getText()+currQuestion.getD());

        // set up the on click events
        btnA.setOnClickListener(this);
        btnB.setOnClickListener(this);
        btnC.setOnClickListener(this);
        btnD.setOnClickListener(this);
        mGoogleClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        //        Initialization of Firebase auth variables
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (FirebaseAuth.getInstance() == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
    }

    @Override
    public void onClick(View v) {
        Log.d("test","tesing");
        switch(v.getId()){
            case R.id.button_a:
                openCommentDialog('a');
                break;
            case R.id.button_b:
                openCommentDialog('b');
                break;
            case R.id.button_c:
                openCommentDialog('c');
                break;
            case R.id.button_d:
                openCommentDialog('d');
                break;
        }
    }
    public void openCommentDialog(char ans){
        AlertDialog ad = new AlertDialog.Builder(this).create();
        ad.setTitle("Comments");
        ad.setMessage("Any comments on this question?\n(You can leave blank.)");
        final EditText input = new EditText(this);
        ad.setView(input);
        ad.setButton(DialogInterface.BUTTON_NEGATIVE,"Nothing to add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                postAnswer(ans,input.getText().toString());
                dialog.cancel();
            }
        });
        ad.setButton(DialogInterface.BUTTON_POSITIVE,"Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                postAnswer(ans,input.getText().toString());
                dialog.cancel();
            }
        });
        ad.show();
    }
    public void postAnswer(char ans, String comment){
        PostAnswer answer = new PostAnswer();
        answer.setChoiceOption(ans);
        answer.setComment(comment);
        answer.setQuestionNo(currQuestion.getId());
        new PostRequest().execute(new GsonBuilder()
                .serializeNulls()
                .create()
                .toJson(answer));
        showProgressDialog();
    }
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //woops
    }

    //this is to do asynchronous calling of the post method due the the fact it cannot happen on the main thread
    private class PostRequest extends AsyncTask<String, Void , Integer> {
        public final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        @Override
        protected Integer doInBackground(String... json) {

            try{
                RequestBody body = RequestBody.create(JSON,json[0]);
                Request request = new Request.Builder()
                        .url("http://7b783e8e.ngrok.io/clicker/select")
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                return response.code();
            }catch (Exception e){
                return 999;
            }
        }

        @Override
        protected void onPostExecute(Integer returnCode) {
            if(returnCode == 200){
                hideProgressDialog();
                Snackbar.make(findViewById(android.R.id.content), "Question Answered", Snackbar.LENGTH_LONG).show();
            }
            else {
                hideProgressDialog();
                Snackbar.make(findViewById(android.R.id.content), "Question Not Answered", Snackbar.LENGTH_LONG).show();
            }
        }
    }//end of post method asynchronous calling


}
