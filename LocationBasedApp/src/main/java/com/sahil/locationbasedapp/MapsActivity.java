package com.sahil.locationbasedapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends Activity implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {


    public static final String TOTAL_LOCATION_COUNT = "totalLocation";
    public static final String LAT = "LAT-";
    public static final String LONG = "LONG-";
    List<Integer> existingMarkers = new ArrayList<Integer>();
    LatLng locationBeingDragged = null;
    private LocationUtils locationUtils;
    private GoogleMap map;
    private Polygon polygon;
    private boolean isMap = true;
    private boolean showCurrentLoc = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        showCurrentLoc = true;
        super.onCreate(savedInstanceState);
        locationUtils = LocationUtils.getLocationUtilsInstance(this);
        setContentView(R.layout.activity_maps);
        map = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map)).getMap();
        map.setOnMapLongClickListener(this);
        map.setOnMarkerDragListener(this);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(locationUtils.getCurrentLatlng(), 17));
        showCoordinatesOnMap();

    }

    public void toggleView(View view) {
        if (isMap == true) {
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            isMap = false;
        } else {
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            isMap = true;
        }
    }

    private void showCoordinatesOnMap() {


        if (locationUtils.servicesConnected()) {
            spotCurrentLocation(true);
        }

        if (null != polygon) {
            polygon.remove();
        }

        List<LatLng> userLocations = locationUtils.getUserLatLongs();
        if (!userLocations.isEmpty()) {
//            if (userLocations.size() == 1) {
//                map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocations.get(userLocations.size() - 1), map.getCameraPosition().zoom));
//            }
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.fillColor(0x500011FF).strokeColor(0x50444444).strokeWidth(2);
            polygon = map.addPolygon(polygonOptions.addAll(userLocations));

            TextView textView = (TextView) findViewById(R.id.txtMapArea);
            double areaInSqMeter = AreaCalculator.calculateAreaOfGPSPolygonOnEarthInSquareMeters(locationUtils.getUserLocations());
            double areaInAcre = areaInSqMeter * 0.000247;
//            String sqMeter = String.format("%.2g%n", areaInSqMeter);
//            String acre = String.format("%.2g%n", areaInAcre);
            String sqMeter = Double.toString(areaInSqMeter);
            String acre = Double.toString(areaInAcre);

            textView.setText(acre+" Acres / "+sqMeter+" sqMtr");
        } else {
            spotCurrentLocation(true);
        }

        for (LatLng loc : userLocations) {
            addMarkerIfAlreadyNotPresent(loc);


        }
        map.setMyLocationEnabled(true);

    }

    public void tagCurrentLocation(View view) {
        locationUtils.storeCurrentLocation();
        Location currentLocation = locationUtils.getCurrentLocation();
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        addMarkerIfAlreadyNotPresent(latLng);

        showCoordinatesOnMap();
    }

    public void spotCurrentLocation(Boolean zoomToLoc) {
        Location currentLocation = locationUtils.getCurrentLocation();

        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        map.setMyLocationEnabled(true);

        if (zoomToLoc) {
            zoomToCurrentLoc(latLng);
        }

    }

    private void zoomToCurrentLoc(LatLng latLng) {
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.maps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        map = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map)).getMap();

        Location location = new Location("");
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        locationUtils.storeLocationInSharedContext(location);
        addMarkerIfAlreadyNotPresent(latLng);
        showCoordinatesOnMap();

    }

    private void addMarkerIfAlreadyNotPresent(LatLng latLng) {
        Integer latLngIndex = locationUtils.getLatLngIndex(latLng);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(200, 50, conf);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setTextSize(25);

        canvas.drawText("hello",0,50, paint);


        if (!existingMarkers.contains(latLngIndex)) {
            Marker marker = map.addMarker(new MarkerOptions().draggable(true).position(latLng).alpha(0.7f).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//            Marker marker = map.addMarker(new MarkerOptions().draggable(true).position(latLng).alpha(0.7f).icon(BitmapDescriptorFactory.fromBitmap(bmp)));
            marker.setTitle(latLngIndex.toString());
            marker.showInfoWindow();
            existingMarkers.add(latLngIndex);

        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, map.getCameraPosition().zoom));

    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        locationBeingDragged = locationUtils.getLatLongsByIndex(Integer.parseInt(marker.getTitle()));
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {


        locationUtils.replaceLocationValues(locationBeingDragged, marker.getPosition());
        showCoordinatesOnMap();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), map.getCameraPosition().zoom));
    }
}
