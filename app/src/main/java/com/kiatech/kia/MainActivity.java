package com.kiatech.kia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.webkit.WebViewClient;

import com.google.firebase.FirebaseApp;

// Main splash screen

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Stuff, making nav bar and status bar color same as background

        getWindow().setNavigationBarColor(getResources().getColor(R.color.white));
        getWindow().setStatusBarColor(getResources().getColor(R.color.white));

        sharedPreferences = getSharedPreferences("KiaSharedPreferences", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // 1 second countdown timer to go to chatbot

        CountDownTimer countDownTimer = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) { }

            @Override
            public void onFinish() {
                if(sharedPreferences.contains("FirstTime")){
                    Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), UserRegistrationActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }.start();
    }
}