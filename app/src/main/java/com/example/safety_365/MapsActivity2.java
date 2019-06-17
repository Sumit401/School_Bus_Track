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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MapsActivity2 extends Fragment implements View.OnClickListener {
    LinearLayout startnavlayout;
    ImageButton startnavbtn;
    Snackbar snackbar;
    ProgressDialog progressDialog;
    int k=1,z=0;
    String url2 = "https://safesteps.in/android2/getallloc.php";
    ArrayList<LatLng> locs=new ArrayList<>();
    ArrayList<String> markers=new ArrayList<>();
    ArrayList<String> pic1=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.activity_maps2, container, false);

       startnavlayout=view.findViewById(R.id.startnavlayout);
       startnavbtn=view.findViewById(R.id.startnavbtn);
       snackbar = Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), "No Internet Connection", Snackbar.LENGTH_INDEFINITE);
       requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
       startnavlayout.setOnClickListener(this);
       startnavbtn.setOnClickListener(this);

       progressDialog = new ProgressDialog(getContext());
       progressDialog.setMessage("Please Wait");
       progressDialog.setCancelable(false);
       progressDialog.show();
           JSONObject jsonObject1= new JSONObject();
           try {
               jsonObject1.put("action","get_all_links");
               Getlocs getlocs=new Getlocs();
               getlocs.execute(jsonObject1.toString());
           } catch (JSONException e) {
               e.printStackTrace();
           }

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    k=1;
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    k=0;
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getContext(), "Permission denied to Access Location", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    //inner class to get all location/stops in between of journey
    @SuppressLint("StaticFieldLeak")
    private class Getlocs extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            JSONObject jsonObject= JsonFunction.GettingData(url2,params[0]);
            if (jsonObject== null){
                return "NULL";
            }
            else
                return Objects.requireNonNull(jsonObject).toString();
        }

        @SuppressLint("RestrictedApi")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equals("NULL")){
                if(z<1){
                    snackbar.show();
                    z=1;
                }

                JSONObject obj1= new JSONObject();
                try {
                    obj1.put("action","get_all_links");
                    Getlocs getlocs=new Getlocs();
                    getlocs.execute(obj1.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            try {
                JSONObject j1 = new JSONObject(s);
                String res = j1.getString("response");
                if (res.equalsIgnoreCase("success")) {
                    JSONArray jsonArray = j1.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject j2 = jsonArray.getJSONObject(i);
                        String latitude1 = String.valueOf(j2.getString("lati"));
                        String longitude2 = String.valueOf(j2.getString("longi"));
                        String name = j2.getString("name");
                        String pics=j2.getString("pic");
                        LatLng latLng=new LatLng(Double.parseDouble(latitude1),Double.parseDouble(longitude2));
                        locs.add(latLng);
                        markers.add(name);
                        pic1.add(pics);
                    }

                }
                // Toast.makeText(getContext(),"Done",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                if (snackbar!=null) {
                    snackbar.dismiss();
                    z = 0;
                }
            }

            catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
        }
    }

    @Override
    public void onClick(View v) {
        if(k>0) {
            LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled = false;
            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ignored) {
            }
            if (gps_enabled) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("latlangposition", locs);
                intent.putExtra("position_name", markers);
                intent.putExtra("pic", pic1);
                startActivity(intent);
                Objects.requireNonNull(getActivity()).finish();
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
        else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
