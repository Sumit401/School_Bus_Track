package com.example.safety_365;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, View.OnClickListener {
    MapView mMapView;
    GoogleMap mMap;
    double oldlat=0,oldlng=0;
    LocationManager locationManager;
    LatLng latLng,myloc;
    Location oldloc;
    String url = "https://safesteps.in/android2/track.php";
    String url2= "https://safesteps.in/android2/track2.php";
    //String url2 = "https://safesteps.in/android2/getallloc.php";
    String name, email, mobile;
    FloatingActionButton stopnavbtn;
    double latitude, longitude;
    Polyline currentPolyline;
    LatLng dest = new LatLng(30.655067, 76.821028);
    LatLng latLng1 = new LatLng(0,0);
    ArrayList<LatLng> studentlatlng=new ArrayList<>();
    Integer waypointorder;
    ArrayList<String> student_name=new ArrayList<>();
    ArrayList<String> pics=new ArrayList<>();
    int dist, z=0,r=0;
    Marker[] marker=new Marker[100];
    Snackbar snackbar;
    double k1,k2,brng;
    String studentname;
    //ImageView imageView,imageView2;
    //ArrayList<ImageView> imageView1=new ArrayList<>();

    @SuppressLint("MissingPermission")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //button and mapview findviewbyid
        stopnavbtn = findViewById(R.id.stopnavigbtn);
        mMapView = findViewById(R.id.mapView);
        //creating map view and calling onMapReady method
        mMapView.onCreate(savedInstanceState);
        //imageView= new ImageView(this);
        //imageView2=new ImageView(this);
        studentlatlng =    (ArrayList<LatLng>) getIntent().getSerializableExtra("latlangposition");
        student_name = (ArrayList<String>) getIntent().getSerializableExtra("position_name");
        pics =         (ArrayList<String>) getIntent().getSerializableExtra("pic");
        snackbar=Snackbar.make(getWindow().getDecorView().getRootView(),"No Internet Connection",Snackbar.LENGTH_INDEFINITE);
        mMapView.getMapAsync(this);
        //Getting SharedPreferences i.e. Session
        SharedPreferences sharedPreferences = this.getSharedPreferences("Login", Context.MODE_PRIVATE);
        name = sharedPreferences.getString("Name", null);
        email = sharedPreferences.getString("Email", null);
        mobile = sharedPreferences.getString("Mobile", null);

        //Stop Navigation Button Listener
        stopnavbtn.setOnClickListener(this);

        //Location manager for Location Updates
        try {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

// onclick listener of stop navigation button
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MapsActivity.this, MainActivity2.class);
        locationManager.removeUpdates(this);
        finish();
        startActivity(intent);
    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mMapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    //On Mapready method called when the activity is created

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Getting last location from function defined below
        Location myLocation= getLastKnownLocation();
        latitude  = myLocation.getLatitude();
        longitude = myLocation.getLongitude();

        //setting camera to last location
        latLng=new LatLng(myLocation.getLatitude(),myLocation.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        mMap.animateCamera(cameraUpdate);

        //for blue dot i.e. current location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        for (int i=0;i<studentlatlng.size();i++)
        {
            marker[i]=mMap.addMarker(new MarkerOptions().title(student_name.get(i)).position(studentlatlng.get(i)));
            //Toast.makeText(this,""+arrayList.get(i),Toast.LENGTH_LONG).show();
            //Picasso.get().load(pics.get(i)).into(imageView);
            //imageView1.add(imageView);
        }
        //Draw Polyline
        SetPolyline setPolyline = new SetPolyline();
        setPolyline.execute();
    }

    //On Location Changed Listener called when location Changes

    @Override
    public void onLocationChanged(Location location) {

        //getting current location
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        latLng = new LatLng(latitude, longitude);

        //moving camera to current location
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Draw Polyline
        SetPolyline setPolyline = new SetPolyline();
        setPolyline.execute();

        //sending current location to server/database
    }


    //generating url to get polyline points
    private String getUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        //Middle points
        StringBuilder mid= new StringBuilder();
        for (int i=0;i<studentlatlng.size();i++){
            LatLng pathpoints=studentlatlng.get(i);
            double latit=pathpoints.latitude;
            double longit=pathpoints.longitude;
            mid.append("|").append(latit).append(",").append(longit);
        }

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&waypoints=optimize:true";

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + mid + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    //generating url to get polyline points
    private String getUrl2(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest ;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url2 = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url2;
    }

    //func to get lastknown location
    @SuppressLint("MissingPermission")
    private Location getLastKnownLocation() {
        while (true) {
            oldloc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (oldloc != null) {
                break;
            }
            else {
                continue;
            }
        }
        return oldloc;
    }

    //inner class sending live location to server
    @SuppressLint("StaticFieldLeak")
    private class SendLoc extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            JSONObject object = JsonFunction.GettingData(url, params[0]);
            if (object== null){
               return "NULL";
            }
            else {
                return Objects.requireNonNull(object).toString();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("NULL")){
                if(r<1){
                    snackbar.show();
                    r=1;
                }
                JSONObject jsonob1 = new JSONObject();
                try {
                    jsonob1.put("mobile", mobile);
                    jsonob1.put("lati",  k1);
                    jsonob1.put("longi", k2);
                    jsonob1.put("oldlat",oldlat);
                    jsonob1.put("oldlng",oldlng);
                    jsonob1.put("brng",brng);
                    SendLoc sendloc = new SendLoc();
                    sendloc.execute(jsonob1.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                snackbar.dismiss();
                r=0;
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
        }
    }

    //class to get polyline encode and decode it to set polyline
    @SuppressLint("StaticFieldLeak")
    private class SetPolyline extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... strings) {
            JSONObject jsonObject ;
            jsonObject = JsonFunction.GettingData(getUrl(latLng, dest),"");
            if (jsonObject== null){
                return "NULL";
            }
            else
            return Objects.requireNonNull(jsonObject).toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("NULL")){
                if(r<1){
                    snackbar.show();
                    r=1;
                }
                SetPolyline setPolyline = new SetPolyline();
                setPolyline.execute();
            }
            PolylineOptions options=new PolylineOptions().width(4).color(Color.BLUE).geodesic(true);
            try {
                JSONObject jsonObject=new JSONObject(s);
                JSONArray jsonArray=jsonObject.getJSONArray("routes");

                for (int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject1=jsonArray.getJSONObject(i);
                    JSONArray s1=jsonObject1.getJSONArray("legs");

                    for (int j=0;j<s1.length();j++){
                        JSONObject jsonObject2=s1.getJSONObject(j);
                        JSONArray jsonArray1=jsonObject2.getJSONArray("steps");

                        for (int k=0;k<jsonArray1.length();k++){
                            JSONObject jsonObject3=jsonArray1.getJSONObject(k);
                            JSONObject jsonObject4=jsonObject3.getJSONObject("polyline");
                            String s2=jsonObject4.getString("points");
                            //decoding points in polyline
                            List<LatLng> decode= PolyUtil.decode(s2);

                            for (int z=0; z<decode.size(); z++){
                                LatLng point = decode.get(z);
                                options.add(point);
                            }
                        }
                    }
                }
                //removing old polyline if exists.
                if (currentPolyline != null)
                    currentPolyline.remove();
                //setting new polyline
                currentPolyline = mMap.addPolyline(options);
                snackbar.dismiss();
                r=0;

/*
                int height = 100;
                int width = 40;
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.mipmap.bustrack);
                Bitmap bi=bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(bi, width, height, false);
*/

                JSONObject obj1=new JSONObject(s);
                JSONArray arr1=obj1.getJSONArray("routes");
                for (int i=0;i<arr1.length();i++){
                    JSONObject obj2=arr1.getJSONObject(i);
                    JSONArray arr2=obj2.getJSONArray("legs");
                    JSONObject obj3=arr2.getJSONObject(0);
                    JSONObject obj4=obj3.getJSONObject("start_location");
                    k1=Double.parseDouble(obj4.getString("lat"));
                    k2=Double.parseDouble(obj4.getString("lng"));

                    double dLon = (k2-oldlng);
                    double y = Math.sin(dLon) * Math.cos(oldlat);
                    double x = Math.cos(k1)*Math.sin(oldlat) - Math.sin(k1)*Math.cos(oldlat)*Math.cos(dLon);
                    brng = Math.toDegrees((Math.atan2(y, x)));
                    brng = (180 - ((brng + 360) % 360));

                    myloc = new LatLng(k1,k2);

                    /*if (liveloc != null){
                        liveloc.remove();
                    }
                    liveloc=mMap.addMarker(new MarkerOptions().position(myloc).icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).rotation((float) brng).flat(true));
*/
                    JSONObject jsonob1 = new JSONObject();
                    try {
                        jsonob1.put("mobile", mobile);
                        jsonob1.put("lati",  k1);
                        jsonob1.put("longi", k2);
                        jsonob1.put("oldlat",oldlat);
                        jsonob1.put("oldlng",oldlng);
                        jsonob1.put("brng",brng);
                        SendLoc sendloc = new SendLoc();
                        sendloc.execute(jsonob1.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    oldlat=k1;
                    oldlng=k2;
                }

                JSONObject object=new JSONObject(s);
                JSONArray array=object.getJSONArray("routes");
                for (int i=0;i<array.length();i++){
                    JSONObject object1=array.getJSONObject(i);
                    JSONArray array1=object1.getJSONArray("waypoint_order");
                    waypointorder=(Integer)array1.get(0);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Checkloc checkloc=new Checkloc();
            checkloc.execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class Checkloc extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... strings) {
            JSONObject jsonObject ;
            latLng1=studentlatlng.get(waypointorder);
            studentname=student_name.get(waypointorder);
            jsonObject=JsonFunction.GettingData(getUrl2(latLng,latLng1),"");
            if (jsonObject == null) {
                return "NULL";
            }
            else
                return Objects.requireNonNull(jsonObject).toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equalsIgnoreCase("NULL")) {
                if (r<1) {
                    snackbar.show();
                    r=1;
                }
                Checkloc checkloc=new Checkloc();
                checkloc.execute();
            }

           JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(s);
                final JSONArray jsonArray=jsonObject.getJSONArray("routes");
                for (int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject1=jsonArray.getJSONObject(i);
                    JSONArray jsonArray1=jsonObject1.getJSONArray("legs");
                    for (int j=0;j<jsonArray1.length(); j++){
                        JSONObject jsonObject2=jsonArray1.getJSONObject(j);
                        JSONObject jsonObject3=jsonObject2.getJSONObject("distance");
                        String k = jsonObject3.getString("value");
                        dist = Integer.parseInt(k);
                        Toast.makeText(MapsActivity.this,""+dist,Toast.LENGTH_SHORT).show();
                    }
                }
                if(dist<100 && z<1 && studentlatlng.contains(latLng1)){
                    z=2;
                    AlertDialog.Builder builder=new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle(studentname+"'/s stoppage has Arrived");
                    builder.setMessage("Did he/she Board ?");
                    builder.setCancelable(false);
                    //builder.setIcon(imageView2.getDrawable());
                    builder.setPositiveButton("Present", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            studentlatlng.remove(latLng1);
                            student_name.remove(studentname);

                            JSONObject jsonObject1=new JSONObject();
                            try {
                                jsonObject1.put("lat",latLng1.latitude);
                                jsonObject1.put("lng",latLng1.longitude);
                                jsonObject1.put("studentname",studentname);
                                jsonObject1.put("attendance",1);
                                Student_board student_board=new Student_board();
                                student_board.execute(jsonObject1.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            z=0;
                        }
                    });
                    builder.setNegativeButton("Absent", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            studentlatlng.remove(latLng1);
                            student_name.remove(studentname);

                            JSONObject jsonObject1=new JSONObject();
                            try {
                                jsonObject1.put("lat",latLng1.latitude);
                                jsonObject1.put("lng",latLng1.longitude);
                                jsonObject1.put("studentname",studentname);
                                jsonObject1.put("attendance",0);
                                Student_board student_board=new Student_board();
                                student_board.execute(jsonObject1.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            /*for (int i=0;i<studentlatlng.size();i++){
                                if (studentlatlng.get(i)==latLng1) {
                                    student_name.remove(i);
                                    studentlatlng.remove(i);
                                }
                            }*/
                            z=0;
                        }
                    });
                    builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            z=0;
                        }
                    });
                    builder.show();
                    snackbar.dismiss();
                    r=0;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @SuppressLint("StaticFieldLeak")
    private class Student_board extends AsyncTask<String,String,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject object=JsonFunction.GettingData(url2,params[0]);
            if (object==null)
                return "NULL";
            else
                return object.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equalsIgnoreCase("NULL")){
                //code
            }else {

            }
        }
    }
}