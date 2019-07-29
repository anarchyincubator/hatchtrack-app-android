package com.example.hatchtracksensor;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ProgressBar;


public class CreateAccActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;

    private Button mButtonCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_acc);

        mProgressBar = findViewById(R.id.progressBarLogin);
        mProgressBar.setVisibility(View.GONE);

        mButtonCreateAccount = findViewById(R.id.buttonCreateAccount);
        mButtonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser();
            }
        });

    }

    protected void createUser(){

    }

}
