package com.example.hatchtracksensor;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.util.Log;

import org.json.JSONObject;


public class CreateAccActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private Button mButtonCreateAccount;
    private TextView mEditTextEmail;
    private TextView mEditTextPassword;
    private TextView mEditTextFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_acc);

        mEditTextEmail = findViewById(R.id.editTextEmail);
        mEditTextPassword = findViewById(R.id.editTextPassword);
        mEditTextFullName = findViewById(R.id.editTextName);

        mProgressBar = findViewById(R.id.progressBarLogin);
        mProgressBar.setVisibility(View.GONE);

        mButtonCreateAccount = findViewById(R.id.buttonCreateAccount);
        mButtonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CreateAccActivity.createUser job = new CreateAccActivity.createUser();
                job.execute("go");
            }
        });

    }
/*
    protected void createUser(){
        Log.i("TAG","createUser");
        String mEmail = mEditTextEmail.getText().toString();
        String mPassword = mEditTextPassword.getText().toString();
        String mFullName = mEditTextFullName.getText().toString();

        RestApi.registerUser(mEmail,mPassword,mFullName);
    }
    */

    private Handler mHandler = new Handler();
    private Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            //mPeepUnitManager = new PeepUnitManager();
            //PeepUnit peep = mPeepUnitManager.getPeepUnitActive();
            Log.i("TIMELINE Runnable:","mHandlerTask");

            //SettingsFragment.SensorUpdateJob sensorUpdateJob = new SettingsFragment.SensorUpdateJob();
            //sensorUpdateJob.execute(peep);

            //mHandler.postDelayed(mHandlerTask, mDbPollInterval);
        }
    };

    private void startRepeatingTask() {
        mHandlerTask.run();
    }

    private void stopRepeatingTask() {
        mHandler.removeCallbacks(mHandlerTask);
    }

    private class createUser extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... Strings) {

            String mEmail = mEditTextEmail.getText().toString();
            String mPassword = mEditTextPassword.getText().toString();
            String mFullName = mEditTextFullName.getText().toString();
           // mEmail,mPassword,mFullName
            JSONObject push_notification_settings = RestApi.registerUser(mEmail,mPassword,mFullName,mFullName);
            //Log.i("doinbg",push_notification_settings.toString());
            try{

            } catch (Exception e) {
                e.printStackTrace();
            }

            return Strings;
        }

        @Override
        protected void onPostExecute(String[] Strings) {
            try {
                //json = timelineJSON;


                stopRepeatingTask();

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
