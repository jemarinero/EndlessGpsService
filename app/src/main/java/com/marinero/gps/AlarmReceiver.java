package com.example.gpsservicetest;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

import static android.content.Context.ALARM_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    private String TAG = "GPS_SERVICE_ALARM_R";
    FileLogger fileLogger;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String fileName = context.getExternalFilesDir(null).toString() + "/Log.txt";
            fileLogger = new FileLogger(fileName);
            String action = intent.getAction();
            fileLogger.writeLog(TAG + " - onReceive action: " + action);
            if (!GpsService.isGpsServiceRunning) {
                fileLogger.writeLog(TAG + " - onReceive: Starting Service");
                Intent serviceIntent = new Intent(context, GpsService.class);
                serviceIntent.putExtra("STARTED_FROM", AlarmReceiver.class.getSimpleName());
                context.startService(serviceIntent);
                return;
            }
            fileLogger.writeLog(TAG + " - onReceive: Service is running");
            setNextAlarm(context);
        } catch (Exception ex) {
            fileLogger.writeLog(TAG + " - onReceive Exception: " + ex);
        }
    }

    @TargetApi(23)
    private void setNextAlarm(Context context) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        long triggerTime = System.currentTimeMillis() + 1 * 60 * 1000;
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (isDozing(context)) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, sender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, triggerTime, sender);
        }
        String log = TAG + " - setNextAlarm: triggerTime = " + triggerTime;
        fileLogger.writeLog(TAG + log);
    }

    @TargetApi(23)
    private boolean isDozing(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isDeviceIdleMode() &&
                    !powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        } else {
            return false;
        }
    }
}
