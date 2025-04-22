package com.example.proyecto1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

public class GpsStatus_Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isGpsEnabled) {
                // Detener el servicio y mostrar notificación
                Notification_Helper.showGpsDisconnectedNotification(context);
                Intent serviceIntent = new Intent(context, Location_Service.class);
                context.stopService(serviceIntent);
            }
        }
    }
}