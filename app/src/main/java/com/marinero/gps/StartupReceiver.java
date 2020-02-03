package com.example.gpsservicetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupReceiver extends BroadcastReceiver {
    FileLogger fileLogger;

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            String fileName = context.getExternalFilesDir(null).toString() + "/Log.txt";
            fileLogger = new FileLogger(fileName);
            String action = intent.getAction();
            fileLogger.writeLog("GPS_SERVICE_STARTUP_R - onReceive action: " + action);
            if (!GpsService.isGpsServiceRunning) {
                fileLogger.writeLog("GPS_SERVICE_STARTUP_R - nReceive: Starting Service");
                Intent serviceIntent = new Intent(context, GpsService.class);
                serviceIntent.putExtra("STARTED_FROM", StartupReceiver.class.getSimpleName());
                context.startService(serviceIntent);
                return;
            }
            fileLogger.writeLog("GPS_SERVICE_STARTUP_R - onReceive: Service is running");

        } catch (Exception ex) {
            fileLogger.writeLog("GPS_SERVICE_STARTUP_R - onReceive Exception: " + ex);
        }
    }
}
