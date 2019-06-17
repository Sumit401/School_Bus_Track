package com.example.safety_365;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity2 extends AppCompatActivity{

    int id1=0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SharedPreferences settings = getSharedPreferences("Login", MODE_PRIVATE);
            settings.edit().clear().apply();
            final ProgressDialog progressDialog=new ProgressDialog(MainActivity2.this);
            progressDialog.setMessage("Wait");
            progressDialog.setTitle("Logging Out");
            progressDialog.setCancelable(false);
            progressDialog.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    Intent intent=new Intent(MainActivity2.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            },1000);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_home: {
                    if(id1 != 0) {
                        MapsActivity2 mp = new MapsActivity2();
                        ft.replace(R.id.frameprofile, mp);
                        ft.commit();
                        id1=0;
                        return true;
                    }
                    break;
                }
                case R.id.navigation_dashboard: {
                    if (id1 != 1) {
                        Dashboard_frag df = new Dashboard_frag();
                        ft.replace(R.id.frameprofile, df);
                        ft.commit();
                        id1=1;
                        return true;
                    }
                    break;
                }
                case R.id.navigation_notifications: {
                    if (id1 !=2) {
                        Account_frag af = new Account_frag();
                        ft.replace(R.id.frameprofile, af);
                        ft.commit();
                        id1=2;
                        return true;
                    }
                    break;
                }
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        SharedPreferences preferences= this.getSharedPreferences("Account",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.clear();
        editor.apply();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Snackbar snackbar=Snackbar.make(getWindow().getDecorView().getRootView(),"No Internet Connection",Snackbar.LENGTH_INDEFINITE);
        FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putString("snackbar", String.valueOf(snackbar));

        MapsActivity2 mp= new MapsActivity2();
        mp.setArguments(bundle);
        //for navigation fragment view
        ft.replace(R.id.frameprofile,mp);
        ft.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences preferences= this.getSharedPreferences("Account",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.clear();
        editor.apply();


    }
}
