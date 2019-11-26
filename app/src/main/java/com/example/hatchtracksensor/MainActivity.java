package com.example.hatchtracksensor;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
//import com.google.firebase.iid.InstanceIdResult;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String TAG = "FCM";

        /*
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        Log.i(TAG, token);
                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
                */

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, token);

        MainActivity.AsyncUpdateTokenAndPlatform job = new MainActivity.AsyncUpdateTokenAndPlatform();
        job.execute(token);
        /*
        if (pinpointManager == null) {
            PinpointConfiguration pinpointConfig = new PinpointConfiguration(
                    getApplicationContext(),
                    AWSMobileClient.getInstance().getCredentialsProvider(),
                    AWSMobileClient.getInstance().getConfiguration());

            pinpointManager = new PinpointManager(pinpointConfig);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String deviceToken =
                                InstanceID.getInstance(MainActivity.this).getToken(
                                        "123456789Your_GCM_Sender_Id",
                                        GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                        Log.e("NotError", deviceToken);
                        pinpointManager.getNotificationClient()
                                .registerGCMDeviceToken(deviceToken);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        */

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Fragment fragment = new PeepDatabaseSyncFragment();
        Bundle args = new Bundle();
        args.putInt("command", ((PeepDatabaseSyncFragment) fragment).DATABASE_TO_APP_SYNC);
        fragment.setArguments(args);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_view, fragment);
        ft.commit();
    }


    private class AsyncUpdateTokenAndPlatform extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... token) {
           // String email = peepUnits[0].getUserEmail();
           // String password = peepUnits[0].getUserPassword();

            SharedPreferences preferences = getApplicationContext().getSharedPreferences("UserData", getApplicationContext().getApplicationContext().MODE_PRIVATE);
            String email = preferences.getString("email", null);
            String password = preferences.getString("password", null);

            String accessToken = RestApi.postUserAuth(email, password);
            String platform = "and";

            if (null != accessToken) {
                RestApi.postNotificationToken(accessToken,token[0],platform,email);
            }

            return token;
        }

        @Override
        protected void onPostExecute(String[] token) {


        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.navHatches) {
            setTitle("Hatches");
            //Fragment fragment = new SensorFragment();
            //FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment fragment = new HatchSelectFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_view, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
        else if (id == R.id.navPeepSelect) {
            setTitle("Peeps");
            Fragment fragment = new PeepSelectFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_view, fragment);
            ft.addToBackStack(null);
            ft.commit();
        } else if (id == R.id.navSettings) {
            Fragment fragment = new SettingsFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_view, fragment);
            ft.addToBackStack(null);
            ft.commit();
        } else if (id == R.id.navLogOut) {
            Intent intent = new Intent(MainActivity.this, LogInActivity.class);
            startActivity(intent);
        } else if (id == R.id.navIncubationGuide) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://chickens.wangahrah.com/incubation-101"));
            startActivity(intent);
        } else if (id == R.id.navCommunityForum) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://community.hatchtrack.com"));
            startActivity(intent);
        } else if (id == R.id.navBuyEggs) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://classifieds.hatchtrack.com"));
            startActivity(intent);
        }
        else if (id == R.id.navHatchtrackStore) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://shop.hatchtrack.com"));
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}
