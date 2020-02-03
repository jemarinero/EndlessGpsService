package com.example.gpsservicetest;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;

public class GpsLocationListener implements LocationListener, GpsStatus.Listener, GpsStatus.NmeaListener {

    private GpsService gpsService;
    private String TAG = "GPS_SERVICE_LISTENER";

    public GpsLocationListener(GpsService service) {
        this.gpsService = service;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"onLocationChanged");
        gpsService.onLocationChanged(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG,"onStatusChanged: "+provider);
        if (status == LocationProvider.OUT_OF_SERVICE) {
            Log.d(TAG,provider + " is out of service");
            gpsService.restartGpsManagers();
        }

        if (status == LocationProvider.AVAILABLE) {
            Log.d(TAG,provider + " is available");
        }

        if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
            Log.d(TAG,provider + " is temporarily unavailable");
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        gpsService.restartGpsManagers();
    }

    @Override
    public void onProviderDisabled(String provider) {
        gpsService.restartGpsManagers();
    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onNmeaReceived(long timestamp, String nmea) {

    }
}
