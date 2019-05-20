package com.example.locationdetector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationDetector {
    private static final String ACCESS_LOCATION_PERMISSION_HAS_NOT_BEEN_GRANTED = "Access location permission has not been granted";
    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private OnLocationUpdateListener onLocationUpdateListener;
    private boolean running;

    public LocationDetector(Context context){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            throw new RuntimeException(ACCESS_LOCATION_PERMISSION_HAS_NOT_BEEN_GRANTED);
        }
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            throw new RuntimeException(ACCESS_LOCATION_PERMISSION_HAS_NOT_BEEN_GRANTED);
        }
        this.context = context;
        fusedLocationProviderClient = new FusedLocationProviderClient(context);
        locationRequest = LocationRequest.create();
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult == null){
                    return;
                }
                for(Location location : locationResult.getLocations()){
                    onLocationUpdateListener.onLocationUpdate(location);
                }
            }
        };
    }
    public void checkLocationSettings(final OnLocationSettingResultListener onLocationSettingResultListener){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
        task.addOnSuccessListener((Activity) context, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                onLocationSettingResultListener.onSatisfiedSetting();
            }
        });
        task.addOnFailureListener((Activity) context, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onLocationSettingResultListener.onUnsatisfiedSetting(e);
            }
        });
    }
    @SuppressLint("MissingPermission")
    public void start(final OnLocationUpdateListener onLocationUpdateListener){
        this.running = true;
        this.onLocationUpdateListener = onLocationUpdateListener;
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }
    public void stop(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        this.running = false;
    }
    public void setPriority(Priority priority){
        switch (priority){
            case NO_POWER:
                locationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
                break;
            case LOW_POWER:
                locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                break;
            case BALANCED_POWER_ACCURACY:
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                break;
            case HIGH_ACCURACY:
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                break;
        }
    }
    public void setInterval(long interval){
        locationRequest.setInterval(interval);
    }
    public void setFastestInterval(long fastestInterval){
        locationRequest.setFastestInterval(fastestInterval);
    }
    public void setNumUpdates(int numUpdates){
        locationRequest.setNumUpdates(numUpdates);
    }

    public boolean isRunning() {
        return running;
    }

    public interface OnLocationSettingResultListener {
        void onSatisfiedSetting();
        void onUnsatisfiedSetting(Exception e);
    }
    public interface OnLocationUpdateListener {
        void onLocationUpdate(Location location);
    }
    public enum Priority{
        NO_POWER, LOW_POWER, BALANCED_POWER_ACCURACY, HIGH_ACCURACY
    }
}
