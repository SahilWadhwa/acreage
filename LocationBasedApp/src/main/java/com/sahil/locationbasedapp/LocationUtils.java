package com.sahil.locationbasedapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LocationUtils implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    public static final String TOTAL_LOCATION_COUNT = "totalLocation";
    public static final String LAT = "LAT-";
    public static final String LONG = "LONG-";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static LocationUtils locationUtils;
    LocationClient mLocationClient;
    private Activity mActivity;

    private LocationUtils() {

    }

    public static LocationUtils getLocationUtilsInstance(Activity activity) {
        if (null != locationUtils) {
            locationUtils.mActivity = activity;
            return locationUtils;
        }

        locationUtils = new LocationUtils();
        locationUtils.mActivity = activity;
        locationUtils.setup();

        return locationUtils;
    }

    private void setup() {
        mLocationClient = new LocationClient(mActivity, locationUtils, locationUtils);
        mLocationClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this.mActivity, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this.mActivity, "Disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        this.mActivity, CONNECTION_FAILURE_RESOLUTION_REQUEST
                );
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    private void showErrorDialog(int errorCode) {
        GooglePlayServicesUtil.getErrorDialog(errorCode, this.mActivity, 1001);
    }

    public void storeCurrentLocation() {
        if (servicesConnected()) {
            storeLocationInSharedContext(getCurrentLocation());
        }
    }

    public Location getCurrentLocation() {
        return mLocationClient.getLastLocation();
    }
    public LatLng getCurrentLatlng() {
        Location lastLocation = mLocationClient.getLastLocation();

        return new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
    }

    public void storeLocationInSharedContext(Location location) {
        if(!isLocationAlreadyPresent(location))
        {
        SharedPreferences myLocationPref = mActivity.getSharedPreferences("myLocationPref", mActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        int locationCount = 0;
        editor = myLocationPref.edit();

        if (myLocationPref.contains(TOTAL_LOCATION_COUNT)) {
            locationCount = myLocationPref.getInt(TOTAL_LOCATION_COUNT, 0);
        } else {
            editor.putInt(TOTAL_LOCATION_COUNT, 0);
        }

        editor.putString(LAT + locationCount, Double.toString(location.getLatitude()));
        editor.putString(LONG + locationCount, Double.toString(location.getLongitude()));

        editor.putInt(TOTAL_LOCATION_COUNT, locationCount + 1);
        editor.commit();
        }
    }

    public boolean servicesConnected() {
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this.mActivity);
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d("Location Updates",
                    "Google Play services is available.");
            return true;
        } else {
            int errorCode = resultCode;
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    this.mActivity,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            if (errorDialog != null) {
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
            }
            return false;
        }
    }

    public List<LatLng> getUserLatLongs() {
        SharedPreferences myLocationPref = mActivity.getSharedPreferences("myLocationPref", mActivity.MODE_PRIVATE);
        ArrayList<LatLng> locations = new ArrayList<LatLng>();


        int locationCount = 0;

        if (myLocationPref.contains(TOTAL_LOCATION_COUNT)) {
            locationCount = myLocationPref.getInt(TOTAL_LOCATION_COUNT, 0);
        }

        for (int i = 0; i <= locationCount - 1; i++) {
            double lat = Double.valueOf(myLocationPref.getString(LAT + i, "")).doubleValue();
            double longitude = Double.valueOf(myLocationPref.getString(LONG + i, "")).doubleValue();
            locations.add(new LatLng(lat, longitude));
        }
        return locations;
    }

    public List<Location> getUserLocations() {
        SharedPreferences myLocationPref = mActivity.getSharedPreferences("myLocationPref", mActivity.MODE_PRIVATE);
        ArrayList<Location> locations = new ArrayList<Location>();

        int locationCount = 0;

        if (myLocationPref.contains(TOTAL_LOCATION_COUNT)) {
            locationCount = myLocationPref.getInt(TOTAL_LOCATION_COUNT, 0);
        }

        for (int i = 0; i <= locationCount - 1; i++) {
            double lat = Double.valueOf(myLocationPref.getString(LAT + i, "")).doubleValue(); //Double.parseDouble(myLocationPref.getString(LAT + i, ""));
            double longitude = Double.valueOf(myLocationPref.getString(LONG + i, "")).doubleValue();//Double.parseDouble(myLocationPref.getString(LONG + i, ""));
            Location newLocation = new Location("flp");
            newLocation.setLatitude(lat);
            newLocation.setLongitude(longitude);
            newLocation.setAccuracy(1);
            locations.add(newLocation);
        }

        return locations;
    }

    public Integer getLatLongsPositionInArray(double latitude, double longi) {
        Integer index = null;
        SharedPreferences myLocationPref = mActivity.getSharedPreferences("myLocationPref", mActivity.MODE_PRIVATE);
        int locationCount = 0;
        if (myLocationPref.contains(TOTAL_LOCATION_COUNT)) {
            locationCount = myLocationPref.getInt(TOTAL_LOCATION_COUNT, 0);
        }

        for (int i = 0; i <= locationCount - 1; i++) {
            double lat = Double.valueOf(myLocationPref.getString(LAT + i, "")).doubleValue();
            double longitude = Double.valueOf(myLocationPref.getString(LONG + i, "")).doubleValue();
            if (lat == latitude && longi == longitude) {
                index = i;
                break;
            }
        }
        return index;
    }

    public LatLng getLatLongsByIndex(Integer index) {
        SharedPreferences myLocationPref = mActivity.getSharedPreferences("myLocationPref", mActivity.MODE_PRIVATE);
        int locationCount = 0;
        if (myLocationPref.contains(TOTAL_LOCATION_COUNT)) {
            locationCount = myLocationPref.getInt(TOTAL_LOCATION_COUNT, 0);
        }

            double lat = Double.valueOf(myLocationPref.getString(LAT + index, "")).doubleValue();
            double longitude = Double.valueOf(myLocationPref.getString(LONG + index, "")).doubleValue();
        return new LatLng(lat, longitude);
    }

    public boolean isLatLongAlreadyPresent(LatLng latLng) {
        if (getLatLongsPositionInArray(latLng.latitude, latLng.longitude)
                != null) {
            return true;
        }
        return false;
    }

    public boolean isLocationAlreadyPresent(Location location) {
        if (getLatLongsPositionInArray(location.getLatitude(), location.getLongitude())
                != null) {
            return true;
        }
        return false;
    }

    public Integer getLatLngIndex(LatLng latLng) {
       return getLatLongsPositionInArray(latLng.latitude, latLng.longitude);
    }

    public Integer getLocationIndex(Location location) {
       return getLatLongsPositionInArray(location.getLatitude(), location.getLongitude());
    }




    private void setDummyCoordinates() {
        // Polylines are useful for marking paths and routes on the map.
//        map.addPolyline(new PolylineOptions().geodesic(true)
//                .addAll(userLocations)
//                .add(new LatLng(-33.866, 151.195))  // Sydney
//                .add(new LatLng(-18.142, 178.431))  // Fiji
//                .add(new LatLng(21.291, -157.821))  // Hawaii
//                .add(new LatLng(37.423, -122.091))  // Mountain View
//                .add(new LatLng(-33.866, 151.195))  // Sydney
//        );
    }

    public void replaceLocationValues(LatLng locationBeingDragged, LatLng newLocation) {
        Integer index = getLatLngIndex(locationBeingDragged);
        if(index != null)
        {
            storeLocationInSharedContext(newLocation, index);
        }
    }

    public void storeLocationInSharedContext(LatLng latLng, Integer index) {
        if(!isLatLongAlreadyPresent(latLng))
        {
            SharedPreferences myLocationPref = mActivity.getSharedPreferences("myLocationPref", mActivity.MODE_PRIVATE);
            SharedPreferences.Editor editor;
            editor = myLocationPref.edit();
            editor.putString(LAT + index, Double.toString(latLng.latitude));
            editor.putString(LONG + index, Double.toString(latLng.longitude));
            editor.commit();
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }


}
