package com.example.proyecto1;

import android.app.Notification;
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
    private static final String CHANNEL_ID = "main_channel";
    private static final String FOREGROUND_CHANNEL_ID = "foreground_channel";

    //--------------------------------- Nuevo sistema de notificaciones ----------------------------------------
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para notificaciones normales (HIGH importance)
            NotificationChannel mainChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Main Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            mainChannel.setDescription("Channel for general notifications");

            // Canal para el foreground service (LOW importance, no interrumpir)
            NotificationChannel foregroundChannel = new NotificationChannel(
                    FOREGROUND_CHANNEL_ID,
                    "Foreground Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            foregroundChannel.setDescription("Channel for foreground service notifications");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(mainChannel);
            manager.createNotificationChannel(foregroundChannel);
        }
    }

    public static Notification getForegroundNotification(Context context) {
        return new NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.gps_notif1_title))
                .setContentText(context.getString(R.string.gps_notif1_text))
                .setSmallIcon(R.drawable.placeholder)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)  // ¡Importante! Evita que se descarte manualmente
                .build();
    }

    public static void showArrivalNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.gps_notif2_title))
                .setContentText(context.getString(R.string.gps_notif2_text))
                .setSmallIcon(R.drawable.placeholder)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(2, builder.build());
    }

    public static void showGpsDisconnectedNotification(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.gps_notif3_title))
                .setContentText(context.getString(R.string.gps_notif3_text))
                .setSmallIcon(org.osmdroid.library.R.drawable.person)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(3, builder.build());
    }

    // ---------------------------------- Sistema anterior de notificaciones genericas -------------------------------------------------
    public static void showNotification(Context context, String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.placeholder)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(1, builder.build());
    }
}