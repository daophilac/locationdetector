package com.example.locationdetector;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;

public class MainActivity extends AppCompatActivity {
    private EditText editTextDistanceToMove;
    private TextView textViewDistanceHasMoved;
    private Button buttonStart;
    private Button buttonStop;
    private TextView textViewAccuracy;
    private TextView textViewStatus;

    private LocationDetector locationDetector;
    private Location currentLocation;
    private float distanceToMove;
    private float distanceHasMoved;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else{
            configureViews();
        }
    }

    private void configureViews(){
        locationDetector = new LocationDetector(this);
        locationDetector.setPriority(LocationDetector.Priority.HIGH_ACCURACY);
        locationDetector.setInterval(2000);
        locationDetector.checkLocationSettings(new LocationDetector.OnLocationSettingResultListener() {
            @Override
            public void onSatisfiedSetting() {

            }

            @Override
            public void onUnsatisfiedSetting(Exception e) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MainActivity.this, 1);
                } catch (IntentSender.SendIntentException e1) {
                    e1.printStackTrace();
                }
            }
        });
        editTextDistanceToMove = findViewById(R.id.edit_text_distance_to_move);
        textViewDistanceHasMoved = findViewById(R.id.text_view_distance_has_moved);
        buttonStart = findViewById(R.id.button_start);
        buttonStop = findViewById(R.id.button_stop);
        textViewAccuracy = findViewById(R.id.text_view_accuracy);
        textViewStatus = findViewById(R.id.text_view_status);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!locationDetector.isRunning()){
                    distanceToMove = Float.parseFloat(editTextDistanceToMove.getText().toString());
                    distanceHasMoved = 0;
                    textViewDistanceHasMoved.setText("0");
                    textViewStatus.setText(getString(R.string.tracking));
                    locationDetector.start(new LocationDetector.OnLocationUpdateListener() {
                        @Override
                        public void onLocationUpdate(Location location) {
                            if(currentLocation == null){
                                currentLocation = location;
                                return;
                            }
                            float distance = currentLocation.distanceTo(location);
                            distanceHasMoved += distance;
                            textViewDistanceHasMoved.setText(String.valueOf(distanceHasMoved));
                            textViewAccuracy.setText(location.getAccuracy() + "");
                            distanceToMove -= distance;
                            if(distanceToMove <= 0){
                                locationDetector.stop();
                                textViewStatus.setText(getString(R.string.done));
                            }
                            currentLocation = location;
                        }
                    });
                }
            }
        });
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(locationDetector.isRunning()){
                    locationDetector.stop();
                    currentLocation = null;
                    textViewStatus.setText(getString(R.string.stopped));
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    configureViews();
                }
                else{
                    new PermissionDeniedDialogFragment().show(getSupportFragmentManager(), this.getPackageName());
                }
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case 1:
                if(resultCode != RESULT_OK){
                    new UnsatisfiedSettingDialogFragment().show(getSupportFragmentManager(), this.getPackageName());
                }
                break;
        }
    }
}
