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


public class ConfirmAccActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private Button mButtonCreateAccount;
    private Button mButtonRequestCode;
    private TextView mEditmConfirmCode;
    private TextView mTextViewEmail;
    String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_confirm);

        mEmail = getIntent().getStringExtra("mEmail");
        if(mEmail!=null) {
            Log.i("ConfirmAccActivity", mEmail);
        }

        mTextViewEmail = findViewById(R.id.textViewEmail);

        mEditmConfirmCode = findViewById(R.id.editmConfirmCode);

        mProgressBar = findViewById(R.id.progressBarLogin);
        mProgressBar.setVisibility(View.GONE);

        mButtonCreateAccount = findViewById(R.id.buttonConfirmAccount);
        mButtonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                ConfirmAccActivity.confirmUser job = new ConfirmAccActivity.confirmUser();
                job.execute();
            }
        });

        mButtonRequestCode = findViewById(R.id.buttonRequestCode);
        mButtonRequestCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfirmAccActivity.requestConfirmCode job = new ConfirmAccActivity.requestConfirmCode();
                job.execute();
            }
        });

    }


    private class confirmUser extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        private JSONObject resp;

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                Log.i("createUser","START");
                String mConfirm = mEditmConfirmCode.getText().toString();
                resp = RestApi.confirmUser(mEmail,mConfirm);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resp;
        }


        @Override
        protected void onPostExecute(JSONObject result) {
            mProgressBar.setVisibility(View.GONE);
            Log.i("onPostExecuteVERIFY",result.toString());
            String exc = result.toString();
            if(exc.contains("Invalid verification code provided, please try again.")){
                mTextViewEmail.setText("Invalid verification code provided, please try again.");
            }
            if(exc.contains("SUCCESS")){
                Intent intent = new Intent(ConfirmAccActivity.this, LogInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("mEmail", mEmail);
                startActivity(intent);
            }
            try {

                //Log.i("ERROR",result.getString("message"));
                //mEditTextEmail.setText(result.getString("message"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // execution of result of Long time consuming operation
    }

    private class requestConfirmCode extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        private JSONObject resp;

        @Override
        protected JSONObject doInBackground(JSONObject... params) {
            try {
                Log.i("createUser","START");
                //String mConfirm = mEditmConfirmCode.getText().toString();
                //resp = RestApi.confirmUser(mEmail,mConfirm);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resp;
        }


        @Override
        protected void onPostExecute(JSONObject result) {
            Log.i("onPostExecuteVERIFY",result.toString());
            String exc = result.toString();
            if(exc.contains("Invalid verification code provided, please try again.")){
                mTextViewEmail.setText("Invalid verification code provided, please try again.");
            }
            try {
                mProgressBar.setVisibility(View.GONE);
                //Log.i("ERROR",result.getString("message"));
                //mEditTextEmail.setText(result.getString("message"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // execution of result of Long time consuming operation
    }

}
