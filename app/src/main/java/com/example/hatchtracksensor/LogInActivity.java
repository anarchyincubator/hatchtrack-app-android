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

    static final String mSignUpURL = "https://hatchtrack.auth.us-west-2.amazoncognito.com/signup?response_type=token&client_id=34uo31crc6dbm4i11sgaqv03lb&redirect_uri=hatchtrack.sensor://main";

    CognitoUserPool mUserPool;

    String mUserPoolId = "us-west-2_wOcu7aBMM";
    String mClientId = "34uo31crc6dbm4i11sgaqv03lb";
    String mClientSecret = "1vvjgracnpsshkl8rpe2fgq5df7eeapvdq9jd486bkd8vdjud5en";

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
        Uri data = intent.getData();
        if (null != data) {
            mTextViewStatus.setVisibility(View.VISIBLE);
            mTextViewStatus.setText("Account registered!");
        }
        else {
            mTextViewStatus.setVisibility(View.INVISIBLE);
        }

        mUserPool = new CognitoUserPool(
                getApplicationContext(),
                mUserPoolId,
                mClientId,
                mClientSecret,
                Regions.US_WEST_2);
    }

    public void onClickButtonSignIn(View v)
    {
        mEmail = mEditTextEmail.getText().toString();
        mPassword = getmEditTextPassword.getText().toString();
        mButtonSignIn.setEnabled(false);
        mButtonCreateAccount.setEnabled(false);
        CognitoUser user = mUserPool.getUser(mEmail);
        user.getSessionInBackground(authenticationHandler);
    }

    public void onClickButtonCreateAccount(View v)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mSignUpURL));
        startActivity(intent);
    }

    private AuthenticationHandler authenticationHandler = new AuthenticationHandler()
    {
        @Override
        public void onSuccess(final CognitoUserSession userSession, final CognitoDevice newDevice)
        {
            // Login success, do startActivity() or other thing
            Log.i("auth","Login success");
            mButtonSignIn.setText("Signed In!");
            Intent intent = new Intent(LogInActivity.this, MainActivity.class);
            //intent.putExtra(EXTRA_ACCOUNT_EMAIL, account.getEmail());
            //intent.putExtra(EXTRA_ACCOUNT_ID, account.getId());
            startActivity(intent);
        }

        @Override
        public void getAuthenticationDetails(final AuthenticationContinuation continuation,
                                             final String userId)
        {
            if (userId != null)
            {
                mEmail = userId;
            }

            Log.i("auth","Login details");
            final AuthenticationDetails authDetails = new AuthenticationDetails(
                    mEmail,
                    mPassword,
                    null);
            continuation.setAuthenticationDetails(authDetails);
            continuation.continueTask();
        }

        @Override
        public void getMFACode(final MultiFactorAuthenticationContinuation continuation)
        {
            Log.i("auth","Login MFA");
        }

        @Override
        public void authenticationChallenge(final ChallengeContinuation continuation)
        {
            Log.i("auth","Login challenge");
        }

        @Override
        public void onFailure(final Exception exception)
        {
            Log.i("auth","Login failure");
        }
    };
}
