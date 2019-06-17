package com.example.safety_365;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Account_frag extends Fragment {
    String url="https://safesteps.in/android2/getaccdetails.php";
    ImageView accountpic;
    String name1,mobile1,addr,lat,lng,pic;
    TextView accountname,accountcontact,accountaddress,accountlat,accountlng;
    String name,mobile,email;
    Snackbar snackbar;
    ProgressDialog progressDialog;
    int z=0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.account,container,false);

        accountpic=view.findViewById(R.id.accountpic);
        accountname=view.findViewById(R.id.accountname);
        accountcontact=view.findViewById(R.id.accountcontact);
        accountaddress=view.findViewById(R.id.accountaddress);
        accountlat=view.findViewById(R.id.accountlat);
        accountlng=view.findViewById(R.id.accountlng);
        snackbar = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), "No Internet Connection", Snackbar.LENGTH_INDEFINITE);

        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("Login", Context.MODE_PRIVATE);
        name = sharedPreferences.getString("Name", null);
        mobile = sharedPreferences.getString("Mobile", null);
        email = sharedPreferences.getString("Email", null);

        SharedPreferences sharedPreferences1=getContext().getSharedPreferences("Account",Context.MODE_PRIVATE);
        name1=sharedPreferences1.getString("Name",null);
        mobile1=sharedPreferences1.getString("Mobile",null);
        addr=sharedPreferences1.getString("Addr",null);
        lat=sharedPreferences1.getString("lat",null);
        lng=sharedPreferences1.getString("lng",null);
        pic=sharedPreferences1.getString("pic",null);


        if(name1==null&&mobile1==null&&addr==null&&lat==null&&lng==null&&pic==null) {

            try {
                JSONObject object = new JSONObject();
                object.put("mobile", mobile);
                GetAccount_Detail gad = new GetAccount_Detail();
                gad.execute(object.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            accountname.setText(name1);
            accountaddress.setText(addr);
            accountcontact.setText(mobile1);
            accountlng.setText(lng);
            accountlat.setText(lat);
            byte[] decodedString = Base64.decode(pic, Base64.DEFAULT);
            Bitmap pic2 = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            accountpic.setImageBitmap(pic2);
        }
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }



    @SuppressLint("StaticFieldLeak")
    private class GetAccount_Detail extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Please Wait");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject jsonObject = JsonFunction.GettingData(url, params[0]);
            if (jsonObject == null)
                return "NULL";
            else
                return jsonObject.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equalsIgnoreCase("NULL"))
            {
                try {
                    JSONObject object=new JSONObject();
                    object.put("mobile",mobile);
                    GetAccount_Detail getAccount_detail=new GetAccount_Detail();
                    getAccount_detail.execute(object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(z<1){
                    snackbar.show();
                    z=1;
                }
            }
            else {
                try {
                    snackbar.dismiss();
                    z=0;
                    progressDialog.dismiss();
                    JSONObject jsonObject1 = new JSONObject(s);
                    if (jsonObject1.getString("success").equalsIgnoreCase("Success")){
                        name1=jsonObject1.getString("name");
                        mobile1=jsonObject1.getString("mobile");
                        addr=jsonObject1.getString("addr");
                        lat=jsonObject1.getString("lat");
                        lng=jsonObject1.getString("lng");
                        pic=jsonObject1.getString("pic");

                        SharedPreferences sharedPreferences2= Objects.requireNonNull(getContext()).getSharedPreferences("Account",Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor= sharedPreferences2.edit();
                        editor.putString("Name",name1);
                        editor.putString("Mobile",mobile1);
                        editor.putString("Addr",addr);
                        editor.putString("lat",lat);
                        editor.putString("lng",lng);
                        editor.putString("pic",pic);
                        editor.apply();

                        accountname.setText(name1);
                        accountcontact.setText(mobile1);
                        accountaddress.setText(addr);
                        accountlat.setText(lat);
                        accountlng.setText(lng);

                        byte[] decodedString = Base64.decode(pic, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        accountpic.setImageBitmap(decodedByte);
                    }else {
                        Snackbar.make(Objects.requireNonNull(getView()),"Error",Snackbar.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
