package com.kacperprzelozny.weatherapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private LocationRequest locationRequest;
    final String api_key = "fa161d44be9d341207ead14e09c333cc";
    final String api_url = "https://api.openweathermap.org/data/2.5/weather?";
    private TextView city_country_view;
    private TextView temperature_view;
    private TextView description_view;
    private TextView humidity_view;
    private TextView max_temp_view;
    private TextView min_temp_view;
    private TextView pressure_view;
    private TextView wind_speed_view;
    private ImageView search;
    private EditText search_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        Objects.requireNonNull(getSupportActionBar()).hide();

        city_country_view = (TextView) findViewById(R.id.county_city);
        temperature_view = (TextView) findViewById(R.id.temperature);
        description_view = (TextView) findViewById(R.id.description);
        humidity_view = (TextView) findViewById(R.id.humidity);
        max_temp_view = (TextView) findViewById(R.id.max_temp);
        min_temp_view = (TextView) findViewById(R.id.min_temp);
        pressure_view = (TextView) findViewById(R.id.pressure);
        wind_speed_view = (TextView) findViewById(R.id.wind_speed);

        search_text = (EditText) findViewById(R.id.location_input);
        search = (ImageView) findViewById(R.id.search_button);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city_name = String.valueOf(search_text.getText());
                getInfoFromApi(0,0, city_name);
            }
        });
        getCurrentLocation();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if (isGPSEnabled()) {

                    getCurrentLocation();

                }else {

                    turnOnGPS();
                }
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                getCurrentLocation();
            }
        }
    }

    private void getCurrentLocation() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (isGPSEnabled()) {

                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);

                                    LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                            .removeLocationUpdates(this);

                                    if (locationResult.getLocations().size() >0){

                                        int index = locationResult.getLocations().size() - 1;
                                        double latitude = locationResult.getLocations().get(index).getLatitude();
                                        double longitude = locationResult.getLocations().get(index).getLongitude();

                                        getInfoFromApi(latitude, longitude, "");
                                    }
                                }
                            }, Looper.getMainLooper());

                } else {
                    turnOnGPS();
                }

            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void getInfoFromApi(double latitude, double longitude, String city_name) {

        RequestQueue volleyQueue = Volley.newRequestQueue(MainActivity.this);
        String fullApi = api_url;
        if(city_name.equals("")){
            fullApi = fullApi + "lat="+latitude+"&lon="+longitude+"&APPID="+ api_key;
        }
        else fullApi = fullApi + "q="+city_name+"&APPID="+ api_key;
        Log.d("URL", fullApi);
        @SuppressLint("SetTextI18n") JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                fullApi,
                null,
                (Response.Listener<JSONObject>) response -> {
                    try {
                        JSONArray weather = response.getJSONArray("weather");
                        JSONObject weather0 = (JSONObject) weather.get(0);
                        String description = weather0.getString("description");

                        JSONObject main = response.getJSONObject("main");
                        String temp = Integer.toString(main.getInt("temp") - 273);
                        String min_temp = Integer.toString(main.getInt("temp_min") - 273);
                        String max_temp = Integer.toString(main.getInt("temp_max") - 273);
                        String pressure = Integer.toString(main.getInt("pressure"));
                        String humidity = Integer.toString(main.getInt("humidity"));

                        JSONObject wind = response.getJSONObject("wind");
                        String wind_speed = Integer.toString(wind.getInt("speed"));

                        JSONObject sys = response.getJSONObject("sys");
                        String country = sys.getString("country");

                        String name = response.getString("name");

                        description_view.setText(description);
                        city_country_view.setText(name + ", " + country);
                        temperature_view.setText(temp + "°C");
                        humidity_view.setText("Humidity: " + humidity + "%");
                        max_temp_view.setText("Max temp: " + max_temp + "°C");
                        min_temp_view.setText("Min temp: " + min_temp + "°C");
                        pressure_view.setText("Pressure: " + pressure + "hPa");
                        wind_speed_view.setText("Wind speed: " + wind_speed + "m/s");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                (Response.ErrorListener) error -> {
                    Toast.makeText(MainActivity.this, "Some error occurred! Cannot fetch current weather", Toast.LENGTH_LONG).show();
                }
        );

        volleyQueue.add(jsonObjectRequest);
    }

    private void turnOnGPS() {



        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });

    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = null;
        boolean isEnabled = false;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;

    }


}