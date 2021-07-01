package com.kiatech.kia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;

public class UserRegistrationActivity extends AppCompatActivity {

    private ViewPager viewPager;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    UserDetails userDetails;

    String gender = "";

    private SliderAdapter sliderAdapter;
    int currentpage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        userDetails = new UserDetails();

        viewPager = (ViewPager)findViewById(R.id.viewPager);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        sharedPreferences = getSharedPreferences("KiaSharedPreferences", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        sliderAdapter = new SliderAdapter(this);
        viewPager.setAdapter(sliderAdapter);

        viewPager.addOnPageChangeListener(viewListener);
    }



    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            currentpage = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public class SliderAdapter extends PagerAdapter {

        LayoutInflater layoutInflater;
        Context context;

        public SliderAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == (RelativeLayout) object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

            View view = null;

            if (position == 0){
                view = layoutInflater.inflate(R.layout.reg_layout_1, container, false);
                Button Next = (Button)view.findViewById(R.id.btnrl1next);
                Next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewPager.setCurrentItem(1);
                    }
                });
            }
            else if (position == 1){
                view = layoutInflater.inflate(R.layout.reg_layout_2, container, false);
                Button Next = (Button)view.findViewById(R.id.btnrl2next);
                EditText Name = (EditText) view.findViewById(R.id.etrl2name);

                Next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        userDetails.setName(Name.getText().toString().trim());
                        viewPager.setCurrentItem(2);
                    }
                });
            }
            else if (position == 2){
                view = layoutInflater.inflate(R.layout.reg_layout_3, container, false);
                Button Next = (Button)view.findViewById(R.id.btnrl3next);
                ImageView Male = (ImageView)view.findViewById(R.id.ivgendermale);
                ImageView Female = (ImageView)view.findViewById(R.id.ivgenderfemale);

                Male.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Male.setBackgroundResource(R.drawable.circle4);
                        Female.setBackgroundResource(0);
                        userDetails.setGender("Male");
                        gender = "Male";
                    }
                });

                Female.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Female.setBackgroundResource(R.drawable.circle4);
                        Male.setBackgroundResource(0);
                        userDetails.setGender("Female");
                        gender = "Female";
                    }
                });

                Next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewPager.setCurrentItem(3);
                    }
                });
            }
            else if (position == 3){
                view = layoutInflater.inflate(R.layout.reg_layout_4, container, false);
                Button Next = (Button)view.findViewById(R.id.btnrl4next);
                ImageView Group1 = (ImageView)view.findViewById(R.id.ivagegrp1);
                ImageView Group2 = (ImageView)view.findViewById(R.id.ivagegrp2);
                ImageView Group3 = (ImageView)view.findViewById(R.id.ivagegrp3);
                ImageView Group4 = (ImageView)view.findViewById(R.id.ivagegrp4);

                if (!gender.equals("Male")){
                    Group1.setImageResource(R.drawable.teengirl);
                    Group2.setImageResource(R.drawable.youngwoman);
                    Group3.setImageResource(R.drawable.middleagewoman);
                    Group4.setImageResource(R.drawable.oldwoman);
                }

                Group1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Group1.setBackgroundResource(R.drawable.circle4);
                        Group2.setBackgroundResource(0);
                        Group3.setBackgroundResource(0);
                        Group4.setBackgroundResource(0);

                        userDetails.setGroup("Teenage");
                        userDetails.setProfilePic((userDetails.getGender().equals("Male"))?(R.drawable.teenman):(R.drawable.teengirl));
                    }
                });

                Group2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Group2.setBackgroundResource(R.drawable.circle4);
                        Group1.setBackgroundResource(0);
                        Group3.setBackgroundResource(0);
                        Group4.setBackgroundResource(0);

                        userDetails.setGroup("Young");
                        userDetails.setProfilePic((userDetails.getGender().equals("Male"))?(R.drawable.youngman):(R.drawable.youngwoman));
                    }
                });

                Group3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Group3.setBackgroundResource(R.drawable.circle4);
                        Group2.setBackgroundResource(0);
                        Group1.setBackgroundResource(0);
                        Group4.setBackgroundResource(0);

                        userDetails.setGroup("Middleage");
                        userDetails.setProfilePic((userDetails.getGender().equals("Male"))?(R.drawable.middleageman):(R.drawable.middleagewoman));
                    }
                });

                Group4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Group4.setBackgroundResource(R.drawable.circle4);
                        Group2.setBackgroundResource(0);
                        Group3.setBackgroundResource(0);
                        Group1.setBackgroundResource(0);

                        userDetails.setGroup("Oldage");
                        userDetails.setProfilePic((userDetails.getGender().equals("Male"))?(R.drawable.oldman):(R.drawable.oldwoman));
                    }
                });

                Next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewPager.setCurrentItem(4);
                    }
                });
            }
            else if (position == 4){
                view = layoutInflater.inflate(R.layout.reg_layout_5, container, false);
                Button Next = (Button)view.findViewById(R.id.btnrl5next);
                ImageView Alexa = (ImageView)view.findViewById(R.id.ivalexa);
                ImageView Alex = (ImageView)view.findViewById(R.id.ivalex);

                Alexa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Alexa.setBackgroundResource(R.drawable.circle);
                        Alex.setBackgroundResource(R.drawable.circle2);
                        userDetails.setCounsellor("female");
                    }
                });

                Alex.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Alex.setBackgroundResource(R.drawable.circle);
                        Alexa.setBackgroundResource(R.drawable.circle2);
                        userDetails.setCounsellor("male");
                    }
                });

                Next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editor.putString("UserDetails", new Gson().toJson(userDetails));
                        editor.putString("UserName", userDetails.getName());
                        editor.putString("BotGender", userDetails.getCounsellor());
                        editor.putBoolean("Registered", true);
                        editor.putInt("FirstTime", 0);
                        editor.commit(); editor.apply();

                        Intent intent = new Intent(getApplicationContext(), HomePageActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((RelativeLayout) object);
        }
    }
}