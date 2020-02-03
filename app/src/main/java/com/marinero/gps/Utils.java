package com.example.gpsservicetest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

public class Utils {
    public static boolean hasUserGrantedAllNecessaryPermissions(Context context){
        return hasUserGrantedPermission(Manifest.permission.ACCESS_COARSE_LOCATION, context)
                && hasUserGrantedPermission(Manifest.permission.ACCESS_FINE_LOCATION, context)
                && hasUserGrantedPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context)
                && hasUserGrantedPermission(Manifest.permission.READ_EXTERNAL_STORAGE, context);
    }
    static boolean hasUserGrantedPermission(String permissionName, Context context){
        boolean granted = ContextCompat.checkSelfPermission(context, permissionName) == PackageManager.PERMISSION_GRANTED;
        return granted;
    }
}
