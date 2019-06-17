package com.example.safety_365;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.util.Objects;

public class Splash_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_);

        SharedPreferences preferences= getSharedPreferences("Login", MODE_PRIVATE);
        final String p_name=preferences.getString("Name",null);
        final String p_phone=preferences.getString("Mobile",null);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run() {
                if(p_phone==null && p_name==null) {
                    Intent intent=new Intent(Splash_Activity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Intent intent=new Intent(Splash_Activity.this,MainActivity2.class);
                    startActivity(intent);
                    finish();
                }
            }
        },2900);
    }
}