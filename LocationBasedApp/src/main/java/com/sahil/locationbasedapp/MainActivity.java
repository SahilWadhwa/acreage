package com.sahil.locationbasedapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {

    public static final String TOTAL_LOCATION_COUNT = "totalLocation";
    public static final String LAT = "LAT-";
    public static final String LONG = "LONG-";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public LocationUtils locationUtils;

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        locationUtils =  LocationUtils.getLocationUtilsInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onStart() {
        super.onStart();
        showLocationList();
    }

    @Override
    protected void onStop() {
//        mLocationClient.disconnect();
        super.onStop();
    }

    public void recordCurrentLocation(View view) {
        TextView listView = (TextView) findViewById(R.id.textLocationData);
        if (locationUtils.servicesConnected()) {
            Location location = locationUtils.getCurrentLocation();
            listView.setText("Lat="+location.getLatitude()+"   Long="+location.getLongitude() + " Acc= "+location.getAccuracy());
        }
    }

    public void addToList(View view) {
        locationUtils.storeCurrentLocation();
        showLocationList();
    }

    public void showMaps(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void showLocationList()
    {
        ArrayList<String> strings = new ArrayList<String>();
        for(Location location : locationUtils.getUserLocations())
        {
            strings.add("Lat="+location.getLatitude()+"   Long="+location.getLongitude());
        }
        String[] list= new String[strings.size()];
        list=strings.toArray(list);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list));
        calculateArea();
    }

    public void clearList(View view)
    {
        SharedPreferences myLocationPref = getSharedPreferences("myLocationPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = myLocationPref.edit();
        editor.clear();
        editor.commit();
        showLocationList();


    }
    public void calculateArea()
    {
        TextView listView = (TextView) findViewById(R.id.txtArea);
        listView.setText("Area in sq mtr - " +Double.toString(AreaCalculator.calculateAreaOfGPSPolygonOnEarthInSquareMeters(locationUtils.getUserLocations())));

    }
}
