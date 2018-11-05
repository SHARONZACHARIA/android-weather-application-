package com.example.sharonzacharia.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import android.widget.TextView;
import android.support.design.widget.CollapsingToolbarLayout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {


    String url;
    private static  final  int  tag = 123;

    String city = "";
    TextView tempText, pressText, humidityText, location,precip,feelslik,windS,condition,cloud,wind_degree,updated,ciityinfo;
    FloatingActionButton locationFAb;
    CollapsingToolbarLayout collapsingtool;
    LocationRequest locationRequest;
    GoogleApiClient googleApiClient;


    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updated = findViewById(R.id.lastUpdated);
        ciityinfo = findViewById(R.id.city_info);
        condition = findViewById(R.id.chanceRainValue);
        cloud = findViewById(R.id.cloudTextValue);
        wind_degree = findViewById(R.id.winddegreeValue);
        precip = findViewById(R.id.precetext);
        feelslik = findViewById(R.id.feelsLiketext);
        windS = findViewById(R.id.windSpedtext);
        tempText = findViewById(R.id.temp);
        pressText = findViewById(R.id.pressure);
        humidityText = findViewById(R.id.humidty);
        locationFAb = findViewById(R.id.fabLocation);
        collapsingtool = findViewById(R.id.collap);

        if(! isLocatonAllowed()) {

            requestLocationPermission();
        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        requestQueue = Volley.newRequestQueue(this);

        locationFAb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parse();
                Toast.makeText(MainActivity.this, "detected location :" + city, Toast.LENGTH_SHORT).show();
                }
        });

    }


    private void requestLocationPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){

        }

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},tag);
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == tag) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Displaying a toast
                Toast.makeText(this, "Permission granted ", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }




    private boolean isLocationAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    public void parse() {
        url = "https://api.apixu.com/v1/current.json?key=YOUR-API-KEY&q=" + city;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject mainobject = response.getJSONObject("current");
                    String temp = mainobject.getString("temp_c");
                    String last_updated = mainobject.getString("last_updated");

                    JSONObject loc = response.getJSONObject("location");
                    String locdname = loc.getString("name");
                    String locdreg = loc.getString("region");
                    String locdcountry= loc.getString("country");

                    JSONObject cur = mainobject.getJSONObject("condition");
                    String  wea_con = cur.getString("text");

                    JSONObject subObj = response.getJSONObject("current");
                    String pressure = subObj.getString("pressure_mb");
                    String humidity = subObj.getString("humidity");
                    String clouds = subObj.getString("cloud");
                    String windSpeed = subObj.getString("wind_kph");
                    String precipitation  = subObj.getString("precip_mm");
                    String feelslike = subObj.getString("feelslike_c");
                    String winddegree = subObj.getString("wind_degree");

                    ciityinfo.setText(locdname + '\n' + locdreg + '\n' + locdcountry);
                    precip.setText(precipitation);
                    feelslik.setText(feelslike + " C");
                    windS.setText(windSpeed + " kph");
                    collapsingtool.setTitle(locdname);
                    tempText.setText(temp + " C");
                    pressText.setText(pressure + " mb");
                    humidityText.setText(humidity);
                    updated.setText( "Last updated on "+last_updated);
                    condition.setText(wea_con);
                    wind_degree.setText(winddegree);
                    cloud.setText(clouds);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(request);


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

       // Toast.makeText(MainActivity.this, "connected", Toast.LENGTH_SHORT).show();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        double lat = location.getLatitude();
       double lon = location.getLongitude();
        try {
            getCity(lat,lon);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void getCity(double lati, double longi) throws IOException {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(lati, longi, 1);
        String b = addresses.get(0).getSubAdminArea();
        city = b;
        parse();


    }



    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();

    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }
}



