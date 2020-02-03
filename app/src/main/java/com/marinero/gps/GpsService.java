package com.example.gpsservicetest;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

public class GpsService extends Service {

    public static boolean isGpsServiceRunning = false;
    private String TAG = "GPS_SERVICE";
    private FileLogger gpsLogger;
    AlarmManager nextPointAlarmManager;
    private Intent alarmIntent;
    private long INTERVAL_ALARM_TIME = 1 * 60 * 1000;
    private long INTERVAL_GPS_TIME = 1 * 60 * 1000;
    private long FASTEST_INTERVAL_GPS_TIME = 1 * 30 * 1000;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mClient;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String fileGPS = getBaseContext().getExternalFilesDir(null).toString() + "/GPSLog.txt";
        gpsLogger = new FileLogger(fileGPS);
        nextPointAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isGpsServiceRunning = true;
        String log;
        if (intent != null) {
            log = "onStartCommand: started from " + intent.getStringExtra("STARTED_FROM");
        } else {
            log = "onStartCommand: started from null";
        }
        Log.d(TAG, log);
        startGpsManager();
        return START_STICKY;
    }

    @TargetApi(23)
    private void setNextAlarm() {
        cancelAlarm();
        alarmIntent = new Intent(this, AlarmReceiver.class);
        long triggerTime = System.currentTimeMillis() + INTERVAL_ALARM_TIME;
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (isDozing()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, sender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, triggerTime, sender);
        }
        Log.d(TAG, "setNextAlarm: triggerTime = " + triggerTime);
    }

    private void cancelAlarm() {
        Log.d(TAG, "cancelAlarm");
        if (alarmIntent != null) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            PendingIntent sender = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            am.cancel(sender);
        }
    }

    @TargetApi(23)
    private boolean isDozing() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            return powerManager.isDeviceIdleMode() &&
                    !powerManager.isIgnoringBatteryOptimizations(getPackageName());
        } else {
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isGpsServiceRunning = false;
        stopGpsManager();
    }

    public void onLocationChanged(Location location) {
        if (location != null) {
            String latitud = String.valueOf(location.getLatitude());
            String longitud = String.valueOf(location.getLongitude());
            String accuracy = String.valueOf(location.getAccuracy());
            String speed = String.valueOf(location.getSpeed());
            String line = "Latitud: " + (latitud != null ? latitud : "N/A")
                    + " Longitud: " + (longitud != null ? longitud : "N/A")
                    + " Accuracy: " + (accuracy != null ? accuracy : "N/A")
                    + " Speed: " + (speed != null ? speed : "N/A");
            gpsLogger.writeLog(line);
        }

    }

    @SuppressWarnings("ResourceType")
    private void startGpsManager() {
        Log.d(TAG, "startGpsManager");
        startLocationUpdates();
        setNextAlarm();
    }

    @SuppressWarnings("ResourceType")
    private void stopGpsManager() {
        Log.d(TAG, "stopGpsManager");
        if (mClient != null) {
            mClient.removeLocationUpdates(locationCallback);
        }
        setNextAlarm();
    }

    void restartGpsManagers() {
        Log.d(TAG, "restartGpsManagers");
        stopGpsManager();
        startGpsManager();
    }

    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(INTERVAL_GPS_TIME);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL_GPS_TIME);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        mClient = LocationServices.getFusedLocationProviderClient(this);
        mClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location lastLocation = locationResult.getLastLocation();
            onLocationChanged(lastLocation);
        }
    };

}
