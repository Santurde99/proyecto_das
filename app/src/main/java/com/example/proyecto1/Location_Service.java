package com.example.proyecto1;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import org.osmdroid.util.GeoPoint;

public class Location_Service extends Service {
    private static final int NOTIFICATION_ID = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GeoPoint puntoObjetivo;
    private GpsStatus_Receiver gpsReceiver;
    private boolean isGpsMonitoringActive = false;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Notification_Helper.createNotificationChannels(this);
        setupGpsMonitoring();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || !intent.hasExtra("lat") || !intent.hasExtra("lng")) {
            stopSelf();
            return START_NOT_STICKY;
        }

        double lat = intent.getDoubleExtra("lat", 0);
        double lng = intent.getDoubleExtra("lng", 0);
        puntoObjetivo = new GeoPoint(lat, lng);

        startForeground(NOTIFICATION_ID, Notification_Helper.getForegroundNotification(this));
        startLocationUpdates();

        return START_STICKY;
    }

    private void setupGpsMonitoring() {
        gpsReceiver = new GpsStatus_Receiver();
        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(gpsReceiver, filter);
        isGpsMonitoringActive = true;
    }

    private void stopGpsMonitoring() {
        if (isGpsMonitoringActive && gpsReceiver != null) {
            unregisterReceiver(gpsReceiver);
            isGpsMonitoringActive = false;
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(3000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || puntoObjetivo == null) return;

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    checkProximity(location);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        } else {
            stopSelf();
        }
    }

    private void checkProximity(Location ubicacionActual) {
        float[] resultados = new float[1];
        Location.distanceBetween(
                ubicacionActual.getLatitude(), ubicacionActual.getLongitude(),
                puntoObjetivo.getLatitude(), puntoObjetivo.getLongitude(),
                resultados
        );

        float distancia = resultados[0];

        if (distancia <= 20) {
            onPointReached();
        }
    }

    private void onPointReached() {
        Main_Activity.add_fixed_nuggets(1000);
        Notification_Helper.showArrivalNotification(this);
        getSharedPreferences("punto_objetivo", MODE_PRIVATE).edit().clear().apply();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        stopGpsMonitoring();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}