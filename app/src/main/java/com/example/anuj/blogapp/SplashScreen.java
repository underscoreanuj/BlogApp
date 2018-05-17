package com.example.anuj.blogapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.SignInButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SplashScreen extends AppCompatActivity {


    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_splash);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(SplashScreen.this, SignupActivity.class));
        }
        else {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(SplashScreen.this, SignupActivity.class));
        }
        else {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
        }

    }
}
