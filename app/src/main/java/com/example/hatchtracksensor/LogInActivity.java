package com.example.hatchtracksensor;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LogInActivity extends AppCompatActivity {

    private Button mButtonSignIn;
    private Button mButtonCreateAccount;
    private TextView mTextViewStatus;
    private EditText mEditTextEmail;
    private EditText mEditTextPassword;

    private Button mButtonForgotPassword; //DBOI
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
        mEditTextPassword = findViewById(R.id.editTextPassword);

        /*
         * We have an Intent with non-null data if we're returning from account registration
         * launched in the onClickButtonCreateAccount function.
         */
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

        // Grab the cached Email and Password.
        mAccountManager = new AccountManager(getApplicationContext());
        mEmail = mAccountManager.getEmail();
        mPassword = mAccountManager.getPassword();
        mEditTextEmail.setText(mEmail);
        mEditTextPassword.setText(mPassword);
    }

    public void onClickButtonSignIn(View v)
    {
        /*
         * Grab the user submitted Email and Password, pass them off to the AccountManager which
         * will validate it with AWS Cognito.
         */
        mEmail = mEditTextEmail.getText().toString();
        mPassword = mEditTextPassword.getText().toString();

        mTextViewStatus.setVisibility(View.INVISIBLE);
        mButtonSignIn.setEnabled(false);
        mButtonCreateAccount.setEnabled(false);

        mAccountManager.setEmailPassword(mEmail, mPassword);
        mAccountManager.startAuth(new AccountManagerCallback() {
            @Override
            public void onSuccess(String response) {
                mButtonSignIn.setText("Signed In!");
                Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
        /*
         * For this routine, we leverage AWS Cognito's automatically generated web page for
         * registering users. All we need to do is generate an Intent to open the URL. The AWS
         * Cognito web page is designed to return us back to the LogInActivity app view once the
         * user is done signing up.
         */
        Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(mAccountManager.getAccountCreateURL()));

        startActivity(intent);
    }

    public void onClickButtonForgotPassword(View v) //DBOI
    {

        /*
         * For this routine, we leverage AWS Cognito's automatically generated web page for
         * registering users. All we need to do is generate an Intent to open the URL. The AWS
         * Cognito web page is designed to return us back to the LogInActivity app view once the
         * user is done signing up.
         */
        Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse(mAccountManager.getForgotPasswordURL()));

        startActivity(intent);
    }
}
