package com.kiatech.kia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class HomePageActivity extends AppCompatActivity {

    CardView cardView1, cardView2, cardView3, cardView4, cardView5, cardView6;
    ImageView Diary;
    TextView Greeting;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        getWindow().setStatusBarColor(Color.parseColor("#ffffff"));

        cardView1 = (CardView)findViewById(R.id.cvhpacard1);
        cardView2 = (CardView)findViewById(R.id.cvhpacard2);
        cardView3 = (CardView)findViewById(R.id.cvhpacard3);
        cardView4 = (CardView)findViewById(R.id.cvhpacard4);
        cardView5 = (CardView)findViewById(R.id.cvhpacard5);
        cardView6 = (CardView)findViewById(R.id.cvhpacard6);
        Diary = (ImageView)findViewById(R.id.ivhpadiary);
        Greeting = (TextView)findViewById(R.id.tvgreeting);

        sharedPreferences = getSharedPreferences("KiaSharedPreferences", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Greeting.setText("Hi, " + sharedPreferences.getString("UserName", "User") + "!");

        Diary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RunModelActivity.class);
                startActivity(intent);
            }
        });

        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
    }

    void openDialog(){
        Dialog dialog = new Dialog(HomePageActivity.this, R.style.myDialog);
        dialog.setContentView(R.layout.action_chooser_layout);

        CardView CalmDown = (CardView)dialog.findViewById(R.id.cvcalmdown);
        CardView TalkCounsellor = (CardView)dialog.findViewById(R.id.cvtalkcounsellor);
        TextView DontWorry = (TextView)dialog.findViewById(R.id.tvdontworry);

        DontWorry.setText("Hey " + sharedPreferences.getString("UserName", "User") +", don't worry! I got you!\n\nI would suggest you to first calm down and then talk to Kia for solving your problem.\n\n\nWhat would you like to do?");

        CalmDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MusicPlayerActivity.class);
                startActivity(intent);
            }
        });

        TalkCounsellor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CounsellorBotActivity.class);
                startActivity(intent);
            }
        });
        
        dialog.show();
    }
}