package com.kiatech.kia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class HomePageActivity extends AppCompatActivity {

    CardView cardView1, cardView2, cardView3, cardView4, cardView5, cardView6;

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

        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RunModelActivity.class);
                startActivity(intent);
            }
        });

        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
    }

    void openDialog(){
        Dialog dialog = new Dialog(HomePageActivity.this, R.style.myDialog);
        dialog.setContentView(R.layout.problem_type_chooser_layout);

        Button Confirm = (Button)dialog.findViewById(R.id.btndlg1confirm);

        Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog2 = new Dialog(HomePageActivity.this, R.style.myDialog);
                dialog2.setContentView(R.layout.action_chooser_layout);

                CardView CalmDown = (CardView)dialog2.findViewById(R.id.cvcalmdown);
                CardView TalkCounsellor = (CardView)dialog2.findViewById(R.id.cvtalkcounsellor);

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

                dialog2.show();
            }
        });
        
        dialog.show();
    }
}