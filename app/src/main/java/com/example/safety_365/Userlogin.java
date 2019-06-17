package com.example.safety_365;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Objects;
import static android.content.Context.MODE_PRIVATE;

public class Userlogin extends Fragment {
    Button login;
    EditText mobile, pass;
    String url = "https://safesteps.in/android2/api.php";
    String MobilePattern = "[6-9]{1}+[0-9]{9}";
    String passpattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s).*$";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.loginxml, container, false);
        mobile = view.findViewById(R.id.loginphone);
        pass = view.findViewById(R.id.loginpass);
        login = view.findViewById(R.id.loginbtn);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                boolean gps_enabled = false;
                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception ignored) {
                }

                final boolean Gps_enabled = gps_enabled;

                if (Gps_enabled) {
                    JSONObject jsonObject = new JSONObject();
                    if (mobile.getText().toString().equals("") || pass.getText().toString().equals("")) {
                        Snackbar.make(v, "Fields can't be Empty", Snackbar.LENGTH_LONG).show();
                    } else {
                        if ((mobile.getText().toString().matches(MobilePattern)) || pass.getText().toString().matches(passpattern)) {
                            try {
                                jsonObject.put("mobile", mobile.getText().toString().trim());
                                jsonObject.put("pass", pass.getText().toString().trim());
                                jsonObject.put("action", "login_user");
                                Login_class login = new Login_class();
                                login.execute(jsonObject.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (!mobile.getText().toString().matches(MobilePattern)) {
                                mobile.setError("Enter Valid Mobile No");
                                mobile.requestFocus();
                            } else if (!pass.getText().toString().matches(passpattern)) {
                                pass.setError("Valid Password (UpperCase, LowerCase, Number and min 8 Chars)");
                                pass.requestFocus();
                            }
                        }
                    }
                } else {
                    LocationRequest locationRequest = LocationRequest.create();
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

                    Task<LocationSettingsResponse> result =
                            LocationServices.getSettingsClient(getContext()).checkLocationSettings(builder.build());

                    result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                            try {
                                LocationSettingsResponse response = task.getResult(ApiException.class);
                                // All location settings are satisfied. The client can initialize location
                                // requests here.
                            } catch (ApiException exception) {
                                switch (exception.getStatusCode()) {
                                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                        // Location settings are not satisfied. But could be fixed by showing the
                                        // user a dialog.
                                        try {
                                            // Cast to a resolvable exception.
                                            ResolvableApiException resolvable = (ResolvableApiException) exception;
                                            // Show the dialog by calling startResolutionForResult(),
                                            // and check the result in onActivityResult().
                                            resolvable.startResolutionForResult(getActivity(), LocationRequest.PRIORITY_HIGH_ACCURACY);
                                        } catch (IntentSender.SendIntentException e) {
                                            // Ignore the error.
                                        } catch (ClassCastException e) {
                                            // Ignore, should be an impossible error.
                                        }
                                        break;
                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                        // Location settings are not satisfied. However, we have no way to fix the
                                        // settings so we won't show the dialog.
                                        break;
                                }
                            }
                        }
                    });

                }

            }
        });
        return view;
    }


    @SuppressLint("StaticFieldLeak")
        private class Login_class extends AsyncTask<String,String,String> {
            ProgressDialog progressDialog=new ProgressDialog(getContext());
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.setTitle("Please Wait");
                progressDialog.setMessage("Loading");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            @Override
            protected String doInBackground(String... params) {
                JSONObject object = JsonFunction.GettingData(url, params[0]);
                if (object==null){
                    return "NULL";
                }else
                return object.toString();
    }
            @Override
            protected void onPostExecute(String s) {
                if (s.equals("NULL")){
                    Snackbar.make(Objects.requireNonNull(getView()),"No Internet Connectivity",Snackbar.LENGTH_SHORT).show();
                }
                super.onPostExecute(s);
                progressDialog.dismiss();
                try {
                    JSONObject object= new JSONObject(s);
                    String res=object.getString("response");

                    if (res.equalsIgnoreCase("Success"))
                    {
                        String res1=object.getString("name");
                        String res3=object.getString("email");
                        String res4=object.getString("mobile");
                        String res5=object.getString("pass");
                        SharedPreferences preferences= getActivity().getSharedPreferences("Login", MODE_PRIVATE);
                        SharedPreferences.Editor editor=preferences.edit();
                        editor.putString("Name",res1);
                        editor.putString("Email",res3);
                        editor.putString("Mobile",res4);
                        editor.putString("Pass",res5);
                        editor.apply();
                        Snackbar.make(Objects.requireNonNull(getView()),"Login Successful", Snackbar.LENGTH_SHORT).show();
                        Intent intent=new Intent(getActivity(),MainActivity2.class);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    else if (res.equalsIgnoreCase("failed"))
                    {
                        Snackbar.make(Objects.requireNonNull(getView()),"Email or Password Incorrect",
                                Snackbar.LENGTH_SHORT).show();
                    }
                    else {
                        Snackbar.make(Objects.requireNonNull(getView()),"Error",
                                Snackbar.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

