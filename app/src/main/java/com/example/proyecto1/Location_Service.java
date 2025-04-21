package com.example.proyecto1;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import org.osmdroid.util.GeoPoint;

public class Location_Service extends Service {
    private static final String CHANNEL_ID = "location_service_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final int ARRIVAL_NOTIFICATION_ID = 2;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GeoPoint puntoObjetivo;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createNotifChannel();
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

        startForeground(NOTIFICATION_ID, crearNotificacionSeguimiento());
        startPosUpdate();

        return START_STICKY;
    }

    private void createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Seguimiento de Ubicación",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Notificación de seguimiento hacia el punto objetivo");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification crearNotificacionSeguimiento() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Siguiendo punto objetivo")
                .setContentText("Moviéndote hacia el punto marcado en el mapa")
                .setSmallIcon(R.drawable.placeholder)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void startPosUpdate() {
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
            onPointReach();

            // Borrar punto guardado
            getSharedPreferences("punto_objetivo", MODE_PRIVATE).edit().clear().apply();

            stopSelf();
        }
    }

    private void onPointReach() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("¡Has llegado al punto objetivo!")
                .setContentText("Felicidades, has alcanzado tu destino, recibes 1000 puntos.")
                .setSmallIcon(R.drawable.placeholder)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Main_Activity.add_nuggets(1000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.notify(ARRIVAL_NOTIFICATION_ID, builder.build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
