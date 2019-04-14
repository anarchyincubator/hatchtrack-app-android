package com.example.hatchtracksensor;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private PeepManager mPeepManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        mPeepManager = new PeepManager();

        Fragment fragment = new SensorFragment();;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_view, fragment);
        ft.commit();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.navPeepSelect) {
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
