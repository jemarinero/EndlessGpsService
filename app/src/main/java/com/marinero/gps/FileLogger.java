package com.example.gpsservicetest;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FileLogger {
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String TAG = "GPS_SERVICE_LOGGER";
    private String gpsLogFile;

    public FileLogger(String fileName) {
        gpsLogFile = fileName;
    }

    public void writeLog(String line) {

        Log.d(TAG, line);

        File dump = new File(gpsLogFile);

        try {
            if (!dump.exists()) {
                dump.createNewFile();
            }

            BufferedWriter output = new BufferedWriter(new FileWriter(dump, true));
            String date = dateTimeFormat.format(Calendar.getInstance().getTime());
            String lineToWrite = date + " - " + line + "\n";
            Log.d(TAG, "writeLog: " + lineToWrite);
            output.write(lineToWrite);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
