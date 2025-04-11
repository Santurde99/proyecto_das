package com.example.proyecto1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class Notification_Helper {
    private Context mContext;
    private NotificationManager mNotificationManager;

    private static final String CHANNEL_ID = "mi_canal_id";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence nombre = "Main Channel";
            String descripcion = "Channel for all notifications";
            int importancia = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel canal = new NotificationChannel(CHANNEL_ID, nombre, importancia);
            canal.setDescription(descripcion);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }
    }

    public static void showNotification(Context context, String title, String mesaje) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // Ícono de la notificación
                .setContentTitle(title)
                .setContentText(mesaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);  // Se elimina cuando el usuario la toca

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return; // No tiene permiso, no enviar la notificación
            }
        }
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(1, builder.build());  // ID único para la notificación
    }
}