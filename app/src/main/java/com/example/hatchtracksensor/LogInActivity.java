package com.example.hatchtracksensor;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.regions.Regions;

public class LogInActivity extends AppCompatActivity {

    private Button mButtonSignIn;
    private Button mButtonCreateAccount;
    private TextView mTextViewStatus;
    private EditText mEditTextEmail;
    private EditText getmEditTextPassword;

    AccountManager mAccountManager;

    String mEmail = "";
    String mPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mButtonSignIn = findViewById(R.id.buttonSignIn);
        mButtonCreateAccount = findViewById(R.id.buttonCreateAccount);
        mTextViewStatus = findViewById(R.id.textViewStatus);
        mEditTextEmail = findViewById(R.id.editTextEmail);
        getmEditTextPassword = findViewById(R.id.editTextPassword);

        Intent intent = getIntent();
        String action = intent.getAction();
        // TODO: Use action from Intent?
        Uri data = intent.getData();
        if (null != data) {
            mTextViewStatus.setVisibility(View.VISIBLE);
            mTextViewStatus.setText("Account registered!");
        }
        else {
            mTextViewStatus.setVisibility(View.INVISIBLE);
        }

        mAccountManager = new AccountManager(getApplicationContext());
    }

    public void onClickButtonSignIn(View v)
    {
        mEmail = mEditTextEmail.getText().toString();
        mPassword = getmEditTextPassword.getText().toString();

        mTextViewStatus.setVisibility(View.INVISIBLE);
        mButtonSignIn.setEnabled(false);
        mButtonCreateAccount.setEnabled(false);

        mAccountManager.setEmailPassword(mEmail, mPassword);
        mAccountManager.startAuth(new MyInterface() {
            @Override
            public void onSuccess(String response) {
                mButtonSignIn.setText("Signed In!");
                Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onFailure(String response) {
                mTextViewStatus.setText("Invalid Email or Password");
                mTextViewStatus.setVisibility(View.VISIBLE);

                mButtonSignIn.setEnabled(true);
                mButtonCreateAccount.setEnabled(true);
            }
        });
    }

    public void onClickButtonCreateAccount(View v)
    {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(mAccountManager.getAccountCreateURL()));

            startActivity(intent);
    }
}
