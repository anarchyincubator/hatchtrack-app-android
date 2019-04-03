package com.example.hatchtracksensor;

import android.content.SharedPreferences;
import android.util.Log;

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

public class AccountManager {

    private static CognitoUserPool mUserPool = null;
    private AccountManagerCallback mAccountManagerCallback;

    // Private data storage for our app used to cache the user's Email and Password.
    private static SharedPreferences mSharedPreferences;

    // These values are obtained from AWS Cognito User Pool details on the Web Console.
    private static final String mSignUpURL = "https://hatchtrack.auth.us-west-2.amazoncognito.com/signup?response_type=token&client_id=34uo31crc6dbm4i11sgaqv03lb&redirect_uri=hatchtrack.sensor://main";
    private static String mUserPoolId = "us-west-2_wOcu7aBMM";
    private static String mClientId = "34uo31crc6dbm4i11sgaqv03lb";
    private static String mClientSecret = "1vvjgracnpsshkl8rpe2fgq5df7eeapvdq9jd486bkd8vdjud5en";

    private static String mEmail = "";
    private static String mPassword = "";

    public AccountManager(android.content.Context context) {
        if (null == mUserPool) {
            // Only initialize once since most all data in this class is static.
            mUserPool = new CognitoUserPool(
                    context,
                    mUserPoolId,
                    mClientId,
                    mClientSecret,
                    Regions.US_WEST_2);

            mSharedPreferences = context.getSharedPreferences(
                    "UserData",
                    context.MODE_PRIVATE);

            // Grab the cached values.
            mEmail = mSharedPreferences.getString("email","");
            mPassword = mSharedPreferences.getString("password","");
        }
    }

    public void setEmailPassword(String email, String password) {
        mEmail = email;
        mPassword = password;

        // Cache the values.
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("email", mEmail);
        editor.putString("password", mPassword);
        editor.apply();
    }

    public String getEmail() {
        return mEmail;
    }

    public String getPassword() {
        return mPassword;
    }

    public void startAuth(AccountManagerCallback accountManagerCallback) {
        /*
         * Begin authorization process with AWS Cognito. On either success or failure, use the
         * functions provided in AccountManagerCallback to inform the callee.
         */
        mAccountManagerCallback = accountManagerCallback;

        CognitoUser user = mUserPool.getUser(mEmail);
        user.getSessionInBackground(authenticationHandler);
    }

    public final String getAccountCreateURL() {
        return mSignUpURL;
    }

    private AuthenticationHandler authenticationHandler = new AuthenticationHandler()
    {
        // Functions called by AWS Cognito during the account log in process.

        @Override
        public void onSuccess(final CognitoUserSession userSession, final CognitoDevice newDevice)
        {
            // Login success, do startActivity() or other thing
            Log.i("auth","Login success");
            mAccountManagerCallback.onSuccess(mEmail);
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
            mAccountManagerCallback.onFailure(mPassword);
        }
    };
}
