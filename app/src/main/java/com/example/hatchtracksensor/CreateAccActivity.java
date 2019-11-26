package com.example.hatchtracksensor;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private TextView mTextViewEmail;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_acc);

        mEditTextEmail = findViewById(R.id.textViewEmail);

        mEditTextEmail = findViewById(R.id.editTextEmail);
        mEditTextPassword = findViewById(R.id.editTextPassword);
        mEditTextFullName = findViewById(R.id.editmConfirmCode);

        mProgressBar = findViewById(R.id.progressBarLogin);
        mProgressBar.setVisibility(View.GONE);

        mButtonCreateAccount = findViewById(R.id.buttonConfirmAccount);
        mButtonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                CreateAccActivity.createUser job = new CreateAccActivity.createUser();
                job.execute();
            }
        });


    }


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
/*
    private class createUser extends AsyncTask<JSONObject, Void, JSONObject[]> {

        @Override
        protected JSONObject[] doInBackground(JSONObject[] JSON) {

            String mEmail = mEditTextEmail.getText().toString();
            String mPassword = mEditTextPassword.getText().toString();
            String mFullName = mEditTextFullName.getText().toString();
           // mEmail,mPassword,mFullName
            //Log.i("doinbg",push_notification_settings.toString());
            JSON = RestApi.registerUser(mEmail,mPassword,mFullName,mFullName);

            try{

            } catch (Exception e) {
                e.printStackTrace();
            }

            return JSON;
        }

        @Override
        protected void onPostExecute(JSONObject[] JSON) {
            try {


                   // Intent intent = new Intent(LogInActivity.this, CreateAccActivity.class);
                    //startActivity(intent);


                stopRepeatingTask();

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    */

    private class createUser extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        private JSONObject resp;

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                Log.i("createUser","START");
                mEmail = mEditTextEmail.getText().toString();
                String mPassword = mEditTextPassword.getText().toString();
                String mFullName = mEditTextFullName.getText().toString();

                resp = RestApi.registerUser(mEmail,mPassword,mFullName,mFullName);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resp;
        }


        @Override
        protected void onPostExecute(JSONObject result) {
            mProgressBar.setVisibility(View.GONE);
            if(result != null) {
                String exc = result.toString();
                Log.i("onPostExecute", result.toString());
                try {
                    //mEditTextEmail.setText(result.getString("message"));
                    if (exc.contains("userPoolId")) {
                        //Unconfirmed user account, go to enter confirm code:
                        Intent intent = new Intent(CreateAccActivity.this, ConfirmAccActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("mEmail", mEmail);
                        startActivity(intent);
                    } else if (exc.contains("UsernameExistsException")) {
                        mEditTextEmail.setText("An account with the given email already exists.");
                    } else {
                        mEditTextEmail.setText("Failed to create account.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Log.i("onPostExecute FAIL", " - ");
            }
        }
            // execution of result of Long time consuming operation
    }

}
